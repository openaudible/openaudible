package org.openaudible.audible;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.AudibleAccountPrefs;
import org.openaudible.Directories;
import org.openaudible.util.HTMLUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class KindleScraper extends AudibleScraper {
	public static KindleScraper instance;
	private static final Log LOG = LogFactory.getLog(KindleScraper.class);
	HashSet<String> digitalProducts = new HashSet<>();
	int maxPages = 1;
	
	
	public KindleScraper(AudibleAccountPrefs account) {
		super(account);
		instance = this;
	}
	
	public void test() throws Exception {
		home();
		if (!loggedIn())
			login();
		
		getFreeBooks();
	}
	
	
	public HtmlAnchor findBooks() throws IOException {
		
		HtmlAnchor next = null;
		ArrayList<HtmlAnchor> nextLinks = new ArrayList<>();
		
		for (HtmlAnchor n : page.getAnchors()) {
			boolean print = false;
			String dp = getDigitalProduct(n);
			if (dp != null) {
				digitalProducts.add(dp);
				// print = true;
			}
			
			if (n.toString().toLowerCase().contains("next")) {
				print = true;
				String clz = n.getAttribute("class");
				if (clz == null) clz = "";
				
				if ("pagnNextLink".equalsIgnoreCase(n.getId()) || clz.contains("pagnNext"))// .equalsIgnoreCase(n.getAttribute("class")))
					next = n;
				nextLinks.add(n);
			}
			if (print) {
				LOG.info(n);
				// LOG.info(n.getHrefAttribute());
			}
		}
		
		
		LOG.info("next links:" + nextLinks.size());
		LOG.info("digitalProducts:" + digitalProducts.size());
		return next;
	}
	
	private String getDigitalProduct(HtmlAnchor n) {
		String find = "/dp/";
		String ref = n.getHrefAttribute();
		int ch = ref.indexOf(find);
		
		if (ch != -1) {
			String id = ref.substring(ch + find.length(), ref.length());
			ch = id.indexOf("/");
			assert (ch != -1);
			if (ch != -1) {
				id = id.substring(0, ch);
				int len = id.length();
				assert (len < 15);
				if (len < 15)
					return id;
			}
			
			
		}
		return null;
	}
	
	public void getProductInfo(String dp) throws IOException {
		
		File x = new File(Directories.getTmpDir(), "dp_" + dp + ".html");
		if (!x.exists()) {
			String base = "https://www.amazon.com/dp/" + dp;
			setURL(base);
			parseDigitalProductPage(page);
			HTMLUtil.writeFile(x, page.getDocumentElement().asXml());
		}
		
		
	}
	
	private void parseDigitalProductPage(Page p) {
	
	
	}
	
	public String getAudibleBase() {
		return "https://amazon.com/";
		
		// return account.audibleRegion.getBaseURL();
	}
	
	
	public void getFreeBooks() throws IOException {
		int pages = 0;
		
		
		String base = "https://www.amazon.com/Books-with-Narration-in-Kindle-Unlimited/b?ie=UTF8&node=9630682011";
		
		setURL(base);
		
		checkLoggedIn();
		
		for (; ; ) {
			int startBooks = digitalProducts.size();
			HtmlAnchor next = findBooks();
			if (next == null) break;
			
			int endBooks = digitalProducts.size();
			if (startBooks == endBooks) break;
			if (++pages > maxPages) break;
			
			page = next.click();
		}
		LOG.info("digitalProducts:" + digitalProducts.size());
		for (String dp : digitalProducts) {
			getProductInfo(dp);
		}
		
	}
	
}

