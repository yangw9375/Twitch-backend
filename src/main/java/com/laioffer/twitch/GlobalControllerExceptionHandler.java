package com.laioffer.twitch;


import com.laioffer.twitch.model.TwitchErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;


@ControllerAdvice
public class GlobalControllerExceptionHandler {


    Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    //每个被标记@ExceptionHandler都会处理一下看看 是不是这种情况
    //generic exception
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<TwitchErrorResponse> handleDefaultException(Exception e) {
        logger.error("", e);
        return new ResponseEntity<>(
                new TwitchErrorResponse("Something went wrong, please try again later.",
                        e.getClass().getName(),
                        e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    //specific exception
    @ExceptionHandler(ResponseStatusException.class)
    public final ResponseEntity<TwitchErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        logger.error("", e.getCause());
        return new ResponseEntity<>(
                new TwitchErrorResponse(e.getReason(), e.getCause().getClass().getName(), e.getCause().getMessage()),
                e.getStatusCode()
        );
    }
}

