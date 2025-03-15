package com.scrooge.exception;

public class WalletAmountMustBeZeroException extends RuntimeException {

    public WalletAmountMustBeZeroException(String message) {

        super(message);
    }
}