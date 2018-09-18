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

package org.semper.reformanda.syndication.rss.itunes;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class ItunesCategory {
	
	private String text;
	// TODO could probably use some validation here around nesting sub-categories too deep.  Could produce invalid feed using this model.
	private List<ItunesCategory> subcategories;
	// TODO the category "name" element should be an attribute, rather than a text node!
	
	@XmlAttribute
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	@XmlElement(name = "category")
	public List<ItunesCategory> getSubcategories() {
		if (subcategories == null) {
			subcategories = new ArrayList();
		}
		
		return subcategories;
	}
}