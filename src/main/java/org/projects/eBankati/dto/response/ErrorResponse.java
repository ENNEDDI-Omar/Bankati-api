package org.projects.eBankati.dto.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {
    private HttpStatus status;
    private String message;
}
