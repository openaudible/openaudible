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

import org.custommonkey.xmlunit.XpathEngine;
import org.junit.Before;
import org.junit.Test;
import org.semper.reformanda.syndication.rss.Channel;
import org.semper.reformanda.syndication.rss.Rss;
import org.semper.reformanda.syndication.rss.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;

public class ItunesImageTest {

    private Rss rss;

    @Before
    public void setUp() {
        rss = new Rss();

        final Channel channel = new Channel();
        rss.setChannel(channel);
    }

    @Test
    public void shouldRenderItunesImageHrefAsAttribute() throws Exception {
        final String imageUrl = "http://www.example.com/images/logo.png";
        final ItunesImage itunesImage = new ItunesImage().setHref(imageUrl);
        rss.getChannel().setItunesImage(itunesImage);

        final Document document = XmlUtils.getDocument(rss);
        final XpathEngine engine = XmlUtils.getXpathEngine();

        final String subcategoryXpath = String.format("/rss/channel/itunes:image[@href='%s']", imageUrl);
        NodeList matchingNodes = engine.getMatchingNodes(subcategoryXpath, document);
        assertEquals("Could not find itunes image href attribute", 1, matchingNodes.getLength());
    }
}