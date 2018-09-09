package org.openaudible;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.util.IO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.openaudible.audible.AudibleScraper;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.books.BookNotifier;
import org.openaudible.convert.AAXParser;
import org.openaudible.convert.BookMerge;
import org.openaudible.convert.ConvertQueue;
import org.openaudible.convert.LookupKey;
import org.openaudible.download.DownloadQueue;
import org.openaudible.progress.IProgressTask;
import org.openaudible.util.CopyWithProgress;
import org.openaudible.util.HTMLUtil;
import org.openaudible.util.TimeToSeconds;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.IQueueListener;
import org.openaudible.util.queues.ThreadedQueue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Audible implements IQueueListener<Book> {
	final static String ignoreSetFileName = "ignore.json";
	private static final Log LOG = LogFactory.getLog(Audible.class);
	public static Audible instance; // Singleton
	final String keysFileName = "keys.json";
	private final HashMap<String, Book> books = new HashMap<>(); // Book.id(), Book
	private final HashSet<String> ignoreSet = new HashSet<>();        // book ID's to ignore.
	public DownloadQueue downloadQueue;
	public ConvertQueue convertQueue;
	public long totalDuration = 0;
	// private String activationBytes = "";
	volatile boolean quit = false;
	boolean convertToMP3 = false;
	String accountPrefsFileName = "account.json";
	String cookiesFileName = "cookies.json";
	String bookFileName = "books.json";
	HashSet<File> mp3Files = null;
	HashSet<File> aaxFiles = null;
	HashSet<Book> toDownload = new HashSet<>();
	HashSet<Book> toConvert = new HashSet<>();
	Object lock = new Object();
	long needFileCacheUpdate = 0;
	int booksUpdated = 0;
	Exception last = null;
	private AudibleAccountPrefs account = new AudibleAccountPrefs();
	private AudibleScraper audibleScraper;
	// AudibleRegion region = AudibleRegion.US;
	private IProgressTask progress;
	private boolean autoConvertToMP3 = false;
	
	public Audible() {
	
	}
	
	public static int getRatingByte(Book b) {
		String ar = b.getRating_average();
		if (ar != null && ar.length() > 0) {
			try {
				double r = Double.parseDouble(ar);
				if (r > 0 && r < 5.0) {
					// Expected range:
					int intR = (int) Math.round(r * 20.0);
					if (intR >= 0 && intR <= 100) {
						return intR;
					}
					
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		return 0;
	}
	
	public void init() throws IOException {
		Directories.assertInitialized();
		// load();
		
		if (instance != null)
			throw new IOException("instance should be null here");
		instance = this;
		downloadQueue = new DownloadQueue(5);
		downloadQueue.addListener(this);
		updateFileCache();
		
	}
	
	public void initConverter() {
		convertQueue = new ConvertQueue();
		convertToMP3 = true;
		convertQueue.addListener(this);
	}
	
	public int getBookCount() {
		return books.size();
	}
	
	public List<Book> getBooks() {
		ArrayList<Book> list = new ArrayList<>();
		synchronized (books) {
			list.addAll(books.values());
		}
		Collections.sort(list, (b1, b2) -> b1.getFullTitle().compareTo(b2.getFullTitle()));
		return list;
	}
	
	boolean hasBook(Book b) {
		synchronized (books) {
			return b != null && books.containsKey(b.id());
		}
	}
	
	// fix book info
	private Book normalizeBook(Book b) {
		String link = b.getInfoLink();
		if (link.startsWith("/")) {
			// convert to full URL.
			b.setInfoLink("https://www.audible.com" + link);
		}
		return b;
	}
	
	boolean takeBook(Book b) {
		if (!ok(b)) {
			LOG.warn("invalid book: " + checkBook(b));
			return false;
		}
		
		if (!hasBook(b)) {
			if (!ignoreBook(b)) {
				synchronized (books) {
					normalizeBook(b);
					
					
					books.put(b.getProduct_id(), b);
				}
				BookNotifier.getInstance().bookAdded(b);
				return true;
			}
		}
		return false;
	}
	
	Book removeBook(Book b) {
		Book out;
		synchronized (books) {
			out = books.remove(b.id());
		}
		assert (out != null);
		return out;
	}
	
	public void addToIgnoreSet(Collection<Book> books) {
		for (Book b : books) {
			removeBook(b);
			ignoreSet.add(b.id());
		}
		saveIgnoreSet();
	}
	
	private void loadIgnoreSet() {
		try {
			Gson gson = new GsonBuilder().create();
			File prefsFile = Directories.META.getDir(ignoreSetFileName);
			
			if (prefsFile.exists()) {
				String content = HTMLUtil.readFile(prefsFile);
				HashSet set = gson.fromJson(content, HashSet.class);
				if (set != null) {
					ignoreSet.clear();
					ignoreSet.addAll(set);
				}
			}
		} catch (Throwable th) {
			LOG.info("Error loadIgnoreSet", th);
		}
		
	}
	
	private void saveIgnoreSet() {
		if (!ignoreSet.isEmpty()) {
			Gson gson = new GsonBuilder().create();
			try {
				HTMLUtil.writeFile(Directories.META.getDir(ignoreSetFileName), gson.toJson(ignoreSet));
			} catch (Throwable th) {
				LOG.error("Error saving ignore list!", th);
			}
		}
	}
	
	
	protected boolean ignoreBook(Book b) {
		String id = b.id();
		if (id.length() == 0)
			return true;
		return ignoreSet.contains(b);
	}
	
	public boolean ok(Book b) {
		String o = checkBook(b);
		return o.isEmpty();
	}
	
	public String checkBook(Book b) {
		return b.checkBook();
	}
	
	public void load() throws IOException {
		try {
			Gson gson = new GsonBuilder().create();
			File prefsFile = Directories.META.getDir(accountPrefsFileName);
			
			if (prefsFile.exists()) {
				String content = HTMLUtil.readFile(prefsFile);
				account = gson.fromJson(content, AudibleAccountPrefs.class);
				if (account.audibleRegion == null)
					account.audibleRegion = AudibleRegion.US;
				
			}
		} catch (Throwable th) {
			LOG.info("Error loading account", th);
		}
		
		if (account == null) {
			account = new AudibleAccountPrefs();
		}
		
		File booksFile = Directories.META.getDir(bookFileName);
		
		BookNotifier.getInstance().setEnabled(false);
		try {
			
			if (booksFile.exists()) {
				String content = HTMLUtil.readFile(booksFile);
				List<Book> bookList = new Gson().fromJson(content, new TypeToken<List<Book>>() {
				}.getType());
				for (Book b : bookList) {
					takeBook(b);
				}
			}
		} finally {
			BookNotifier.getInstance().setEnabled(true);
			BookNotifier.getInstance().booksUpdated();
		}
		
		LookupKey.instance.load(Directories.BASE.getDir(keysFileName));
		loadIgnoreSet();
	}
	
	public synchronized void save() throws IOException {
		Gson gson = new GsonBuilder().create();
		HTMLUtil.writeFile(Directories.META.getDir(accountPrefsFileName), gson.toJson(account));
		HTMLUtil.writeFile(Directories.META.getDir(bookFileName), gson.toJson(getBooks()));
		if (audibleScraper != null) {
			audibleScraper.saveCookies();
		}
	}
	
	public void update() throws Exception {
		updateFileCache();
		
		updateLibrary(false);
		updateInfo();
		
		Collection<Book> results = downloadQueue.addAll(toDownload());
		if (results.size() > 0) {
			LOG.info("Books to download:" + results.size());
		}
		
		Collection<Book> toConvert = toConvert();
		
		if (autoConvertToMP3) {
			results = convertQueue.addAll(toConvert);
			if (results.size() > 0) {
				LOG.info("Added " + results.size() + " book(s) to convert queue.");
			}
		} else {
			LOG.info(toConvert.size() + " book(s) are not converted to MP3. Type convert to start process.");
		}
	}
	
	public HashSet<File> getFileSet(Directories dir) {
		HashSet<File> set = new HashSet<>();
		File d = dir.getDir();
		for (File f : d.listFiles()) {
			String name = f.getName();
			if (name.startsWith("."))
				continue;
			if (dir == Directories.MP3 && !name.toLowerCase().endsWith(".mp3"))
				continue;
			if (dir == Directories.AAX && !name.toLowerCase().endsWith(".aax"))
				continue;
			set.add(f);
		}
		
		return set;
	}
	
	public void findOrphanedFiles(IProgressTask task) {
		HashSet<File> aaxs = getFileSet(Directories.AAX);
		aaxs.addAll(aaxFiles);
		
		List<Book> bookList = getBooks();
		for (Book b : bookList) {
			File aax = getAAXFileDest(b);
			aaxs.remove(aax);
		}
		
		if (aaxs.size() > 0) {
			int count = 0;
			if (task != null)
				task.setTask("Importing " + aaxs.size() + " audible files", "");
			LOG.info("orphaned aax files:" + aaxs.size());
			for (File f : aaxs) {
				LOG.info(" orphan: " + f.getName());
				try {
					Book b = AAXParser.instance.parseAAX(f, Directories.getDir(Directories.ART), AAXParser.CoverImageAction.saveInDirectory);
					takeBook(b);
					task.setSubTask(b.toString());
					File imageDest = Audible.instance.getImageFileDest(b);
					if (!imageDest.exists()) {
					
					}
					count++;
					
					if (task != null) {
						task.setTask(null, "" + count + " " + b.getShortTitle());
						if (task.wasCanceled()) break;
					}
					
				} catch (Throwable e) {
					LOG.info("Error parsing aax file", e);
					
				}
			}
		}
		
		HashSet<File> mp3s = getFileSet(Directories.MP3);
		mp3s.addAll(mp3Files);
		
		for (Book b : bookList) {
			File mp3 = getMP3FileDest(b);
			mp3s.remove(mp3);
		}
		
		if (mp3s.size() > 0) {
			LOG.info("orphaned mp3 files:" + mp3s.size());
			for (File f : mp3s) {
				LOG.info("	" + f.getName());
			}
		}
		
		// Look for books with missing info and re-parse if needed.
		// Information can be lost if
		boolean needSave = false;
		for (Book b : getBooks()) {
			if (!b.has(BookElement.summary) && hasAAX(b)) {
				task.setTask("Updating book information", "Reading " + b);
				needSave = AAXParser.instance.parseBook(b);
			}
		}
		
		if (needSave) {
			try {
				save();
			} catch (IOException e) {
				LOG.error("Saving error..", e);
			}
		}
		
	}
	
	public void updateFileCache() {
		mp3Files = getFileSet(Directories.MP3);
		aaxFiles = getFileSet(Directories.AAX);
		needFileCacheUpdate = System.currentTimeMillis();
		synchronized (lock) {
			toDownload.clear();
			toConvert.clear();
			long seconds = 0;
			for (Book b : getBooks()) {
				if (isIgnoredBook(b)) continue;
				
				if (canDownload(b)) toDownload.add(b);
				if (canConvert(b)) toConvert.add(b);
				seconds += TimeToSeconds.parseTimeStringToSeconds(b.getDuration());
			}
			totalDuration = seconds;
		}
		
	}
	
	
	public int getDownloadCount() {		synchronized (lock) {
		
		return toDownload.size();
	}
	}
	
	public int getConvertCount() {
		synchronized (lock) {
			
			return toConvert.size();
		}
	}
	
	
	public int mp3Count() {synchronized (lock){
		return mp3Files.size();}
		
	}
	
	public int aaxCount() {
		synchronized (lock) {
			return aaxFiles.size();
		}
	}
	
	public void updateLibrary(boolean quick) throws Exception {
		booksUpdated = 0;
		AudibleScraper s = getScraper();
		Collection<Book> list = null;
		if (quick)
			list = s.fetchLibraryQuick(books);
		else
			list = s.fetchLibrary();
		for (Book b : list) {
			takeBook(b);
			HashSet<BookElement> changes = updateBook(b);
			if (changes.size() > 0) {
				if (!changes.contains(BookElement.cust_id) || changes.size() != 1)
					booksUpdated++;
			}
		}

//        if (booksUpdated > 0 && quick)
//            updateLibrary(false);
		
		
		LOG.info("Updated " + list.size() + " books");
	}
	
	private HashSet<BookElement> updateBook(Book updatedBook) {
		Book book;
		synchronized (books) {
			book = books.get(updatedBook.getProduct_id());
		}
		
		if (book != null) {
			if (!book.has(BookElement.purchase_date)) {
				LOG.info("book purchase date:" + updatedBook.getPurchaseDate());
				
			}
			return BookMerge.instance.merge(book, updatedBook);
		}
		return new HashSet<>();
	}
	
	public Book findFirst(String args) {
		ArrayList<Book> bks = find(args);
		return (bks.size() > 0) ? bks.get(0) : null;
	}
	
	public Book findFirst(String args, boolean throwIfNotOneResult) throws Exception {
		ArrayList<Book> bks = find(args);
		if (bks.size() == 0) {
			if (throwIfNotOneResult)
				throw new Exception("No results for " + args);
			return null;
		}
		if (bks.size() > 1 && throwIfNotOneResult) {
			throw new Exception("too many results for " + args);
		}
		
		return bks.get(0);
	}
	
	// return list of books containing string in title, author, narrator,asis
	public ArrayList<Book> find(String string) {
		ArrayList<Book> list = new ArrayList<>();
		
		string = string.toLowerCase();
		for (Book b : getBooks()) {
			BookElement elem[] = {BookElement.asin, BookElement.author, BookElement.fullTitle, BookElement.narratedBy};
			boolean match = false;
			for (BookElement e : elem) {
				if (b.get(e).toLowerCase().contains(string)) {
					match = true;
					break;
				}
			}
			
			if (match)
				list.add(b);
		}
		return list;
	}
	
	public boolean hasMP3(Book b) {
		checkFileCache();
		return mp3Files.contains(getMP3FileDest(b));
	}
	
	public boolean hasAAX(Book b) {
		checkFileCache();
		return aaxFiles.contains(getAAXFileDest(b));
		// return getAudibleFileDest(b).exists();
	}
	
	private void checkFileCache() {
		boolean update = false;
		if (needFileCacheUpdate == 0)
			update = true;
		else {
			long delta = System.currentTimeMillis() - needFileCacheUpdate;
			if (delta > 60000 * 5)
				update = true;
		}
		if (update) {
			updateFileCache();
		}
	}
	
	public String inspect(Book b) {
		String o = b.toString();
		o += " hasMP3=" + hasMP3(b) + " hasAAX=" + hasAAX(b) + " hasImage=" + hasImage(b);
		if (hasAAX(b))
			o += " aax=" + getAAXFileDest(b);
		if (hasMP3(b))
			o += " mp3=" + getMP3FileDest(b);
		
		return o;
	}
	
	public File getMP3FileDest(Book b) {
		return Directories.MP3.getDir(b.getProduct_id() + ".mp3");
	}
	
	public boolean hasImage(Book b) {
		return getImageFileDest(b).exists();
	}
	
	public File getImageFileDest(Book b) {
		if (b == null || b.getProduct_id() == null || b.getProduct_id().length() == 0) {
			assert (false);
			return null;
		}
		return Directories.ART.getDir(b.getProduct_id() + ".jpg");
	}
	
	public void export(Writer w, List<Book> bookList) throws IOException {
		CSVWriter writer = new CSVWriter(w, ',');
		
		BookElement items[] = BookElement.values();
		
		// header values.
		String line[] = new String[BookElement.values().length];
		for (BookElement e : items)
			line[e.ordinal()] = e.displayName();
		writer.writeNext(line);
		
		
		for (Book b : bookList) {
			for (BookElement e : items) {
				String v = b.get(e);
				line[e.ordinal()] = v;
			}
			writer.writeNext(line, true);
		}
		
		writer.close();
	}
	
	public void export(File f) throws IOException {
		try (FileWriter out = new FileWriter(f)) {
			export(out, getBooks());
		}
	}
	
	public void exportJSON(List<Book> list, File f) throws IOException {
		Gson gson = new Gson();
		String json = gson.toJson(list);
		try (FileWriter writer = new FileWriter(f)) {
			writer.write(json);
		}
	}
	
	
	public File getAAXFileDest(Book b) {
		if (b.getProduct_id().length() == 0) {
			new IOException("Bad book").printStackTrace();
			return null;
		}
		return Directories.AAX.getDir(b.getProduct_id() + ".AAX");
	}
	
	public boolean canDownload(Book b) {
		return !hasAAX(b) && !hasMP3(b) && downloadQueue.canAdd(b);
	}
	
	public boolean canConvert(Book b) {
		return hasAAX(b) && !hasMP3(b) && convertQueue.canAdd(b);
	}
	
	public Set<Book> toDownload() {
		synchronized (lock) {
			return new HashSet<>(toDownload);
		}
	}
	
	public Set<Book> toConvert() {
		synchronized (lock) {
			return new HashSet<Book>(toConvert);
		}
	}
	
	public void quit() {
		convertQueue.quit();
		downloadQueue.quit();
		try {
			save();
		} catch (IOException e) {
			LOG.error("save", e);
		}
	}
	
	// refreshes book with latest from audible.com
	public void updateInfo() throws Exception {
		AudibleScraper s = getScraper();
		ArrayList<Book> list = new ArrayList<>();
		
		for (Book b : Audible.instance.getBooks()) {
			if (!s.hasInfo(b) && !b.getInfoLink().isEmpty()) {
				list.add(b);
			}
		}
		
		LOG.info("Getting info for " + list.size() + " books");
		for (Book b : list) {
			LOG.info("Getting info for " + b);
			s.getInfo(b);
		}
		
	}
	
	
	public void updateInfo(Book b) throws Exception {
		getScraper().getInfo(b);
	}
	
	
	public AudibleScraper getScraper() throws Exception {
		return getScraper(true);
	}
	
	public AudibleScraper getScraper(boolean connect) throws Exception {
		boolean ok = false;
		
		if (audibleScraper == null) {
			
			// if (account == null || account.audibleUser.isEmpty())  throw new Exception("audible user name not set");
//            if (account == null || account.audiblePassword.isEmpty())
//                throw new Exception("audible password not set");
			
			
			audibleScraper = new AudibleScraper(account);
			
			
			if (getProgress() != null)
				audibleScraper.setProgress(getProgress());
			if (connect) {
				try {
					audibleScraper.connect();
					ok = true;
					
				} finally {
					if (!ok && audibleScraper != null) {
						audibleScraper.quit();
						audibleScraper = null;
					}
				}
			}
		} else {
			if (connect) {
				if (!audibleScraper.checkLoggedIn()) {
					audibleScraper.home();
				}
			}
			
		}
		
		return audibleScraper;
		
	}
	
	public IProgressTask getProgress() {
		return progress;
	}
	
	public void setProgress(IProgressTask progress) {
		if (this.progress != null && progress != null) {
			LOG.info("Double progress trouble.");
			if (last != null) last.printStackTrace();
		}
		if (this.progress == null && progress == null) {
			LOG.info("progress cleared twice.");
			if (last != null) last.printStackTrace();
		}
		this.progress = progress;
		
		if (audibleScraper != null)
			audibleScraper.setProgress(progress);
		
		last = new Exception(progress != null ? "setting" : "clearing");
		
	}
	
	
	String inspectCookies(Collection<Cookie> col) {
		String out = "";
		out += "Size:" + col.size();
		
		ArrayList<Cookie> sorted = new ArrayList<>();
		sorted.addAll(col);
		
		Collections.sort(sorted, (c1, c2) -> c1.getName().compareTo(c2.getName()));
		
		for (Cookie c : sorted) {
			out += c.getName() + "=" + c.getValue() + " [" + c.getDomain() + "]\n";
		}
		
		return out;
	}
	
	public void setExternalCookies(AudibleScraper s, Collection<Cookie> cookies) {
		CookieManager cm = s.getWebClient().getCookieManager();

//        try {
//            s.home();
//            if (s.checkLoggedIn())
//                return;
//        } catch (Throwable th) {
//            th.printStackTrace();
//        }
		
		LOG.info("Scraper Cookies: " + inspectCookies(cm.getCookies()));
		LOG.info("Browser Cookies: " + inspectCookies(cookies));
		
		int updated = 0;
		int found = 0;
		for (Cookie c : cookies) {
			String name = c.getName();
			String value = c.getValue();
			Cookie existing = cm.getCookie(name);
			if (existing != null) {
				found++;
				String eval = existing.getValue();
				if (eval.equals(value)) {
					LOG.info("Same Value as Existing: " + existing + " value=" + value);
				} else {
					updated++;
					LOG.info("Different Value as Existing: " + existing + " value=" + value);
					Cookie nc = new Cookie(existing.getDomain(), name, value);
					cm.addCookie(nc);
				}
			} else {
				LOG.info("new cookie:" + c);
				cm.addCookie(c);
			}
		}
		
		LOG.info("Found " + found + " cookies. updated:" + updated);
	}
	
	@Override
	public void itemEnqueued(ThreadedQueue<Book> queue, Book o) {
	}
	
	@Override
	public void itemDequeued(ThreadedQueue<Book> queue, Book o) {
	}
	
	@Override
	public void jobStarted(ThreadedQueue<Book> queue, IQueueJob job, Book b) {
		LOG.info(queue.toString() + " started:" + b + " size:" + queue.size());
		BookNotifier.getInstance().bookUpdated(b);
	}
	
	@Override
	public void jobError(ThreadedQueue<Book> queue, IQueueJob job, Book b, Throwable th) {
		BookNotifier.getInstance().bookUpdated(b);
	}
	
	@Override
	public void jobCompleted(ThreadedQueue<Book> queue, IQueueJob job, Book b) {
		LOG.info(queue.toString() + " completed:" + b + " remaining queue size:" + queue.size());
		if (queue == downloadQueue) {
			try {
				AAXParser.instance.update(b);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			convertQueue.add(b);
		}
		
		
		needFileCacheUpdate = 0;
		checkFileCache();
		try {
			save();
		} catch (Throwable e) {
			LOG.error("error saving...", e);
		}
		BookNotifier.getInstance().bookUpdated(b);
	}
	
	@Override
	public void jobProgress(ThreadedQueue<Book> queue, IQueueJob job, Book o, String task, String subtask) {
	
	}
	
	public AudibleAccountPrefs getAccount() {
		return account;
	}
	
	public void logout() {
		
		if (audibleScraper != null)
			audibleScraper.logout();
		AudibleScraper.deleteCookies();
	}
	
	public String getAudibleURL() {
		return getAccount().audibleRegion.getBaseURL();
	}
	
	// used for drag and drop into the app to convert any aax file.
	public Book importAAX(final File aaxFile, final IProgressTask task) throws InterruptedException, IOException, CannotReadException, ReadOnlyFileException, InvalidAudioFrameException, TagException {
		
		task.setTask("Parsing " + aaxFile.getName());
		final Book book = AAXParser.instance.parseAAX(aaxFile, null, AAXParser.CoverImageAction.useBiggerImage);
		if (!hasBook(book)) {
			task.setTask("Importing " + book);
			// Copy it to the default directory.
			File dest = getAAXFileDest(book);
			if (dest.exists())
				throw new IOException("Book already exists:" + dest.getAbsolutePath());
			if (task != null)
				CopyWithProgress.copyWithProgress(task, aaxFile, dest);
			else
				IO.copy(aaxFile, dest);
			updateFileCache();
			boolean test = hasAAX(book);
			assert (test);
			
			boolean ok = takeBook(book);
			if (ok)
				convertQueue.add(book);
		}
		
		return book;
	}
	
	public boolean isIgnoredBook(Book b) {
		return ignoreSet.contains(b.id());
	}
	
	public boolean inDownloadSet(Book b) {
		synchronized (lock) {
			return toDownload.contains(b);
		}
	}
	
	public boolean inConvertSet(Book b) {
		synchronized (lock) {
			return toConvert.contains(b);
		}
	}
}

