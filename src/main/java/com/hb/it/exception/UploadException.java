package com.hb.it.exception;


/**
 * @author Mx 
 *	文件上传异常
 */
public class UploadException extends Exception {
	
	private static final long serialVersionUID = -798903326156063864L;

	public UploadException() {
        super();
    }

    public UploadException(String message) {
        super(message);
    }

    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadException(Throwable cause) {
        super(cause);
    }

    protected UploadException(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
