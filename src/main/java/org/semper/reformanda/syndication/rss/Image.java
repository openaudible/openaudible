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

public class Image {

	private String url;
	private String title;
	private String link;
	private String width;
	private String height;
	private String description;

	public String getUrl() {
		return url;
	}

	public Image setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Image setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getLink() {
		return link;
	}

	public Image setLink(String link) {
		this.link = link;
		return this;
	}

	public String getWidth() {
		return width;
	}

	public Image setWidth(String width) {
		this.width = width;
		return this;
	}

	public String getHeight() {
		return height;
	}

	public Image setHeight(String height) {
		this.height = height;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public Image setDescription(String description) {
		this.description = description;
		return this;
	}
}