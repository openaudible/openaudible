package org.openaudible.desktop.swt.manager.browser;

import com.gargoylesoftware.htmlunit.util.Cookie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.openaudible.Directories;
import org.openaudible.audible.AudibleClient;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.util.Platform;

import java.io.File;
import java.net.URI;
import java.util.Collection;


public class AudibleBrowser {
	public final static Log logger = LogFactory.getLog(AudibleBrowser.class);
	final BrowserWebClient browserWebClient;
	protected Browser browser;
	int index;
	boolean busy;
	Image icon = null;
	boolean title = false;
	Composite parent;
	Text locationBar;
	ToolBar toolbar;
	Canvas canvas;
	ToolItem itemBack, itemForward;
	Label status;
	ProgressBar progressBar;
	SWTError error = null;
	Collection<Cookie> cookies;
	String customHeader[];
	
	
	public AudibleBrowser(Composite parent, String url) {
		this.parent = parent;
		
		if (Platform.isWindows())
			silenceWindowsExplorer();
		int style = 0;
		
		if (Platform.isLinux())
			style = SWT.WEBKIT;
		
		browser = new Browser(parent, style);
		browser.addTitleListener(event -> getShell().setText(event.title));
		
		
		customHeader = new String[1];
		customHeader[0] = "User-agent: " + AudibleClient.swtWindows;
		
		initResources();
		
		getShell().addListener(SWT.Close, event -> {
			event.doit = false;
			setVisible(false);
		});
		
		
		getShell().addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				new Exception("browser shell disposed:").printStackTrace();
				;
				
			}
		});
		
		
		if (true)
			show(false, null, null, true, true, true, true);
		else
			show(false, null, null, false, false, false, false);
		
		locationBar.setText(url);
		
		browserWebClient = new BrowserWebClient(this);
		
		
		if (url.length() > 0) {
			setUrl(url);
		}
		
		
	}
	
	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a missing String. Returns the key if not found.
	 */
	static String getResourceString(String key) {
		switch (key) {
			case "window.title":
				return "Audible";
		}
		return key;
	}
	
	public static AudibleBrowser newBrowserWindow(String url, boolean visible) {
		
		class BrowserWindow extends Window {
			public BrowserWindow(Shell parentShell) {
				super(parentShell);
			}
			
			protected void handleShellCloseEvent() {
				System.out.println("handleClose...");
				
				// for example: setReturnCode(OK);
				// Do whatever you want
			}
			
		}
		
		final Shell shell;
		if (false) {
			//
			BrowserWindow window = new BrowserWindow(null);
			window.create();
			
			
			shell = window.getShell();
		} else
		{
			shell = new Shell(Display.getCurrent());
			
			
		}
		shell.setVisible(visible);
		shell.setActive();
		
		// Shell shell = new Shell(display);
		FillLayout layout = new FillLayout();
		
		System.out.println("style=" + shell.getStyle());
		
		
		shell.setLayout(layout);
		shell.setText(url);
		shell.setSize(900, 800);
		shell.setVisible(visible);
		
		try {
			AudibleBrowser app = new AudibleBrowser(shell, url);
			return app;
		} catch (Throwable th) {
			shell.dispose();
			String err = "Uh oh. An error occurred opening the internal web browser. \n\nThis means your system may not be compatible with this version of OpenAudible.\n\nCheck the console window and copy this error and stack trace below. \n" +
					"Create or view existing code issues at:\nhttps://github.com/openaudible/openaudible/issues \n";
			String m = "" + th.getMessage();
			m = m.replace("No more handles", "");
			m = m.replace("[", " ");
			m = m.replace("]", " ");
			err += "\n\nError code:\n" + m.trim();
			logger.error(err, th);
			MessageBoxFactory.showError(null, "Error opening internal web browser", err);
			return null;
		}
		
		
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		
		AudibleBrowser app = newBrowserWindow("https://audible.com", true);
		
		while (!app.getShell().isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		app.dispose();
		display.dispose();
	}
	
	public static void showHelp() {
		File dir = Directories.getHelpDirectory();
		File index = new File(dir, "index.html");
		if (index.exists()) {
			URI uri = index.toURI();
			String u = uri.toString();
			newBrowserWindow(u, true);
		} else {
			MessageBoxFactory.showError(null, "Unable to open help. Expected at:" + index.getAbsolutePath());
		}
	}
	
	public BrowserWebClient getBrowserWebClient() {
		return browserWebClient;
	}
	
	// Silence Windows SWT.browser widget from making awful clicks.
	// For windows 32 and 64 bit SWT applications.
	// Uses reflection to call OS.CoInternetSetFeatureEnabled(OS.FEATURE_DISABLE_NAVIGATION_SOUNDS, OS.SET_FEATURE_ON_PROCESS, true);
	// Without importing platform specific
	// #import org.eclipse.swt.internal.win32.OS
	private void silenceWindowsExplorer() {
		try {
			Class<?> c = Class.forName("org.eclipse.swt.internal.win32.OS");
			java.lang.reflect.Method method = c.getDeclaredMethod("CoInternetSetFeatureEnabled", Integer.TYPE, Integer.TYPE, Boolean.TYPE);
			method.invoke(null, new Object[]{21, 2, true});
		} catch (Throwable th) {
			// Might fail.. but probably will never do harm.
			th.printStackTrace();
		}
	}
	
	/**
	 * Disposes of all resources associated with a particular instance of the BrowserApplication.
	 */
	public void dispose() {
		freeResources();
	}
/*

    public Browser getBrowser() {
        return browser;
    }
*/
	
	public SWTError getError() {
		return error;
	}
	
	public void setShellDecoration(Image icon, boolean title) {
		this.icon = icon;
		this.title = title;
	}
	
	void show(boolean owned, Point location, Point size, boolean addressBar, boolean menuBar, boolean statusBar, boolean toolBar) {
		final Shell shell = browser.getShell();
		if (owned) {
			if (location != null)
				shell.setLocation(location);
			if (size != null)
				shell.setSize(shell.computeSize(size.x, size.y));
		}
		statusBar = true;
		
		FormData data = null;
		if (toolBar) {
			toolbar = new ToolBar(parent, SWT.NONE);
			data = new FormData();
			data.top = new FormAttachment(0, 5);
			toolbar.setLayoutData(data);
			itemBack = new ToolItem(toolbar, SWT.PUSH);
			itemBack.setText(getResourceString("Back"));
			itemForward = new ToolItem(toolbar, SWT.PUSH);
			itemForward.setText(getResourceString("Forward"));
			final ToolItem itemStop = new ToolItem(toolbar, SWT.PUSH);
			itemStop.setText(getResourceString("Stop"));
			final ToolItem itemRefresh = new ToolItem(toolbar, SWT.PUSH);
			itemRefresh.setText(getResourceString("Refresh"));
			final ToolItem itemGo = new ToolItem(toolbar, SWT.PUSH);
			itemGo.setText(getResourceString("Go"));
			
			
			itemBack.setEnabled(browser.isBackEnabled());
			itemForward.setEnabled(browser.isForwardEnabled());
			Listener listener = event -> {
				ToolItem item = (ToolItem) event.widget;
				if (item == itemBack)
					browser.back();
				else if (item == itemForward)
					browser.forward();
				else if (item == itemStop)
					browser.stop();
				else if (item == itemRefresh)
					browser.refresh();
				else if (item == itemGo)
					setUrl(locationBar.getText());
			};
			itemBack.addListener(SWT.Selection, listener);
			itemForward.addListener(SWT.Selection, listener);
			itemStop.addListener(SWT.Selection, listener);
			itemRefresh.addListener(SWT.Selection, listener);
			itemGo.addListener(SWT.Selection, listener);
			
			canvas = new Canvas(parent, SWT.NO_BACKGROUND);
			data = new FormData();
			data.width = 24;
			data.height = 24;
			data.top = new FormAttachment(0, 5);
			data.right = new FormAttachment(100, -5);
			canvas.setLayoutData(data);
			
			canvas.addListener(SWT.MouseDown, e -> setUrl(getResourceString("Startup")));
			
		}
		if (addressBar) {
			locationBar = new Text(parent, SWT.BORDER);
			data = new FormData();
			if (toolbar != null) {
				data.top = new FormAttachment(toolbar, 0, SWT.TOP);
				data.left = new FormAttachment(toolbar, 5, SWT.RIGHT);
				data.right = new FormAttachment(canvas, -5, SWT.DEFAULT);
			} else {
				data.top = new FormAttachment(0, 0);
				data.left = new FormAttachment(0, 0);
				data.right = new FormAttachment(100, 0);
			}
			locationBar.setLayoutData(data);
			locationBar.addListener(SWT.DefaultSelection, e -> setUrl(locationBar.getText()));
		}
		if (statusBar) {
			status = new Label(parent, SWT.NONE);
			progressBar = new ProgressBar(parent, SWT.NONE);
			
			data = new FormData();
			data.left = new FormAttachment(0, 5);
			data.right = new FormAttachment(progressBar, 0, SWT.DEFAULT);
			data.bottom = new FormAttachment(100, -5);
			status.setLayoutData(data);
			
			data = new FormData();
			data.right = new FormAttachment(100, -5);
			data.bottom = new FormAttachment(100, -5);
			progressBar.setLayoutData(data);
			/*
			LocationListener ll = new LocationListener() {
				@Override
				public void changing(LocationEvent locationEvent) {
				
				}
				
				@Override
				public void changed(LocationEvent locationEvent) {
					
					try {
						String u = locationEvent.location;
						if (u.startsWith("http")) {
							
							URL url = new URL(u);
							String h = url.getHost();
							if (h.contains("audible")) {
								ConnectionNotifier.instance.setLastURL(locationEvent.location);
							} else {
								System.err.println("ignore non-audible..." + h);
							}
							
						}
					} catch (Throwable th) {
						th.printStackTrace();
					}
					
				}
			};
			
			browser.addLocationListener(ll);
			*/
			
			browser.addStatusTextListener(event -> status.setText(event.text));
		}
		
		/* Define the function to call from JavaScript */
/*
		new BrowserFunction(browser, "cookieCallback") {
			@Override
			public Object function(Object[] objects) {
				
				ArrayList<Cookie> list = new ArrayList<>();
				String u = browser.getUrl();
				
				if (u.contains("audible.")) {
					// Get host from url.
					// https://www.audible.com/
					String host = "www.audible.com";
					
					if (u.startsWith("http")) {
						String[] parts = u.split("/");
						if (parts != null && parts.length > 2) {
							if (parts[2].contains("audible"))
								host = parts[2];
						}
					}
					
					
					Object[] keyValuePairs = (Object[]) objects[0];
					for (Object o : keyValuePairs) {
						Object arr[] = (Object[]) o;
						Cookie c = new Cookie(host, arr[0].toString(), arr[1].toString());
						list.add(c);
					}
					cookies = list;
				} else
					logger.info("Expected url to include audible, instead: " + u);
				
				logger.info("cookieCallback: " + list.size());
				
				return null;
			}
		};
*/
		
		/* Define the function to call from JavaScript */
		/*
		new BrowserFunction(browser, "pageInfoCallback") {
			@Override
			public Object function(Object[] objects) {
				String u = browser.getUrl();
				logger.info("pageInfoCallback: " + u);
				return null;
			}
		};
		*/
		
		parent.setLayout(new FormLayout());
		
		Control aboveBrowser = toolBar ? canvas : (addressBar ? locationBar : null);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = aboveBrowser != null ? new FormAttachment(aboveBrowser, 5, SWT.DEFAULT) : new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = status != null ? new FormAttachment(status, -5, SWT.DEFAULT) : new FormAttachment(100, 0);
		browser.setLayoutData(data);
		
		if (statusBar || toolBar) {
			browser.addProgressListener(new ProgressListener() {
				@Override
				public void changed(ProgressEvent event) {
					if (event.total == 0)
						return;
					int ratio = event.current * 100 / event.total;
					if (progressBar != null)
						progressBar.setSelection(ratio);
					busy = event.current != event.total;
					if (!busy) {
						index = 0;
						if (canvas != null)
							canvas.redraw();
					}
				}
				
				@Override
				public void completed(ProgressEvent event) {
					if (progressBar != null)
						progressBar.setSelection(0);
					busy = false;
					index = 0;
					if (canvas != null) {
						itemBack.setEnabled(browser.isBackEnabled());
						itemForward.setEnabled(browser.isForwardEnabled());
						canvas.redraw();
					}
				}
			});
		}
		if (addressBar || statusBar || toolBar) {
			browser.addLocationListener(new LocationListener() {
				@Override
				public void changed(LocationEvent event) {
					busy = true;
					if (event.top && locationBar != null)
						locationBar.setText(event.location);
				}
				
				@Override
				public void changing(LocationEvent event) {
				}
			});
		}
		
		parent.layout(true);
		if (owned)
			shell.open();
	}
	
	
	public void setUrl(String u) {
		try {
			locationBar.setText(u);
			browserWebClient.getPage(u);
		} catch (Throwable th) {
			logger.error(u, th);// browser.setUrl(u, null, customHeader);
		}
	}
	
	/**
	 * Grabs input focus
	 */
	public void focus() {
		if (locationBar != null)
			locationBar.setFocus();
		else if (browser != null)
			browser.setFocus();
		else
			parent.setFocus();
	}
	
	/**
	 * Frees the resources
	 */
	void freeResources() {
	
	}
	/*
	public Collection<Cookie> getCookies() {
		cookies = null;
		SWTAsync.block(new SWTAsync("getCookies") {
						   @Override
						   public void task() {
							   String listCookies = "document.cookie.split( ';' ).map( function( x ) { return x.trim().split( '=' ); } )";
							   String pageInfo = "";// document.cookie.split( ';' ).map( function( x ) { return x.trim().split( '=' ); } )";
				
							   browser.execute("cookieCallback(" + listCookies + ");");
							   browser.execute("pageInfoCallback(" + pageInfo + ");");
				
				
						   }
					   }
		);
		
		return cookies;
	}
	*/
	
	/**
	 * Loads the resources
	 */
	void initResources() {
	}
	
	public Shell getShell() {
		return browser.getShell();
	}
	
	public boolean isDisposed() {
		return browser == null || browser.isDisposed();
	}
	
	public void close() {
		if (!isDisposed()) browser.close();
		browser = null;
		browserWebClient.close();
	}
	
	public boolean isVisible() {
		return !isDisposed() && browser.isVisible();
	}
	
	public void setVisible(boolean b) {
		
		if (!isDisposed()) {
			browser.getShell().setVisible(b);
		}
	}
}
