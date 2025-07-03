package tn.gti.E_Transfert.dto.error;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
public class ErrorResponse {
    String errorCode;
    String message;
    Map<String, String> details;
    LocalDateTime timestamp;
}