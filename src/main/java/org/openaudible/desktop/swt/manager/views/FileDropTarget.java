package org.openaudible.desktop.swt.manager.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Control;
import org.openaudible.desktop.swt.manager.AudibleGUI;

import java.io.File;
import java.util.ArrayList;


public class FileDropTarget extends DropTargetAdapter {
	public final FileTransfer fileTransfer = FileTransfer.getInstance();
	public Transfer[] types = new Transfer[]{fileTransfer};
	static FileDropTarget instance = new FileDropTarget();
	private static final Log LOG = LogFactory.getLog(FileDropTarget.class);
	
	public static void attach(Control control) {
		DropTarget target = new DropTarget(control, DND.DROP_COPY);
		target.setTransfer(new Transfer[]{instance.fileTransfer});
		target.addDropListener(instance);
	}
	
	
	@Override
	public void drop(DropTargetEvent event) {
		System.out.println("drop:" + event + " data=" + event.data);
		ArrayList<File> aaxFiles = new ArrayList<File>();
		
		if (fileTransfer.isSupportedType(event.currentDataType)) {
			
			System.out.println("dropAccept files!");
			
			//list out selected file
			String[] files = (String[]) event.data;
			for (String name : files) {
				
				String[] split = name.split("\\.");
				String ext = split[split.length - 1];
				File file = new File(name);
				if (file.getName().toLowerCase().endsWith(".aax")) {
					aaxFiles.add(file);
					
				}
				
				System.out.println("file:" + name + " exists=" + file.exists());
				
			}//end for loop        event.detail = DND.DROP_COPY;
		}
		
		if (aaxFiles.size() > 0) {
			AudibleGUI.instance.importBooks(aaxFiles);
		}
	}
	
	
	@Override
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}
}

