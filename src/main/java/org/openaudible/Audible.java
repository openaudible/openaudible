package org.openaudible;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.audible.AudibleScraper;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.books.BookNotifier;
import org.openaudible.convert.AAXParser;
import org.openaudible.convert.BookMerge;
import org.openaudible.convert.ConvertQueue;
import org.openaudible.download.DownloadQueue;
import org.openaudible.progress.IProgressTask;
import org.openaudible.util.EventTimer;
import org.openaudible.util.HTMLUtil;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.IQueueListener;
import org.openaudible.util.queues.ThreadedQueue;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Audible implements IQueueListener<Book> {
    public final static int version = 20170805;
    private static final Log LOG = LogFactory.getLog(Audible.class);
    public static Audible instance; // Singleton
    public DownloadQueue downloadQueue;
    public ConvertQueue convertQueue;
    // private String activationBytes = "";
    volatile boolean quit = false;
    boolean convertToMP3 = false;
    String prefsFileName = "account.json";
    String cookiesFileName = "cookies.json";
    String bookFileName = "books.json";
    HashSet<File> mp3Files = null;
    HashSet<File> aaxFiles = null;
    long needFileCacheUpdate = 0;
    int booksUpdated = 0;
    Exception last = null;
    private AudibleAccountPrefs account = new AudibleAccountPrefs();
    private AudibleScraper audibleScraper;
    private IProgressTask progress;
    private boolean autoConvertToMP3 = false;
    private final HashMap<String, Book> books = new HashMap<>(); // Book.id(), Book


    public Audible() {
        LOG.info("openaudible " + version);
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

    public void initConverter() throws IOException {
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

    boolean takeBook(Book b) {
        if (!ok(b)) {
            LOG.warn("invalid book: " + checkBook(b));
            return false;
        }

        if (!hasBook(b)) {
            if (!ignoreBook(b)) {
                synchronized (books) {
                    books.put(b.getProduct_id(), b);
                }
                BookNotifier.getInstance().bookAdded(b);
                return true;
            }
        }
        return false;
    }

    Book removeBook(Book b) {
        Book out = null;
        synchronized (books) {
            out = books.remove(b.getProduct_id());
        }
        assert (out != null);
        return out;
    }

    protected boolean ignoreBook(Book b) {
        String id = b.getProduct_id();
        if (id.length() == 0)
            return true;
        return id.contains("FR_PRMO_");
    }

    public boolean ok(Book b) {
        String o = checkBook(b);
        return o.isEmpty();
    }

    public String checkBook(Book b) {
        // BookElement required[] = { BookElement.product_id, BookElement.user_id, BookElement.cust_id };
        BookElement required[] = {BookElement.product_id, BookElement.fullTitle};
        for (BookElement e : required) {
            if (!b.has(e))
                return "required:" + e + " missing from " + b;
        }
        return "";

    }

    public void load() throws IOException {
        try {
            Gson gson = new GsonBuilder().create();
            File prefsFile = Directories.META.getDir(prefsFileName);

            if (prefsFile.exists()) {
                String content = HTMLUtil.readFile(prefsFile);
                account = gson.fromJson(content, AudibleAccountPrefs.class);
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
        updateFileCache();
    }

    public synchronized void save() throws IOException {
        Gson gson = new GsonBuilder().create();
        HTMLUtil.writeFile(Directories.META.getDir(prefsFileName), gson.toJson(account));
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

        ArrayList<Book> toConvert = toConvert();

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
        Collections.addAll(set, dir.getDir().listFiles());
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

    }

    public void updateFileCache() {
        EventTimer evt = new EventTimer();
        mp3Files = getFileSet(Directories.MP3);
        aaxFiles = getFileSet(Directories.AAX);
        needFileCacheUpdate = System.currentTimeMillis();

        // LOG.info(evt.reportString("updateFileCache mp3:" + mp3Files.size() + " aax:" + aaxFiles.size()));
    }

    public int mp3Count() {
        return mp3Files.size();
    }

    public int aaxCount() {
        return aaxFiles.size();
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
        if (booksUpdated > 0 && quick)
            updateLibrary(false);


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

    // quick hack to export as text file.
    // lots of room for improvement.
    // Available as json file when exported as web page.
    public void export(File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        String sep = System.getProperty("line.separator");
        String tab = "\t";
        BookElement items[] = BookElement.values();
        String line = "";
        for (BookElement e : items)
            line += e.displayName() + tab;
        line += sep;

        fos.write(line.getBytes());
        for (Book b : getBooks()) {
            line = "";
            for (BookElement e : items) {
                String value = b.get(e);
                value = value.replace(tab, " ");
                value = value.replace(sep, " ");
                line += value + tab;
            }
            line += sep;
            fos.write(line.getBytes());
        }
        fos.close();
    }

    void setTask(String task, String subTask) {
    }

    public File getAAXFileDest(Book b) {
        if (b.getProduct_id().length() == 0) {
            new IOException("Bad book").printStackTrace();
            return null;
        }
        return Directories.AAX.getDir(b.getProduct_id() + ".AAX");
    }

    public ArrayList<Book> toDownload() {
        ArrayList<Book> list = new ArrayList<>();
        for (Book b : getBooks()) {
            if (!hasAAX(b) && !hasMP3(b))
                list.add(b);
        }
        return list;
    }

    public ArrayList<Book> toConvert() {
        ArrayList<Book> list = new ArrayList<>();
        for (Book b : getBooks()) {
            if (hasAAX(b) && !hasMP3(b))
                list.add(b);

        }
        return list;
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

    public String getActivationBytes() {
        if (account.audibleKey == null)
            account.audibleKey = "";
        return account.audibleKey;
    }

    public void setActivationBytes(String activationBytes) {
        account.audibleKey = activationBytes;
    }

    public void updateInfo(Book b) throws Exception {
        getScraper().getInfo(b);
    }

    public void fetchDecrpytionKey() throws Exception {
        account.audibleKey = getScraper().fetchDecrpytionKey();
        save();
    }

    public AudibleScraper getScraper() throws Exception {
        return getScraper(true);
    }

    public AudibleScraper getScraper(boolean connect) throws Exception {
        boolean ok = false;

        if (audibleScraper == null) {
            if (account == null || account.audibleUser.isEmpty())
                throw new Exception("audible user name not set");
//            if (account == null || account.audiblePassword.isEmpty())
//                throw new Exception("audible password not set");


            audibleScraper = new AudibleScraper(account.audibleUser, account.audiblePassword);


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

    public void redeemGiftCode(String s) throws IOException, SAXException {
        audibleScraper.redeemGiftCode(s);
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

    public void setExternalCookies(Collection<Cookie> cookies) throws Exception {
        AudibleScraper s = getScraper(false);
        CookieManager cm = s.getWebClient().getCookieManager();
        // cm.clearCookies();

        try {
            // s.setURL("https://www.audible.com/"); // http is fine... better?
            s.home();
            if (s.checkLoggedIn())
                return;
        } catch (Throwable th) {
            th.printStackTrace();
        }

        LOG.info("CookieManager: " + inspectCookies(cm.getCookies()));
        LOG.info("Browser Cooks: " + inspectCookies(cookies));

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
        s.home();
    }

    @Override
    public void itemEnqueued(ThreadedQueue<Book> queue, Book o) {
    }

    @Override
    public void itemDequeued(ThreadedQueue<Book> queue, Book o) {
    }

    @Override
    public void jobStarted(ThreadedQueue<Book> queue, IQueueJob job, Book o) {
    }

    @Override
    public void jobError(ThreadedQueue<Book> queue, IQueueJob job, Book o, Throwable th) {
    }

    @Override
    public void jobCompleted(ThreadedQueue<Book> queue, IQueueJob job, Book o) {
        needFileCacheUpdate = 0;
        try {
            save();
        } catch (Throwable e) {
            LOG.error("error saving...", e);
        }
    }


    public AudibleAccountPrefs getAccount() {
        return account;
    }

    public boolean hasLogin() {
        if (account == null)
            return false;

        return !account.audiblePassword.isEmpty() && !account.audibleUser.isEmpty();
    }

}