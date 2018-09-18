package org.openaudible.audible;

/**
 * Created  6/26/2017.
 */
public class AudibleLoginError extends Exception {
	
	public AudibleLoginError() {
		super();
	}
	
	public AudibleLoginError(String why) {
		super(why);
	}
}
