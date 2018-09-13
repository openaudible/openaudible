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
import org.openaudible.util.HTMLUtil;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

// audible.com web page scraper
// a singleton class..
public class AudibleScraper {
	final static String cookiesFileName = "cookies.json";
	private static final Log LOG = LogFactory.getLog(AudibleScraper.class);
	static int maxLoginAttempts = 1;
	final AudibleAccountPrefs account;
	private final AudibleClient webClient;
	public HtmlPage page;
	boolean debugCust = false;
	boolean loggedIn = false;
	String clickToDownload = "Click to download ";
	private IProgressTask progress;

	
	public AudibleScraper(AudibleAccountPrefs account) {
		webClient = new AudibleClient();
		this.account = account;
		loadCookies();
	}
	
	public static void deleteCookies() {
		File cookiesFile = Directories.META.getDir(cookiesFileName);
		if (cookiesFile.exists()) {
			cookiesFile.delete();
		}
	}
	
	public HtmlPage getPage() {
		return page;
	}
	
	public void setPage(HtmlPage page) {
		assert (page != null);
		this.page = page;
		LOG.info("pageLoaded:'" + page.getTitleText()+"' "+page.getUrl());
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
				}
				
			}
		}
	}
	
	private void loadCookies() {

		try {
			CookieManager cm = getWebClient().getCookieManager();

			File cookiesFile = Directories.META.getDir(cookiesFileName);
			if (cookiesFile.exists()) {
				String content = HTMLUtil.readFile(cookiesFile);

				List<BasicClientCookie> list = new Gson().fromJson(content, new TypeToken<List<BasicClientCookie>>() {
				}.getType());

				for (BasicClientCookie bc : list) {
					Cookie c = new Cookie(bc.getDomain(), bc.getName(), bc.getValue());
					cm.addCookie(c);
					// LOG.info("Cookie: "+c);

				}
				LOG.info("Loaded " + list.size() + " cookies");
			}
		} catch(Throwable th)
		{
			LOG.error("error loading cookies...", th);
		}
	}
	
	public void logout() {
		
		
		try {
			setURL("/signout", "Signing out");
		} catch (Throwable th) {
			LOG.info("signout error, ignorning...");
			
		}
		CookieManager cm = getWebClient().getCookieManager();
		cm.clearCookies();
		File cookiesFile = Directories.META.getDir(cookiesFileName);
		if (cookiesFile.exists()) {
			cookiesFile.delete();
		}
		ConnectionNotifier.getInstance().signout();
		
		
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
		return login(0);
	}
	
	protected boolean login(int attempt) throws IOException {

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

		HtmlForm login;

		try {
			login = page.getFormByName("signIn");
		}catch(Throwable th)
		{
			HTMLUtil.debugNode(page, "login");
			return false;
		}
		
		if (login == null) {
			// TODO: find sign-in anchor and click it..
			
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
			LOG.info("Appears to be a captcha... I am a bot.");
			return false;
		}
		
		LOG.info("Submitting login credentials");
		pass.setValueAttribute(copy.audiblePassword);
		email.setValueAttribute(copy.audibleUser);
		
		if (getProgress() != null)
			getProgress().setTask("Submitting credentials...");
		HTMLUtil.debugNode(page, "submitting-credentials");
		
		setPage(submit.click());
		boolean ok = checkLoggedIn();
		
		if (!ok) {
			HTMLUtil.debugNode(page, "login failed");


			LOG.info("Login failed, see html files at:" + HTMLUtil.debugFile("submitting-credentials").getAbsolutePath() + " and " + HTMLUtil.debugFile("login failed").getAbsolutePath());

			ConnectionNotifier.getInstance().loginFailed(page.getUrl().toString(), page.getTitleText(), page.asXml());

			if (attempt < maxLoginAttempts) {
				login(attempt + 1);
			}
			
		} else {
			HTMLUtil.debugFile("submitting-credentials").delete();
		}
		
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
		return "/";
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
	
	public boolean setURLAndLogIn(String u) throws IOException {
		
		setURL(u);
		if (!checkLoggedIn()) {

			String url = page.getUrl().toString();
			String title = page.getTitleText();


			LOG.info("not logged in after going to:" + u);
			// trouble.. try again
			login();
			return checkLoggedIn();
		}
		return true;
	}
	
	public void lib() throws Exception {
		String browserURL = ConnectionNotifier.instance.getLastURL();
		
/*
		if (browserURL.startsWith(getAudibleBase())) {
			// a bit of a hack.. try to log in using library URL in browser.
			LOG.info("Using library location from browser: " + browserURL);
			try {
				if (setURLAndLogIn(browserURL))
					return;
			} catch (Throwable e) {

				LOG.error(e);
			}
		}
*/

		if (!setURLAndLogIn("/lib"))
			throw new Exception("Unable to access your library. Try logging in with Browser (Cmd-B) to view your library page and try again..  \n\nThere may also be a change in audible's web site that has broken this code.");

/*
        HtmlAnchor lib=null;
        if (page!=null)
        {
            String debug = "";

            for (HtmlAnchor  a : page.getAnchors())
            {
                String ref = a.getHrefAttribute();
                debug += ref+"\n";
                if (a.getHrefAttribute().startsWith("/lib"))
                {
                    lib = a;
                    browserURL = ref;
                    break;
                }
            }

            // LOG.info(debug);
        }

        if (lib!=null)
        {
            page = lib.click();
            if (!checkLoggedIn()) {
                LOG.info("Clicked lib, but not logged in anymore.");
                // trouble.. try again
                login();
                if (!checkLoggedIn())
                    throw new Exception("Got logged out. Try logging in with Browser to your library page and try again..");
            }
            return;
        }

        boolean ok = setURLAndLogIn("/lib");

        setURL("/lib");
        if (!checkLoggedIn()) {
            // trouble.. try again
            login();
            if (!checkLoggedIn())
                throw new Exception("Got logged out. Try logging in with Browser and try again..");
            setURL("/lib");
        }

*/
	
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
		return setURL(u, "");
	}
	
	public Page setURL(String u, String task) throws FailingHttpStatusCodeException, IOException {
		return setURL(u, "", true);
	}
	
	public Page setURL(String u, String task, boolean appendURL) throws FailingHttpStatusCodeException, IOException {
		
		if (u.startsWith("/"))
			u = getAudibleBase() + u;
		if (appendURL) {
			if (!task.isEmpty()) {
				if (u.length() > 20)
					task += "\n";
				else
					task += ": ";
			}
			task += u;
		}
		getProgress().setSubTask(task);
		LOG.info("setURL:" + u);
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
	
	
	public Collection<Book> fetchLibraryQuick(HashMap<String, Book> books) throws Exception {
		return _fetchLibrary(books);
	}
	
	
	// isPartialBook. A part of a single book. Not to be confused with one of a series.
	// In the US, productID that ends in a lowercase letter is a partial.
	// in the UK, a partial book has PUB_000123bUK
	// So if a book product ID contains any lower case letter, it should be a partial..
	public boolean isPartialBook(Book b) {
		String pid = b.getProduct_id();
		return !pid.equals(pid.toUpperCase());
	}
	
	
	public Collection<Book> _fetchLibrary(HashMap<String, Book> existingBooks) throws Exception {
		if (page == null)
			home();
		// LOG.info("Accessing audible library...");
		HashSet<Book> results = new HashSet<>();
		// getWebClient().setJavascriptEnabled(false);
		progress.setTask("Scanning your library to get your list of books...", "");
		int pageNum = 0;
		HtmlElement next = null;
		String prev = "";
		LibraryParser.instance.howToListenFound = false;
		
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
				lib();
				// setURL("/lib", "Reading Library...");
				setPageFilter();
				
			} else {
				// this is a bit of a hack. Extract the URL from the "next" HtmlButton.
				String u = next.getAttribute("data-url");
				if (u != null) {
					if (!u.endsWith("&"))
						u += "&";
					u += "page=" + pageNum;
					setURL(u, "Reading Library page " + pageNum + "... Found " + results.size() + " books", false);
				} else {
					// this is simple, but it doesn't work. Not sure why. Javascript, something else..
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
			int partialBooks = 0;
			
			for (Book b : list) {
				// LOG.info(b.toString());
				if (results.contains(b)) {
					LOG.error("duplicate book:" + b);
					assert (false);
				}
				
				if (isPartialBook(b)) {
					partialBooks++;
					continue;
				}
				
				results.add(b);
				if (existingBooks != null) {
					if (!existingBooks.containsKey(b.getProduct_id()))
						newBooks++;
				} else {
					newBooks++;
				}
			}
			LOG.info("pageNum=" + pageNum + " total=" + list.size() + " new=" + newBooks + " partial=" + partialBooks);
			
			
			if (newBooks == 0) {
				
				if (LibraryParser.instance.howToListenFound) {
					LOG.error("Looks like your settings need changing. Using your browser, go to Audible: Account: Settings. Then disable: Check for Audible Download Manager ");
					throw new AudibleSettingsError();
				}
				
				break;
			}
			
			
			next = LibraryParser.instance.getNextPage(page);
			if (next == null)
				break;
			//LOG.info("next page:"+next);
		}
		
		return results;
	}
	
	private void setPageFilter() {
		try {
			DomElement purchaseDateFilter = page.getElementByName("purchaseDateFilter");
			
			HtmlSelect h = (HtmlSelect) purchaseDateFilter;
			int i = h.getSelectedIndex();
			if (i != 0) {
				HtmlOption all = h.getOption(0);
				String url = all.getAttribute("data-url");
				try {
					if (url != null && !url.isEmpty()) {
						//LOG.info("url: "+ url);
						String newURL = url + "&purchaseDateFilter=all&programFilter=all&sortBy=PURCHASE_DATE.dsc";
						page = (HtmlPage) setURL(newURL, "Setting view filter");
						LOG.info("new URL: " + page.getUrl());
					}
					h = (HtmlSelect) page.getElementByName("purchaseDateFilter");
					i = h.getSelectedIndex();
					if (i != 0) {
						LOG.error("Expected filter to be set to 0, not " + i);
					}
					
				} catch (Exception e) {
					LOG.error("Error setting filter.. update may be required.", e);
				}
			}
			
			return;
			
		} catch (Throwable th) {
			LOG.info("Unable to set purchaseDateFilter. Writing debug log to no_date.html. This may mean we are unable to get all of your books. You may need to log in with the Browser and set the filters to show all books.");
			HTMLUtil.debugNode(page, "no_date.html");
			
		}
		
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