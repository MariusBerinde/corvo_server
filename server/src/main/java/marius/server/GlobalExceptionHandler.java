package marius.server;

import jakarta.servlet.http.HttpServletRequest;
import marius.AppExceptions;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Richiesta malformata IP={}", request.getRemoteAddr());
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(AppExceptions.UserNotAuthException.class)
    public ResponseEntity<String> userNotAuthExceptionHandler(AppExceptions.UserNotAuthException ex) {
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(AppExceptions.UserNotFoundException.class)
    public ResponseEntity<String> userNotFoundExceptionHandler(AppExceptions.UserNotFoundException ex) {
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(AppExceptions.UserSamePasswordExcemptio.class)
    public ResponseEntity<String> userSamePasswordHandler(AppExceptions.UserSamePasswordExcemptio ex){
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
