package org.openaudible.desktop.swt.manager.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;

public class MenuCommand implements IMenuCommand {
	final String name;
	public Object data;
	int style = SWT.PUSH;
	boolean isCheckable = false;
	
	public MenuCommand(String n, Object d, int s) {
		name = n;
		data = d;
		style = s;
	}
	
	public MenuCommand(String n, Object d) {
		name = n;
		data = d;
	}
	
	@Override
	public void execute() {
		System.err.println("override required for " + this);
	}
	
	@Override
	public void update(MenuItem m) {
		m.setEnabled(isEnabled(m));
		if (isCheckable())
			m.setSelection(isChecked(m));
	}
	
	@Override
	public int getStyle() {
		int s = style;
		if (isCheckable())
			return SWT.CHECK;
		return s;
	}
	
	@Override
	public String getImage() {
		return null;
	}
	
	@Override
	public char getMenuEquivalent() {
		return AppMenu.kNoEquiv;
	}
	
	@Override
	public String getMenuName() {
		return name;
	}
	
	@Override
	public boolean isChecked(MenuItem m) {
		return false;
	}
	
	public boolean isCheckable() {
		return isCheckable;
	}
	
	@Override
	public boolean isEnabled(MenuItem m) {
		return true;
	}
}
