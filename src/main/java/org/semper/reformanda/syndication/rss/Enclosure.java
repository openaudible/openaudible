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

package org.semper.reformanda.syndication.rss;

import org.semper.reformanda.syndication.util.MimeTypeAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

public class Enclosure {
	
	private URI url;
	private long length;
	private MimeType type;
	
	@XmlAttribute
	public URI getUrl() {
		return url;
	}
	
	public Enclosure setUrl(final URI url) {
		this.url = url;
		return this;
	}
	
	@XmlAttribute
	public long getLength() {
		return length;
	}
	
	public Enclosure setLength(final long length) {
		this.length = length;
		return this;
	}
	
	@XmlAttribute
	@XmlJavaTypeAdapter(MimeTypeAdapter.class)
	public MimeType getType() {
		return type;
	}
	
	public Enclosure setType(final MimeType type) {
		this.type = type;
		return this;
	}
}