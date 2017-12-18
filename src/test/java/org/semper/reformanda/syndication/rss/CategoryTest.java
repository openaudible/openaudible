package org.semper.reformanda.syndication.rss;

import org.custommonkey.xmlunit.XpathEngine;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CategoryTest {

    public static final String CATEGORY_TEXT = "Category 1";
    public static final String CATEGORY_TEXT_2 = "Category 2";
    public static final String CATEGORY_DOMAIN = "http://www.theTestPodcast.com/categories/test";
    private static final String rssString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<rss xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:itunes=\"http://www.itunes.com/dtds/podcast-1.0.dtd\" version=\"2.0\">\n" +
                    "    <channel>\n" +
                    "        <title>Test Podcast</title>\n" +
                    "        <link>http://www.theTestPodcast.com</link>\n" +
                    "        <description>This is a test block of text, meant to give a more verbose description of what the podcast is about.</description>\n" +
                    "        <category domain=\"" + CATEGORY_DOMAIN + "\">" + CATEGORY_TEXT + "</category>\n" +
                    "        <category domain=\"" + CATEGORY_DOMAIN + "\">" + CATEGORY_TEXT_2 + "</category>\n" +
                    "    </channel>" +
                    "</rss>";
    private Rss rss;

    @Before
    public void setUp() {
        rss = new Rss();

        final Channel channel = new Channel();
        rss.setChannel(channel);
    }

    @Test
    public void shouldCreateCategoryWithTextValue() throws Exception {
        final String categoryName = CATEGORY_TEXT;
        rss.getChannel().setCategory(Collections.singletonList(new Category().setTextValue(categoryName)));

        final Document document = XmlUtils.getDocument(rss);
        final XpathEngine engine = XmlUtils.getXpathEngine();

        NodeList matchingNodes = engine.getMatchingNodes(String.format("/rss/channel/category", categoryName), document);
        assertEquals("Could not category element", 1, matchingNodes.getLength());
        assertEquals("Category element had unexpected text", categoryName, matchingNodes.item(0).getTextContent());
    }

    @Test
    public void shouldCreeateCategoryWithDomainAsHtmlAtttribute() throws Exception {
        final String categoryName = CATEGORY_TEXT;
        final String categoryDomain = CATEGORY_DOMAIN;
        rss.getChannel().setCategory(Collections.singletonList(new Category()
                .setTextValue(categoryName)
                .setDomain(categoryDomain)));

        final Document document = XmlUtils.getDocument(rss);
        final XpathEngine engine = XmlUtils.getXpathEngine();

        NodeList matchingNodes = engine.getMatchingNodes(String.format("/rss/channel/category", categoryName), document);
        assertEquals("Could not category element", 1, matchingNodes.getLength());
        assertEquals("Category domain had unexpected or absent value", categoryDomain, matchingNodes.item(0).getAttributes().getNamedItem("domain").getTextContent());

    }

    @Test
    public void shouldUnmarshalCategoryText() throws Exception {
        final Rss rss = XmlUtils.unmarshalString(rssString, Rss.class);
        assertEquals(CATEGORY_TEXT_2, rss.getChannel().getCategory().get(1).getTextValue());
    }

    @Test
    public void shouldUnmarshalCategoryDomain() throws Exception {
        final Rss rss = XmlUtils.unmarshalString(rssString, Rss.class);
        assertEquals(CATEGORY_DOMAIN, rss.getChannel().getCategory().get(0).getDomain());
    }
}