package org.remdev.services.fileserver.models;

public class ResponseMessage {
    private String message;
    private int code;

    private ResponseMessage(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public static ResponseMessage error(String message) {
        return new ResponseMessage(message, 1);
    }

    public static ResponseMessage success(String message) {
        return new ResponseMessage(message, 0);
    }
}
