package org.openaudible.desktop.swt.i8n;


import org.apache.commons.io.FileUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class GrepSource {
    static HashMap hash = new HashMap();
    static GrepSource gs;
    boolean found = false;

    public static boolean searchSrc(String key) {
        if (gs == null) {
            gs = new GrepSource();

            gs.loadCache(new File("src"));
            gs.grep("FRIEND_SUBJECT");
        }
        return gs.grep(key);
    }

    String fileToString(String f) {
        DataInputStream dis = null;
        String cache = (String) hash.get(f);
        if (cache != null)
            return cache;
        try {
            String out = FileUtils.readFileToString(new File(f), "utf8");

            hash.put(f, out);
            // System.out.println("File:" + f);
            return out;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error reading file:" + f + " len=" + f.length() + " " + e.getMessage());
            // e.printStackTrace();
            if (dis != null) {
                try {
                    dis.close();
                } catch (Exception eee) {
                }
            }
            hash.put(f, "");
        }
        return "";
    }

    private void loadCache(File dir) {
        File f[] = dir.listFiles();
        for (int x = 0; x < f.length; x++) {
            if (found)
                break;
            if (f[x].isDirectory()) {
                loadCache(f[x]);
            } else {
                String fname = f[x].getAbsolutePath();
                if (fname.endsWith(".java") && fname.indexOf("SyncI18nEN") == -1) {
                    String text = fileToString(fname);
                    hash.put(fname, text);
                }
            }
        }
    }

    public boolean grep(String key) {
        int fc = 0;

        Set s = hash.keySet();
        key = "\"" + key + "\"";
        for (Iterator i = s.iterator(); i.hasNext(); ) {
            String fname = (String) i.next();
            String text = (String) hash.get(fname);
            if (text.indexOf(key) != -1) {
                fc++;
            }
        }

        return fc != 0;
    }
}
