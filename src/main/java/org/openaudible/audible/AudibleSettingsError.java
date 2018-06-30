package org.openaudible.audible;

public class AudibleSettingsError extends Exception {
    public AudibleSettingsError() {
        super("Software Verification must be turned off in Audible:Account:Settings");
    }
}
