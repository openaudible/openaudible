package org.openaudible.audible;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openaudible.Audible;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.Directories;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.progress.IProgressTask;
import org.openaudible.util.EventTimer;
import org.openaudible.util.HTMLUtil;
import org.openaudible.util.Util;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

// audible.com web page scraper
// Not thread safe, run single instance at a time.
public class AudibleScraper {
    private static final Log LOG = LogFactory.getLog(AudibleScraper.class);
    private final AudibleClient webClient;
    public HtmlPage page;
    final AudibleAccountPrefs account;
    final static String cookiesFileName = "cookies.json";

    boolean debugCust = true;
    boolean loggedIn = false;
    String clickToDownload = "Click to download ";

    private IProgressTask progress;


    public AudibleScraper(AudibleAccountPrefs account) {
        webClient = new AudibleClient();
        this.account = account;
        try {
            loadCookies();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public HtmlPage getPage() {
        return page;
    }

    public void setPage(HtmlPage page) {
        assert (page != null);
        this.page = page;
        LOG.info("pageLoaded:" + page.getUrl() + " " + page.getTitleText());


        if (page != null && debugCust) {
            String xml = page.asXml();
            int i1 = xml.indexOf("cust_id");

            int i2 = xml.indexOf("order_number");
            String s1 = "", s2 = "";
            if (i1 != -1) {
                s1 = xml.substring(i1 - 300, i1 + 300);
            }
            if (i2 != -1) {
                s2 = xml.substring(i2 - 300, i2 + 300);
            }
            if (s1.length() > 0 || s2.length() > 0) {
                LOG.info("Found cust_id:s1=" + s1 + "\ns2=" + s2);
            }
        }


        // progress.setSubTask("page.getTitleText());

    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        if (loggedIn != this.loggedIn) {
            this.loggedIn = loggedIn;
            String title = "null";
            if (page != null)
                title = page.getTitleText();

            LOG.info("Setting logged in to " + loggedIn + " page=" + title);
            ConnectionNotifier.getInstance().connectionChanged(loggedIn);

            if (loggedIn) {
                try {
                    saveCookies();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (page != null) {
                    String u = page.getUrl().toString();
                    ConnectionNotifier.getInstance().setLastURL(u);
                }

            }
        }
    }

    private void loadCookies() throws IOException {

        CookieManager cm = getWebClient().getCookieManager();

        File cookiesFile = Directories.META.getDir(cookiesFileName);
        if (cookiesFile.exists()) {
            String content = HTMLUtil.readFile(cookiesFile);

            List<BasicClientCookie> list = new Gson().fromJson(content, new TypeToken<List<BasicClientCookie>>() {
            }.getType());

            for (BasicClientCookie bc : list) {
                Cookie c = new Cookie(bc.getDomain(), bc.getName(), bc.getValue());
                cm.addCookie(c);
            }
        }
    }

    public void logout() {
        CookieManager cm = getWebClient().getCookieManager();
        cm.clearCookies();
        File cookiesFile = Directories.META.getDir(cookiesFileName);
        if (cookiesFile.exists()) {
            cookiesFile.delete();
        }
        setLoggedIn(false);
    }

    public void saveCookies() throws IOException {

        CookieManager cm = getWebClient().getCookieManager();
        ArrayList<BasicClientCookie> list = new ArrayList<>();

        for (Cookie c : cm.getCookies()) {
            BasicClientCookie bc = new BasicClientCookie(c.getName(), c.getValue());
            bc.setDomain(c.getDomain());
            bc.setPath(c.getPath());
            list.add(bc);
        }


        File cookiesFile = Directories.META.getDir(cookiesFileName);
        if (cookiesFile.exists()) {
            cookiesFile.delete();
        }

        String o = new Gson().toJson(list);
        FileUtils.writeByteArrayToFile(cookiesFile, o.getBytes());
    }


    protected boolean login() throws IOException {

        AudibleAccountPrefs copy = account;

        if (account.audibleUser.length() == 0 || account.audiblePassword.length() == 0) {
            copy = ConnectionNotifier.getInstance().getAccountPrefs(account);
            if (copy == null) return false;   // exit
        }


        if (copy.audibleUser.length() == 0)
            throw new IOException("audibleUser not set");
        if (copy.audiblePassword.length() == 0)
            throw new IOException("audiblePass not set");


        if (getProgress() != null)
            getProgress().setTask("Logging on to audible...");

        HtmlForm login = page.getFormByName("signIn");

        if (login == null) {
            LOG.info("login form not found for page:" + page.getTitleText());
            return false;
        }
        HtmlInput email = null, pass = null;
        HtmlInput submit = null;

        email = (HtmlInput) findById("ap_email");
        pass = (HtmlInput) findById("ap_password");

        submit = (HtmlInput) HTMLUtil.findByType("submit", login);
        if (submit == null)
            submit = (HtmlInput) findById("signInSubmit");

        if (submit == null) {
            HTMLUtil.debugNode(page, "submit-not-found");
            HTMLUtil.debugNode(login, "submit-form");
            for (Node n : HTMLUtil.getChildren(login)) {
                System.out.println(n);
            }

            throw new IOException("Unable to log in, expected form element not found.");
        }

        if (!submit.getEnclosingForm().equals(login))
            throw new IOException("bad elem");
        if (!email.getEnclosingForm().equals(login))
            throw new IOException("bad email elem");
        if (!pass.getEnclosingForm().equals(login))
            throw new IOException("bad pass elem");
        HtmlElement ap_captcha_table = findById("ap_captcha_table");
        HtmlElement captchaImageDiv = findById("ap_captcha_img");

        if (captchaImageDiv != null || ap_captcha_table != null) {

            return false;
        }

        LOG.info("Submitting login credentials");
        pass.setValueAttribute(copy.audiblePassword);
        email.setValueAttribute(copy.audibleUser);

        if (getProgress() != null)
            getProgress().setTask("Submitting credentials...");

        setPage(submit.click());
        boolean ok = checkLoggedIn();

        if (!ok)
            HTMLUtil.debugNode(page, "login submitted");

        return ok;
    }

    // Returns true if logged in, false if not logged into audible.
    public boolean checkLoggedIn() {
        if (page == null)
            return false;


        Node signIn = HTMLUtil.findByName("signIn", page);
        HtmlAnchor sI = getAnchor("/sign-in");

        if (signIn != null || sI != null) {
            setLoggedIn(false);
            return false;
        }


        HtmlAnchor signOut = getAnchor("/signout");
        HtmlAnchor accountDetails = getAnchor("/account-details");


        if (accountDetails != null || signOut != null) {
            assert (signIn == null);

            setLoggedIn(true);
            return true;
        }


        if (signOut == null && accountDetails == null && signIn == null) {
            HTMLUtil.debugNode(page, "checkLoggedIn");

        }
        return isLoggedIn();
    }

    public String homeURL() {
        return "/access";
    }

    public String getPageURL() {
        if (page != null) {
            URL u = page.getUrl();
            if (u != null) {
                return u.toString();
            }
        }
        return null;
    }

    public void home() throws FailingHttpStatusCodeException, IOException, AudibleLoginError, InterruptedException {

//        if (checkLoggedIn()) {
//            setURL(homeURL());
//            return;
//        }

        boolean saved = true;
        if (true)
            getWebClient().setJavascriptEnabled(true);
        try {
            setURL(homeURL());
            // HTMLUtil.debugNode(page, "homeURL");

            if (checkLoggedIn())
                return;

            HtmlAnchor signIn = getAnchor("/sign-in");
            if (signIn != null) {
                setPage(signIn.click());
                // HTMLUtil.debugNode(page, "sign-in");

                Thread.sleep(2000);
                // getWebClient().waitForBackgroundJavaScript(5000);
                login();
                if (checkLoggedIn())
                    return;
            }
        } finally {
            getWebClient().setJavascriptEnabled(saved);
        }

        if (!checkLoggedIn())
            throw new AudibleLoginError();

    }

    public void connect() throws Exception {
        home();
        if (!loggedIn) {
            throw new AudibleLoginError();
        }
    }

    public void signOut() throws FailingHttpStatusCodeException, IOException {
        if (getProgress() != null)
            getProgress().setTask("Signing out");
        setURL("/signout");
    }

    private HtmlAnchor getAnchor(String string) {
        for (HtmlAnchor n : page.getAnchors()) {
            if (n.getHrefAttribute().contains(string))
                return n;
        }
        return null;
    }

    /*

    public boolean clickLib() throws Exception {
        HtmlAnchor lib=null;
        for (HtmlAnchor n : page.getAnchors()) {
            if (n.getHrefAttribute().contains("/lib"))
                lib = n;
        }


        if (lib!=null) {
            setPage(lib.click());
            return true;
        } else {
            return false;
        }





    }

*/

    public void lib() throws Exception {

        setURL("/lib");
        if (!checkLoggedIn()) {
            // trouble.. try again
            login();
            if (!checkLoggedIn())
                throw new Exception("Got logged out. Try logging in with Browser and try again..");
            setURL("/lib");
        }


    }

    private HtmlElement findById(String id) {
        try {
            return page.getHtmlElementById(id);

        } catch (Throwable th) {

        }
        return null;
    }


    public String getAudibleBase() {
        return account.audibleRegion.getBaseURL();
    }

    public Page setURL(String u) throws FailingHttpStatusCodeException, IOException {
        return setURL(u, u);
    }

    public Page setURL(String u, String task) throws FailingHttpStatusCodeException, IOException {
        LOG.info("setURL:" + u);
        getProgress().setSubTask(task);
        EventTimer evt = new EventTimer();
        if (u.startsWith("/"))
            u = getAudibleBase() + u;

        Page p = getWebClient().getPage(u);

        if (p instanceof HtmlPage) {
            setPage((HtmlPage) p);
        } else {
            LOG.info("Page not HTMLPage as expected:" + p.getClass() + " " + p.getWebResponse().getContentAsString());
        }
        return p;
    }


    void setTask(String task, String subTask) {
    }


    public void getCustAndOrder(Book b) throws Exception {


        if (!checkLoggedIn())
            throw new Exception("Not logged in.");

        String link = b.getInfoLink();
        if (link.startsWith("/")) {
            link = Audible.instance.getAudibleURL() + link;

            if (link.startsWith("http")) {
                HtmlPage page = (HtmlPage) setURL(link);
                String xml = HTMLUtil.debugNode(page, "book_details");
                int ch = xml.indexOf("cds.audible.com");

                if (ch != -1) {

                    String dl = xml.substring(ch - 50, ch + 300);
                    System.out.println(dl);

                }
            }
        }

    }


/*
    private DomDocumentFragment getLibraryFragment(int pageNum, int items) throws IOException, SAXException {
        EventTimer evt = new EventTimer();
        String url = getAudibleBase()+"/lib-ajax";
        WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.POST);
        webRequest.setRequestBody("progType=all&timeFilter=all&itemsPerPage=" + items + "&searchTerm=&searchType=&sortColumn=&sortType=down&page=" + pageNum + "&mode=normal&subId=&subTitle=");
        WebResponse webResponse = getWebClient().getWebConnection().getResponse(webRequest);
        String content = webResponse.getContentAsString();
        DomDocumentFragment frag = new DomDocumentFragment(page);
        HTMLParser.parseFragment(frag, content);
        LOG.info(evt.reportString("get Library Page:" + pageNum));
        return frag;
    }


    public Collection<Book> fetchFirstLibraryPage() throws Exception {
        if (page == null)
            home();
        LOG.info("Accessing audible library...");
        HashSet<Book> results = new HashSet<>();
        lib();
        return results;
    }
*/

    public Collection<Book> fetchLibraryQuick(HashMap<String, Book> books) throws Exception {
        return _fetchLibrary(books);
    }

    public Collection<Book> _fetchLibrary(HashMap<String, Book> existingBooks) throws Exception {
        if (page == null)
            home();
        LOG.info("Accessing audible library...");
        HashSet<Book> results = new HashSet<>();
        // getWebClient().setJavascriptEnabled(false);
        progress.setTask("Scanning your library to get your list of books...", "");
        int pageNum = 0;
        HtmlElement next = null;
        String prev = "";

        while (true) {
            progress.throwCanceled();

            if (!checkLoggedIn()) {
                login();
                if (!checkLoggedIn()) {
                    throw new Exception("Unable to remain logged in");
                }
            }


            pageNum++;

            if (next == null) {
                assert (pageNum == 1);
                setURL("/lib", "Reading Library...");

                DomElement purchaseDateFilter = page.getElementByName("purchaseDateFilter");
                if (purchaseDateFilter!=null)
                {
                    DomNodeList<DomNode> nodes = purchaseDateFilter.getChildNodes();
                    for (DomNode n:nodes)
                    {
                        if (n instanceof HtmlOption)
                        {
                            // "all" is first option..
                            ((HtmlOption) n).click();
                            break ;
                        }
                    }
                }
            } else {
                // getProgress().setTask("Getting a list of your library.  );
                EventTimer evt = new EventTimer();
                String u = next.getAttribute("data-url");
                if (u != null) {

                    if (!u.endsWith("&"))
                        u += "&";
                    u += "page=" + pageNum;
                    setURL(u, "Reading Library page " + pageNum + "... Found " + results.size() + " books");
                } else {
                    page = next.click();   // go to next page.
                    // LOG.info(next.getClass() + " " + evt.reportString("next-click") + next.asXml());
                }

            }

            String cur = page.getUrl().toString();
            // LOG.info("curr=" + cur + "\nprev=" + prev);
            assert (!prev.equals(cur));
            prev = cur;

            progress.throwCanceled();

            ArrayList<Book> list = LibraryParser.instance.parseLibraryFragment(page);

            int newBooks = 0;

            for (Book b : list) {
                LOG.info(b.toString());
                if (b.partial())
                    continue;
                if (results.contains(b)) {
                    LOG.error("duplicate book:" + b);
                    assert (false);
                }

                results.add(b);
                if (existingBooks != null) {
                    if (!existingBooks.containsKey(b.getProduct_id()))
                        newBooks++;
                } else {
                    newBooks++;
                }
            }

            if (newBooks == 0)
                break;
            next = LibraryParser.instance.getNextPage(page);
            if (next == null)
                break;
        }

        return results;
    }

    public Collection<Book> fetchLibrary() throws Exception {
        return _fetchLibrary(null);
    }

    public void quit() {
        if (getWebClient() != null) {
            getWebClient().close();
        }
    }

    public String getAudibleBookURL(Book b) {
        String url = getAudibleBase() + b.getInfoLink();
        return url;
    }



    public boolean hasInfo(Book b) {
        BookElement required[] = {BookElement.asin, BookElement.narratedBy};
        for (BookElement e : required) {
            if (!b.has(e))
                return false;
        }
        return true;
    }

    public boolean getInfo(Book b) throws Exception {

        String link = b.getInfoLink();

        if (link.length() == 0)
            throw new Exception("Product link page unavailable. Book may be discontinued or a library scan is needed.");
        setURL(link);
        URL u = page.getUrl();
        String path = u.getPath();
        if ("/pderror".equals(path)) {
            b.setInfoLink(""); // clear link... it is no longer available.
            return false;
        }

        if (getProgress() != null)
            getProgress().setTask("Parsing book " + b);

        BookPageParser.instance.parseBookPage(page, b);
        return true;
    }

    public IProgressTask getProgress() {
        return progress;
    }

    public void setProgress(IProgressTask progress) {
        this.progress = progress;
    }

    public boolean loggedIn() {
        if (page != null) {
            loggedIn = checkLoggedIn();
        }

        return loggedIn;
    }

    public AudibleClient getWebClient() {
        return webClient;
    }

}