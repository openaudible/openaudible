package org.openaudible.util;

public enum Platform {
    mac, win, linux;
    private static Platform platform;

    public static Platform getPlatform() {
        if (platform == null) {
            String os = System.getProperty("os.name");
            if (os == null) os = "";
            os = os.toLowerCase();
            if (os.contains("mac ")) platform = mac;
            else if (os.contains("windows ")) platform = win;
            else platform = linux;  //
        }
        return platform;
    }


    public static boolean isMac() {
        return getPlatform().equals(mac);
    }

    public static boolean isWindows() {
        return getPlatform().equals(win);
    }

    public static boolean isLinux() {
        return getPlatform().equals(linux);
    }
}
