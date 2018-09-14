package org.openaudible.desktop.swt.manager.views;

import com.gargoylesoftware.htmlunit.History;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;
import org.eclipse.swt.widgets.Composite;


public class AudibleWebPage extends AudibleBrowser implements WebWindow {

	String name = "";

	public AudibleWebPage(Composite parent, String url) {
		super(parent, url);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String s) {
		name = s;
	}

	@Override
	public Page getEnclosedPage() {
		return null;
	}

	@Override
	public void setEnclosedPage(Page page) {

	}

	@Override
	public WebWindow getParentWindow() {
		return null;
	}

	@Override
	public WebWindow getTopWindow() {
		return this;
	}

	@Override
	public WebClient getWebClient() {
		assert (false);
		return null;
	}

	@Override
	public History getHistory() {
		return null;
	}

	@Override
	public void setScriptableObject(ScriptableObject scriptableObject) {

	}

	@Override
	public ScriptableObject getScriptableObject() {
		return null;
	}

	@Override
	public JavaScriptJobManager getJobManager() {
		return null;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public int getInnerWidth() {
		return 0;
	}

	@Override
	public void setInnerWidth(int i) {

	}

	@Override
	public int getOuterWidth() {
		return 0;
	}

	@Override
	public void setOuterWidth(int i) {

	}

	@Override
	public int getInnerHeight() {
		return 0;
	}

	@Override
	public void setInnerHeight(int i) {

	}

	@Override
	public int getOuterHeight() {
		return 0;
	}

	@Override
	public void setOuterHeight(int i) {

	}
}
