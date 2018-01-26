package org.semper.reformanda.syndication.util;

import org.semper.reformanda.syndication.rss.itunes.YesNo;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class YesNoTypeAdapter extends XmlAdapter<String, YesNo> {

    @Override
    public YesNo unmarshal(final String blockValueString) throws Exception {
        return YesNo.fromText(blockValueString);
    }

    @Override
    public String marshal(YesNo blockValue) throws Exception {
        // Since "yes" is the only value that has effect, don't marshal otherwise.
        return YesNo.YES == blockValue ? blockValue.toString() : null;
    }
}