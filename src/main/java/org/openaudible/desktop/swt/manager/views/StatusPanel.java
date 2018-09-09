package org.openaudible.desktop.swt.manager.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.audible.ConnectionListener;
import org.openaudible.audible.ConnectionNotifier;
import org.openaudible.books.Book;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.i8n.Translate;
import org.openaudible.desktop.swt.manager.AudibleGUI;
import org.openaudible.desktop.swt.util.shop.FontShop;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StatusPanel extends GridComposite implements BookListener, ConnectionListener {
	Label stats[];
	// Label connected;
	
	
	class StatusClick extends MouseAdapter {
		final Status status;
		
		StatusClick(Status s) {
			status = s;
		}
		
		@Override
		public void mouseDown(MouseEvent mouseEvent) {
			super.mouseDown(mouseEvent);
			System.out.println("click:" + status);
			if (status.canFilterByStatusType())
				AudibleGUI.instance.setStatusFilter(status);
			else
				AudibleGUI.instance.setStatusFilter(null);
			
		}
	}
	
	StatusPanel(Composite c) {
		super(c, SWT.NONE);
		initLayout(2, false, GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		
		BookNotifier.getInstance().addListener(this);
		ConnectionNotifier.getInstance().addListener(this);
		
		stats = new Label[Status.values().length];
		int index = 0;
		for (Status e : Status.values()) {
			if (!e.display())
				continue;
			String labelName = e.displayName();
			Label l = newLabel();
			l.setText(Translate.getInstance().labelName(labelName) + ": ");
			l.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
			
			l.setFont(FontShop.instance.tableFontBold());
			l.setBackground(bgColor);
			
			
			// l.addListener(SWT.MouseHover);
			Label d = newLabel();
			GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
			// gd.widthHint=120;
			d.setLayoutData(gd);
			
			d.setFont(FontShop.instance.tableFont());
			d.setBackground(bgColor);
			d.setData(e);
			
			
			// allow user to click on status and filter
			StatusClick clickHandler = new StatusClick(e);
			l.addMouseListener(clickHandler);
			d.addMouseListener(clickHandler);
			
			stats[index++] = d;
		}
		
		_update();
	}
	
	AtomicInteger cache = new AtomicInteger();    // caches gui drawing.
	
	private void _update() {
		boolean update = cache.getAndIncrement() == 0;
		
		if (!update) return;
		
		SWTAsync.run(new SWTAsync("update") {
			@Override
			public void task() {
				cache.set(0);
				
				for (Label s : stats) {
					if (s == null) continue;
					
					Status e = (Status) s.getData();
					String value = AudibleGUI.instance.getStatus(e);
					s.setText(value);
				}
			}
		});
		
	}
	
	@Override
	public void booksSelected(final List<Book> list) {
		_update();
	}
	
	@Override
	public void bookAdded(Book book) {
		_update();
		
	}
	
	@Override
	public void bookUpdated(Book book) {
		_update();
	}
	
	@Override
	public void booksUpdated() {
		_update();
	}
	
	
	@Override
	public void connectionChanged(boolean connected) {
		_update();
	}
	
	@Override
	public AudibleAccountPrefs getAccountPrefs(AudibleAccountPrefs in) {
		return in;
	}
	
	public enum Status {
		Connected, Books, Hours, AAX_Files, MP3_Files, To_Download, To_Convert, Downloading, Converting;  //Connection,
		
		public String displayName() {
			return name().replace('_', ' ');
		}     // TODO: Translations
		
		public boolean display() {
			switch (this) {
				case To_Convert:
				case Downloading:
				case To_Download:
				case Converting:
				case MP3_Files:
				case Connected:
				case Hours:
					return true;
				
				case AAX_Files:
				case Books:
					return false;
				
				default:
					assert (false);
				
			}
			
			return true;
		}
		
		public boolean canFilterByStatusType() {
			return canFilterByStatusType(this);
		}
		
		public static boolean canFilterByStatusType(Status result) {
			switch (result) {
				case Connected:
				case Hours:
					return false;
				
				case Books:
				case AAX_Files:
				case MP3_Files:
				case To_Download:
				case To_Convert:
				case Downloading:
				case Converting:
					return true;
				default:
					assert (false);
				
			}
			return false;
		}
		
	}
	
	@Override
	public void loginFailed(String url, String html) {
		// TODO Auto-generated method stub
		
	}
	
	
}
