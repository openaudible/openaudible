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

/**
 * Created by jcain on 8/22/15.
 */
public class ItunesImage {
	
	private String href;
	
	@XmlAttribute
	public String getHref() {
		return href;
	}
	
	public ItunesImage setHref(String href) {
		this.href = href;
		return this;
	}
}