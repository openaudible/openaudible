package org.semper.reformanda.syndication.rss.itunes;

import java.util.HashMap;
import java.util.Map;

public enum Explicit {
	YES("yes"),
	CLEAN("clean");

	private static final Map<String, Explicit> reverseMapping = new HashMap<String, Explicit>();

	private final String textRepresentation;

	Explicit(final String textRepresentation) {
		this.textRepresentation = textRepresentation;
	}

	// sync'd for HashMap lookup
	public static synchronized Explicit fromText(final String explicitTextString) {
		return reverseMapping.get(explicitTextString);
	}

	@Override
	public String toString() {
		return textRepresentation;
	}
}