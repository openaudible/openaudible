/*
 */
package org.openaudible.desktop.swt.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.Directories;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.desktop.swt.util.shop.FontShop;
import org.openaudible.util.Console;

import java.io.File;
import java.io.IOException;

public class AppLoader {
	public final static Log logger = LogFactory.getLog(GUI.class);
	Display display;
	final Application application;
	
	/**
	 * Load GUI
	 */
	public AppLoader(String args[]) {
		
		// application uses console for logging.
		Console.instance.install();
		
		String java = System.getProperty("java.version");
		logger.info("Starting " + getAppName() + " build " + Version.appVersion + " for " + SWT.getPlatform() + " swt " + SWT.getVersion() + " jvm " + java);
		
		/* Apply application name to Display */
		Display.setAppName(getAppName());
		
		display = new Display();
		
		/* Create the Working Directory if it does not yet exist */
		try {
			createWorkingDir();
			
		} catch (Throwable th) {
			th.printStackTrace();
			String msg = "Unable to create or load required directories. Check that the drives are read/writable.\nError:" + th.getMessage();
			MessageBoxFactory.showError(new Shell(SWT.NONE), "Error creating directories!", msg);
			System.exit(1);
		}
		
		new FontShop(display);
		application = new Application(display);
		application.startUp();
		application.run();
		// Call exit to close any audio threads...
		display.dispose();
		Runtime.getRuntime().exit(0);
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new AppLoader(args);
	}
	
	/**
	 * Create the Home Directory
	 */
	private void createWorkingDir() throws IOException {
		String homePath = null;
		homePath = System.getProperty("user.home");
		File hp = new File(homePath);
		if (!hp.exists()) {
			System.err.println("Bad path for home dir: " + hp.getAbsolutePath());
		}
		if (GUI.isMac()) {
			/* On Mac, append "Library/Preferences" to the user.home directory */
			hp = new File(hp, "Library");
			if (!hp.isDirectory())
				hp = new File(homePath);
		}
		
		File prefs = new File(hp, Version.appName);
		if (!prefs.isDirectory()) {
			boolean ok = prefs.mkdirs();
			if (!ok) {
				throw new IOException("Unable to create preference directory:" + prefs.getAbsolutePath());
			}
		}
		Directories.init(prefs, prefs);     // base and etc are in same dir..
	}
	
	
	public static final String getAppName() {
		return Version.appName;
	}
	
}
