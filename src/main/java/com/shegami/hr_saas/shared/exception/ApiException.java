package com.shegami.hr_saas.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiException {

    private String Message;
    private HttpStatus httpStatus;
    private Date date;
}