package com.scrooge.exception;

public class InvalidUserEmailException extends RuntimeException{

    public InvalidUserEmailException(String message) {

        super(message);
    }
}
