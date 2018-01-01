package org.openaudible.desktop.swt.manager.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.i8n.ITranslatable;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.manager.Application;
import org.openaudible.desktop.swt.util.shop.PaintShop;

import java.util.ArrayList;
import java.util.Vector;

/**
 * This is the Menu that is displayed on top of the application
 */
public class AppMenu implements ITranslatable, SelectionListener
{
	public static final int kNoEquiv = 0;
	public static AppMenu instance;
	final CommandCenter commandCenter;
	boolean languageChange;
	final Application app;
	final Shell shell;
	final boolean isMac;
	Menu mbar;
	Menu fileMenu;
	Menu editMenu;
	Menu controlMenu;
	Command actionCommands[] = { Command.ViewInAudible, Command.Show_MP3, Command.Play, Command.Download, Command.Convert, Command.Refresh_Book_Info };
	Command appCommands[] = { Command.Connect, Command.Quick_Refresh, Command.Rescan_Library, Command.Download_All, Command.Convert_All, Command.Export_Web_Page, Command.Fetch_Decryption_Key,
			Command.Browser, Command.Check_For_Update, Command.About };
	Menu debugMenu = null;
	ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
	boolean showUnimplemented = true;
	private Menu actionMenu;

	/**
	 * Instantiate a new Sync Menu.
	 */
	public AppMenu(Application app, Shell shell, CommandCenter commandCenter)
	{
		this.app = app;
		this.shell = shell;
		this.commandCenter = commandCenter;
		isMac = Application.isMac();
		initMenuBar();
		instance = this;
	}

	public AppMenu(Shell shell)
	{
		this(Application.instance, shell, Application.instance.commandCenter);
	}

	public Menu getActionMenu()
	{
		return actionMenu;
	}

	MenuItem getItem(Menu m, Command cmd)
	{
		if (m != null)
		{
			MenuItem items[] = m.getItems();
			for (int x = 0; x < items.length; x++)
			{
				Object obj = items[x].getData();
				if (obj != null)
				{
					if (obj instanceof Command)
					{
						if (obj == cmd)
							return items[x];
					}
				}
			}
		}
		Application.report("getItem error " + cmd + " for " + m);
		return null;
		// return menuTable.get(new Integer(id));
	}

	public MenuItem newSeparator(Menu parent)
	{
		return new MenuItem(parent, SWT.SEPARATOR);
	}

	public MenuItem newMItem(Menu parent, Command id)
	{
		return newMItem(parent, id, SWT.NONE);
	}

	public MenuItem addSeparator(Menu parent)
	{
		return new MenuItem(parent, SWT.SEPARATOR);
	}

