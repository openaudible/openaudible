/**
 * @author : Paul Taylor
 * <p/>
 * Version @version:$Id: InvalidAudioFrameException.java 345 2007-08-07 16:14:03Z paultaylor $
 * Date :${DATE}
 * <p/>
 * Jaikoz Copyright Copyright (C) 2003 -2005 JThink Ltd
 */
package org.jaudiotagger.audio.exceptions;

/**
 * Thrown if portion of file thought to be an AudioFrame is found to not be.
 */
public class InvalidAudioFrameException extends Exception {
    public InvalidAudioFrameException(String message) {
        super(message);
    }
}
