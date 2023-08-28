package com.kdsoftware.mancalarealtime.DTOs;

import lombok.Data;

@Data
public class ResponseObject<T>
{
    private T Result;
    private String message;
}
