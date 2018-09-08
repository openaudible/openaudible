package org.openaudible.desktop.swt.manager.menu;

import org.openaudible.desktop.swt.i8n.Translate;

public enum Command {
	Preferences,
	Quit,
	Cut,
	Copy,
	Paste,
	About,
	ViewInAudible,
	Show_MP3, Show_AAX,
	Play,
	Console,
	Export_Web_Page,
	Export_Book_List,
	Import_AAX_Files,
	Refresh_Book_Info,
	Connect,
	Quick_Refresh,
	Rescan_Library,
	Download,
	ParseAAX,
	Convert,
	Download_All,
	Convert_All,
	Help,
	Browser,
	Check_For_Update,
	AppWebPage,
	Logout,
	Test1,
	MenuSeparator,
	Ignore_Book;
	
	
	public char getKeyEquiv() {
		char cmd = 0;
		switch (this) {
			
			case Preferences:
				cmd = ',';
				break;
			case Quit:
				cmd = 'Q';
				break;
			case Cut:
				cmd = 'X';
				break;
			case Copy:
				cmd = 'C';
				break;
			case Paste:
				cmd = 'V';
				break;
			case About:
				break;
			case ViewInAudible:
				break;
			case Show_MP3:
				break;
			case Show_AAX:
				break;
			case Play:
				break;
			case Export_Web_Page:
				break;
			case Export_Book_List:
				break;
			case Refresh_Book_Info:
				break;
			case Connect:
				break;
			case Quick_Refresh:
				cmd = 'R';
				break;
			case Rescan_Library:
				break;
			case Download:
				break;
			case ParseAAX:
				break;
			case Convert:
				break;
			case Download_All:
				break;
			case Convert_All:
				break;
			case Browser:
				cmd = 'B';
				break;
			case Logout:
			case Console:
				break;
		}
		return cmd;
		
	}
	
	public String getImage() {
		String file = null;
		switch (this) {
			
			case Preferences:
				file = "cog.png";
				break;
			case Quit:
				break;
			case Cut:
				file = "cut.gif";
				break;
			case Copy:
				file = "copy.gif";
				break;
			case Paste:
				file = "paste.gif";
				break;
			case About:
				break;
			case ViewInAudible:
				break;
			case Show_MP3:
				break;
			case Show_AAX:
				break;
			case Play:
				break;
			case Export_Web_Page:
				break;
			case Export_Book_List:
				break;
			case Refresh_Book_Info:
				break;
			case Connect:
				break;
			case Quick_Refresh:
				break;
			case Rescan_Library:
				break;
			case Download:
				break;
			case ParseAAX:
				break;
			case Convert:
				break;
			case Download_All:
				break;
			case Convert_All:
				break;
			case Browser:
				break;
		}
		return file;
		
	}
	
	
	public String getToolTip() {
		return "";
	}
	
	public String getMenuName() {
		return Translate.getInstance().commandName(this.name());
	}
	
	
	public String getDisabledImage() {
		return null;
	}
	
}
