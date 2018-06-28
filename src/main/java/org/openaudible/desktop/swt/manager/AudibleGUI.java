package org.openaudible.desktop.swt.manager;

import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.openaudible.Audible;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.Directories;
import org.openaudible.audible.*;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.books.BookListener;
import org.openaudible.books.BookNotifier;
import org.openaudible.convert.AAXParser;
import org.openaudible.convert.FFMPEG;
import org.openaudible.desktop.swt.gui.GUI;
import org.openaudible.desktop.swt.gui.MessageBoxFactory;
import org.openaudible.desktop.swt.gui.SWTAsync;
import org.openaudible.desktop.swt.gui.progress.ProgressDialog;
import org.openaudible.desktop.swt.gui.progress.ProgressTask;
import org.openaudible.desktop.swt.manager.views.AudibleBrowser;
import org.openaudible.desktop.swt.manager.views.PasswordDialog;
import org.openaudible.desktop.swt.manager.views.StatusPanel;
import org.openaudible.feeds.pagebuilder.WebPage;
import org.openaudible.util.HTMLUtil;
import org.openaudible.util.Platform;
import org.openaudible.util.TimeToSeconds;
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

public class AudibleGUI implements BookListener, ConnectionListener {
    private static final Log LOG = LogFactory.getLog(AudibleGUI.class);
    public static AudibleGUI instance;
    final Audible audible = new Audible();
    boolean hasFFMPEG = false;
    BookNotifier bookNotifier = BookNotifier.getInstance();
    boolean loggedIn = false;
    int downloadCount, convertCount;
    String textFilter = "";
    AudibleBrowser browser = null;

    public Prefs prefs = new Prefs();
    final String appPrefsFileName = "settings.json";
    private long totalDuration;

    public AudibleGUI() {
        assert (instance == null);
        instance = this;
        bookNotifier.addListener(this);
        LOG.info("audible desktop " + Version.appVersion);
    }

