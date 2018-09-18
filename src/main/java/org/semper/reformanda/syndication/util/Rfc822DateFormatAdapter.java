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

package org.semper.reformanda.syndication.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Rfc822DateFormatAdapter extends XmlAdapter<String, Date> {
	
	public static final String RFC_822_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
	
	@Override
	public Date unmarshal(String dateString) throws Exception {
		return new SimpleDateFormat(RFC_822_DATE_FORMAT).parse(dateString);
	}
	
	@Override
	public String marshal(Date date) throws Exception {
		return new SimpleDateFormat(RFC_822_DATE_FORMAT).format(date);
	}
}