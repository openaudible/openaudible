package org.openaudible.desktop.swt.manager.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.i8n.ITranslatable;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.manager.Application;
import org.openaudible.desktop.swt.manager.Version;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.util.ArrayList;
import java.util.Vector;

/**
 * This is the Menu that is displayed on top of the application
 */
public class AppMenu implements ITranslatable, SelectionListener {
	public static final int kNoEquiv = 0;
	public static AppMenu instance;
	private final CommandCenter commandCenter;
	private final Application app;
	private final Shell shell;
	private final boolean isMac;
	private Menu mbar;
	private Menu fileMenu;
	private Menu editMenu;
	private Menu controlMenu;
	private Menu aboutMenu;
	
	private final Command[] actionCommands = {Command.ViewInAudible, Command.Show_MP3, Command.Play, Command.Download,
			Command.Convert, Command.Refresh_Book_Info, Command.Ignore_Book};
	private final Command[] controlCommands = {Command.Connect, Command.Quick_Refresh, Command.Rescan_Library, Command.Download_All, Command.Convert_All,
			Command.MenuSeparator, Command.Browser, Command.Logout_and_Clear_Cookies}; // , Command.MenuSeparator, Command.Logout_and_Clear_Cookies};
	
	private final Command[] aboutCommands = {Command.Help, Command.AppWebPage, Command.Check_For_Update, Command.About};
	
	private final ArrayList<MenuItem> menuItems = new ArrayList<>();
	private Menu actionMenu;
	
	public AppMenu(Application app, Shell shell, CommandCenter commandCenter) {
		this.app = app;
		this.shell = shell;
		this.commandCenter = commandCenter;
		isMac = Application.isMac();
		initMenuBar();
		instance = this;
	}
	
	
	private MenuItem getItem(Menu m, Command cmd) {
		if (m != null) {
			MenuItem items[] = m.getItems();
			for (MenuItem item : items) {
				Object obj = item.getData();
				if (obj != null) {
					if (obj instanceof Command) {
						if (obj == cmd)
							return item;
					}
				}
			}
		}
		Application.report("getItem error " + cmd + " for " + m);
		return null;
	}
	
	private MenuItem newSeparator(Menu parent) {
		return new MenuItem(parent, SWT.SEPARATOR);
	}
	
	private MenuItem newMItem(Menu parent, Command id) {
		return newMItem(parent, id, SWT.NONE);
	}
	
	
	// Mac uses a system menu for 3 items. Implement these differently.
	private MenuItem installSystemMenu(final Command cmd) {
		if (isMac) {
			int id = 0;
			switch (cmd) {
				case Preferences:
					id = SWT.ID_PREFERENCES;
					break;
				case About:
					id = SWT.ID_ABOUT;
					break;
				case Quit:
					id = SWT.ID_QUIT;
					break;
				default: // Only the menus above are system menus.
					return null;
				
			}
			
			Menu systemMenu = Display.getDefault().getSystemMenu();
			if (systemMenu != null) {
				for (MenuItem systemItem : systemMenu.getItems()) {
					if (systemItem.getID() == id) {
						final Listener listener = event -> commandCenter.handleMenuAction(cmd, null);
						
						systemItem.addListener(SWT.Selection, listener);
						return systemItem;
					}
				}
			}
		}
		return null;
	}
	
	
	private MenuItem newMItem(Menu parent, Command cmd, int style) {
		MenuItem item = installSystemMenu(cmd);
		if (item != null)
			return item; // is a special Mac OS menu
		if (cmd == Command.MenuSeparator) {
			return newSeparator(parent);
		}
		
		
		item = new MenuItem(parent, style);
		item.setData(cmd);
		char equivalent = cmd.getKeyEquiv();
		String name = cmd.getMenuName();
		
		if (equivalent != 0 && style != SWT.POP_UP) {
			if (!Application.isMac()) {
				item.setAccelerator(SWT.CTRL + equivalent);
				name += " \tCtrl+" + equivalent;
			} else {
				item.setAccelerator(SWT.COMMAND + equivalent);
			}
			// updateAccelerator(item, name, equivalent, false);
			item.setText(name);
		} else {
			item.setText(name);
		}
		
		item.addSelectionListener(this);
		if (!isMac && cmd.getImage() != null) {
			Image img = PaintShop.getImage(cmd.getImage());
			if (img != null)
				item.setImage(img);
		}
		
		registerMenuItem(item);
		
		return item;
	}
	
	
	private Menu newMenu(String name) {
		name = Translate.getInstance().menuName(name);
		final MenuItem am = new MenuItem(mbar, SWT.CASCADE);
		am.setText(name);
		Menu m = newMenu(SWT.DROP_DOWN);
		am.setMenu(m);
		return m;
	}
	
	private Menu newMenu(int style) {
		final Menu m = new Menu(shell, style);
		m.addListener(SWT.Show, event -> updateMenus());
		return m;
	}
	
