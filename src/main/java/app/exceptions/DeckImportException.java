package app.exceptions;

import java.io.Serial;

public class DeckImportException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DeckImportException(String message) {
        super(message);
    }
}
