package org.openaudible.desktop.swt.manager;

import com.gargoylesoftware.htmlunit.util.Cookie;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.openaudible.Audible;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.AudibleAutomated;
import org.openaudible.Directories;
import org.openaudible.audible.AudibleLoginError;
import org.openaudible.audible.AudibleScraper;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.convert.AAXParser;
import org.openaudible.convert.FFMPEG;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.gui.progress.ProgressDialog;
import org.openaudible.desktop.swt.gui.progress.ProgressTask;
import org.openaudible.desktop.swt.manager.views.AudibleBrowser;
import org.openaudible.desktop.swt.manager.views.StatusPanel;
import org.openaudible.util.Platform;
import org.openaudible.feeds.pagebuilder.WebPage;
import org.openaudible.util.queues.IQueueJob;
import org.openaudible.util.queues.IQueueListener;
import org.openaudible.util.queues.ThreadedQueue;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AudibleGUI implements BookListener {
    private static final Log LOG = LogFactory.getLog(AudibleGUI.class);
    public static AudibleGUI instance;
    AudibleAutomated audible;
    // HashMap<Book, File> mp3Files = new HashMap<Book, File>(), aaxFiles = new HashMap<Book, File>();
    boolean hasFFMPEG = false;
    BookNotifier bookNotifier = BookNotifier.getInstance();
    boolean loggedIn = false;
/*

    void updateFileCache() {
        aaxFiles.clear();
        mp3Files.clear();
        for (Book b : audible.getBooks()) {
            File a = audible.getAAXFileDest(b);
            File m = audible.getMP3FileDest(b);
            if (a.exists())
                aaxFiles.put(b, a);
            if (m.exists())
                mp3Files.put(b, a);
        }

    }
*/
    int downloadCount, convertCount;
    String textFilter = "";
    AudibleBrowser browser = null;

    public AudibleGUI() {
        assert (instance == null);
        instance = this;
        bookNotifier.addListener(this);
        LOG.info("audible desktop " + Version.INT_VERSION + " core " + Audible.version);
    }

    public boolean checkFFMPEG() {

        try {
            String vers = FFMPEG.getVersion();
            LOG.info("using "+vers);
            hasFFMPEG = true;
        } catch (Exception th) {
            LOG.error("error finding ffmpeg", th);
            MessageBoxFactory.showError(null, "Warning, ffmpeg not found:" + th);
            hasFFMPEG = false;
        }

        return hasFFMPEG;
    }

    public void init() throws IOException {
        assert (Audible.instance == null); // for now;
        Directories.assertInitialized();

        if (Audible.instance == null) {
            audible = new AudibleAutomated();
        }

        try {
            audible.init();
            audible.initConverter();

            // Listen for events about jobs:
            BookQueueListener queueListener = new BookQueueListener();

            // downloading aax files
            audible.downloadQueue.addListener(queueListener);
            // converting aax to mp3.
            audible.convertQueue.addListener(queueListener);

            checkFFMPEG();

            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        } catch (Throwable th) {
            th.printStackTrace();
            showError(th, "starting application");
            System.exit(1);
        }
    }

    public void fetchDecryptionKey() {
        try {
            if (!audible.getAccount().audibleKey.isEmpty())
                throw new Exception("Audible key already set.");

            String key = audible.getScraper(true).fetchDecrpytionKey();

            audible.getAccount().audibleKey = key;
            audible.save();

        } catch (Throwable th) {
            LOG.info("Error getting key.", th);
            MessageBoxFactory.showError(null, "Unable to get Key\nError:" + th);
        }
    }

    public int selectedAAXCount() {
        int count = 0;
        for (Book b : getSelected()) {
            if (audible.hasAAX(b))
                count++;
        }
        return count;
    }

    public boolean canGetDecryptionKey() {
        AudibleAccountPrefs a = audible.getAccount();
        return audible.hasLogin() && a.audibleKey.isEmpty();
    }

    public boolean canDownloadAll() {
        return audible.aaxCount() < audible.getBookCount() && audible.hasLogin();
    }

    public boolean canConvertAll() {
        return audible.mp3Count() < audible.getBookCount() && audible.hasLogin();
    }

    public void convertAll() {
        ArrayList<Book> l = new ArrayList();
        for (Book b : audible.getBooks()) {
            if (!audible.hasMP3(b))
                l.add(b);
        }
        convertMP3(l);
    }

    public void downloadAll() {
        ArrayList<Book> l = new ArrayList();
        for (Book b : audible.getBooks()) {
            if (!audible.hasAAX(b))
                l.add(b);
        }
        downloadAAX(l);
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public int getConvertCount() {
        return convertCount;
    }

    public void connect() {
        if (!hasLogin()) {
            MessageBoxFactory.showGeneral(null, 0, "TITLE_SET_CREDENTIALS", "MESSAGE_SET_CREDENTIALS");
        } else {

            ProgressTask task = new ProgressTask("Connecting...") {
                public void run() {
                    AudibleScraper scraper = null;

                    try {
                        scraper = connect(this);
                        setTask("Checking library...", "");
                        scraper.lib();
                        setTask("Completed", "");
                    } catch (Exception e) {
                        LOG.info("Error connecting", e);
                        if (!wasCanceled())
                            showError(e, "refreshing book information");
                    } finally {
                        audible.setProgress(null);
                        if (scraper != null) {
                            scraper.setProgress(null);
                        }

                    }

                }
            };

            ProgressDialog.doProgressTask(task);

        }

    }

    public void downloadAAX(Collection<Book> list) {
        audible.downloadQueue.addAll(list);
    }

    public void convertMP3(Collection<Book> list) {
        audible.convertQueue.addAll(list);
        bookNotifier.booksUpdated();
    }

    public boolean hasAAX(Book b) {
        return audible.hasAAX(b);
    }

    public boolean hasMP3(Book b) {
        return audible.hasMP3(b);
    }

    public void refreshLibrary(final boolean quickRescan) {

        final ProgressTask task = new ProgressTask("Refresh Library") {
            public void run() {
                try {

                    loggedIn = false;
                    connect(this);

                    audible.updateLibrary(quickRescan);
                    setTask("Completed", "");
                    audible.save();
                    bookNotifier.booksUpdated();
                    loggedIn = true;
                } catch (AudibleLoginError e) {
                    browse();
                    MessageBoxFactory.showGeneral(null, 0, "Log in via web browser...", "Unable to connect right now.\n\nTry logging on to Audible from this web page and try again.");

                } catch (Exception e) {
                    LOG.info("Error connecting", e);
                    if (!wasCanceled())
                        showError(e, "refreshing library");
                } finally {
                    audible.setProgress(null);
                }

            }
        };

        ProgressDialog.doProgressTask(task);

        if (loggedIn && !task.wasCanceled())
            downloadAndConvertWithDialog();

    }

    private AudibleScraper connect(ProgressTask progressTask) throws Exception {
        audible.setProgress(progressTask);
        progressTask.setTask("Connecting to audible.com...", "");
        AudibleScraper s = audible.getScraper(false);
        if (s != null && !s.isLoggedIn()) {
            LOG.info("Setting cookies 1");

            SWTAsync.block(new SWTAsync() {
                @Override
                public void task() {
                    LOG.info("Done Setting cookies 2");
                    updateCookies(false);
                    LOG.info("Done Setting cookies 3");

                }
            });
            LOG.info("Done Setting cookies 4");

        }
        s.home();
        return s;
    }

    private void downloadAndConvertWithDialog() {
        ArrayList<Book> dl = audible.toDownload();
        ArrayList<Book> conv = audible.toConvert();

        if (dl.size() == 0 && conv.size() == 0) {
            String upToDate = "Your library is up to date! Go buy more Audible books!";
            MessageBoxFactory.showGeneral(null, SWT.ICON_INFORMATION, "Up to date", upToDate);
        } else {
            String msg = "";
            if (dl.size() != 0)
                msg = "You have " + dl.size() + " book(s) to download.\n";
            if (conv.size() != 0)
                msg = "You have " + conv.size() + " book(s) to convert to MP3\n";
            msg += "Would you like to start these job(s) now?";

            boolean ok = MessageBoxFactory.showGeneralYesNo(null, "Start jobs?", msg);
            if (ok) {
                audible.convertQueue.addAll(conv);
                audible.downloadQueue.addAll(dl);
            }

        }
    }

    public List<Book> getSelected() {
        return BookNotifier.getInstance().getSelected();
    }

    public void downloadSelected() {
        downloadAAX(getSelected());
    }

    public void convertSelected() {
        convertMP3(getSelected());
    }

    Book onlyOneSelected() {
        if (getSelected().size() == 1)
            return getSelected().get(0);
        return null;
    }

    public boolean canConvert() {
        if (!hasFFMPEG) return false;

        for (Book b : getSelected()) {
            if (audible.hasAAX(b) && !audible.hasMP3(b) && audible.convertQueue.canAdd(b))
                return true;
        }
        return false;
    }

    public boolean canPlay() {
        Book b = onlyOneSelected();
        return b != null && audible.hasMP3(b);
    }

    public boolean canDownload() {
        for (Book b : getSelected()) {
            if (!audible.hasAAX(b) && audible.downloadQueue.canAdd(b))
                return true;
        }
        return false;
    }

    public boolean hasLogin() {
        return true;
        // return audible.hasLogin();
    }

    public boolean canViewInAudible() {
        Book b = onlyOneSelected();
        if (b != null) {
            if (!b.getInfoLink().isEmpty())
                return true;
            // might have to search...
            // Can search for Product_ID and get one result..

            return false;
        }
        return false;
    }

    public boolean canViewInSystem() {
        Book b = onlyOneSelected();
        if (b != null) {
            if (audible.hasMP3(b))
                return true;
        }
        return false;
    }

    public int bookCount() {
        return audible.getBookCount();
    }

    public String getStatus(StatusPanel.Status e) {
        String out = "";

        switch (e) {
            case AAX_Files:
                return "" + audible.aaxCount();
            case Books:
                return "" + audible.getBookCount();
            case MP3_Files:
                return "" + audible.mp3Count();
            // case Connection: return ConnectionNotifier.getInstance().getStateString();
            case To_Download:
                return "" + getDownloadCount();
            case To_Convert:
                return "" + getConvertCount();
            case Downloading:
                int dl = audible.downloadQueue.jobsInProgress();
                int dq = audible.downloadQueue.size();
                if (dl == 0 && dq == 0)
                    return "";
                out += dl;

                if (dq > 0)
                    out += " of " + (dq + dl);

                return out;

            case Converting:
                int cl = audible.convertQueue.jobsInProgress();
                int cq = audible.convertQueue.size();
                if (cl == 0 && cq == 0)
                    return "";
                out += "" + cl;

                if (cq > 0)
                    out += " of " + (cq + cl);
                return out;

            default:
                break;

        }
        return "";
    }

    public void play() {
        try {
            Book b = onlyOneSelected();
            File m = audible.getMP3FileDest(b);
            if (m.exists()) {
                Desktop.getDesktop().open(m);
            }
        } catch (Throwable th) {
            showError(th, "launching player");
        }
    }

    public void explore() {
        try {
            Book b = onlyOneSelected();
            File m = audible.getMP3FileDest(b);
            if (m.exists()) {
                String mac = "open -R ";
                String win = "Explorer /select, ";

                String cmd = null;
                if (Platform.isMac())
                    cmd = mac;
                if (Platform.isWindows())
                    cmd = win;
                // TODO: Support linux.
                assert (cmd != null);
                if (cmd != null) {
                    cmd += m.getAbsolutePath() + "\"";
                    System.err.println(cmd);
                    Runtime.getRuntime().exec(cmd);
                }

                // Desktop.getDesktop().open(m.getParentFile());
            }
        } catch (Throwable th) {
            showError(th, "showing file in system");
        }
    }

    public void viewInAudible() {
        String link = onlyOneSelected().getInfoLink();
        if (link.startsWith("/"))
            link = "https://www.audible.com" + link;
        if (link.startsWith("http")) {
            browse(link);

			/*

			try
			{
				Desktop.getDesktop().browse(new URI(link));
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/

        }
    }

    public void exportCSV() {
        try {
            audible.export();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void exportWebPage() {
        try {
            File destDir = new File("M:\\Books\\Audible\\Web");
            ArrayList<Book> list = new ArrayList<Book>();
            list.addAll(audible.getBooks());
            Collections.sort(list);
            // sort by purchase date.
            list.sort((b1, b2) -> -1 * b1.getPurchaseDate().compareTo(b2.getPurchaseDate()));

            PageBuilderTask task = new PageBuilderTask(destDir, list);
            ProgressDialog.doProgressTask(task);
            File index = new File(destDir, "index.html");
            if (index.exists()) {
                try {
                    URI i = index.toURI();
                    String u = i.toString();
                    AudibleGUI.instance.browse(u);

                } catch (Exception e) {
                    showError(e, "displaying web page");
                }
            }

        } catch (Exception e) {
            showError(e, "exporting to web page");
        }

    }

    public void debugSelection() {

    }

    public void refreshBookInfo() {

        ProgressTask task = new ProgressTask("Refresh Book Info") {
            public void run() {
                AudibleScraper scraper = null;

                try {
                    audible.setProgress(this);

                    setTask("Connecting to audible.com...", "");
                    scraper = audible.getScraper();
                    scraper.setProgress(this);
                    int count = 0;
                    List<Book> selected = getSelected();
                    for (Book b : selected) {
                        count++;
                        setTask("" + count + " of " + selected.size() + " " + b.toString());
                        audible.updateInfo(b);
                        AAXParser.instance.update(b);
                        bookNotifier.bookUpdated(b);
                    }
                    audible.save();
                    setTask("Completed", "");
                    bookNotifier.booksUpdated();
                } catch (Exception e) {
                    if (!wasCanceled())
                        showError(e, "refreshing book information");
                } finally {
                    audible.setProgress(null);
                    if (scraper != null)
                        scraper.setProgress(null);
                }

            }
        };

        ProgressDialog.doProgressTask(task);

    }

    private void showError(Throwable th, String string) {
        LOG.error(string, th);
        MessageBoxFactory.showError(null, "Error " + string + ".\n" + th.getMessage());
    }

    @Override
    public void booksSelected(List<Book> list) {

    }

    @Override
    public void bookAdded(Book book) {
    }

    @Override
    public void bookUpdated(Book book) {
    }

    @Override
    public void booksUpdated() {
        audible.updateFileCache();
        int d = 0;
        int c = 0;
        for (Book b : audible.getBooks()) {
            boolean m = audible.hasMP3(b);
            if (!audible.hasAAX(b)) {
                d++;
            } else {
                if (!m) c++;
            }

        }
        downloadCount = d;
        convertCount = c;
    }

    // if search text is filled, return books that match.
    // otherwise, return all books (default)
    public List<Book> getDisplayedBooks() {
        ArrayList<Book> displayed = new ArrayList<Book>();
        if (textFilter.isEmpty())
            displayed.addAll(Audible.instance.getBooks());
        else {
            for (Book b : Audible.instance.getBooks()) {
                if (bookContainsText(b, textFilter))
                    displayed.add(b);
            }

        }
        return displayed;
    }

    public void filterDisplayedBooks(String text) {
        textFilter = text;
        bookNotifier.booksUpdated();
    }

    private boolean bookContainsText(Book b, String text) {
        text = text.toLowerCase();
        BookElement elems[] = {BookElement.fullTitle, BookElement.author, BookElement.narratedBy, BookElement.shortTitle};

        for (BookElement e : elems) {
            if (b.has(e) && b.get(e).toLowerCase().contains(text))
                return true;
        }
        return false;
    }

    public void parseAAX() {
        ProgressTask task = new ProgressTask("Parse AAX File") {
            public void run() {

                try {
                    for (Book b : getSelected()) {
                        if (wasCanceled())
                            break;

                        setTask("Parse AAX" + b);
                        if (Audible.instance.hasAAX(b)) {
                            AAXParser.instance.update(b);
                            bookNotifier.bookUpdated(b);
                        }

                    }
                    audible.save();
                    setTask("Completed", "");
                    bookNotifier.booksUpdated();
                    System.err.println("Updated :" + getSelected().size());

                } catch (Exception e) {
                    showError(e, "debug");
                } finally {

                }

            }
        };

        ProgressDialog.doProgressTask(task);

    }

    public void redeemGiftCode() {
        String e = ""; // get from desktop, example: "XXXXXXXXXXXXX" or "XXXXXXXXXXXXX, YYYYYYYYYYYYY"
        try {
            if (e.length() > 0) {
                if (e.contains(",")) {
                    for (String s : e.split(","))
                        audible.redeemGiftCode(s.trim());

                } else {
                    audible.redeemGiftCode(e.trim());

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBoxFactory.showError(null, "Error redeeming gift code!\n" + ex.getMessage());
        }
        // TODO Auto-generated method stub

    }

    public void browse() {
        SWTAsync.run(new SWTAsync() {
            @Override
            public void task() {
                browse("https://www.audible.com/lib");
            }
        });
    }

    public void browse(String url) {
        SWTAsync.assertGUI();

        if (browser == null || browser.getBrowser().isDisposed()) {
            browser = AudibleBrowser.newBrowserWindow(Application.display, url);
        } else {
            browser.getBrowser().setUrl(url);
        }

    }

    public boolean updateCookies(boolean showBrowser) {
        SWTAsync.assertGUI();

        if (browser == null || browser.getBrowser().isDisposed()) {
            if (showBrowser)
                browse("https://www.audible.com");
            else
                return false;
        }
        final Collection<Cookie> cookies = browser.getCookies();

        if (cookies != null) {

            try {
                audible.setExternalCookies(cookies);
                LOG.info("Set " + cookies.size() + " cookies");
                return true;
            } catch (Throwable e) {
                LOG.info("unable to set cookies: ", e);
            }

        }


        return false;
    }

    class BookQueueListener implements IQueueListener<Book> {

        @Override
        public void itemEnqueued(ThreadedQueue<Book> queue, Book o) {
            bookNotifier.booksUpdated();
        }

        @Override
        public void itemDequeued(ThreadedQueue<Book> queue, Book o) {
            bookNotifier.booksUpdated();
        }

        @Override
        public void jobStarted(ThreadedQueue<Book> queue, IQueueJob job, Book o) {
            bookNotifier.bookUpdated(o);
        }

        @Override
        public void jobError(ThreadedQueue<Book> queue, IQueueJob job, Book o, Throwable th) {
            bookNotifier.bookUpdated(o);
        }

        @Override
        public void jobCompleted(ThreadedQueue<Book> queue, IQueueJob job, Book o) {
            bookNotifier.bookUpdated(o);
        }

    }

    class PageBuilderTask extends ProgressTask {
        final WebPage pageBuilder;
        final List<Book> books;

        PageBuilderTask(File dest, final List<Book> list) {
            super("Creating Your Audiobook Web Page");
            pageBuilder = new WebPage(dest);
            pageBuilder.setProgress(this);
            books = list;
        }

        @Override
        public void run() {
            try {
                pageBuilder.buildPage(books);
            } catch (Exception e) {
                LOG.error("error", e);
                if (!wasCanceled())
                    showError(e, "building web page");

            }
        }
    }

}