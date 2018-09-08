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

import org.semper.reformanda.syndication.rss.atom.AtomLink;
import org.semper.reformanda.syndication.rss.itunes.*;
import org.semper.reformanda.syndication.util.ExplicityTypeAdapter;
import org.semper.reformanda.syndication.util.Rfc822DateFormatAdapter;
import org.semper.reformanda.syndication.util.YesNoTypeAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Built from the following specifications:
 * <p>
 * RSS Spec: http://cyber.law.harvard.edu/rss/rss.html
 * ATOM Spec: https://tools.ietf.org/html/rfc4287
 * Itunes Spec: http://www.apple.com/itunes/podcasts/specs.html
 */
// TODO group these according to where they come from...
@XmlType(propOrder = {"title", "link", "atomLink", "pubDate", "lastBuildDate", "category", "ttl", "language", "copyright", "webMaster",
		"managingEditor", "description", "image", "generator", "docs", "owner", "author", "explicit", "itunesImage", "itunesCategory",
		"complete", "newFeedUrl", "items"})
public class Channel {

	// TODO required vs non-required fields

	// Generic RSS fields - REQUIRED
	private String title;
	private String link;
	private String description;
	// Generic RSS fields - OPTIONAL
	private String language;
	private String copyright;
	private String managingEditor;
	private String webMaster;
	private Date pubDate;
	private Date lastBuildDate;
	private List<Category> category;
	private String generator = "jaxbRss by Josh Cain";
	private URL docs;
	// TODO cloud
	private int ttl;
	private Image image;
	// TODO rating
	// TODO textInput
	// TODO skip hours
	// TODO skip days

	// Atom Fields
	private AtomLink atomLink;

	// iTunes Fields
	private Owner owner;
	private String author;
	private Explicit explicit;
	private ItunesImage itunesImage; // TODO URI?
	private ItunesCategory itunesCategory; // TODO sub-cats
	private YesNo complete;
	private URL newFeedUrl; // TODO test unmarshalling of a URL - might need a mapper here

	private List<Item> items;

	public Channel() {
		try {
			docs = new URL("http://blogs.law.harvard.edu/tech/rss");
		} catch (MalformedURLException e) {
			// should never happen, but if we don't have a "docs" element, no one really cares.
		}
	}

	public String getTitle() {
		return title;
	}

	public Channel setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getLink() {
		return link;
	}

	public Channel setLink(String link) {
		this.link = link;
		return this;
	}

	@XmlJavaTypeAdapter(Rfc822DateFormatAdapter.class)
	public Date getPubDate() {
		return pubDate;
	}

	public Channel setPubDate(Date pubDate) {
		this.pubDate = pubDate;
		return this;
	}

	@XmlJavaTypeAdapter(Rfc822DateFormatAdapter.class)
	public Date getLastBuildDate() {
		return lastBuildDate;
	}

	public Channel setLastBuildDate(Date lastBuildDate) {
		this.lastBuildDate = lastBuildDate;
		return this;
	}

	public List<Category> getCategory() {
		return category;
	}

	public Channel setCategory(List<Category> category) {
		this.category = category;
		return this;
	}

	public int getTtl() {
		return ttl;
	}

	public Channel setTtl(int ttl) {
		this.ttl = ttl;
		return this;
	}

	public String getLanguage() {
		return language;
	}

	public Channel setLanguage(String language) {
		this.language = language;
		return this;
	}

	public String getCopyright() {
		return copyright;
	}

	public Channel setCopyright(String copyright) {
		this.copyright = copyright;
		return this;
	}

	public String getWebMaster() {
		return webMaster;
	}

	public Channel setWebMaster(String webMaster) {
		this.webMaster = webMaster;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public Channel setDescription(String description) {
		this.description = description;
		return this;
	}

	public Image getImage() {
		return image;
	}

	public Channel setImage(final Image image) {
		this.image = image;
		return this;
	}

	@XmlElement(namespace = "http://www.w3.org/2005/Atom", name = "link")
	public AtomLink getAtomLink() {
		return atomLink;
	}

	public Channel setAtomLink(AtomLink atomLink) {
		this.atomLink = atomLink;
		return this;
	}

	@XmlElement(namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
	public Owner getOwner() {
		return owner;
	}

	public Channel setOwner(Owner owner) {
		this.owner = owner;
		return this;
	}

	@XmlElement(namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
	public String getAuthor() {
		return author;
	}

	public Channel setAuthor(String author) {
		this.author = author;
		return this;
	}

	@XmlElement(namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
	@XmlJavaTypeAdapter(ExplicityTypeAdapter.class)
	public Explicit getExplicit() {
		return explicit;
	}

	public Channel setExplicit(Explicit explicit) {
		this.explicit = explicit;
		return this;
	}

	@XmlElement(namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd", name = "image")
	public ItunesImage getItunesImage() {
		return itunesImage;
	}

	public Channel setItunesImage(ItunesImage itunesImage) {
		this.itunesImage = itunesImage;
		return this;
	}

	@XmlElement(namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd", name = "category")
	public ItunesCategory getItunesCategory() {
		return itunesCategory;
	}

	public Channel setItunesCategory(ItunesCategory itunesCategory) {
		this.itunesCategory = itunesCategory;
		return this;
	}

	@XmlElement(name = "item")
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	@XmlElement(namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd")
	@XmlJavaTypeAdapter(YesNoTypeAdapter.class)
	public YesNo getComplete() {
		return complete;
	}

	public Channel setComplete(final YesNo complete) {
		this.complete = complete;
		return this;
	}

	@XmlElement(namespace = "http://www.itunes.com/dtds/podcast-1.0.dtd", name = "new-feed-url")
	public URL getNewFeedUrl() {
		return newFeedUrl;
	}

	public Channel setNewFeedUrl(final URL newFeedUrl) {
		this.newFeedUrl = newFeedUrl;
		return this;
	}

	public String getManagingEditor() {
		return managingEditor;
	}

	public Channel setManagingEditor(final String managingEditor) {
		this.managingEditor = managingEditor;
		return this;
	}

	public String getGenerator() {
		return generator;
	}

	public Channel setGenerator(final String generator) {
		this.generator = generator;
		return this;
	}

	public URL getDocs() {
		return docs;
	}

	public Channel setDocs(final URL docs) {
		this.docs = docs;
		return this;
	}
}