package org.openaudible.audible;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.progress.IProgressTask;
import org.openaudible.progress.NullProgressTask;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// WebClient to connect to audible.com site
// Cookies are used to track logged in state and can be transfered from external web browser.
public class AudibleClient extends WebClient {
	private static final Log LOG = LogFactory.getLog(AudibleClient.class);
	static Logger system = Logger.getLogger("AudibleClient");
	Cache cache = new Cache();
	boolean useJS = true;
	protected boolean allowNetworkAccess = true;        // if set to false, disallow navigation, clicks, etc.
	protected HtmlPage lastPage;
	
	/*
		String DEFAULT_MOBILE_USER_AGENT_STRING = "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25";
		String netscape4 = "Mozilla/4.08 [en] (WinNT; I ;Nav)";
		String chrome = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
	*/
	// want a consistent user agent.. but not sure how much it makes a difference, if any.
	public static String swtWindows = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; Trident/7.0; rv:11.0) like Gecko";
	
	public AudibleClient() {
		super(BrowserVersion.BEST_SUPPORTED);
		this.getOptions().setThrowExceptionOnScriptError(false);
		this.getOptions().setMaxInMemory(1024 * 1024);
		assert (this.getOptions().isRedirectEnabled());
		
		getBrowserVersion().setUserAgent(swtWindows);
		
		// Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36
		
		cache.setMaxSize(500);
		// this.setCache(cache);
		final boolean maxDebug = false;
		
		this.setAjaxController(new AjaxController() {
			@Override
			public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
				return true;
			}
		});
		
		
		this.waitForBackgroundJavaScript(15000);
		if (!maxDebug) {
			java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
			java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
			java.util.logging.Logger.getLogger("org.apache.http.client").setLevel(Level.OFF);
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		} else {
			LOG.trace("trace1");
			java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.ALL);
			java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.ALL);
			java.util.logging.Logger.getLogger("org.apache.http.client").setLevel(Level.ALL);
			
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
			LOG.trace("trace2");
			
			setLevel(system, Level.ALL);
			// boolean b = LOG.isTraceEnabled();
			// assert(b);
			
			LOG.trace("trace3");
			
			
		}
		
		this.getOptions().setJavaScriptEnabled(useJS);
		this.getOptions().setCssEnabled(false);//if you don't need css
		
		if (useJS) {
			JavaScriptEngine sriptEngine = getJavaScriptEngine();
			HtmlUnitContextFactory factory = sriptEngine.getContextFactory();
			Context context = factory.enterContext();
			context.setOptimizationLevel(9);
			
			// sriptEngine.compile(owningPage, scope, sourceCode, sourceName, startLine)
			
		}
		
		
		new WebConnectionWrapper(this) {
			
			public WebResponse getResponse(WebRequest request) throws IOException {
				WebResponse response = super.getResponse(request);
				String u = request.getUrl().toExternalForm();
				if (u.contains(".js")) {
					System.err.println(u);
					
					
					String content = response.getContentAsString("UTF-8");
					if (content.contains("append: function")) {
						System.err.println(content);
						
					}
					//change content
					
					WebResponseData data = new WebResponseData(content.getBytes("UTF-8"),
							response.getStatusCode(), response.getStatusMessage(), response.getResponseHeaders());
					response = new WebResponse(data, request, response.getLoadTime());
				}
				return response;
			}
		};
		
		
	}
	
	private static void setLevel(Logger pLogger, Level pLevel) {
		Handler[] handlers = pLogger.getHandlers();
		for (Handler h : handlers) {
			h.setLevel(pLevel);
		}
		pLogger.setLevel(pLevel);
	}
	
	public boolean setJavascriptEnabled(boolean b) {
		boolean p = getOptions().isJavaScriptEnabled();
		if (useJS)
			this.getOptions().setJavaScriptEnabled(b);
		return p;
	}
	
	
	public void throwIfNetworkDisabled() throws AudibleLoginError {
		if (!allowNetworkAccess)
			throw new AudibleLoginError("network disabled");
		
	}
	
	@Override
	public HtmlPage getPage(String url) throws IOException, FailingHttpStatusCodeException {
		HtmlPage p = getPage(url, new NullProgressTask());
		lastPage = p;
		return p;
	}
	
	public HtmlPage getPage(String url, IProgressTask task) throws IOException, FailingHttpStatusCodeException {
		HtmlPage p = super.getPage(url);
		lastPage = p;
		return p;
	}
	
	
	public HtmlPage getLastPage() {
		return lastPage;
	}
	
	public void stop() {
	}
}
