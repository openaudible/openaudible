package org.openaudible.desktop.swt.i8n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


public class Translate {
    private static Translate instance = new Translate();
    ResourceBundle bundle;
    Map<String, String> map = new HashMap<>();
    Locale locale = new Locale("en", "US");
    private Translate() {
        update();
    }

    public static Translate getInstance() {
        return instance;
    }

    public static Locale getLocale() {
        return instance.locale;
    }

    public static void setLocale(Locale l) {
        instance.locale = l;
        instance.update();
    }

    private void update() {
        bundle = ResourceBundle.getBundle("languages.i18n", locale);
        for (String k : bundle.keySet()) {
            String v = fromProperties(bundle.getString(k));
            map.put(k, v);
        }
        // displayValues(bundle);
    }

    public void displayValues(ResourceBundle bundle) {
        for (String k : bundle.keySet()) {
            String v = bundle.getString(k);
            System.out.println("i18n: " + k + "=" + v);
        }
    }

    public String getTranslation(String k, String def) {
        String key = k.replace(" ", "_").toUpperCase();
        String v = map.get(key);
        if (v == null) {
            if (def != null) {
                //System.out.println(key + "=" + toProperties(def));
                map.put(key, def);
                return def;
            } else {
                // System.out.println(key + "=" + key);
                return k;
            }
        }
        return v;
    }

    public String getTranslation(String key) {
        return getTranslation(key, null);
    }

    private String fromProperties(String trans) {
        String out = trans.replace("\\n", "\n");
        out = out.replace("\\r", "\r");
        out = out.replace("\\t", "\t");
        return out;
    }

    private String toProperties(String trans) {
        String out = trans.replace("\n", "\\n");
        out = out.replace("\r", "\\r");
        out = out.replace("\t", "\\t");
        return out;
    }


    private String itemName(String key, String item, String def) {
        String what = item + "_" + key.toUpperCase().replace(" ", "_");
        assert (!key.toUpperCase().contains(what));
        if (def == null)
        {
            def = key.replace('_',' ');
        }
        return getTranslation(what, def);
    }

    public String buttonName(String n, String def) {
        return itemName(n, "BUTTON", def);
    }

    public String groupName(String n, String def) {
        return itemName(n, "GROUP", def);
    }

    public String labelName(String n, String def) {
        return itemName(n, "LABEL", def);
    }

    public String menuName(String n, String def) {
        return itemName(n, "MENU", def);
    }

    public String commandName(String n, String def) {
        return itemName(n, "COMMAND", def);
    }

    public String buttonName(String n) {
        return buttonName(n, null);
    }

    public String groupName(String n) {
        return groupName(n, null);
    }

    public String labelName(String n) {
        return labelName(n, null);
    }

    public String menuName(String n) {
        return menuName(n, null);
    }

    public String commandName(String n) {
        return commandName(n, null);

    }

    public String getTitle(String n) {
        return itemName(n, "TITLE", null);
    }

    public String getMessage(String n) {
        return itemName(n, "MESSAGE", null);
    }


}
