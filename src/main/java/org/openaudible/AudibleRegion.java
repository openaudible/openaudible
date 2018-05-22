package org.openaudible;

import java.security.InvalidParameterException;

public enum AudibleRegion {
    US, UK, DE, FR, AU, IT, JP, CA;

    // audible.de, audible.fr, audible.com.au, audible.it, audible.jp, audible.ca

    public String getBaseURL() {
        return "https://"+this.getBaseDomain();
    }

    public String getBaseDomain() {
        switch (this) {
            case US:
                return "www.audible.com";
            case UK:
                return "audible.co.uk";
            case DE:
                return "audible.de";
            case FR:
                return "audible.fr";
            case AU:
                return "audible.com.au";
            case IT:
                return "audible.it";
            case JP:
                return "audible.co.jp";
            case CA:
                return "audible.ca";
            default:
                throw new InvalidParameterException("Invalid region:" + this);
        }
    }

    public static AudibleRegion fromText(String text) {
        for (AudibleRegion r : values()) {
            if (text.equalsIgnoreCase(r.name()))
                return r;
            if (text.equalsIgnoreCase(r.displayName()))
                return r;
        }

        assert (false);
        return US;
    }

    public String displayName() {
        return getBaseDomain()+" ("+this.name().toUpperCase()+")";
    }
}
