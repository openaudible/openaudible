package org.openaudible.audible;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.JsonArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.util.HTMLUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public enum BookPageParser {
    instance;


    private static final Log LOG = LogFactory.getLog(BookPageParser.class);
    public  String extract(String c, DomNode h) {
        return HTMLUtil.text(HTMLUtil.findByClass(c, h));
    }

    public  String extractParagraph(String c, DomNode h) {
        String out = "";
        DomNode node = (DomNode) HTMLUtil.findByClass(c, h);
        if (node != null) {
            NodeList cn = node.getChildNodes();
            for (int x = 0; x < cn.getLength(); x++) {
                Node y = cn.item(x);
                String text = y.getTextContent();
                if (text != null) {
                    text = text.trim();
                    if (out.length() > 0)
                        out += "\n";
                    out += text;
                }

            }
        }
        return out;
    }


    List<String> getCDATATags(String html)
    {
        ArrayList<String> list = new ArrayList<>();
        String startTag="<![CDATA[";
        String endTag = "//]]>";

        int ch = 0;
        for (;;)
        {
            int start = html.indexOf(startTag, ch);
            if (start == -1) break;
            int end = html.indexOf(endTag, ch);
            assert(end!=-1);
            if (end == -1) break;
            String cdata = html.substring(start+startTag.length(), end).trim();
            list.add(cdata);
            ch = end+endTag.length();
        }


        return list;
    }

    public boolean parseBookPage(HtmlPage page, Book b) {
        DomNode h = page;
        HTMLUtil.debugNode(page, "book_info");
        String xml = page.asXml();
        List<String> cdataList = getCDATATags(xml);
        for (String cd:cdataList)
        {
            if (cd.startsWith("["))
            {
                cd = cd.replace("\n", "");      // getting parse errors.

                try {
                    JSONArray jsonArray = new JSONArray(cd);
                    for (int x=0;x<jsonArray.length();x++)
                    {
                        JSONObject obj = jsonArray.getJSONObject(x);
                        extractFromJSON(obj, b);
                    }
                }catch(Throwable th)
                {
                    LOG.info(cd);
                    LOG.error("cdata json parse error", th);
                }
            }

        }


        return true;
    }


    /*
      "image": "https://m.media-amazon.com/images/I/51u1om96bmL._SL500_.jpg",
  "@type": "Audiobook",
  "author": [{
    "@type": "Person",
    "name": "Amanda Hodgkinson"
  }],
  "readBy": [{
    "@type": "Person",
    "name": "Robin Sachs"
  }],
  "description": "<p>A tour de force that echoes modern classics like <i>Suite Francaise<\/i> and <i>The Postmistress<\/i>. <\/p><p>\"Housekeeper or housewife?\" the soldier asks Silvana as she and eight-year-old Aurek board the ship that will take them from Poland to England at the end of World War II. There her husband, Janusz, is already waiting for them at the little house at 22 Britannia Road. But the war has changed them all so utterly that they'll barely recognize one another when they are reunited. \"Survivor,\" she answers.<\/p><p>Silvana and Aurek spent the war hiding in the forests of Poland. Wild, almost feral Aurek doesn't know how to tie his own shoes or sleep in a bed. Janusz is an Englishman now-determined to forget Poland, forget his own ghosts from the way, and begin a new life as a proper English family. But for Silvana, who cannot escape the painful memory of a shattering wartime act, forgetting is not a possibility.<\/p>",
  "abridged": "false",
  "inLanguage": "english",
  "bookFormat": "AudiobookFormat",
  "@context": "https://schema.org",
  "datePublished": "2011-04-28",
  "duration": "PT11H19M",
  "name": "22 Britannia Road",
  "publisher": "Penguin Audio",
  "aggregateRating": {
    "@type": "AggregateRating",
    "ratingValue": "3.6842105263157894",
    "ratingCount": "171"
  }
     */

    private void extractFromJSON(JSONObject obj, Book b) {
        String typ = obj.optString("@type");
        if (typ == null || typ.isEmpty())
            return;
        if (!"AudioBook".equalsIgnoreCase(typ)) // && !"Product".equalsIgnoreCase(typ))
            return;

        LOG.info(obj.toString(2));

        for (String k:obj.keySet())
        {
            System.out.println(k+" = "+ obj.get(k));
            Object value = obj.get(k);
            String str = value!=null ? value.toString():"";

            BookElement elem = null;

            switch(k)
            {
                case "description":
                    elem = BookElement.description;
                    break;
                case "sku":

                    // elem = BookElement.product_id;
                    break;

                case "duration":
                    // format is like "PT11H19M" .. skipping for now.

                    break;

                case "productID":
                    elem = BookElement.asin;
                    if (b.has(elem)) {
                        assert (b.get(elem).equals(str));
                    }
                    break;
                case "datePublished":
                    elem = BookElement.release_date;
                    break;
                case "author":
                    str = personToString(obj.getJSONArray(k));
                    elem = BookElement.author;
                    break;
                case "readBy":
                    str = personToString(obj.getJSONArray(k));
                    elem = BookElement.narratedBy;
                    break;

                case "aggregateRating":
                    JSONObject rating = obj.getJSONObject(k);
                    double rvalue = rating.optDouble("ratingValue", 0);
                    int rcount = rating.optInt("ratingCount",0);
                    if (rvalue>0)
                    b.setRating_average(rvalue);
                    if (rcount>0)
                        b.setRating_count(rcount);
                    break;
                case "name":
                    elem = BookElement.fullTitle;
                    break;
                case "publisher":
                    elem = BookElement.publisher;
                    break;
                default:
                    LOG.info("Skipping "+k+" = "+ str);
                    break;
            }

            if (elem!=null && !str.isEmpty())
            {
                if (!str.equals(b.get(elem))) {
                    LOG.info("set " + elem + " from " + b.get(elem) + " to " + str);
                    b.set(elem, str);
                }
            }


        }



    }


//      "author": [{
//        "@type": "Person",
//                "name": "Susan Smith"
//    }],
//            "readBy": [{
//        "@type": "Person",
//                "name": "Robin Racer"
//    }],

    private String personToString(JSONArray arr) {
        String out = "";
        for (int x=0;x<arr.length();x++)
        {
            JSONObject p = arr.getJSONObject(x);
            assert(p.getString("@type").equals("Person"));
            String name = p.optString("name","");
            if (!name.isEmpty())
            {
                if (!out.isEmpty())
                    out+=",";
                out += name;
            }
        }

        return out;
    }

}
