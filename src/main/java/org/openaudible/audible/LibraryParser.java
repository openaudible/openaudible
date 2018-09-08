package org.openaudible.audible;

import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.util.HTMLUtil;
import org.openaudible.util.Util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Parse a page of library inforation.
// http://audible.com/lib
// When audible changes the format of the above web page... this class will need to be updated.
public enum LibraryParser {
	instance;
	private static final Log LOG = LogFactory.getLog(LibraryParser.class);
	boolean debug = false;
	
	boolean howToListenFound = false;
	
	
	// Expected Columns:
	// Image
	// Title
	// Author
	// Length
	// Date Added
	// Rate and Review
	// Downloaded
	// Other Actions
	
	private enum BookColumns {
		Image, Title, Author, Length, Date_Added, Ratings, Download, Other;
		
		public static int size() {
			return values().length;
		}
		
		// get product id and asin first.
		static BookColumns[] parseOrder = {Download, Other, Image, Title, Author, Length, Date_Added, Ratings};
		
	}
	
	
	// return "next" button for next page of results.
	public HtmlElement getNextPage(HtmlPage page) {
		HtmlElement next = null;
		DomNodeList<HtmlElement> buttons = page.getDocumentElement().getElementsByTagName("button");
		
		for (HtmlElement a : buttons) {
			// System.out.println(a);
			if (a.toString().toLowerCase().contains("pagenext")) {
				assert (next == null);
				next = a;
			}
		}
		
		
		return next;
	}
	
	
	public ArrayList<Book> parseLibraryFragment(HtmlPage p) {
		ArrayList<Book> list = new ArrayList<>();
		ArrayList<String> colNames = new ArrayList<>();
		
		HtmlTable table = p.getFirstByXPath("//table");
		if (table == null)
			return list;
		
		if (debug) HTMLUtil.debugNode(table, "lib_table");
		
		int purchaseDateIndex = -1;
		List<HtmlElement> header = table.getElementsByTagName("th");
		
		if (header.size() != BookColumns.size()) {
			LOG.info("Skipping table with:" + header.size() + " cols");
			
			return list;
		}
		
		
		int index = 0;
		for (HtmlElement h : header) {
			String xml = h.asXml();
			colNames.add(h.asText());
			
			if (xml.contains("PURCHASE_DATE")) {
				purchaseDateIndex = index;
			}
			index++;
		}
		
		assert (purchaseDateIndex == BookColumns.Date_Added.ordinal());
		int rindex = 0;
		
		for (HtmlTableRow r : table.getRows()) {
			
			rindex++;
			if (rindex == 1)
				continue;    // skip header row.
			Book b = parseLibraryRow(p, r);
			
			if (b != null) {
				String chk = b.checkBook();
				if (chk.isEmpty()) {
					list.add(b);
				} else {
					LOG.info("Warning, problem parsing book: " + b + " error: " + chk);
				}
			}
			
		}
		
		LOG.info("Library page contains: " + list.size() + " book(s)");
		
		return list;
	}
	
	
	String debugString = "OR_ORIG";
	
	
	private Book parseLibraryRow(HtmlPage p, HtmlTableRow r) {
		
		String xml = Util.cleanString(r.asXml());
		if (r.getCells().size() == 0)
			return null;    // empty row.
		if (xml.contains("/howtolisten")) {
			// this is a problem.. settings need to be changed.
			howToListenFound = true;
		}
		
		if (r.getCells().size() != BookColumns.size()) {
			LOG.error("wrong number of columns found: " + r.getCells().size() + " != " + BookColumns.size());
			LOG.error(xml);
			if (debug) HTMLUtil.debugNode(r, "bad_col.xml");
			return null;
		}
		
		
		Book b = new Book();
		
		String asin = HTMLUtil.findHidden(r, "asin");
		b.setAsin(asin);
		
		
		if (!debugString.isEmpty()) {
			int count = Util.substringCount(debugString, xml);
			if (count > 0)
				LOG.info("Found debugString: " + count + " " + debugString);
		}
		
		if (debug) HTMLUtil.debugNode(r, "cur_row");
		List<HtmlElement> cells = r.getElementsByTagName("td");
		
		for (BookColumns col : BookColumns.parseOrder) {
			HtmlElement cell = cells.get(col.ordinal());
			parseBookColumn(p, col, cell, b);
		}
		
		return b;
	}
	
	
	private void parseBookColumn(HtmlPage p, BookColumns col, HtmlElement cell, Book b) {
		
		// HTMLUtil.debugNode(cell, col.name()+".xml");
		String text = Util.cleanString(cell.asText());
		String xml = Util.cleanString(cell.asXml());
		DomNodeList<HtmlElement> anchors;
		
		if (xml.contains(debugString)) {
			LOG.info("Found product_id in " + col);
			LOG.info(col.name() + "=" + text);
			LOG.info("xml=" + Util.cleanString(xml));
		}
		
		int ch = text.indexOf("\n");
		if (ch != -1)
			text = text.substring(0, ch);
		
		
		switch (col) {
			case Image:
				break;
			case Title:
				anchors = cell.getElementsByTagName("a");
				for (int x = 0; x < anchors.size(); x++) {
					HtmlAnchor a = (HtmlAnchor) anchors.get(x);
					String href = a.getHrefAttribute();
					
					// /pd/Fiction/Exodus-Audiobook/B008I3VMMQ?
					if (href.startsWith("/pd/")) {
						URL url = p.getUrl();
						String protocol = url.getProtocol();
						String host = url.getHost();
						
						int q = href.indexOf("?");
						if (q != -1)
							href = href.substring(0, q);
						
						boolean ok = false;
						
						
						if (b.has(BookElement.asin) && href.contains(b.getAsin()))
							ok = true;
						
						if (b.has(BookElement.product_id) && href.contains(b.getProduct_id()))
							ok = true;
						if (ok) {
							
							String full_url = String.format("%s://%s%s", protocol, host, href);
							
							b.setInfoLink(full_url);
						} else
							LOG.info("Unknown product link for " + b + " at " + url);
					}
				}
				
				if (text.contains("by parts")) {
					LOG.error("error with title: " + text);
					if (debug) HTMLUtil.debugNode(cell, col.name() + ".xml");
					// bug check.
				}
				
				b.setFullTitle(text);
				break;
			case Author:
				b.setAuthor(text);
				// attempt to get link to author page.
				try {
					anchors = cell.getElementsByTagName("a");
					if (anchors.size() == 1) {
						HtmlAnchor first = (HtmlAnchor) anchors.get(0);
						URL url = p.getFullyQualifiedUrl(first.getHrefAttribute());
						String href = url.toString();
						if (href.contains("?"))
							href = href.substring(0, href.indexOf("?"));
						b.setAuthorLink(href);
					}
				} catch (Throwable th) {
					LOG.error("error getting author link:" + xml);
				}
				break;
			case Length:
				b.setDuration(text);
				break;
			case Date_Added:
				b.setPurchaseDate(text);
				break;
			case Ratings:
				// TODO: Update ratings.
				break;
			case Download:
				break;
			case Other:
				anchors = cell.getElementsByTagName("a");
				for (int x = 0; x < anchors.size(); x++) {
					HtmlAnchor a = (HtmlAnchor) anchors.get(x);
					String url = a.getHrefAttribute();
					
					
					if (url.contains("cds.audible")) {
						parseDownloadURL(a, b);
					}
				}
				break;
		}
	}

    /*
            String url = "https://cds.audible.com/download/admhelper?user_id=xxx-yyy&amp;product_id=BK_PENG_00000&amp;domain=www.audible.com&amp;order_number=xxxx&amp;
                cust_id=xxx&amp;DownloadType=Now&amp;transfer_player=1&amp;title=Book Title&amp;codec=LC_32_22050_Mono&amp;awtype=AAX";
    */
	
	
	private void parseDownloadURL(HtmlAnchor a, Book b) {
		String url = a.getHrefAttribute();
		
		HashMap<String, String> args = Util.urlGetArgs(url);
		for (String k : args.keySet()) {
			
			BookElement elem = BookElement.findByName(k);
			if (elem != null) {
				b.set(elem, args.get(k));
			}
		}
		
	}
	
	
}
