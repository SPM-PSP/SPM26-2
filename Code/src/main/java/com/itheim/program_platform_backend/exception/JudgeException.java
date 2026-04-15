package com.itheim.program_platform_backend.exception;

public class JudgeException extends RuntimeException {
    public JudgeException(String message) {
        super(message);
    }

    public JudgeException(String message, Throwable cause) {
        super(message, cause);
    }
}