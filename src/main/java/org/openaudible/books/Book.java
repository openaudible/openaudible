package org.openaudible.books;


import org.openaudible.util.TimeToSeconds;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Book implements Comparable<Book>, Serializable {
    private final HashMap<String, String> map = new HashMap<>();

    public Book(HashMap<String, String> m) {
        map.putAll(m);
    }

    public Book() {
    }

    public String get(BookElement m) {
        String o = map.get(m.name());
        if (o == null)
            o = "";
        return o;
    }

    public boolean has(BookElement m) {
        String s = get(m);
        return s.length() > 0;
    }

    public String clean(BookElement e, String value) {
        if (value == null) value = "";

        if (e == BookElement.fullTitle || e == BookElement.shortTitle)
            value = value.replace("(Unabridged)", "").trim();

        return value;
    }

    // sets value for book element.
    // returns true if the value has changed.
    public boolean set(BookElement m, String v) {
        v = clean(m, v);
        String old = get(m);

        map.put(m.name(), v);
        boolean changed = !v.equals(old);

        if (!old.isEmpty() && v.isEmpty()) {
            // clearing required fields is bad..
            assert (m != BookElement.fullTitle);
            assert (m != BookElement.product_id);
        }


        return changed;
    }

    public String toString() {
        String st = shortTitle();
        if (getFullTitle().length() > 32) {
            if (st.length() > 32) {
                st = st.substring(0, 32);
            }
        }
        return st; //  + " [product_id=" + getProduct_id() + " author=" + getAuthor() + ", narrated by=" + getNarratedBy() + "]";
    }

    public String shortTitle() {
        return (getShortTitle().length() > 0) ? getShortTitle() : getFullTitle();
    }

    public boolean equals(Book that) {
        if (that == null) return false;
        if (this == that) return true;
        boolean e1 = this.getProduct_id().equals(that.getProduct_id());
        return e1;
    }

    public boolean isOK() {
        return checkBook().isEmpty();
    }

    // product_id is our primary key.
    public String checkBook() {
        // BookElement required[] = { BookElement.product_id, BookElement.user_id, BookElement.cust_id };
        BookElement required[] = {BookElement.product_id, BookElement.fullTitle};
        for (BookElement e : required) {
            if (!this.has(e))
                return "required:" + e + " missing from " + this;
        }
        return "";
    }

    // unique value for book.
    public String id() {
        return getProduct_id();
    }


    @Override
    public int compareTo(Book that) {
        return this.getFullTitle().compareTo(that.getFullTitle());
    }

    public String getFullTitle() {
        return get(BookElement.fullTitle);
    }

    public void setFullTitle(String fullTitle) {
        set(BookElement.fullTitle, fullTitle);
    }

    public String getProduct_id() {
        return get(BookElement.product_id);
    }

    public void setProduct_id(String product_id) {
        set(BookElement.product_id, product_id);
    }

    public String getCodec() {
        return get(BookElement.codec);
    }

    public void setCodec(String codec) {
        set(BookElement.codec, codec);
    }

    public String getAsin() {
        return get(BookElement.asin);
    }

    public void setAsin(String asin) {
        set(BookElement.asin, asin);
    }

    public String getInfoLink() {
        return get(BookElement.infoLink);
    }

    public void setInfoLink(String infoLink) {
        set(BookElement.infoLink, infoLink);
    }

    public String getAuthor() {
        return get(BookElement.author);
    }

    public void setAuthor(String author) {
        set(BookElement.author, author);
    }

    public String getNarratedBy() {
        return get(BookElement.narratedBy);
    }

    public void setNarratedBy(String narratedBy) {
        set(BookElement.narratedBy, narratedBy);
    }

    public String getSummary() {
        return get(BookElement.summary);
    }

    public void setSummary(String summary) {
        set(BookElement.summary, summary);
    }

    // Short description (shorter than summary)
    public String getDescription() {
        return get(BookElement.description);
    }

    public void setDescription(String d) {
        set(BookElement.description, d);
    }

    public String getDuration() {
        return get(BookElement.duration);
    }

    // hh:mm:ss
    public void setDuration(String d) {
        set(BookElement.duration, d);
    }

    public String getFormat() {
        return get(BookElement.format);
    }

    public void setFormat(String format) {
        set(BookElement.format, format);
    }

    public String getRating_average() {
        return get(BookElement.rating_average);
    }

    public void setRating_average(double rating_average) {
        set(BookElement.rating_average, "" + rating_average);
    }

    public void setRating_average(String rating_average) {
        set(BookElement.rating_average, rating_average);
    }

    public String getRating_count() {
        return get(BookElement.rating_count);
    }

    public void setRating_count(int rating_count) {
        set(BookElement.rating_count, "" + rating_count);
    }

    public void setRating_count(String rating_count) {
        set(BookElement.rating_count, rating_count);
    }

    public String getRelease_date() {
        return get(BookElement.release_date);
    }

    public void setRelease_date(String release_date) {
        set(BookElement.release_date, release_date);
    }

    public String getPublisher() {
        return get(BookElement.publisher);
    }

    public void setPublisher(String publisher) {
        set(BookElement.publisher, publisher);
    }

    public String getGenre() {
        return get(BookElement.genre);
    }

    public void setGenre(String genre) {
        set(BookElement.genre, genre);
    }

    public String getShortTitle() {
        return get(BookElement.shortTitle);
    }

    public void setShortTitle(String shortTitle) {
        set(BookElement.shortTitle, shortTitle);
    }

    public String getCopyright() {
        return get(BookElement.copyright);
    }

    public void setCopyright(String copyright) {
        set(BookElement.copyright, copyright);
    }

    public String inspect(String sep) {
        String out = "";
        for (BookElement e : BookElement.values()) {
            if (has(e))
                out += e.name() + "=" + get(e) + sep;
        }

        return out;
    }

    @Override
    public int hashCode() {
        assert (!getProduct_id().isEmpty());
        return getProduct_id().hashCode();
    }

    public String getPurchaseDate() {
        return get(BookElement.purchase_date);
    }

    public void setPurchaseDate(String purchaseDateText) {
        set(BookElement.purchase_date, purchaseDateText);
    }

    public String getDurationHHMM() {
        long seconds = TimeToSeconds.parseTimeStringToSeconds(getDuration());
        return TimeToSeconds.secondsToHHMM(seconds);
    }

    public String getReleaseDateSortable() {
        String date = getRelease_date();
        if (!date.isEmpty()) {
            // 11-MAR-2015
            SimpleDateFormat parseFormat = new SimpleDateFormat("dd-MMM-yyyy");
            try {
                Date d = parseFormat.parse(date);
                SimpleDateFormat dispalyFormat = new SimpleDateFormat("yyyy-MM-dd");
                String out = dispalyFormat.format(d);
                return out;
            } catch (ParseException e) {
             e.printStackTrace();
            }
        }

        return date;
    }


    public String getPurchaseDateSortable() {
        String date = getPurchaseDate();
        if (!date.isEmpty()) {
            String dt[] = date.split("-");
            if (dt.length == 3) {
                return "20" + dt[2] + "-" + dt[0] + "-" + dt[1];    // yyyy-mm-dd for sorting and viewing
                // warning, y3k bug
            } else {

            }
        }

        return date;
    }
}
