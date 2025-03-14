package com.scrooge.exception;

public class InsufficientTransferAmountException extends RuntimeException {

    public InsufficientTransferAmountException(String message) {

        super(message);
    }
}