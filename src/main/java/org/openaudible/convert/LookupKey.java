package org.openaudible.convert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.util.HTMLUtil;
import org.openaudible.util.Platform;
import org.openaudible.util.SimpleProcess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public enum LookupKey {
    instance;
    private static final Log LOG = LogFactory.getLog(LookupKey.class);

    HashMap<String, String> map = new HashMap<>();     // hash, key
    File prefs = null;


    public void load(File prefsFile) throws IOException {
        if (false) return;  // for testing without cache.
        if (prefsFile.exists()) {
            Gson gson = new GsonBuilder().create();
            String content = HTMLUtil.readFile(prefsFile);
            HashMap m = gson.fromJson(content, HashMap.class);
            if (m != null)
                map.putAll(m);
        }
        prefs = prefsFile;
    }

    public void save() throws IOException {
        if (prefs != null && map.size() > 0) {
            Gson gson = new Gson();
            String json = gson.toJson(map);
            try (FileWriter writer = new FileWriter(prefs)) {
                writer.write(json);
            }
        }
    }


    public String getFileChecksum(String path) throws IOException, InterruptedException {

        assert (path.toLowerCase().endsWith(".aax"));    // expected

        ArrayList<String> args = new ArrayList<>();
        args.add(FFMPEG.getExecutable());
        args.add("-i");
        args.add(path);

        SimpleProcess ffmpeg = new SimpleProcess(args);
        SimpleProcess.Results results = ffmpeg.getResults();
        String find = "[aax] file checksum == ";

        String r = results.getErrorString();
        int ch = r.indexOf(find);
        if (ch == -1) {
            throw new IOException("Unable to find expected output: " + find);
        }
        r = r.substring(ch + find.length(), r.length());
        ch = r.indexOf("\n");
        if (ch == -1)
            ch = r.indexOf("\r");
        r = r.substring(0, ch);
        r = r.trim();

        return r;
    }

    public String getKeyFromAAX(String path) throws IOException, InterruptedException {
        File f = new File(path);
        if (!f.exists())
            throw new IOException("file not found:" + path);
        String hash = LookupKey.instance.getFileChecksum(f.getAbsolutePath());
        String key = LookupKey.instance.lookupKey(hash);

        return key;
    }

    public String getKeyFromAAX(File f) throws IOException, InterruptedException {

        if (!f.exists())
            throw new IOException("file not found:" + f.getAbsolutePath());
        return getKeyFromAAX(f.getAbsolutePath());
    }

    public String lookupKey(String hash) throws IOException, InterruptedException {

        String result = map.get(hash);
        if (result != null)
            return result;

        File tablesDir = new File("bin" + File.separatorChar + "tables");
        if (!tablesDir.exists())
            throw new IOException("fnf:" + tablesDir.getAbsolutePath());

        assert (hash.length() > 10);    // should be hex string.

        ArrayList<String> args = new ArrayList<>();
        args.add(getExecutable());
        // I don't know wjh
        if (!Platform.isWindows()) {
            for (File f : tablesDir.listFiles())
                if (f.getName().contains(".rt"))
                    args.add(f.getAbsolutePath());
        } else {
            args.add(tablesDir.getAbsolutePath());
        }

        args.add("-h");
        args.add(hash);

        SimpleProcess rcrack = new SimpleProcess(args);
        SimpleProcess.Results results = rcrack.getResults();
        String find = "hex:";

        String r = results.getOutputString();
        LOG.info(r);

        int ch = r.indexOf(find);
        if (ch == -1) {
            LOG.info(results.getErrorString());
            throw new IOException("Unable to find expected output: " + find);
        }

        r = r.substring(ch + find.length(), r.length());
        ch = r.indexOf("\n");
        if (ch == -1)
            ch = r.indexOf("\t");
        r = r.substring(0, ch);
        result = r.trim();
        LOG.info(result);

        map.put(hash, result);
        save();

        return result;
    }

    private String getExecutable() {

        // File.separatorChar++
        String s = "bin" + File.separatorChar + Platform.getPlatform().name() + "_rcrack";
        if (Platform.isWindows()) s += ".exe";

        File f = new File(s);
        if (f.exists()) {
            return f.getAbsolutePath();
        }

        assert (f.exists());
        return null;
    }

/*
    public static void main(String a[]) {
        try {

            File test = new File("test.aax");
            String key = instance.getKeyFromAAX(test);
            LOG.info(test.getName()+" -> "+ key);

            ///String hash = LookupKey.instance.getFileChecksum(test.getAbsolutePath());
            // LookupKey.instance.lookupKey(hash, new NullProgressTask());
        } catch (Throwable th) {
            th.printStackTrace();
        }

    }
*/

}
