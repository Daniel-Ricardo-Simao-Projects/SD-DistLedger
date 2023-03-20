package pt.tecnico.distledger.server.serverExceptions;

public class DestAccountDoesntExistException extends Exception {
    public DestAccountDoesntExistException() {
        super();
    }

    @Override
    public String getMessage() {
        return "AccountTo not found";
    }
}
