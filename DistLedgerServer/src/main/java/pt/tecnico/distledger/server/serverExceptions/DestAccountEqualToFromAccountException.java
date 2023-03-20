package pt.tecnico.distledger.server.serverExceptions;

public class DestAccountEqualToFromAccountException extends Exception {
    public DestAccountEqualToFromAccountException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Destination account is equal to from account";
    }
}