	private void initMenuBar() {
		mbar = new Menu(shell, SWT.BAR);
		
		fileMenu = newMenu("File");
		newMItem(fileMenu, Command.Console);
		newSeparator(fileMenu);
		newMItem(fileMenu, Command.Import_AAX_Files);
		newSeparator(fileMenu);
		newMItem(fileMenu, Command.Export_Web_Page);
		newMItem(fileMenu, Command.Export_Book_List);
		
		if (!isMac) {
			newSeparator(fileMenu);
		}
		newMItem(fileMenu, Command.Quit);
		
		
		editMenu = newMenu("Edit");
		newMItem(editMenu, Command.Cut);
		newMItem(editMenu, Command.Copy);
		
		newMItem(editMenu, Command.Paste);
		newSeparator(editMenu);
		newMItem(editMenu, Command.Preferences);
		
		controlMenu = newMenu("Controls");
		for (Command c : controlCommands) {
			newMItem(controlMenu, c);
		}
		if (Version.appDebug)
			newMItem(controlMenu, Command.Test1);
		
		
		actionMenu = newMenu("Actions");
		for (Command c : actionCommands) {
			newMItem(actionMenu, c);
		}
		
		aboutMenu = newMenu("Help");
		for (Command c : aboutCommands) {
			newMItem(aboutMenu, c);
		}
		
		shell.setMenuBar(mbar);
		
	}
	
	private void setEnabled(Menu m, Command cmd, boolean value) {
		MenuItem mi = getItem(m, cmd);
		if (mi != null)
			mi.setEnabled(value);
	}
	
	private void setChecked(Menu m, Command cmd, boolean value) {
		MenuItem mi = getItem(m, cmd); // (value);
		mi.setSelection(value);
	}
	
	// Must be called from display thread.
	public void updateMenus() {
		if (app.quitting)
			return;
		
		MenuItem browser;
		
		for (MenuItem m : menuItems) {
			if (m.isDisposed()) {
				System.err.println(m + " menuitem disposed");
				
				// menuItems.remove(m);
				continue;
				// assert(!m.isDisposed());
			}
			
			Object t = m.getData();
			if (t instanceof IMenuCommand) {
				((IMenuCommand) t).update(m);
			} else if (t instanceof Command) {
				Command c = (Command) t;
				commandCenter.updateMenu(c, m);
			}
			
			
		}
		
		setEnabled(editMenu, Command.Copy, false);
		setEnabled(editMenu, Command.Paste, false);
		setEnabled(editMenu, Command.Cut, false);
		
		
	}
	
	
	private void initMnemonics() {
		if (mbar != null)
			initMnemonics(mbar.getItems());
	}
	
	/**
	 * Init the mnemonics
	 */
	// public void initMnemonics() {
	private void initMnemonics(MenuItem items[]) {
		
		/* Store chars that have been used as mnemonic */
		Vector<String> chars = new Vector<>();
		/* For each MenuItem */
		for (MenuItem item : items) {
			String name = item.getText();
			/* Replace any & that are existing */
			name = name.replaceAll("&", "");
			/* For each char in the name */
			for (int b = 0; b < name.length(); b++) {
				/* Check if char is available and no whitespace */
				if (name.substring(b, b + 1) != null && !name.substring(b, b + 1).equals(" ")) {
					/* Check if char has been used as mnemonic before */
					if (!chars.contains(name.substring(b, b + 1).toLowerCase())) {
						/* Set mnemonic */
						item.setText(name.substring(0, b) + "&" + name.substring(b, name.length()));
						/* Add char as used mnemonic */
						chars.add(name.substring(b, b + 1).toLowerCase());
						break;
					}
				}
			}
			/* Also check MenuItems ob possible Sub-Menus */
			if (item.getMenu() != null)
				initMnemonics(item.getMenu().getItems());
		}
	}
	
	public void updateI18N() {
		initMnemonics();
	}
	
	
	public void widgetSelected(SelectionEvent selectionevent) {
		Object obj = selectionevent.getSource();
		if (obj instanceof MenuItem) {
			MenuItem mi = (MenuItem) obj;
			obj = mi.getData();
			if (obj instanceof Command)
				commandCenter.handleMenuAction((Command) obj, mi);
			else if (obj instanceof MenuCommand) {
				MenuCommand mc = (MenuCommand) obj;
				mc.execute();
			}
			
		}
		
	}
	
	public void widgetDefaultSelected(SelectionEvent selectionevent) {
		widgetSelected(selectionevent);
	}
	
	private void registerMenuItem(final MenuItem mi) {
		menuItems.add(mi);
		mi.addListener(SWT.Dispose, event -> menuItems.remove(mi));
	}
	
	public Menu getBookTableMenu() {
		Menu menu = newMenu(SWT.POP_UP);
		for (Command c : actionCommands) {
			newMItem(menu, c);
		}
		
		return menu;
	}
}