package com.eyu.onequeue.exception;

/**
 * 
 * @author solq
 */
public class QJsonException extends QException {

    private static final long serialVersionUID = 2694896782105939920L;

    public QJsonException(short code, String message, Throwable cause) {
	super(code, message, cause);
    }
    public QJsonException(short code, String message) {
	super(code, message);
    }
    public QJsonException(short code) {
	super(code);
    }
}
