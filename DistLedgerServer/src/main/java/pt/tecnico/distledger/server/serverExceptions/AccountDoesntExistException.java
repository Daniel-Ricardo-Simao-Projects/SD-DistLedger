package pt.tecnico.distledger.server.serverExceptions;

public class AccountDoesntExistException extends Exception {
    public AccountDoesntExistException() {
        super();
    }

    @Override
    public String getMessage() {
        return "User not found";
    }
}
