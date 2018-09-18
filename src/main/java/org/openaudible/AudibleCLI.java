package org.openaudible;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.audible.AudibleClient;
import org.openaudible.audible.AudibleScraper;
import org.openaudible.books.Book;
import org.openaudible.convert.AAXParser;
import org.openaudible.progress.NullProgressTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class AudibleCLI {
	private static final Log LOG = LogFactory.getLog(AudibleCLI.class);
	
	final Audible audible = new Audible();
	volatile boolean quit = false;
	
	public AudibleCLI() {
	}
	
	private static void println(Object o) {
		System.out.println(o);
	}
	
	public static void main(String s[]) {
		try {
			File etc = new File("books-etc");
			File base = new File("books");
			AudibleCLI instance = new AudibleCLI();
			Directories.init(etc, base);
			
			instance.init();
			
			instance.run();
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}
	
	public void init() throws IOException {
		audible.init();
		audible.initConverter();
		java.util.logging.Logger.getLogger("audiblescrape").setLevel(Level.INFO);
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	}
	
	private void doCommandLine() throws Exception {
		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader keyboard = new BufferedReader(in);
		println("command Line:");
		
		while (!quit) {
			String s = keyboard.readLine();
			if (s == null || s.length() == 0)
				continue;
			println(s);
			try {
				String r[] = s.split(" ");
				AudibleCmd f = AudibleCmd.valueOf(r[0]);
				doCommand(f, r);
			} catch (IllegalArgumentException iae) {
				println("unknown command");
			} catch (Throwable th) {
				th.printStackTrace();
				println(th);
				
			}
		}
	}
	
	private void run() {
		try {
			try {
				audible.load();
			} catch (Throwable th) {
				LOG.error("Unable to load prefs", th);
			}
			LOG.info("Audible Download Manager");
			
			doCommandLine();
			audible.save();
		} catch (Throwable th) {
			// TODO Auto-generated catch block
			th.printStackTrace();
		} finally {
			audible.quit();
		}
	}
	
	AudibleScraper createScraper() throws Exception {
		return new AudibleScraper(audible.getAccount(), new AudibleClient(), new NullProgressTask());
		
	}
	
	
	private void doCommand(AudibleCmd f, String[] r) throws Exception {
		String args = "";
		for (int x = 1; x < r.length; x++) {
			if (args.length() > 0)
				args += " ";
			args += r[x];
		}
		AudibleScraper s;
		
		println("Running " + f + " " + args);
		switch (f) {
			case help:
				for (AudibleCmd v : AudibleCmd.values())
					println(v);
				println("Try... update... post... list... ");
				break;
			case update:
				audible.update();
				break;
			case save:
				audible.save();
				break;
			case convert:
				if (args.length() > 0) {
					audible.convertQueue.add(audible.findFirst(args, true));
				} else {
					audible.convertQueue.addAll(audible.toConvert());
				}
				break;
			case queues:
				println("Convert Queue: " + audible.convertQueue.size());
				println("Download Queue: " + audible.downloadQueue.size());
				break;
			
			case find:
				for (Book b : audible.find(args)) {
					println(b);
					println(b.inspect("\n  "));
				}
				
				break;
			
			case updateInfo:
				s = createScraper();
				
				for (Book b : audible.find(args))
					audible.updateInfo(b, s);
				break;
			case download:
				audible.downloadQueue.addAll(audible.find(args));
				break;
			
			case load:
				audible.load();
				break;
			case toDownload: {
				Collection<Book> list = audible.toDownload();
				for (Book b : list)
					println(b);
				println("Need to download:" + list.size());
				break;
			}
			
			case toConvert: {
				Collection<Book> list = audible.toConvert();
				for (Book b : list)
					println(b);
				println("Need to convert:" + list.size());
				break;
			}
			case info:
				createScraper().getInfo(audible.findFirst(args, true));
				break;
			
			case home:
				createScraper().home();
				break;
			
			case export:
				audible.export(new File("books.csv"));
				break;
			case library:
				audible.updateLibrary(createScraper(), false);
				break;
			
			case test2:
				test(audible.find(r[1]));
				
				break;
			
			case quit:
				quit = true;
				break;
			case list: {
				int c = 0;
				for (Book b : audible.getBooks()) {
					c++;
					println(c + ". " + b);
				}
			}
			break;
			
			case names:
				for (Book b : audible.getBooks()) {
					println(BookToFilenameStrategy.instance.getReadableFileName(b));
				}
				int l1 = 0, l2 = 0;
				for (Book b : audible.getBooks()) {
					String n1 = b.getFullTitle();
					String n2 = b.getShortTitle();
					if (n1.length() > l1)
						l1 = n1.length();
					if (n2.length() > l2) {
						l2 = n2.length();
						println("SHORT: " + n2);
					}
					if (!n1.equals(n2)) {
						println("1. " + n1 + "\n2. " + n2);
					}
					
				}
				println("Longest names: short=" + l2 + " full=" + l1);
				
				break;
			
			case parseAAX:
				AAXParser.instance.update(audible.findFirst(args, true));
				break;
			
			default:
				println("FAILED " + f);
				break;
			
		}
		println("Done " + f);
	}
	
	private void test(List<Book> bookList) {
		
		for (Book b : bookList) {
			
			if (audible.hasMP3(b)) {
				File mp3File = audible.getMP3FileDest(b);
				File aaxFile = audible.getAAXFileDest(b);
				long diff = mp3File.length() - aaxFile.length();
				
				println(mp3File.getName() + " " + mp3File.length() + ", " + aaxFile.length() + ", diff=" + diff);
				
			}
		}
		
	}
	
	
	enum AudibleCmdFinal {
		help, connect, list, info, act, update, download, convert, quit // find, has, asText, asXML, home, quit, library, names, signout, title, list, forms, web, access, toDownload, setURL, gettest2, test1, test2, act, retag, parseAAX, toConvert, download, updateInfo, cookies, queues
	}
	
	enum AudibleCmd {
		help, update, info, convert, export, save, load, find, has, asText, asXML, home, quit, library, names, signout, title, list, forms, web, access, toDownload, setURL, gettest2, test1, test2, act, retag, parseAAX, toConvert, download, updateInfo, cookies, queues
	}
	
}