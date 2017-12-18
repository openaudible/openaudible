package org.openaudible.desktop.util;

public enum GUIUtil {
    instance;
    private static String os_name = System.getProperty("os.name");
    private static String os_version = System.getProperty("os.version");
    private static String java_version = System.getProperty("java.version");
    private static String java_vm_version = System.getProperty("java.vm.version");

    public boolean isMac() {
        return (os_name != null && os_name.indexOf("Mac ") >= 0);
    }

    public boolean isMacOSX() {
        return (os_name != null && os_name.indexOf("Mac OS X") >= 0);
    }

    public boolean isMacClassic() {
        return (isMac() && !isMacOSX());
    }

    public boolean isWindows() {
        return (os_name != null && os_name.indexOf("Windows ") >= 0);
    }

}