package pt.tecnico.distledger.server.serverExceptions;

public class AmountIsZeroException extends Exception {
    public AmountIsZeroException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Amount has to be greater than zero";
    }
}
