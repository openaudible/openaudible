package org.openaudible;

import org.openaudible.books.Book;

import java.util.Arrays;

public enum BookToFilenameStrategy {
    instance;    // Singleton
    final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47,
            (int) '\'', (int) ','};        // Note, both % and # are allowed.. but need to be escaped if used in a web page.

    static {
        Arrays.sort(illegalChars);
    }

    int maxLen = 128;    // long file names are ok

    public String getFileName(Book b) {
        return b.getProduct_id();
    }

    public String getReadableFileName(Book b) {
        return _getReadableFileName(b);
    }

    private String fullFileName(Book b) {
        return b.shortTitle();
    }

    private String _getReadableFileName(Book b) {
        String n = fullFileName(b);
        n = cleanFileName(n);
        if (n.length() > maxLen) {
            n = n.substring(0, maxLen);
            assert (n.length() == maxLen);
        }
        return n;
    }

    boolean isCleanFile(String f) {
        return f.equals(cleanFileName(f));
    }

    private String _removeAll(String n, String what) {
        String name = n;
        for (; ; ) {
            int ch = name.indexOf(what);
            if (ch == -1)
                break;
            String newName = name.substring(0, ch);
            newName += name.substring(ch + what.length(), name.length());
            name = newName;
        }

        return name;
    }

    String cleanFileName(String name) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            int c = (int) name.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char) c);
            }
        }

        name = cleanName.toString();

        String find[] = {"Unabridged", "_ep6_", "(1)", "()"};
        for (String f : find) {
            name = _removeAll(name, f);
        }

        name = name.replaceAll("  ", " ");    // no double spaces.
        return name;
    }

}
