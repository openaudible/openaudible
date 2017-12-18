package org.semper.reformanda.syndication.util;

import org.semper.reformanda.syndication.rss.itunes.Explicit;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ExplicityTypeAdapter extends XmlAdapter<String, Explicit> {

    @Override
    public Explicit unmarshal(final String explicitTextString) throws Exception {
        return Explicit.fromText(explicitTextString);
    }

    @Override
    public String marshal(final Explicit explicit) throws Exception {
        return explicit.toString();
    }
}