package com.scrooge.web.exception;

import com.scrooge.exception.ResourceAlreadyExistException;
import com.scrooge.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public String resourceAlreadyExistException(ResourceAlreadyExistException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());


        return "redirect:/wallets/create";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String resourceNotFoundException(ResourceNotFoundException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());


        return "redirect:/wallets/create";
    }

}
