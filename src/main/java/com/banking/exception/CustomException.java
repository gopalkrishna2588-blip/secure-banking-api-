package com.banking.exception;

public class CustomException {

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message) { super(message); }
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) { super(message); }
    }

    public static class DuplicateAccountException extends RuntimeException {
        public DuplicateAccountException(String message) { super(message); }
    }

    public static class DuplicateTransactionException extends RuntimeException {
        private final Object existingResult;
        public DuplicateTransactionException(String msg, Object existing) {
            super(msg);
            this.existingResult = existing;
        }
        public Object getExistingResult() { return existingResult; }
    }

    public static class InvalidTransactionException extends RuntimeException {
        public InvalidTransactionException(String message) { super(message); }
    }

    public static class AccountSuspendedException extends RuntimeException {
        public AccountSuspendedException(String message) { super(message); }
    }
}