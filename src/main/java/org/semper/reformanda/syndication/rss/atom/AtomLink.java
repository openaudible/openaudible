/**
 * Copyright 2015 Joshua Cain
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.semper.reformanda.syndication.rss.atom;

import org.semper.reformanda.syndication.rss.MimeType;
import org.semper.reformanda.syndication.util.MimeTypeAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(namespace = "http://www.w3.org/2005/Atom")
public class AtomLink {

	// TODO this is Stringly-typed.  Should go back and make links URLs, enumerate types, etc.
	private String href;
	private String rel;
	private MimeType type;

	public String getHref() {
		return href;
	}

	@XmlAttribute
	public AtomLink setHref(final String href) {
		this.href = href;
		return this;
	}

	public String getRel() {
		return rel;
	}

	@XmlAttribute
	public AtomLink setRel(final String rel) {
		this.rel = rel;
		return this;
	}

	public MimeType getType() {
		return type;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(MimeTypeAdapter.class)
	public AtomLink setType(final MimeType type) {
		this.type = type;
		return this;
	}
}