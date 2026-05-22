package com.notfound.bookstorenotificationservice.client;

public class UserServiceApiResponse<T> {

    public static final int SUCCESS_CODE = 1000;
    public static final int OK_CODE = 200;

    private Integer code;
    private String message;
    private T result;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return code != null
                && result != null
                && (code == SUCCESS_CODE || code == OK_CODE);
    }
}
