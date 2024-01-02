package org.example;

import org.example.generated.dto.ErrorDto;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DogNotFoundException.class)
    public ErrorDto handleDogNotFoundException(DogNotFoundException e) {
        return new ErrorDto().code(404).message(e.getMessage());
    }
}
