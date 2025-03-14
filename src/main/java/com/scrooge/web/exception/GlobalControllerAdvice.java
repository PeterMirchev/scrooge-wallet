package com.scrooge.web.exception;

import com.scrooge.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public String resourceAlreadyExistException(ResourceAlreadyExistException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/new-wallet";
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public String EmailAlreadyExistException(EmailAlreadyExistException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("emailAlreadyExistException", message.getMessage());

        return "redirect:/register";
    }

    @ExceptionHandler(NotificationException.class)
    public String notificationException(NotificationException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("notificationException", message.getMessage());

        return "redirect:/login";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String resourceNotFoundException(ResourceNotFoundException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/new-wallet";
    }

    @ExceptionHandler(InsufficientAmountException.class)
    public String insufficientAmountException(InsufficientAmountException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/{id}";
    }

    @ExceptionHandler(InsufficientTransferAmountException.class)
    public ModelAndView insufficientTransferAmountException(InsufficientTransferAmountException message, RedirectAttributes redirectAttributes) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("errorMessage", message.getClass().getSimpleName());
        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            MethodArgumentTypeMismatchException.class,
            MissingPathVariableException.class,
            MissingRequestCookieException.class,
            MissingRequestHeaderException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundException() {

        return new ModelAndView("not-found");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("not-found");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}
