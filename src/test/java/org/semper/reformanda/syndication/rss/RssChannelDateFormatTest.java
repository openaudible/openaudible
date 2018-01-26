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

import org.custommonkey.xmlunit.XpathEngine;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Date;

public class RssChannelDateFormatTest {

    public final Long DATE_MILLINIUM = 946702800000L;
    private Rss rss;

    @Before
    public void setUp() {
        rss = new Rss();

        final Channel channel = new Channel();
        rss.setChannel(channel);
    }

    private Date getDate() {
        Date d = new Date(DATE_MILLINIUM);
        System.err.println(d.toString());

        return d;
    }

    @Test
    public void shouldRenderPubDateAsRfc822Format() throws Exception {


        rss.getChannel().setPubDate(getDate());

        final Document document = XmlUtils.getDocument(rss);
        final XpathEngine engine = XmlUtils.getXpathEngine();

        NodeList matchingNodes = engine.getMatchingNodes("/rss/channel/pubDate", document);
        String test = matchingNodes.item(0).getTextContent();
        //assertTrue(test.startsWith("Sat, 01 Jan 2000"));
    }

    @Test
    public void shouldRenderBuildDateAsRfc822Format() throws Exception {
        rss.getChannel().setLastBuildDate(getDate());


        final Document document = XmlUtils.getDocument(rss);
        final XpathEngine engine = XmlUtils.getXpathEngine();

        NodeList matchingNodes = engine.getMatchingNodes("/rss/channel/lastBuildDate", document);
        String test = matchingNodes.item(0).getTextContent();
        //assertTrue(test.startsWith("Sat, 01 Jan 2000"));
    }
}