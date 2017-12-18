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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Collections;

public class XmlUtils {
    private static final Logger log = LogManager.getLogger(XmlUtils.class);

    public static XpathEngine getXpathEngine() {
        final NamespaceContext context = new SimpleNamespaceContext(Collections.singletonMap("itunes", "http://www.itunes.com/dtds/podcast-1.0.dtd"));
        final XpathEngine engine = XMLUnit.newXpathEngine();
        engine.setNamespaceContext(context);
        return engine;
    }

    public static Document getDocument(final Object objectToMarshal) throws JAXBException, ParserConfigurationException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Rss.class);
        final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        jaxbMarshaller.marshal(objectToMarshal, document);

        return document;
    }

    public static <T> T unmarshalString(final String string, final Class<T> clazz) throws JAXBException {
        return unmarshalObject(new ByteArrayInputStream(string.getBytes()), clazz);
    }

    public static <T> T unmarshalObject(final InputStream input, final Class<T> clazz) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
        return (T) unmarshaller.unmarshal(input);
    }

    public static void printDocument(final Document doc) throws IOException, TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(outputStream, "UTF-8")));
        log.debug(outputStream);
    }
}