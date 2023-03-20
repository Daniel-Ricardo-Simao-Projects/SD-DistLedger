package pt.tecnico.distledger.server.serverExceptions;

public class AccountAlreadyExistsException extends Exception {

    public AccountAlreadyExistsException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Username already taken";
    }
}
