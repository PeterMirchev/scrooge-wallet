package com.scrooge.exception;

public class WalletAmountMustBeZero extends RuntimeException {

    public WalletAmountMustBeZero(String message) {

        super(message);
    }
}