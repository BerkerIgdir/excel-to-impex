package business.classes.exceptions;

public class WrongPathTypeException extends RuntimeException {

    public static final String NOT_REGULAR_FILE = "The URL does not represent a regular file!";
    public static final String NOT_DIRECTORY_FILE = "A directory path must be provided!";
    public WrongPathTypeException(String explanation) {
        super(explanation);
    }
    public WrongPathTypeException(Throwable explanation) {
        super(explanation);
    }
}
