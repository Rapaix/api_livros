package com.rapaix.api_livros.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GutendexException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(GutendexException.class);

    public GutendexException(String message) {
        super(message);
        logger.error(message);
    }

    public GutendexException(String message, Throwable cause) {
        super(message, cause);
        logger.error(message, cause);
    }
}