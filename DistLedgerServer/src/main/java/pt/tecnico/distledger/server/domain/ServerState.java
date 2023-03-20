package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.serverExceptions.*;

import java.util.*;

public class ServerState {

    private static final int ACTIVE = 1;

    private static final int INACTIVE = 0;

    private List<Operation> ledger;

    private Map<String, Integer> accounts;

    private int status;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.status = ACTIVE;
        this.accounts.put("broker", 1000);
    }

    public synchronized void createAccount(String userId) throws AccountAlreadyExistsException, ServerUnavailableException {
        if (isInactive()) { throw new ServerUnavailableException(); }

        if (accounts.containsKey(userId)) { throw new AccountAlreadyExistsException(); }

        accounts.put(userId, 0);
        CreateOp createOp = new CreateOp(userId);
        ledger.add(createOp);
    }

    public synchronized void deleteAccount(String userId) throws BalanceIsntZeroException, AccountDoesntExistException, CannotRemoveBrokerException, ServerUnavailableException {
        if (isInactive()) { throw new ServerUnavailableException(); }

        if (isBroker(userId)) { throw new CannotRemoveBrokerException(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { throw new AccountDoesntExistException(); }
        else if (balance != 0) { throw new BalanceIsntZeroException(); }
        else {
            accounts.remove(userId);
            DeleteOp deleteOp = new DeleteOp(userId);
            ledger.add(deleteOp);
        }
    }

    public synchronized int getBalanceById(String userId) throws AccountDoesntExistException, ServerUnavailableException {
        if (isInactive()) { throw new ServerUnavailableException(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { throw new AccountDoesntExistException(); }

        return balance;
    }

    public synchronized void transferTo(String userId, String destAccount, int amount) throws ServerUnavailableException, DestAccountEqualToFromAccountException, AccountDoesntExistException, DestAccountDoesntExistException, AmountIsZeroException, TransferBiggerThanBalanceException, NegativeBalanceException {
        if (isInactive()) { throw new ServerUnavailableException(); }
        if (userId.equals(destAccount)) { throw new DestAccountEqualToFromAccountException(); }

        Integer senderBalance = accounts.get(userId);
        Integer receiverBalance = accounts.get(destAccount);

        if (senderBalance == null) { throw new AccountDoesntExistException(); }
        if (receiverBalance == null) { throw new DestAccountDoesntExistException(); }
        if (amount < 0) { throw new NegativeBalanceException(); }
        if (amount == 0) { throw new AmountIsZeroException(); }
        if (senderBalance < amount) { throw new TransferBiggerThanBalanceException(); }

        accounts.put(userId, senderBalance - amount);
        accounts.put(destAccount, receiverBalance + amount);

        TransferOp transferOp = new TransferOp(userId, destAccount, amount);
        ledger.add(transferOp);
    }

    public void activateServer() { this.status = ACTIVE; }

    public void deactivateServer() { this.status = INACTIVE; }

    public List<Operation> getLedgerState() { return ledger; }

    private boolean isInactive() { return status == INACTIVE; }

    private boolean isBroker(String userId) { return "broker".equals(userId); }
}