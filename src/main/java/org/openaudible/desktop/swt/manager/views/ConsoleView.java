package org.openaudible.desktop.swt.manager.views;


import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.util.Console;

import java.util.ArrayList;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


public class ConsoleView implements Console.ILogRecordPublisher {
	private Display display;
	private StyledText consoleText;
	private static final int PREFERRED_LINES = 256;
	private static final int MAX_LINES = 4000 + PREFERRED_LINES;
	
	private Clipboard clipboard;
	SimpleFormatter formatter = new SimpleFormatter();
	
	
	private void init() throws IllegalStateException {
		
		
		if (Console.instance != null) {
			for (LogRecord l : Console.instance.getHistory())
				doLog(l, true);
		}
		
		
		SWTAsync.run(new SWTAsync("console setTopIndex") {
			@Override
			public void task() {
				if (consoleText != null && !consoleText.isDisposed()) {
					consoleText.setTopIndex(consoleText.getLineCount() - 1);
				}
			}
		});
		
		// reset state
		consoleText.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				if (clipboard != null) {
					clipboard.dispose();
				}
			}
		});
		
	}
	
	
	@Override
	public void publish(LogRecord l) {
		doLog(l, false);
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.gudy.azureus2.ui.swt.IView#initialize(org.eclipse.swt.widgets.Composite
	 * )
	 */
	public StyledText initialize(Composite composite) {
		display = composite.getDisplay();
		consoleText = new StyledText(composite, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		
		init();
		
		Menu copyMenu = new Menu(consoleText);
		MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
		copyItem.addSelectionListener(new SelectionListener() {
			/*
			 * @see SelectionListener.widgetSelected (SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyToClipboard();
			}
			
			/*
			 * @see SelectionListener.widgetDefaultSelected(SelectionEvent)
			 */
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				copyToClipboard();
			}
		});
		copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
		
		
		MenuItem clearItem = new MenuItem(copyMenu, SWT.NONE);
		clearItem.addSelectionListener(new SelectionListener() {
			/*
			 * @see SelectionListener.widgetSelected (SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				clear();
			}
			
			/*
			 * @see SelectionListener.widgetDefaultSelected(SelectionEvent)
			 */
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				clear();
			}
		});
		clearItem.setText(JFaceResources.getString("Clear")); //$NON-NLS-1$
		
		
		consoleText.setMenu(copyMenu);
		consoleText.getShell().setMenu(copyMenu);
		
		consoleText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'a' || e.keyCode == 'A'))
					consoleText.setSelection(new Point(0, consoleText.getText().length()));
				if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'c' || e.keyCode == 'C'))
					copyToClipboard();
				if ((e.stateMask == SWT.CTRL || e.stateMask == SWT.COMMAND) && (e.keyCode == 'w' || e.keyCode == 'W'))
					consoleText.getShell().close();
			}
		});
		
		return consoleText;
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.gudy.azureus2.ui.swt.IView#getComposite()
	 */
	public StyledText getStyledText() {
		return consoleText;
	}
	
	
	ArrayList<LogRecord> queue = new ArrayList<LogRecord>();
	
	
	private void doLog(final LogRecord LogRecord, final boolean supressScrolling) {
		if (display == null || display.isDisposed())
			return;
		
		boolean needUpdate = false;
		synchronized (queue) {
			queue.add(LogRecord);
			if (queue.size() == 1)
				needUpdate = true;
		}
		if (!needUpdate)
			return;
		
		SWTAsync.run(new SWTAsync("doLog") {
			@Override
			public void task() {
				try {
					ArrayList<LogRecord> records;
					synchronized (queue) {
						if (queue.size() == 0) {
							return;
						}
						records = new ArrayList<LogRecord>();
						records.addAll(queue);
						queue.clear();
					}
					if (consoleText == null || consoleText.isDisposed())
						return;
					
					ScrollBar sb = consoleText.getVerticalBar();
					boolean autoScroll = !supressScrolling && (sb.getSelection() == (sb.getMaximum() - sb.getThumb()));
					StringBuffer buf = new StringBuffer();
					int nbLines = consoleText.getLineCount();
					if (nbLines + records.size() > MAX_LINES)
						consoleText.replaceTextRange(0, consoleText.getOffsetAtLine(PREFERRED_LINES), ""); //$NON-NLS-1$
					
					for (LogRecord l : records) {
						buf.append(formatter.format(l));
						if (buf.length() > 10240) {
							consoleText.append(String.valueOf(buf));
							buf.setLength(0);
						}
					}
					
					consoleText.append(String.valueOf(buf));
					
					if (autoScroll)
						consoleText.setSelection(consoleText.getText().length());
					
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
			}
		});
	}
	
	
	public void clear() {
		SWTAsync.assertGUI();
		consoleText.setText("");
		consoleText.setSelection(0);
	}
	
	public void scrollToEnd() {
		SWTAsync.assertGUI();
		
		ScrollBar sb = consoleText.getVerticalBar();
		if (sb != null) {
			sb.setSelection(sb.getMaximum());
		}
	}
	
	
	/**
	 * Copy the contents of the statuses to the clipboard.
	 */
	public void copyToClipboard() {
		if (clipboard == null) {
			clipboard = new Clipboard(consoleText.getDisplay());// clipboard.dispose();
		}
		String text = consoleText.getSelectionText();
		if (text.length() == 0)
			text = consoleText.getText();
		text = text.replace((char) 0, ' '); // for some reason we have null
		// (char)0 chars that terminate the
		// clipboard...
		if (text.length() > 0) {
			clipboard.setContents(new Object[]{text}, new Transfer[]{TextTransfer.getInstance()});
		}
	}
	
}
