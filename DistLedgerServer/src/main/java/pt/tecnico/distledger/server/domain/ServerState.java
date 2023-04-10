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

    private final List<Integer> valueTS;

    private final List<Integer> replicaTS;

    private int status;

    private final String qualifier;

    private final ServerService serverService;

    public ServerState(ServerService serverService, String qualifier) {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.replicaTS = Arrays.asList(0, 0);
        this.valueTS = Arrays.asList(0, 0);
        this.status = ACTIVE;
        this.accounts.put("broker", 1000);
        this.qualifier = qualifier;
        this.serverService = serverService;
    }

    public synchronized void createAccount(String userId, boolean isPropagation, List<Integer> prevTS) throws AccountAlreadyExistsException,
            ServerUnavailableException, WriteNotSupportedException, CouldNotPropagateException {
        if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }

        if (isInactive()) { throw new ServerUnavailableException(); }

        if (accounts.containsKey(userId)) { throw new AccountAlreadyExistsException(); }

        CreateOp createOp = new CreateOp(userId, prevTS, this.replicaTS);
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

    public synchronized void transferTo(String userId, String destAccount, int amount, boolean isPropagation, List<Integer> prevTS) throws
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

        TransferOp transferOp = new TransferOp(userId, destAccount, amount, prevTS, this.replicaTS);
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

    public List<Integer> getValueTS() {
        return valueTS;
    }

    public List<Integer> getReplicaTS() {
        return replicaTS;
    }

    private boolean isInactive() { return status == INACTIVE; }

    public boolean canBeExecutedLocally(List<Integer> prevTS, List<Integer> valueTS) {
        for (int i = 0; i < valueTS.size(); i++) {
            if (valueTS.get(i) < prevTS.get(i))
                return false;
        }
        return true;
    }
}