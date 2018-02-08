package org.openaudible;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created  6/26/2017.
 */
public enum Directories {
    BASE, WEB, META, AAX, ART, MP3, TMP, APP;

    final static String dirPrefsName = "directories.json";

    static String paths[];

    public static void init(File etc, File base) throws IOException {
        assert (paths == null);

        paths = new String[Directories.values().length];
        setPath(META, etc.getAbsolutePath());
        setPath(BASE, base.getAbsolutePath());

        try {
            File jarFile = new File(Directories.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            String appPath = jarFile.getParentFile().getAbsolutePath();
            String debugPath = "target";
            // when running from IDE, it jarFile will not be the jar but will be the target/classes dir.
            if (appPath.endsWith(debugPath)) {
                String path2 = appPath.substring(0, appPath.length() - debugPath.length() - 1);
                File dir = new File(path2);
                assert (dir.isDirectory());
                if (dir.isDirectory())
                    appPath = dir.getAbsolutePath();
            }

            setPath(APP, appPath);


            File help = getHelpDirectory();
            if (!help.exists())
                System.out.println("missing help:" + help.getAbsolutePath());

        } catch (Throwable e) {
            e.printStackTrace();
            assert (false);  // unusual but not the end of the world.
        }


        load();
        mkdirs();
        cleanTempDir();
        save();
    }

    public static void mkdirs() throws IOException {
        for (Directories d : Directories.values()) {
            File dir = getDir(d);
            if (dir.isDirectory()) continue;
            boolean ok = dir.mkdir();
            if (!ok) throw new IOException("Unable to create directory: " + dir.getAbsolutePath());

        }

    }

    public static void save() throws IOException {
        File f = META.getDir(dirPrefsName);
        JsonObject j = toJSON();
        FileUtils.write(f, j.toString(), "utf-8");
    }

    public static void load() throws IOException {
        File f = META.getDir(dirPrefsName);
        if (f.length() > 0) {

            JsonParser parser = new JsonParser();
            String json = FileUtils.readFileToString(f, "utf8");

            JsonElement jsonElement = parser.parse(json);   // new FileReader(f));
            JsonObject j = jsonElement.getAsJsonObject();
            fromJSON(j);

        }

    }

    public static File getHelpDirectory() {

        File dir = new File(getDir(Directories.APP), "help");
        if (!dir.exists()) {
            File dir2 = new File(getDir(Directories.APP), "src" + File.separator + "main" + File.separator + "help");
            if (dir2.exists())
                return dir2;
            System.out.println(dir2.getAbsolutePath());

        }

        // assert(dir.exists());
        return dir;
    }

    public static JsonObject toJSON() {
        JsonObject out = new JsonObject();
        for (Directories d : Directories.values()) {
            if (paths[d.ordinal()] != null)
                out.addProperty(d.name(), d.getPath());
        }
        return out;
    }

    public static void fromJSON(JsonObject j) {
        for (Directories d : Directories.values()) {
            JsonElement e = j.get(d.name());
            if (e != null)
                setPath(d, e.getAsString());
        }
    }

    public static boolean setPath(Directories d, String path) {
        assert (paths != null);

        File f = new File(path);
        assert (f.exists());
        paths[d.ordinal()] = path;
        return f.exists();
    }

    public static String getPath(Directories d) {
        String path = paths[d.ordinal()];
        if (path == null || path.length() == 0) {
            if (d != Directories.BASE) {
                File f = new File(BASE.getPath(), d.defaultDirName());
                return f.getAbsolutePath();
            }
        }
        return path;
    }

    public static void cleanTempDir() {
        for (File f : getTmpDir().listFiles()) {
            String n = f.getName();
            if (n.endsWith(".part") || n.endsWith("_temp.mp3"))
                f.delete();
        }

    }

    public static File getTmpDir() {
        return getDir(Directories.TMP);
    }

    public static File getDir(Directories d) {
        File t = new File(getPath(d));
        return t;
    }

    public static void assertInitialized() throws IOException {
        if (paths == null)
            throw new IOException("Directories not initialized");
    }

    public String displayName() {

        String out = this.name().toLowerCase();


        switch (this) {
            case BASE:
                out = "Working";
                break;
            case APP:
                out = "Application";
                break;
            case WEB:
                out = "Web/MP3";
                break;
            case META:
                out = "Preferences";
                break;
            case AAX:
                out = "Audible (AAX)";
                break;
            case MP3:
                out = "MP3";
                break;
            case TMP:
                out = "Temp Files";
                break;
        }
        return out;
    }

    public boolean setPath(String path) {
        return setPath(this, path);
    }

    public String getPath() {
        return getPath(this);
    }

    private String defaultDirName() {
        switch (this) {
            case BASE:
                return "";
            default:
                return this.name().toLowerCase();
        }
    }

    public File getDir() {
        return getDir(this);
    }

    public File[] getFiles() {
        return getDir().listFiles();
    }

    public File getDir(String s) {
        return new File(getDir(), s);
    }
}
