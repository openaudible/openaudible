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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * It has one optional attribute, domain, a string that identifies a categorization taxonomy.
 * <p>
 * The value of the element is a forward-slash-separated string that identifies a hierarchic location in the indicated taxonomy.
 * Processors may establish conventions for the interpretation of categories. Two examples are provided below:
 *
 * <category>Grateful Dead</category>
 * <category domain="http://www.fool.com/cusips">MSFT</category>
 * <p>
 * You may include as many category elements as you need to, for different domains, and to have an item cross-referenced in different parts of the same domain.
 */
public class Category {

	private String domain;
	private String textValue;

	@XmlAttribute
	public String getDomain() {
		return domain;
	}

	public Category setDomain(String domain) {
		this.domain = domain;
		return this;
	}

	@XmlValue
	public String getTextValue() {
		return textValue;
	}

	public Category setTextValue(String textValue) {
		this.textValue = textValue;
		return this;
	}
}