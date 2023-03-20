package pt.tecnico.distledger.server.serverExceptions;

public class NegativeBalanceException extends Exception {
    public NegativeBalanceException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Amount has to be greater than zero";
    }
}
