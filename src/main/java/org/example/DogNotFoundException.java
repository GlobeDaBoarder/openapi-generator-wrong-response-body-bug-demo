package org.example;

public class DogNotFoundException extends RuntimeException {
    public DogNotFoundException(String s) {
        super(s);
    }
}
