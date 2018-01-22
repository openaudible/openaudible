package org.openaudible;

import java.security.InvalidParameterException;

public enum AudibleRegion {
    US, UK;
    public String getBaseURL()
    {
        switch(this)
        {
            case US:
                return "https://www.audible.com";

            case UK:
                return "https://www.audible.co.uk";
            default:
                throw new InvalidParameterException("Invalid region:"+this);
        }
    }

    public static AudibleRegion fromText(String text) {
        for (AudibleRegion r:values())
        {
            if (text.equalsIgnoreCase(r.name()))
                return r;
            if (text.equalsIgnoreCase(r.displayName()))
                return r;
        }

        assert(false);
        return US;
    }

    public String displayName() {
            switch(this)
            {
                case US:
                    return "www.audible.com (US)";

                case UK:
                    return "www.audible.co.uk (UK)";
                default:
                    throw new InvalidParameterException("Invalid region:"+this);
            }


    }
}
