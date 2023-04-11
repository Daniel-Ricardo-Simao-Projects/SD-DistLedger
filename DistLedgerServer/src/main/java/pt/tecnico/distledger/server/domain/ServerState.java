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

    private final List<Integer> replicaTS;

    private final List<Integer> valueTS;

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
            ServerUnavailableException, CouldNotPropagateException {
        // TODO Remove
        /*if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }*/

        if (isInactive()) { throw new ServerUnavailableException(); }

        if (accounts.containsKey(userId)) { throw new AccountAlreadyExistsException(); }

        incrementTS(replicaTS);

        CreateOp createOp = new CreateOp(userId, prevTS, this.replicaTS);
        
        if (isLessOrEqual(prevTS, valueTS)) {
            incrementTS(valueTS);
        }
        // TODO Remove
        /* if(qualifier.equals("A") && !isPropagation) {
            if (!serverService.propagateStateService(createOp)) {
                throw new CouldNotPropagateException();
            }
        }*/


        accounts.put(userId, 0);
        ledger.add(createOp);
    }

    public synchronized int getBalanceById(String userId, List<Integer> prevTS) throws AccountDoesntExistException, ServerUnavailableException, UserIsAheadOfServerException {
        if (isInactive()) { throw new ServerUnavailableException(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { throw new AccountDoesntExistException(); }

        if (!isLessOrEqual(prevTS, valueTS)) { throw new UserIsAheadOfServerException(); }

        return balance;
    }

    public synchronized void transferTo(String userId, String destAccount, int amount, boolean isPropagation, List<Integer> prevTS) throws
            ServerUnavailableException, DestAccountEqualToFromAccountException, AccountDoesntExistException,
            DestAccountDoesntExistException, TransferBiggerThanBalanceException,
            CouldNotPropagateException, InvalidAmountException {
        // TODO Remove
        /*if(qualifier.equals("B") && !isPropagation) {
            throw new WriteNotSupportedException();
        }*/

        if (isInactive()) { throw new ServerUnavailableException(); }
        if (userId.equals(destAccount)) { throw new DestAccountEqualToFromAccountException(); }

        Integer senderBalance = accounts.get(userId);
        Integer receiverBalance = accounts.get(destAccount);

        if (senderBalance == null) { throw new AccountDoesntExistException(); }
        if (receiverBalance == null) { throw new DestAccountDoesntExistException(); }
        if (amount <= 0) { throw new InvalidAmountException(); }
        if (senderBalance < amount) { throw new TransferBiggerThanBalanceException(); }

        incrementTS(replicaTS);

        TransferOp transferOp = new TransferOp(userId, destAccount, amount, prevTS, this.replicaTS);

        if (isLessOrEqual(prevTS, valueTS)) {
            incrementTS(valueTS);
        }

        // TODO Remove
        /*if(qualifier.equals("A") && !isPropagation) {
            if(!serverService.propagateStateService(transferOp)) {
                throw new CouldNotPropagateException();
            }
        }*/

        accounts.put(userId, senderBalance - amount);
        accounts.put(destAccount, receiverBalance + amount);

        ledger.add(transferOp);
    }

    public void activateServer() { this.status = ACTIVE; }

    public void deactivateServer() { this.status = INACTIVE; }

    public List<Operation> getLedgerState() { return ledger; }

    public List<Integer> getReplicaTS() {
        return replicaTS;
    }

    public List<Integer> getValueTS() {
        return valueTS;
    }

    private boolean isInactive() { return status == INACTIVE; }

    private boolean isLessOrEqual(List<Integer> TS1, List<Integer> TS2) {
        for (int i = 0; i < TS1.size(); i++) {
            if (TS1.get(i) > TS2.get(i)) {
                return false;
            }
        }
        return true;
    }

    private void incrementTS(List<Integer> TS) {
        if (qualifier.charAt(0) == 'A') {
            TS.set(0, TS.get(0) + 1);
        } else {
            TS.set(1, TS.get(1) + 1);
        }
    }

    private boolean isBroker(String userId) { return "broker".equals(userId); }
}