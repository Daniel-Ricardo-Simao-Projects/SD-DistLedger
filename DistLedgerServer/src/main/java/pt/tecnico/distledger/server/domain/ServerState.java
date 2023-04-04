package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.grpc.ServerService;
import pt.tecnico.distledger.server.serverExceptions.*;

import java.util.*;

public class ServerState {

    private static final int ACTIVE = 1;

    private static final int INACTIVE = 0;

    private final List<Operation> ledger;

    private final Map<String, Integer> accounts;

    private final Map<String, Integer> TS;

    private int status;

    private final String qualifier;

    private final ServerService serverService;

    public ServerState(ServerService serverService, String qualifier) {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.TS = new HashMap<>();
        this.status = ACTIVE;
        this.accounts.put("broker", 1000);
        this.qualifier = qualifier;
        this.serverService = serverService;
    }

    public synchronized void createAccount(String userId, boolean isPropagation) throws AccountAlreadyExistsException,
            ServerUnavailableException, WriteNotSupportedException, CouldNotPropagateException {
        if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }

        if (isInactive()) { throw new ServerUnavailableException(); }

        if (accounts.containsKey(userId)) { throw new AccountAlreadyExistsException(); }

        CreateOp createOp = new CreateOp(userId);
        if(qualifier.equals("A") && !isPropagation) {
            if (!serverService.propagateStateService(createOp)) {
                throw new CouldNotPropagateException();
            }
        }

        accounts.put(userId, 0);
        ledger.add(createOp);
    }

    public synchronized int getBalanceById(String userId) throws AccountDoesntExistException, ServerUnavailableException {
        if (isInactive()) { throw new ServerUnavailableException(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { throw new AccountDoesntExistException(); }

        return balance;
    }

    public synchronized void transferTo(String userId, String destAccount, int amount, boolean isPropagation) throws
            ServerUnavailableException, DestAccountEqualToFromAccountException, AccountDoesntExistException,
            DestAccountDoesntExistException, TransferBiggerThanBalanceException, WriteNotSupportedException,
            CouldNotPropagateException, InvalidAmountException {
        if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }

        if (isInactive()) { throw new ServerUnavailableException(); }
        if (userId.equals(destAccount)) { throw new DestAccountEqualToFromAccountException(); }

        Integer senderBalance = accounts.get(userId);
        Integer receiverBalance = accounts.get(destAccount);

        if (senderBalance == null) { throw new AccountDoesntExistException(); }
        if (receiverBalance == null) { throw new DestAccountDoesntExistException(); }
        if (amount <= 0) { throw new InvalidAmountException(); }
        if (senderBalance < amount) { throw new TransferBiggerThanBalanceException(); }

        TransferOp transferOp = new TransferOp(userId, destAccount, amount);
        if(qualifier.equals("A") && !isPropagation) {
            if(!serverService.propagateStateService(transferOp)) {
                throw new CouldNotPropagateException();
            }
        }

        accounts.put(userId, senderBalance - amount);
        accounts.put(destAccount, receiverBalance + amount);

        ledger.add(transferOp);
    }

    public void activateServer() { this.status = ACTIVE; }

    public void deactivateServer() { this.status = INACTIVE; }

    public List<Operation> getLedgerState() { return ledger; }

    public Collection<Integer> getTS() { return TS.values(); }

    private boolean isInactive() { return status == INACTIVE; }

    private boolean isBroker(String userId) { return "broker".equals(userId); }

}