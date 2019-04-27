package db;

public class RegisterException extends Exception {
    public enum ErrorCode {
        noError,
        registerCategoryNotFound,
        registerNumberExceeded,
        patientNotExist,
        sqlException,
        retryTimeExceeded,
    }

    public ErrorCode error;

    RegisterException(String reason, ErrorCode err) {
        super(reason);
        error = err;
    }
}
