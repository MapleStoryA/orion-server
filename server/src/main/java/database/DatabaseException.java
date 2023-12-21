package database;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = -420103154764822555L;

    public DatabaseException(String msg) {
        super(msg);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
