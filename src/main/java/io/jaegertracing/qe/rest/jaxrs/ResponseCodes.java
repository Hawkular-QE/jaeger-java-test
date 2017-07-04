package io.jaegertracing.qe.rest.jaxrs;

public enum ResponseCodes {

    GET_SUCCESS_200(200),
    CREATE_SUCCESS_200(200),
    CREATE_SUCCESS_201(201),
    UPDATE_SUCCESS_204(204),
    UPDATE_SUCCESS_200(200),
    DELETE_SUCCESS_200(200),
    DELETE_SUCCESS_204(204),
    NO_CONTENT_204(204);

    private int code;

    ResponseCodes(int code) {
        this.code = code;
    }

    public int value() {
        return this.code;
    }

    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}
