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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

@XmlRootElement
public class Rss {
	
	private Channel channel;
	
	public static Optional<Rss> fromXmlStream(final InputStream inputStream) {
		if (Objects.isNull(inputStream)) {
			return Optional.empty();
		}
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Rss.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			return Optional.of((Rss) jaxbUnmarshaller.unmarshal(inputStream));
		} catch (JAXBException e) {
			// TODO not sure I want to put logging in this library - perhaps slf4j or commons if I need to...
		}
		
		return Optional.empty();
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	@XmlElement
	public Rss setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}
	
	@XmlAttribute
	public String getVersion() {
		return "2.0";
	}
}