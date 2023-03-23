package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.grpc.ServerService;
import pt.tecnico.distledger.server.serverExceptions.*;

import java.util.*;

public class ServerState {

    private static final int ACTIVE = 1;

    private static final int INACTIVE = 0;

    private List<Operation> ledger;

    private Map<String, Integer> accounts;

    private int status;

    private String qualifier;

    private ServerService serverService;

    public ServerState(ServerService serverService, String qualifier) {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.status = ACTIVE;
        this.accounts.put("broker", 1000);
        this.qualifier = qualifier;
        this.serverService = serverService;
    }

    public synchronized void createAccount(String userId, boolean isPropagation) throws AccountAlreadyExistsException, ServerUnavailableException, WriteNotSupportedException {
        if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }

        if (isInactive()) { throw new ServerUnavailableException(); }

        if (accounts.containsKey(userId)) { throw new AccountAlreadyExistsException(); }

        CreateOp createOp = new CreateOp(userId);
        if(qualifier.equals("A") && !isPropagation) {
            if (!serverService.propagateStateService(createOp)) {
                throw new ServerUnavailableException();
            }
        }

        accounts.put(userId, 0);
        ledger.add(createOp);
    }

    public synchronized void deleteAccount(String userId, boolean isPropagation) throws BalanceIsntZeroException, AccountDoesntExistException, CannotRemoveBrokerException, ServerUnavailableException, WriteNotSupportedException {
        if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }

        if (isInactive()) { throw new ServerUnavailableException(); }

        if (isBroker(userId)) { throw new CannotRemoveBrokerException(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { throw new AccountDoesntExistException(); }
        else if (balance != 0) { throw new BalanceIsntZeroException(); }
        else {
            DeleteOp deleteOp = new DeleteOp(userId);
            if(qualifier.equals("A") && !isPropagation) {
                if(!serverService.propagateStateService(deleteOp)) {
                    throw new ServerUnavailableException();
                }
            }

            accounts.remove(userId);
            ledger.add(deleteOp);
        }
    }

    public synchronized int getBalanceById(String userId) throws AccountDoesntExistException, ServerUnavailableException {
        if (isInactive()) { throw new ServerUnavailableException(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { throw new AccountDoesntExistException(); }

        return balance;
    }

    public synchronized void transferTo(String userId, String destAccount, int amount, boolean isPropagation) throws ServerUnavailableException, DestAccountEqualToFromAccountException, AccountDoesntExistException, DestAccountDoesntExistException, AmountIsZeroException, TransferBiggerThanBalanceException, NegativeBalanceException, WriteNotSupportedException {
        if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }

        if (isInactive()) { throw new ServerUnavailableException(); }
        if (userId.equals(destAccount)) { throw new DestAccountEqualToFromAccountException(); }

        Integer senderBalance = accounts.get(userId);
        Integer receiverBalance = accounts.get(destAccount);

        if (senderBalance == null) { throw new AccountDoesntExistException(); }
        if (receiverBalance == null) { throw new DestAccountDoesntExistException(); }
        if (amount < 0) { throw new NegativeBalanceException(); }
        if (amount == 0) { throw new AmountIsZeroException(); }
        if (senderBalance < amount) { throw new TransferBiggerThanBalanceException(); }

        TransferOp transferOp = new TransferOp(userId, destAccount, amount);
        if(qualifier.equals("A") && !isPropagation) {
            if(!serverService.propagateStateService(transferOp)) {
                throw new ServerUnavailableException();
            }
        }

        accounts.put(userId, senderBalance - amount);
        accounts.put(destAccount, receiverBalance + amount);

        ledger.add(transferOp);
    }

    public void activateServer() { this.status = ACTIVE; }

    public void deactivateServer() { this.status = INACTIVE; }

    public List<Operation> getLedgerState() { return ledger; }

    private boolean isInactive() { return status == INACTIVE; }

    private boolean isBroker(String userId) { return "broker".equals(userId); }
}