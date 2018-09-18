package org.openaudible.desktop;

import org.openaudible.desktop.swt.manager.AppLoader;
import org.openaudible.desktop.swt.manager.Version;
import org.openaudible.desktop.swt.util.shop.BuildInstaller;


public class Application {
	public static void main(String[] args) throws Exception {
		
		if (args.length == 1 && "--version".equals(args[0])) {
			System.out.println(Version.appVersion);
			return;
		}
		if (args.length > 0 && "--install".equals(args[0])) {
			BuildInstaller.main(args);
		}
		
		AppLoader.main(args);
	}
}