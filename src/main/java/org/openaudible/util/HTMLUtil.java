package org.openaudible.util;

import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openaudible.Directories;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HTMLUtil {
	private static final Log LOG = LogFactory.getLog(HTMLUtil.class);
	
	public static Node findByClass(String c, DomNode h) {
		return h.getFirstByXPath("//*[@class='" + c + "']");
	}
	
	public static Node findByName(String c, DomNode h) {
		return h.getFirstByXPath("//*[@name='" + c + "']");
	}
	
	public static Node findById(String c, DomNode h) {
		return h.getFirstByXPath("//*[@id='" + c + "']");
	}
	
	public static Collection<Node> getChildren(DomNode h) {
		ArrayList<Node> list = new ArrayList<>();
		DomNodeList<DomNode> l = h.getChildNodes();
		list.addAll(l);
		
		return list;
	}
	
	public static Node findByType(String c, DomNode h) {
		return h.getFirstByXPath("//*[@type='" + c + "']");
	}
	
	public static String inspect(Node n) {
		String text = n.getTextContent();
		return ("node: " + n + "\ntext:" + text + "\ntype:" + n.getClass().getName() + " Children:" + n.getChildNodes().getLength());
	}
	
	public static String text(Node n) {
		return text(n, true);
	}
	
	public static String text(Node n, boolean cleanText) {
		if (n != null) {
			String s = n.getTextContent();
			
			if (cleanText) {
				s = s.trim();
				s = s.replace("\n", " ");
				s = s.replace("\r", " ");
				s = s.replace("\t", " ");
				while (s.contains("  "))
					s = s.replace("  ", " ");
			}
			
			return s;
		}
		return "";
	}
	
	public static String readFile(File f) throws IOException {
		if (!f.exists()) return "";
		String content = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
		return content;
	}
	
	public static void writeFile(File f, String str) throws IOException {
		Files.write(Paths.get(f.getAbsolutePath()), str.getBytes());
	}
	
	public static String findHidden(HtmlElement elem, String n) {
		List<HtmlElement> test = elem.getElementsByAttribute("input", "name", n);
		if (!test.isEmpty()) {
			HtmlInput i = (HtmlInput) test.get(0);
			String v = i.getValueAttribute();
			
			if (v != null && v.length() > 0)
				return v;
		}
		return null;
	}
	
	public static File debugFile(String what) {
		return new File(Directories.getTmpDir(), what);
	}
	
	public static File debugToFile(String what, String text) throws IOException {
		File f = debugFile(what);
		FileUtils.writeByteArrayToFile(f, text.getBytes());
		return f;
	}
	
	public static String debugNode(DomNode p, String what) {
		String xml = "";
		
		try {
			xml = p.asXml();
			if (!what.contains("."))
				what += ".html";
			
			File x = new File(Directories.getTmpDir(), what);
			HTMLUtil.writeFile(x, xml);
			
			//			File h = new File(what + ".html");
			//			HTMLUtil.writeFile(h, p.as());
			LOG.info("Debug: " + x.getAbsolutePath());
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return xml;
		
	}
	
	public static List<HtmlTable> getTables(DomNode page) {
		ArrayList<HtmlTable> list = new ArrayList<>();
		list.addAll((Collection<? extends HtmlTable>) page.getByXPath("//table"));
		return list;
	}
	
}
