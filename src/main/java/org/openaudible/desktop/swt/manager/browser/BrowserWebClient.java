package org.openaudible.desktop.swt.manager.browser;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Display;
import org.openaudible.audible.AudibleClient;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.progress.IProgressTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BrowserWebClient extends AudibleClient implements ProgressListener {
	public final static Log logger = LogFactory.getLog(BrowserWebClient.class);
	
	final AudibleBrowser browser;
	final CompletionCallback callback = new CompletionCallback();
	Object lock = new Object();
	
	int urlCount = 0;
	int completeCount = 0;
	private long start;
	
	
	public BrowserWebClient(AudibleBrowser b) {
		super();
		SWTAsync.assertGUI();
		browser = b;
		browser.browser.addProgressListener(this);
		allowNetworkAccess = false;
	}
	
	@Override
	public HtmlPage getPage(final String url, IProgressTask task) {
		
		
		System.out.println(new Date() + " getPage:" + url);
		
		callback.setInUse(true);
		urlCount++;
		
		
		try {
			
			SWTAsync.block(new SWTAsync("setURL") {
				@Override
				public void task() {
					try {
						browser.browser.setUrl(url);
						waitForCallback(task);
					} catch (Throwable th) {
						logger.error("error getPage:" + url, th);
					}
				}
			});
			
			lastPage = callback.page;
			return callback.page;
			
		} finally {
			synchronized (lock) {
				this.callback.setInUse(false);
			}
			System.out.println(new Date() + " finally");
		}
	}
	
	private HtmlPage waitForCallback(IProgressTask task) {
		long start = System.currentTimeMillis();
		long timeout = start + 60 * 1000;
		
		System.err.println("waitForCallback start");
		
		while (!callback.accepted) {
			Display.getCurrent().readAndDispatch();
			if (browser.isDisposed()) break;
			if (System.currentTimeMillis() > timeout) {
				logger.error("timeout waiting for web load..." + callback);
				break;
			}
			if (task != null && task.wasCanceled())
				break;
		}
		HtmlPage p = callback.page;
		if (p == null) {
			logger.error("No page for " + callback);
			logger.error(callback.progressEvent);
		}
		System.err.println("waitForCallback finish time=" + (System.currentTimeMillis() - start) + " ok=" + callback.accepted);
		return p;
	}
	
	/*
	String get(String w) {
		if (!w.endsWith(";"))
			w += ";";
		
		return eval("return " + w);
	}
	
	String eval(String w) {
		try {
			Object o = browser.browser.evaluate(w);
			String clz = o != null ? o.getClass().toString() : "";
			System.out.println("eval(" + w + ") -> " + o + " " + clz);
			
			if (o != null) {
				if (o instanceof String)
					return (String) o;
				return o.toString();
			}
			
		} catch (Throwable th) {
			logger.error("unable to eval " + w, th);
		}
		return null;
	}
	
	*/
	
	private HtmlPage getHtmlPage() {
		try {
			
			String html = this.browser.browser.getText();
			final List<NameValuePair> responseHeaders = new ArrayList<>();
			String u = browser.browser.getUrl();
			URL url = new URL(u);
			WebRequest request = new WebRequest(url);
			WebResponseData webResponseData = new WebResponseData(html.getBytes(), 200, "OK", responseHeaders);
			WebResponse webResponse = new WebResponse(webResponseData, request, 0);
			WebWindow w = this.getCurrentWindow();
			HtmlPage p = HTMLParser.parseHtml(webResponse, w);
			p.initialize();
			// String title = p.getTitleText();
			if (u.toLowerCase().contains("audible"))
				lastPage = p;
			// logger.info("xml=" + p.asXml());
			return p;
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void changed(ProgressEvent progressEvent) {
		
		callback.progressEvent = progressEvent;
		
		boolean busy = progressEvent.current != progressEvent.total;
		debug("event:" + progressEvent + " busy=" + busy);
		
		
	}
	
	private void debug(String s) {
		callback.debug += s + ",";
	}
	
	@Override
	public void completed(ProgressEvent progressEvent) {
		
		synchronized (lock) {
			
			completeCount++;
			if (callback.inUse.get()) {
				callback.page = getHtmlPage();
				callback.accepted = true;
				callback.title = callback.page != null ? callback.page.getTitleText() : "";
				lock.notifyAll();
			} else {
				System.out.println("callback not in use");
				
				
			}
		}
		
		System.out.println(callback + " urlCount=" + urlCount + " completeCount=" + completeCount);
	}
	
	@Override
	public void close() {
		
		callback.reset();
		if (browser.isDisposed()) {
			super.close();
		}
		
	}
	
	
	class CompletionCallback {
		
		final AtomicBoolean inUse = new AtomicBoolean(false);
		HtmlPage page = null;
		volatile boolean accepted = false;
		String debug = "";
		ProgressEvent progressEvent;
		String title;
		
		public String toString() {
			return "callback:" + title + " inUse=" + inUse + " debug=" + debug;
		}
		
		
		public void setInUse(boolean b) {
			if (inUse.get() && b) {
				assert (false);
			}
			inUse.set(b);
			if (b)
				reset();
			
		}
		
		public void reset() {
			accepted = false;
			page = null;
			debug = "";
			progressEvent = null;
		}
		
	}
	
	@Override
	public void stop() {
		SWTAsync.block(new SWTAsync() {
			@Override
			public void task() {
				browser.browser.stop();
				
			}
		});
	}
	
}

