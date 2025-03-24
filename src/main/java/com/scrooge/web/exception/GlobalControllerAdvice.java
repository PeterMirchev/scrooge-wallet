package com.scrooge.web.exception;

import com.scrooge.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public String handleResourceAlreadyExistException(ResourceAlreadyExistException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/new-wallet";
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public String handleEmailAlreadyExistException(EmailAlreadyExistException message, RedirectAttributes redirectAttributes) {
        System.out.println("Handler called for EmailAlreadyExistException: " + message.getMessage());
        redirectAttributes.addFlashAttribute("emailAlreadyExistException", message.getMessage());

        return "redirect:/register";
    }

    @ExceptionHandler(NotificationException.class)
    public String handleNotificationException(NotificationException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("notificationException", message.getMessage());

        return "redirect:/login";
    }

    @ExceptionHandler(InvalidUserEmailException.class)
    public String handleInvalidUserEmailException(InvalidUserEmailException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("invalidUserEmailException", message.getMessage());

        return "redirect:/wallets/transfer";
    }

    @ExceptionHandler({WalletAmountMustBeZeroException.class, InvalidInternalTransferAmountException.class})
    public String handleWalletAmountMustBeZero(RuntimeException message, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        Map<String, String> attribute = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/" + attribute.get("id");
    }

    @ExceptionHandler({
            InsufficientTransferAmountException.class,
            InvocationTargetException.class
    })
    public String handleInsufficientTransferAmountException(InsufficientTransferAmountException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("insufficientTransferAmountException", message.getMessage());

        return "redirect:/wallets/transfer";
    }

    @ExceptionHandler({
            ReceiverHasNoWalletException.class
    })
    public String handleReceiverHasNoWalletException(ReceiverHasNoWalletException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("receiverHasNoWalletException", message.getMessage());

        return "redirect:/wallets/transfer";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/new-wallet";
    }

    @ExceptionHandler(InsufficientAmountException.class)
    public String handleInsufficientAmountException(InsufficientAmountException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/transfer";
    }

    @ExceptionHandler(InternalTransactionException.class)
    public String handleInternalTransactionException(InternalTransactionException message, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", message.getMessage());

        return "redirect:/wallets/{id}";
    }

    @ExceptionHandler(NotificationServiceException.class)
    public String handleNotificationServiceException(RedirectAttributes redirectAttributes, NotificationServiceException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("notificationServiceException", message);
        return "redirect:/login";
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            MethodArgumentTypeMismatchException.class,
            MissingPathVariableException.class,
            MissingRequestCookieException.class,
            MissingRequestHeaderException.class,
            MissingRequestValueException.class,
            AuthorizationDeniedException.class
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