    public boolean checkFFMPEG() {

        try {
            // Thread.sleep(4000);
            String vers = FFMPEG.getVersion();
            LOG.info("using " + vers);
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


        try {

            audible.init();
            audible.initConverter();

            // Listen for events about jobs:
            BookQueueListener queueListener = new BookQueueListener();

            // downloading aax files
            audible.downloadQueue.addListener(queueListener);
            // converting aax to mp3.
            audible.convertQueue.addListener(queueListener);


            ConnectionNotifier.instance.addListener(this);


            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        } catch (Throwable th) {
            th.printStackTrace();
            showError(th, "starting application");
            System.exit(1);
        }
    }

    @Override
    public void connectionChanged(boolean connected) {

    }

    AudibleAccountPrefs userPass = null;

    @Override
    public AudibleAccountPrefs getAccountPrefs(AudibleAccountPrefs in) {
        if (in.audiblePassword.isEmpty() || in.audibleUser.isEmpty()) {
            userPass = in;

            SWTAsync.block(new SWTAsync("get password") {
                @Override
                public void task() {

                    PasswordDialog gp = new PasswordDialog(null, "Audible Credenials Required",
                            "Please enter a user/password for " + userPass.audibleRegion.getBaseDomain(),
                            userPass.audibleUser, userPass.audiblePassword);
                    int status = gp.open();
                    if (status == Window.OK) {
                        userPass.audibleUser = gp.getUserName();
                        userPass.audiblePassword = gp.getPassword();
                    }
                    userPass = null;
                }
            });
            LOG.info("Done getting credentials");
            in = userPass;
            userPass = null;
        }
        return in;
    }


/*
    public void fetchDecryptionKeyOld() {
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
    public void fetchDecryptionKey() {
        try {
            File aax = null;
            for (Book b:getSelected())
            {

            }

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


    public String lookupKey(final File aaxFile) {

        class LookupTask extends ProgressTask {
            LookupTask() {
                super("Look up encrpytion key...");

            }

            String result = null;
            String err = null;

            public void run() {
                try {
                    result = LookupKey.instance.getKeyFromAAX(aaxFile, this);
                } catch (Exception e) {
                    err = e.getMessage();

                }
            }
        }
        ;
        LookupTask task = new LookupTask();

        ProgressDialog.doProgressTask(task);
        if (task.err != null) {
            MessageBoxFactory.showError(null, "Unable to get Key\nError:" + task.err);
            return null;
        } else {
            return task.result;
        }

    }
*/

    public int selectedAAXCount() {
        int count = 0;
        for (Book b : getSelected()) {
            if (audible.hasAAX(b))
                count++;
        }
        return count;
    }


    public boolean canDownloadAll() {
        return audible.aaxCount() < audible.getBookCount();
    }

    public boolean canConvertAll() {
        return audible.mp3Count() < audible.getBookCount();
    }

    public void convertAll() {
        ArrayList<Book> l = new ArrayList<>();
        for (Book b : audible.getBooks()) {
            if (!audible.hasMP3(b))
                l.add(b);
        }
        convertMP3(l);
    }

    public void downloadAll() {
        ArrayList<Book> l = new ArrayList<>();
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
            MessageBoxFactory.showGeneral(null, 0, "Missing credentials", "This version requires your audible email and password to be set in preferences.");
        } else {

            ProgressTask task = new ProgressTask("Connecting...") {
                public void run() {
                    AudibleScraper scraper = null;

                    try {
                        scraper = connect(this);
                        if (scraper == null) return;
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
        bookNotifier.setEnabled(false);
        audible.convertQueue.addAll(list);
        bookNotifier.setEnabled(true);
        bookNotifier.booksUpdated();
    }

    public boolean hasAAX(Book b) {
        return audible.hasAAX(b);
    }

    public boolean hasMP3(Book b) {
        return audible.hasMP3(b);
    }

    public void refreshLibrary(final boolean quickRescan) {

        final ProgressTask task = new ProgressTask("Audible Library Update...") {
            public void run() {
                try {

                    loggedIn = false;
                    AudibleScraper s = connect(this);
                    if (s == null)
                        return;
                    audible.updateLibrary(quickRescan);
                    setTask("Completed", "");
                    audible.save();
                    bookNotifier.booksUpdated();
                    loggedIn = true;
                } catch (AudibleLoginError e) {

                    MessageBoxFactory.showGeneral(null, 0, "Log in via web browser...", "Unable to connect right now.\n\nTry logging on to Audible from this web page and try again.\n\nIf this keeps ");

                } catch (Throwable e) {
                    LOG.info("Error refreshing library", e);
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


    // returns null if not logged in.
    private AudibleScraper connect(ProgressTask progressTask) throws Exception {
        audible.setProgress(progressTask);
        progressTask.setTask("Connecting...", "");
        final AudibleScraper s = audible.getScraper(false);
        if (s != null && !s.isLoggedIn()) {
            if (browser != null) {
                LOG.info("Setting cookies 1");

                SWTAsync.block(new SWTAsync("connect") {
                    @Override
                    public void task() {
                        LOG.info("Done Setting cookies 2");
                        updateCookies(s, false);
                        LOG.info("Done Setting cookies 3");
                    }
                });
                LOG.info("Done Setting cookies 4");
            }

        }

        try {

            s.home();

            if (ConnectionNotifier.getInstance().isConnected())
                return s;

        } catch (Throwable th) {
            LOG.error("unable to connect", th);
        }

        String page = s.getPageURL();
        if (page == null)
            page = audible.getAudibleURL();

        browse(page);
        return null;

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

            LOG.info(msg + " autoConvert=" + prefs.autoConvert);

            boolean ok = prefs.autoConvert;
            if (!ok) ok = MessageBoxFactory.showGeneralYesNo(null, "Start jobs?", msg);
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
        if (GUI.isLinux())
            return false;   // TODO: FIX for Linux

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

    // has login credentials.
    public boolean hasLogin() {
        return true;
    }

    public boolean canViewInAudible() {
        Book b = onlyOneSelected();
        if (b != null) {
            return !b.getInfoLink().isEmpty();
            // might have to search...
            // Can search for Product_ID and get one result..

        }
        return false;
    }

    public boolean canViewInSystem() {

        if (GUI.isLinux())
            return false;        // TODO: Fix for Linux. How to display a file in "desktop"
        Book b = onlyOneSelected();
        if (b != null) {
            return audible.hasMP3(b);
        }
        return false;
    }

    public int bookCount() {
        return audible.getBookCount();
    }

    public String getStatus(StatusPanel.Status e) {
        String out = "";

        switch (e) {
            case Hours:
                float hours = AudibleGUI.instance.getTotalDuration() / 3600.0f;
                if (hours < 100)
                    return "" + Math.round(hours * 10) / 10.0;
                else return "" + Math.round(hours);

            case AAX_Files:
                return "" + audible.aaxCount();
            case Books:
                return "" + audible.getBookCount();
            case MP3_Files:
                return "" + audible.mp3Count();
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
            case Connected:
                return ConnectionNotifier.getInstance().isConnected() ? "Yes" : "No";

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
                GUI.explore(m);


                // Desktop.getDesktop().open(m.getParentFile());
            }
        } catch (Throwable th) {
            showError(th, "showing file in system");
        }
    }

    public void viewInAudible() {
        String link = onlyOneSelected().getInfoLink();
        if (link.startsWith("/"))
            link = audible.getAudibleURL() + link;

        if (link.startsWith("http")) {
            browse(link);
        }
    }


    public void exportWebPage() {
        try {
            File destDir = Directories.getDir(Directories.WEB);

            ArrayList<Book> list = new ArrayList<>();
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

                    setTask("Connecting", "");
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
        // TODO: Ensure this isn't called too frequently.
        audible.updateFileCache();
        int d = 0;
        int c = 0;
        long seconds = 0;

        for (Book b : audible.getBooks()) {
            boolean m = audible.hasMP3(b);
            if (!audible.hasAAX(b)) {
                d++;
            } else {
                if (!m) c++;
            }

            seconds += TimeToSeconds.parseTimeStringToSeconds(b.getDuration());

        }
        downloadCount = d;
        convertCount = c;
        totalDuration = seconds;
    }


    // if search text is filled, return books that match.
    // otherwise, return all books (default)
    public List<Book> getDisplayedBooks() {
        ArrayList<Book> displayed = new ArrayList<>();
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


    public void browse() {
        browse(audible.getAudibleURL() + "/lib");
    }


    public void browse(final String url) {

        SWTAsync.run(new SWTAsync("browse") {
            @Override
            public void task() {
                if (browser == null || browser.isDisposed()) {
                    browser = AudibleBrowser.newBrowserWindow(Application.display, url);
                } else {
                    browser.setUrl(url);
                }
            }
        });


    }

    public boolean updateCookies(AudibleScraper s, boolean showBrowser) {
        SWTAsync.assertGUI();
        if (browser == null || browser.isDisposed()) {
            if (showBrowser)
                browse(audible.getAudibleURL());
            else
                return false;
        }
        final Collection<Cookie> cookies = browser.getCookies();
        if (cookies != null) {

            try {
                audible.setExternalCookies(s, cookies);
                LOG.info("Set " + cookies.size() + " cookies");
                return true;
            } catch (Throwable e) {
                LOG.info("unable to set cookies: ", e);
            }
        }

        return false;
    }

    public boolean logout() {
        SWTAsync.assertGUI();

        if (browser != null && browser.isDisposed()) {
            browser.close();
        }


        try {
            audible.logout();
            return true;
        } catch (Throwable e) {
            LOG.info("unable to set cookies: ", e);
        }

        return false;
    }

    // returns task in progress, or tasks that can be done for a book.
    // book may be null.
    public String getTaskString(final Book b) {
        String out = "";
        if (b != null) {
            if (hasMP3(b))
                return "Converted to MP3";
            if (hasAAX(b)) {
                if (audible.convertQueue.isQueued(b))
                    return "In convert queue";
                if (audible.convertQueue.inJob(b))
                    return "Converting...";
                if (!audible.convertQueue.canAdd(b))
                    return "Unable to convert";     // ?
                return "Ready to convert to MP3";
            }

            if (audible.downloadQueue.isQueued(b))
                return "In download queue";
            if (audible.downloadQueue.inJob(b))
                return "Downloading...";

            if (!audible.downloadQueue.canAdd(b))
                return "Unable to download";

            if (ConnectionNotifier.getInstance().isConnected())
                return "Ready to download";
            return "Not downloaded";
        }
        return out;
    }

    // total book time, in seconds.
    public long getTotalDuration() {
        return totalDuration;
    }

    public void test1() {
        if (KindleScraper.instance == null) {
            new KindleScraper(audible.getAccount());

        }

        try {
            KindleScraper.instance.test();

        } catch (Throwable th) {
            LOG.debug("Error", th);
        }
    }

    class BookQueueListener implements IQueueListener<Book> {

        @Override
        public void itemEnqueued(final ThreadedQueue<Book> queue, final Book o) {
            bookNotifier.booksUpdated();
        }

        @Override
        public void itemDequeued(final ThreadedQueue<Book> queue, final Book o) {
            bookNotifier.booksUpdated();
        }

        @Override
        public void jobStarted(final ThreadedQueue<Book> queue, final IQueueJob job, final Book o) {
            bookNotifier.bookUpdated(o);
        }

        @Override
        public void jobError(final ThreadedQueue<Book> queue, final IQueueJob job, final Book o, final Throwable th) {
            bookNotifier.bookUpdated(o);
        }

        @Override
        public void jobCompleted(final ThreadedQueue<Book> queue, final IQueueJob job, final Book o) {
            booksUpdated();
            bookNotifier.bookUpdated(o);
        }

        @Override
        public void jobProgress(final ThreadedQueue<Book> queue, final IQueueJob job, final Book book, final String task, final String subtask) {
            String msg = "";

            if (queue == audible.downloadQueue)
                msg = "Downloading ";
            else
                msg = "Converting ";

            assert (msg.length() > 0);

            if (subtask != null) {
                msg += subtask;
            }
            bookNotifier.bookProgress(book, msg);
        }
    }


    class PageBuilderTask extends ProgressTask {
        final WebPage pageBuilder;
        final List<Book> books;

        PageBuilderTask(File dest, final List<Book> list) {
            super("Creating Your Audiobook Web Page");
            pageBuilder = new WebPage(dest, this);
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

    public void load() throws IOException {
        Audible.instance.load();

        try {
            Gson gson = new GsonBuilder().create();
            File prefsFile = Directories.META.getDir(appPrefsFileName);

            if (prefsFile.exists()) {
                String content = HTMLUtil.readFile(prefsFile);
                prefs = gson.fromJson(content, Prefs.class);
            }


        } catch (Throwable th) {
            LOG.info("Error loading prefs", th);
            prefs = new Prefs();
        }

    }

    public void save() throws IOException {
        Audible.instance.save();
        Gson gson = new GsonBuilder().create();
        HTMLUtil.writeFile(Directories.META.getDir(appPrefsFileName), gson.toJson(prefs));
    }

    public void applicationStarted() {


        ProgressTask task = new ProgressTask("Loading") {
            int books = 0;

            public void setTask(String t, String s) {
                super.setTask(t, s);
                if (false && audible.getBookCount() != books) {
                    books = audible.getBookCount();
                    if (books % 3 == 0)
                        BookNotifier.getInstance().booksUpdated();

                }
            }


            public void run() {
                try {
                    audible.setProgress(this);
                    this.setTask("Loading");
                    load();

                    this.setTask("Finding Audible Files");
                    audible.findOrphanedFiles(this);
                    this.setTask("Updating");
                    BookNotifier.getInstance().booksUpdated();

                    audible.updateFileCache();
                    // audibleGUI.updateFileCache();

                    BookNotifier.getInstance().booksUpdated();
                    backgroundVersionCheck();
                    new Thread(() -> checkFFMPEG()).start();


                } catch (Exception e) {
                    LOG.error("Error starting", e);

                    MessageBoxFactory.showError(null, e);// , "loading library");
                } finally {
                    audible.setProgress(null);
                }

            }
        };
        ProgressDialog.doProgressTask(task);
    }

    // SWT Shell accessor.
    private Shell getShell() {
        return GUI.shell;
    }

    private void backgroundVersionCheck() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // only alert if new version is available.
            VersionCheck.instance.checkForUpdate(getShell(), false);

        }).start();
    }


    public void importAAXFiles() {
        try {
            String ext = "*.aax";
            String name = "Audible Files";
            org.eclipse.swt.widgets.FileDialog dialog = new org.eclipse.swt.widgets.FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
            dialog.setFilterNames(new String[]{name});
            dialog.setFilterExtensions(new String[]{ext});

            String path = dialog.open();
            String files[] = dialog.getFileNames();
            System.out.println(path);

            if (files != null && files.length > 0) {
                File test = new File(path);
                File dir = test.getParentFile();

                ArrayList<File> aaxFiles = new ArrayList<>();
                for (String s : files) {
                    File f = new File(dir, s);
                    assert (f.exists());
                    aaxFiles.add(f);
                }

                importBooks(aaxFiles);

            }

        } catch (Exception e) {
            MessageBoxFactory.showError(getShell(), e.getMessage());
        }
    }

    public void importBooks(final List<File> aaxFiles) {
        if (SWTAsync.inDisplayThread()) {
            // hack. Need to release current GUI thread and start progress in new thread.
            new Thread(() -> importBooks(aaxFiles)).start();
            return;
        }

        SWTAsync.assertNot();

        ProgressTask task = new ProgressTask("Importing...") {
            public void run() {

                try {
                    for (File f : aaxFiles) {
                        setTask("Importing", f.getName());
                        Audible.instance.importAAX(f, this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error(e);
                    if (!wasCanceled())
                        MessageBoxFactory.showError(null, "Error importing book" + e.getMessage());
                }
            }
        };
        ProgressDialog.doProgressTask(task);
    }

    public void exportBookList() {
        try {
            String ext = "*.csv";
            String name = "CSV (Excel) File";
            org.eclipse.swt.widgets.FileDialog dialog = new org.eclipse.swt.widgets.FileDialog(getShell(), SWT.SAVE);
            dialog.setFilterNames(new String[]{name});
            dialog.setFilterExtensions(new String[]{ext});
            dialog.setFileName("books.csv");
            String path = dialog.open();
            if (path != null) {
                File f = new File(path);
                audible.export(f);
                if (f.exists())
                    LOG.info("exported books to: " + f.getAbsolutePath());
            }

        } catch (Exception e) {
            MessageBoxFactory.showError(getShell(), e.getMessage());
        }
    }

    public void exportBookJSON() {
        try {
            String ext = "*.json";
            String name = "JSON File";
            org.eclipse.swt.widgets.FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
            dialog.setFilterNames(new String[]{name});
            dialog.setFilterExtensions(new String[]{ext});
            dialog.setFileName("books.json");
            String path = dialog.open();
            if (path != null) {
                File f = new File(path);
                audible.export(f);
                if (f.exists())
                    LOG.info("exported books to: " + f.getAbsolutePath());
            }

        } catch (Exception e) {
            MessageBoxFactory.showError(getShell(), e.getMessage());
        }

    }

	@Override
	public void loginFailed(String url, String html)
	{
		
        SWTAsync.slow(new SWTAsync("Login problem...") {
            public void task() {
        		String message = "There was a problem logging in... Try to view the page in the OpenAudible Browser?";
        		boolean ok = MessageBoxFactory.showGeneralYesNo(null, "Trouble logging in", message);
        		if (ok)
        			browse(url);
            }
        });

		
		
	}

}