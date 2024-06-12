package ru.almasgali.filesync_server.exceptions.storage;

public class OldVersionException extends StorageException {
    public OldVersionException(String message) {
        super(message);
    }

    public OldVersionException(String message, Throwable cause) {
        super(message, cause);
    }
}
