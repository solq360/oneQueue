package com.eyu.onequeue.exception;

/**
 * 
 * @author solq
 */
public class QSocketException extends QException {
 
    private static final long serialVersionUID = 1694427937902798536L;
    public QSocketException(short code, String message, Throwable cause) {
	super(code, message, cause);
    }
    public QSocketException(short code, String message) {
	super(code, message);
    }
    public QSocketException(short code) {
	super(code);
    }
}
