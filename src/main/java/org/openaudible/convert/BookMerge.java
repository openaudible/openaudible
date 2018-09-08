package org.openaudible.convert;

import org.openaudible.books.Book;
import org.openaudible.books.BookElement;

import java.util.HashSet;

public enum BookMerge {
	
	instance;
	
	public HashSet<BookElement> merge(Book book, Book book2) {
		HashSet<BookElement> list = new HashSet<>();
		
		if (book.getProduct_id().equals(book2.getProduct_id())) {
			for (BookElement e : BookElement.values()) {
				if (mergeItem(book, book2, e))
					list.add(e);
				
			}
		} else {
			assert (false);
		}
		return list;
	}
	
	public boolean mergeItem(Book book, Book book2, BookElement e) {
		String s1 = book.get(e);
		String s2 = book2.get(e);
		String result = mergeItem(s1, s2, e);
		book.set(e, result);
		return !result.equals(s1);
	}
	
	// s1 = original value
	// s2 = possibly newer value
	public String mergeItem(String s1, String s2, BookElement e) {
		if (s1 == null)
			s1 = "";
		if (s2 == null)
			s2 = "";
		if (s1.equals(s2))
			return s1; // normal case..
		
		switch (e) {
			case cust_id:
			case user_id:
			case infoLink:
				
				if (!s2.isEmpty())
					return s2;
			
			default:
				break;
		}
		
		int l1 = s1.length();
		int l2 = s2.length();
		
		
		if (l1 > l2) {
			if (l2 > 0)
				System.err.println("Using old " + e + ":\n  " + s1 + " not\n  " + s2);
			return s1;
		} else {
			if (l1 > 0)
				System.err.println("Update aax " + e + ": " + s2 + " from " + s1);
			return s2;
		}
	}
	
}
