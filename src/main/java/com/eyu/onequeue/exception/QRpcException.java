package com.eyu.onequeue.exception;

/**
 * 
 * @author solq
 */
public class QRpcException extends QEnhanceException {

    private static final long serialVersionUID = 236549335634949047L;

    public QRpcException(short code, String message, Throwable cause) {
	super(code, message, cause);
    }

    public QRpcException(short code, String message) {
	super(code, message);
    }

    public QRpcException(short code) {
	super(code);
    }
}
