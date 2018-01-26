package org.openaudible.audible;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.jetty.util.IO;
import org.openaudible.Audible;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.Directories;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.progress.IProgressTask;
import org.openaudible.util.EventTimer;
import org.openaudible.util.HTMLUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

    boolean debugCust = false;
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


    private static String extract_activation_bytes(byte b[]) throws Exception {
        LOG.info("extract_activation_bytes");

        String data = new String(b, "UTF-8");
        LOG.info(data);

        if (data.contains("BAD_LOGIN"))
            throw new Exception("BAD_LOGIN");
        if (data.contains("Whoops"))
            throw new Exception("Whoops");
        if (!data.contains("group_id"))
            throw new Exception("no group_id");

        int index = findKeyStart(b);

        if (index == -1)
            throw new Exception("No key found");

        String code = "";
        for (int x = 0; x < 4; x++) {
            byte i = b[index + 3 - x];
            String o = Integer.toHexString((i & 0xFF)).toUpperCase();
            if (o.length() == 1) // Make byte two chars long.
                o = "0" + o;
            code += o;
        }
        LOG.info("code=" + code);
        return code;

    }

    // Find first "line start" that doesn't end with )\n
    private static int findKeyStart(byte[] b) {
        int index = -1;
        for (int x = 1; x < b.length - 2; x++) {
            if (b[x] == '\n') {
                if (b[x - 1] != ')')
                    return index;
                index = x + 1;
            }

        }
        return -1;
    }

    public static String extract(String c, DomNode h) {
        return HTMLUtil.text(HTMLUtil.findByClass(c, h));
    }

    public static String extractParagraph(String c, DomNode h) {
        String out = "";
        DomNode node = (DomNode) HTMLUtil.findByClass(c, h);
        if (node != null) {
            NodeList cn = node.getChildNodes();
            for (int x = 0; x < cn.getLength(); x++) {
                Node y = cn.item(x);
                String text = y.getTextContent();
                if (text != null) {
                    text = text.trim();
                    if (out.length() > 0)
                        out += "\n";
                    out += text;
                }

            }
        }
        return out;
    }

    public HtmlPage getPage() {
        return page;
    }

    public void setPage(HtmlPage page) {
        this.page = page;

        progress.setSubTask(page.getTitleText());

        if (page != null && debugCust) {
            String xml = page.asXml();
            int i1 = xml.indexOf("cust_id");
            int i2 = xml.indexOf("downloadCustId");
            String s1 = "", s2 = "";
            if (i1 != -1) {
                s1 = xml.substring(i1 - 100, i1 + 100);
            }
            if (i2 != -1) {
                s2 = xml.substring(i2 - 100, i2 + 100);
            }
            if (s1.length() > 0 || s2.length() > 0) {
                LOG.info("Found cust_id:s1=" + s1 + "\ns2=" + s2);
            }

        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        if (loggedIn != this.loggedIn) {
            this.loggedIn = loggedIn;
            LOG.info("Setting logged in to " + loggedIn);
            ConnectionNotifier.getInstance().connectionChanged(loggedIn);

            if (loggedIn) {
                try {
                    saveCookies();
                } catch (IOException e) {
                    e.printStackTrace();
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



    ArrayList<Book> parseLibraryFragment(DomDocumentFragment fragment) throws IOException {
        ArrayList<Book> list = new ArrayList<>();
        ArrayList<String> colNames = new ArrayList<>();

        HtmlTable table = fragment.getFirstByXPath("//table");
        if (table == null)
            return list;

        int purchaseDateIndex = -1;

        List<HtmlElement> header = table.getElementsByTagName("th");
        int index = 0;
        for (HtmlElement h : header) {
            String xml = h.asXml();
            if (xml.contains("PURCHASE_DATE")) {
                // <th><a href="#" class="adbl-sort-by adbl-link adbl-sort-up" id="SortByDtPurchased" name="" title="">Purchase Date</a></th>
                purchaseDateIndex = index;
            }

            index++;

        }

        if (purchaseDateIndex == -1) {
            LOG.debug("No purchase date column!?");
        }


        for (HtmlTableRow r : table.getRows()) {
            for (HtmlTableCell cell : r.getCells()) {

                String cx = cell.asXml();
                if (cx.contains("adbl-download-it")) {
                    Book b = new Book();

                    for (DomElement e : cell.getHtmlElementDescendants()) {
                        String clz = e.getAttribute("class");
                        String n = e.getAttribute("name");
                        String v = e.getAttribute("value");

                        if ("adbl-download-it".equals(clz)) {
                            String fullTitle = e.getAttribute("title");

                            if (fullTitle.startsWith(clickToDownload)) {
                                fullTitle = fullTitle.substring(clickToDownload.length(), fullTitle.length());
                            }
                            b.setFullTitle(fullTitle);

                        }
                        if ("productId".equals(n))
                            b.setProduct_id(v);

                    }

                    if (b.getProduct_id() != null && !b.partial()) {
                        for (HtmlTableCell c : r.getCells()) {
                            if ("titleInfo".equals(c.getAttribute("name"))) {
                                for (DomNode d : c.getChildNodes()) {

                                    if (d instanceof HtmlAnchor) {
                                        HtmlAnchor link = (HtmlAnchor) d;

                                        if ("tdTitle".equals(link.getAttribute("name"))) {
                                            b.setInfoLink(link.getAttribute("href"));
                                        }

                                    }

                                }

                            }
                        }

                        String downloadCustId = getHidden("downloadCustId");

                        if (downloadCustId != null)
                            b.set(BookElement.cust_id, downloadCustId);
                        else
                            LOG.info("Warning: No cust_id found for book: " + b);

                        String downloadUserId = getHidden("cust_id");
                        if (downloadUserId != null)
                            b.set(BookElement.user_id, downloadUserId);
                        else
                            LOG.info("Warning: No user_id found for book: " + b);


                        List<HtmlElement> cells = r.getElementsByTagName("td");
                        if (purchaseDateIndex != -1 && purchaseDateIndex < cells.size()) {
                            HtmlElement pd = cells.get(purchaseDateIndex);
                            String purchaseDateText = pd.asText().trim();
                            if (purchaseDateText.length() == 0)
                                LOG.info("Purchase date blank! " + pd.asXml());
                            b.setPurchaseDate(purchaseDateText);

                            LOG.info(b + "->" + purchaseDateText + " " + b.getPurchaseDate());

                        } else {
                            if (purchaseDateIndex != -1)
                                LOG.info("No purchase date for " + b);
                        }


                        String err = "";
                        if (!b.has(BookElement.user_id))
                            err += "no user_id ";
                        if (!b.has(BookElement.cust_id))
                            err += "no cust_id ";
                        if (err.length() > 0)
                            LOG.info("Error: " + b + " " + err);


                        list.add(b);
                        if (getProgress() != null)
                            getProgress().setSubTask(b.toString());

                    }

                }
            }
        }

        LOG.info("Library page contains: " + list.size() + " book(s)");

        return list;
    }

    private boolean login() throws IOException {

        AudibleAccountPrefs copy = account;

        if (account.audibleUser.length() == 0 || account.audiblePassword.length() == 0)
        {
            copy = ConnectionNotifier.getInstance().getAccountPrefs(account);
            if (copy==null) return false;   // exit
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
            assert(signIn==null);

            setLoggedIn(true);
            return true;
        }


        if (signOut == null && accountDetails == null && signIn == null) {
            HTMLUtil.debugNode(page, "checkLoggedIn");

        }
        return isLoggedIn();
    }

    public String homeURL()
    {
        return "/access";
    }

    public String getPageURL()
    {
        if (page!=null)
        {
            URL u = page.getUrl();
            if (u!=null)
            {
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
            HTMLUtil.debugNode(page, "homeURL");

            if (checkLoggedIn())
                return;

            HtmlAnchor signIn = getAnchor("/sign-in");
            if (signIn != null) {
                setPage(signIn.click());
                HTMLUtil.debugNode(page, "sign-in");

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

    public void signOut() throws FailingHttpStatusCodeException, IOException, InterruptedException {
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

    public void clickLib() throws Exception {
        HtmlAnchor lib=null;
        for (HtmlAnchor n : page.getAnchors()) {
            if (n.getHrefAttribute().contains("/lib"))
                lib = n;
        }
        if (lib!=null) {
            setPage(lib.click());
        } else {
            lib();
        }





    }

    public void lib() throws Exception {
        if (!checkLoggedIn())
            throw new Exception("Not logged in at start lib.");
        if (getProgress() != null)
            getProgress().setTask("Loading Library");
        clickLib();
        if (!checkLoggedIn()) {
            // getWebClient().waitForBackgroundJavaScript(10000);  // needed? prob. not.
            HTMLUtil.debugNode(page, "got logged out 1");
            login();
            if (!checkLoggedIn()) {
                HTMLUtil.debugNode(page, "got logged out 2");
                throw new Exception("lib got logged out");
            }
        }

        String downloadCustId = getHidden("downloadCustId");
        String downloadUserId = getHidden("cust_id");
        if (downloadCustId == null || downloadUserId == null) {
            HTMLUtil.debugNode(page, "lib-no-cust");
            throw new IOException("Unable to determine required library variables in library:");
        }
    }

    private HtmlElement findById(String id) {
        try {
            return page.getHtmlElementById(id);

        } catch (Throwable th) {

        }
        return null;
    }


    public String getAudibleBase()
    {
        return account.audibleRegion.getBaseURL();
    }

    public Page setURL(String u) throws FailingHttpStatusCodeException, IOException {

        if (getProgress() != null)
            getProgress().setSubTask(u);


        EventTimer evt = new EventTimer();
        if (u.startsWith("/"))
            u = getAudibleBase()+ u;

        if (getProgress() != null)
            getProgress().setSubTask(u);

        Page p = getWebClient().getPage(u);

        if (p instanceof HtmlPage) {
            setPage((HtmlPage) p);
        } else {
            LOG.info("Page not HTMLPage as expected:" + p.getClass() + " " + p.getWebResponse().getContentAsString());
        }
        return p;
    }

    String replaceAll(String haystack, String find, String replacement) {
        while (haystack.contains(find))
            haystack = haystack.replaceAll(find, replacement);
        return haystack;
    }

    public String cleanString(String out) {
        out = replaceAll(out, "\r", "\n");
        out = replaceAll(out, "  ", " ");
        out = replaceAll(out, "\t\t", "\t");
        out = replaceAll(out, " \n", "\n");
        out = replaceAll(out, "\t\n", "\n");
        out = replaceAll(out, "\n\n", "\n");
        return out;
    }

    String getHidden(String n) {
        return HTMLUtil.findHidden(page, n);
    }

    String escape(String s) throws Exception {
        char bad[] = {'\n', '/', '#'};
        for (char c : bad) {
            if (s.indexOf(c) != -1)
                throw new Exception("TODO: Fix");
        }
        return s;
    }

    void setTask(String task, String subTask) {
    }

    private ArrayList<Book> libraryPage(int pageNum, int items) throws IOException, SAXException, InterruptedException {
        if (getProgress() != null) {
            if (getProgress().wasCanceled()) throw new InterruptedException("Canceled");

            getProgress().setTask("Retrieving page " + pageNum + " of audible library.", "Fetching...");
        }
        return parseLibraryFragment(getLibraryFragment(pageNum, items));
    }

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

    public DomDocumentFragment redeemGiftCode(String code) throws IOException, SAXException {
        if (!loggedIn)
            throw new IOException("Not logged in");
        setURL("/mt/giftmembership");

        String url = getAudibleBase()+"/gift-redemption?isGM=gm";

        WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.POST);
        webRequest.setRequestBody("giftClaimCode=" + code);

        WebResponse webResponse = getWebClient().getWebConnection().getResponse(webRequest);
        String content = webResponse.getContentAsString();
        LOG.info(content);
        DomDocumentFragment frag = new DomDocumentFragment(page);
        HTMLParser.parseFragment(frag, content);
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

    public Collection<Book> fetchLibraryQuick(HashMap<String, Book> books) throws Exception {
        return _fetchLibrary(books);
    }

    public Collection<Book> _fetchLibrary(HashMap<String, Book> existingBooks) throws Exception {
        if (page == null)
            home();
        LOG.info("Accessing audible library...");
        HashSet<Book> results = new HashSet<>();

        getWebClient().setJavascriptEnabled(false);

        lib();

        for (int x = 1; x < 2000; x++) {
            LOG.info("Getting page " + x + " of audible library...");
            ArrayList<Book> list = libraryPage(x, 100);
            int newBooks = 0;

            for (Book b : list) {
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
/*

    public String getActivationBytes() {
        return activationBytes;
    }

    public void setActivationBytes(String activationBytes) {
        this.activationBytes = activationBytes;
    }
*/

    public String getURLField(String u, String which) throws URISyntaxException {
        for (NameValuePair nvp : URLEncodedUtils.parse(new URI(u), "UTF-8")) {
            LOG.info(nvp.getName() + "=" + nvp.getValue());

            if (nvp.getName().equals(which)) {
                return nvp.getValue();
            }
        }
        return null;
    }

    //// BOOK INFO PAGE CODE

    // Based on https://github.com/inAudible-NG/audible-activator
    // Gets activation bytes that are needed to decrypt audible content
    // A player is registered.. the key extracted.. then unregistered.
    // If this fails, the most common cause is that you need to request
    // Audible reset your Audible desktop player devices. This can't
    // currently be done using the audible deactivate devices web page
    // as those only appear to show devices such as iOS and Kindle.
    // But a quick online chat should suffice.
    //
    public String fetchDecrpytionKey() throws Exception {
        if (!loggedIn)
            throw new Exception("Not logged in");
        if (getProgress() != null)
            getProgress().setTask("Fetching player");

        String playerId = "";
        String r = RandomStringUtils.randomAlphabetic(20);
        LOG.info("randomString=" + r + " len=" + r.length());

        String b64 = Base64.encodeBase64String(r.getBytes()).trim();
        LOG.info(b64);
        playerId = b64;
        // Object page=null;


        String oldAgent = getWebClient().getBrowserVersion().getUserAgent();

        try {

            getWebClient().getBrowserVersion().setUserAgent("Audible Download Manager");

            if (getProgress() != null)
                getProgress().setSubTask("Authorizing");

            String u = getAudibleBase()+"/player-auth-token?playerType=software&bp_ua=y&playerModel=Desktop&playerId=" + playerId + "&playerManufacturer=Audible&serial=";
            LOG.info(u);
            Page p = getWebClient().getPage(u);
            page = null;
            if (p instanceof HtmlPage) {
                page = (HtmlPage) p;
                HTMLUtil.debugNode(page, "player-auth-token");
            }


            String outURL = p.getUrl().toString();
            LOG.info("outURL=" + outURL);

            String playerToken = getURLField(outURL, "playerToken");


            // {u'playerToken': u'eyJQTEFZRVJfVFlQRSI6InNvZnR3YXJlIiwiQ1JFQVRJT05fVElNRV9MT05HIjoxNDUwODIyMDY5MzAxLCJDVVNUT01FUl9JRCI6IkEzU1RKTVE1M0NZOEdQIiwiTUFSS0VUUExBQ0UiOiJBRjJNMEtDOTRSQ0VBIiwiUExBWUVSX0lEIjoiZGEzOWEzZWU1ZTZiNGIwZDMyNTViZmVmOTU2MDE4OTBhZmQ4MDcwOSIsIlBMQVlFUl9NT0RFTCI6IkRlc2t0b3AiLCJQTEFZRVJfTUFOVUYiOiJBdWRpYmxlIn0='}
            String content = p.getWebResponse().getContentAsString();


            if (playerToken == null || playerToken.isEmpty()) {
                LOG.info("content=" + content);
                List<com.gargoylesoftware.htmlunit.util.NameValuePair> headers = p.getWebResponse().getResponseHeaders();
                LOG.info("headers=" + headers);
                login();
                outURL = p.getUrl().toString();
                playerToken = getURLField(outURL, "playerToken");
                if (playerToken == null)
                    throw new Exception("Unable to get activation player token.");
            }
            LOG.info("playerToken=" + playerToken);

            // # Step 2

            getWebClient().getBrowserVersion().setUserAgent("Audible Download Manager");

            //  Step 3, de-register first, in order to stop hogging all activation slots (there are 8 of them!)
            // I don't know why this is needed.. didn't we just create this player token?
            unregisterPlayer(playerToken);

            // # Step 4

            u = getAudibleBase()+"/license/licenseForCustomerToken?" + "customer_token=" + playerToken;
            LOG.info(u);

            if (getProgress() != null)
                getProgress().setSubTask("Registering player");
            p = getWebClient().getPage(u);

            // get raw bytes. (Do not get as string as it will be corrupted by the key's binary.)
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            InputStream is = p.getWebResponse().getContentAsStream();
            IO.copy(is, bas);
            is.close();
            unregisterPlayer(playerToken);

            return extract_activation_bytes(bas.toByteArray());

        } finally {
            getWebClient().getBrowserVersion().setUserAgent(oldAgent);
        }

    }

    private Throwable unregisterPlayer(String playerToken) {
        try {
            if (getProgress() != null)
                getProgress().setSubTask("Unregistering client");
            LOG.info("Unregistering client");
            String u = "/license/licenseForCustomerToken?customer_token=" + playerToken + "&action=de-register";
            setURL(u);
            Page resultPage = getWebClient().getPage(u);
            WebResponse response = resultPage.getWebResponse();
            String content = response.getContentAsString();
            LOG.info("De-register Response: " + content);
        } catch (Throwable th) {
            th.printStackTrace();
            return th;
        }
        return null;
    }

    private boolean parseBookPage(HtmlPage page, Book b) throws Exception {
        DomNode h = page;
        if (getProgress() != null)
            getProgress().setTask("Parsing book", b.toString());

        String asin = HTMLUtil.findHidden(page, "asin");
        if (asin == null) {
            for (HtmlAnchor a : page.getAnchors()) {
                String d1 = a.getAttribute("data-asin");
                if (d1 != null && d1.length() > 0) {
                    asin = d1;
                    break;
                }
            }
        }

        if (asin != null && asin.length() > 0) {
            b.setAsin(asin);
        } else {
            // trouble ahead... ?


        }

        if (b.getAsin().length() == 0) {
            LOG.info("No ASIN Found for " + b + ", " + b.getInfoLink());
            HTMLUtil.debugNode(page, "parse_book_no_asin");
        }

        String c = "";
        String narrator = "";

        Node narratedBy = HTMLUtil.findByClass("adbl-narrator-row", h);
        if (narratedBy != null) {
            NodeList list = narratedBy.getChildNodes();
            int narratorListCount = list.getLength();

            for (int x = 0; x < narratorListCount; x++) {
                Node cn = list.item(x);
                c = HTMLUtil.text(cn.getAttributes().getNamedItem("class"));
                if ("adbl-prod-author".equals(c)) {
                    narrator = HTMLUtil.text(cn);
                }
            }
            b.setNarratedBy(narrator);
        }

        b.setSummary(extractParagraph("adbl-content", h)); //

        b.setFullTitle(extract("adbl-prod-h1-title", h));
        b.setAuthor(extract("adbl-prod-author", h));
        b.setFormat(extract("adbl-format-type", h));
        b.setDuration(extract("adbl-run-time", h));
        b.setRating_average(extract("rating-average", h));
        b.setRating_count(extract("rating-count", h));
        b.setRelease_date(extract("adbl-date adbl-release-date", h));

        LOG.info(b.inspect(","));

        return true;
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
            return false;
        setURL(link);
        URL u = page.getUrl();
        String path = u.getPath();
        if ("/pderror".equals(path)) {
            b.setInfoLink(""); // clear link... it is no longer available.
            return false;
        }

        if (getProgress() != null)
            getProgress().setTask("Parsing book " + b);

        parseBookPage(page, b);
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