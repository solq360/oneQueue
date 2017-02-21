package com.eyu.onequeue.exception;

/**
 * 
 * @author solq
 */
public class QEnhanceException extends QException {

    private static final long serialVersionUID = 236549335634949047L;

    public QEnhanceException(short code, String message, Throwable cause) {
	super(code, message, cause);
    }

    public QEnhanceException(short code, String message) {
	super(code, message);
    }

    public QEnhanceException(short code) {
	super(code);
    }
}
