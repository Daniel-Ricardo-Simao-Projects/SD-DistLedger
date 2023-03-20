package pt.tecnico.distledger.server.serverExceptions;

public class TransferBiggerThanBalanceException extends Exception {
    public TransferBiggerThanBalanceException() {
        super();
    }

    @Override
    public String getMessage() {
        return "Balance lower than amount to send";
    }
}
