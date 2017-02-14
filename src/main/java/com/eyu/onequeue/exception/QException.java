package com.eyu.onequeue.exception;

/**
 * 
 * @author solq
 */
public class QException extends RuntimeException {

    private static final long serialVersionUID = -3110633035340065406L;

    private short code;

    public short getCode() {
	return code;
    }

    public QException(short code, String message, Throwable cause) {
	super(message, cause);
	this.code = code;
    }

    public QException(short code) {
	super();
	this.code = code;
    }

    public QException(short code, String message) {
	super(message);
	this.code = code;
    }

}