	public MenuItem installSystemMenu(final Command cmd)
	{
		if (isMac)
		{
			int id = 0;
			switch (cmd)
			{
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
			if (systemMenu != null)
			{
				for (MenuItem systemItem : systemMenu.getItems())
				{
					if (systemItem.getID() == id)
					{
						final Listener listener = new Listener()
						{
							public void handleEvent(Event event)
							{
								commandCenter.handleMenuAction(cmd, null);
							}
						};

						systemItem.addListener(SWT.Selection, listener);
						return systemItem;
					}
				}
			}
		}
		return null;
	}

	public MenuItem newMItem(Menu parent, Command cmd, int style)
	{
		MenuItem item = installSystemMenu(cmd);
		if (item != null)
			return item; // is a special Mac OS menu

		item = new MenuItem(parent, style);
		item.setData(cmd);
		char equivalent = cmd.getKeyEquiv();
		String name = cmd.getMenuName();

		if (equivalent != 0 && style != SWT.POP_UP)
		{
			if (!Application.isMac())
			{
				item.setAccelerator(SWT.CTRL + equivalent);
				name += " \tCtrl+" + equivalent;
			} else
			{
				item.setAccelerator(SWT.COMMAND + equivalent);
			}
			// updateAccelerator(item, name, equivalent, false);
			item.setText(name);
		} else
		{
			item.setText(name);
		}

		item.addSelectionListener(this);
		if (!isMac && cmd.getImage() != null)
		{
			Image img = PaintShop.getImage(cmd.getImage());
			if (img != null)
				item.setImage(img);
		}

		registerMenuItem(item);

		return item;
	}

	public Menu createWinSizeMenu()
	{
		Menu m = newMenu("Window Size");
		String menuSize[] = { "800x600", "1024x768", "1024x1024", "1152x768", "1280x768", "1366x768", "1280x1080", "1920x1080" };

		for (String t : menuSize)
		{
			MenuItem item = new MenuItem(m, SWT.PUSH);
			item.setData(t);
			item.setText(t);
			item.addSelectionListener(this);
		}
		return m;
	}

	protected Menu newMenu(String name)
	{
		name = Translate.getInstance().menuName(name);
		final MenuItem am = new MenuItem(mbar, SWT.CASCADE);
		am.setText(name);
		Menu m = newMenu(SWT.DROP_DOWN);
		am.setMenu(m);
		return m;
	}

	private Menu newMenu(int style)
	{
		final Menu m = new Menu(shell, style);

		m.addListener(SWT.Show, new Listener()
		{
			public void handleEvent(Event event)
			{
				updateMenus();
			}
		});

		return m;
	}

	protected void initMenuBar()
	{
		mbar = new Menu(shell, SWT.BAR);

		if (!isMac)
		{
			fileMenu = newMenu("File");
			MenuItem exit = newMItem(fileMenu, Command.Quit);
		}

		editMenu = newMenu("Edit");
		newMItem(editMenu, Command.Cut);
		newMItem(editMenu, Command.Copy);

		newMItem(editMenu, Command.Paste);
		newSeparator(editMenu);
		newMItem(editMenu, Command.Preferences);

		controlMenu = newMenu("Controls");
		for (Command c : appCommands)
		{
			if (isMac && c == Command.About)
				continue;

			newMItem(controlMenu, c);
		}

		actionMenu = newMenu("Actions");
		for (Command c : actionCommands)
		{
			newMItem(actionMenu, c);
		}

		shell.setMenuBar(mbar);

	}

	private void setEnabled(Menu m, Command cmd, boolean value)
	{
		MenuItem mi = getItem(m, cmd);
		if (mi != null)
			mi.setEnabled(value);
	}

	private void setChecked(Menu m, Command cmd, boolean value)
	{
		MenuItem mi = getItem(m, cmd); // (value);
		mi.setSelection(value);
	}

	// Must be called from display thread.
	public void updateMenus()
	{
		if (app.quitting)
			return;

		for (MenuItem m : menuItems)
		{
			if (m.isDisposed())
			{
				System.err.println(m + " menuitem disposed");

				// menuItems.remove(m);
				continue;
				// assert(!m.isDisposed());
			}

			Object t = m.getData();
			if (t instanceof IMenuCommand)
			{
				((IMenuCommand) t).update(m);
			} else if (t instanceof Command)
			{
				Command c = (Command) t;
				m.setEnabled(CommandCenter.instance.getEnabled(c));
			}
		}

		setEnabled(editMenu, Command.Copy, false);
		setEnabled(editMenu, Command.Paste, false);
		setEnabled(editMenu, Command.Cut, false);

	}

	public void initMnemonics()
	{
		if (mbar != null)
			initMnemonics(mbar.getItems());
	}

	/**
	 * Init the mnemonics
	 */
	// public void initMnemonics() {
	void initMnemonics(MenuItem items[])
	{

		/** Store chars that have been used as mnemonic */
		Vector chars = new Vector();
		/** For each MenuItem */
		for (int a = 0; a < items.length; a++)
		{
			String name = items[a].getText();
			/** Replace any & that are existing */
			name = name.replaceAll("&", "");
			/** For each char in the name */
			for (int b = 0; b < name.length(); b++)
			{
				/** Check if char is available and no whitespace */
				if (name.substring(b, b + 1) != null && !name.substring(b, b + 1).equals(" "))
				{
					/** Check if char has been used as mnemonic before */
					if (!chars.contains(name.substring(b, b + 1).toLowerCase()))
					{
						/** Set mnemonic */
						items[a].setText(name.substring(0, b) + "&" + name.substring(b, name.length()));
						/** Add char as used mnemonic */
						chars.add(name.substring(b, b + 1).toLowerCase());
						break;
					}
				}
			}
			/** Also check MenuItems ob possible Sub-Menus */
			if (items[a].getMenu() != null)
				initMnemonics(items[a].getMenu().getItems());
		}
	}

	/**
	 * Update the accelerators on the menuitems
	 */
	public void updateAccelerators()
	{
	}

	/**
	 * Update all controlls text with i18n
	 */
	public void updateI18N()
	{
		languageChange = true;
		/** Update accelerators */
		updateAccelerators();
		/** Update the mnemonics */
		initMnemonics();
	}

	/**
	 * Update the accelerators on the selected menuitem
	 *
	 * @param menuItem
	 *            Selected menuitem
	 * @param text
	 *            Translation key of the MenuItem's text
	 * @param type
	 *            Translation key of the selected menuitem
	 * @param points
	 *            TRUE if "..." should be added to the item
	 */
	private void updateAccelerator(MenuItem menuItem, String text, String type, boolean points)
	{

	}

	/**
	 * Remove all accelerators except the ones from the Edit Menu
	 */
	void removeAccelerators()
	{
	}

	/**
	 * Update the accelerators on the selected menuitem
	 *
	 * @param menuItem
	 *            Selected menuitem
	 * @param type
	 *            Translation key of the selected menuitem
	 * @param points
	 *            TRUE if "..." should be added to the item
	 */
	void updateAccelerator(MenuItem menuItem, String type, boolean points)
	{
		updateAccelerator(menuItem, type, type, points);
	}

	/**
	 * This is a workaround for bug #915624. Update hotkeys that use a single character (in this case 'n' and 'f').
	 */
	void updateOneCharAccelerators()
	{
	}

	// boolean useSleak = false;
	public void widgetSelected(SelectionEvent selectionevent)
	{
		Object obj = selectionevent.getSource();
		if (obj instanceof MenuItem)
		{
			MenuItem mi = (MenuItem) obj;
			obj = mi.getData();
			if (obj instanceof Command)
				commandCenter.handleMenuAction((Command) obj, mi);
			else if (obj instanceof MenuCommand)
			{
				MenuCommand mc = (MenuCommand) obj;
				mc.execute();
			}

		}

	}

	public void widgetDefaultSelected(SelectionEvent selectionevent)
	{
		widgetSelected(selectionevent);
	}

	public void registerMenuItem(final MenuItem mi)
	{
		menuItems.add(mi);

		mi.addListener(SWT.Dispose, new Listener()
		{
			public void handleEvent(Event event)
			{
				menuItems.remove(mi);
			}
		});

	}

	public void setMBarVisible(boolean b)
	{
		if (b)
		{
			initMenuBar();
		} else
		{
			for (MenuItem mi : mbar.getItems())
			{
				Menu m = mi.getMenu();
				for (MenuItem mi2 : m.getItems())
					menuItems.remove(mi2);
				m.dispose();
			}

			mbar.dispose();
		}

	}

	public Menu getBookTableMenu()
	{
		Menu menu = newMenu(SWT.POP_UP);
		for (Command c : actionCommands)
		{
			newMItem(menu, c);
		}

		return menu;
	}
}