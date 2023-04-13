package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.grpc.ServerService;
import pt.tecnico.distledger.server.serverExceptions.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

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

    public synchronized CreateOp createAccount(String userId, List<Integer> prevTS) throws
            ServerUnavailableException {
        
        if (isInactive()) { throw new ServerUnavailableException(); }

        incrementTS(replicaTS);

        CreateOp createOp = new CreateOp(userId, prevTS, this.replicaTS, false);

        ledger.add(createOp);

        return createOp;
    }
    
    public synchronized void checkCreateStability(Operation createOp) {

        String userId = createOp.getAccount();

        List<Integer> prevTS = createOp.getPrevTS();

        if (accounts.containsKey(userId)) {
            ledger.remove(createOp);
            return;
        }

        if (isLessOrEqual(prevTS, valueTS)) {
            incrementTS(valueTS);
            createOp.setStable(true);
            accounts.put(userId, 0);
        }

    }

    public synchronized int getBalanceById(String userId, List<Integer> prevTS) throws AccountDoesntExistException, ServerUnavailableException, UserIsAheadOfServerException {
        if (isInactive()) { throw new ServerUnavailableException(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { throw new AccountDoesntExistException(); }

        if (!isLessOrEqual(prevTS, valueTS)) { throw new UserIsAheadOfServerException(); }

        return balance;
    }

    public synchronized TransferOp transferTo(String userId, String destAccount, int amount, List<Integer> prevTS) throws
            ServerUnavailableException {

        if (isInactive()) { throw new ServerUnavailableException(); }

        incrementTS(replicaTS);

        TransferOp transferOp = new TransferOp(userId, destAccount, amount, prevTS, this.replicaTS, false);

        ledger.add(transferOp);

        return transferOp;
    }

    public synchronized void checkTransferStability(TransferOp transferOp) {
        String userId = transferOp.getAccount();
        String destAccount = transferOp.getDestAccount();
        int amount = transferOp.getAmount();
        List<Integer> prevTS = transferOp.getPrevTS();

        if (userId.equals(destAccount)) {
            ledger.remove(transferOp);
            return;
        }

        Integer senderBalance = accounts.get(userId);
        Integer receiverBalance = accounts.get(destAccount);

        if ((senderBalance == null) || (receiverBalance == null) || (amount <= 0) || (senderBalance < amount)) {
            ledger.remove(transferOp);
            return;
        }

        if (isLessOrEqual(prevTS, valueTS)) {
            incrementTS(valueTS);
            transferOp.setStable(true);
            accounts.put(userId, senderBalance - amount);
            accounts.put(destAccount, receiverBalance + amount);
        }
    }

    public void activateServer() { this.status = ACTIVE; }

    public void deactivateServer() { this.status = INACTIVE; }

    public List<Operation> getLedgerState() { return ledger; }

    public void gossip() {
        String destQualifier = "A";
        if (Objects.equals(this.qualifier, "A")) destQualifier = "B";
        serverService.propagateStateService(this.ledger, destQualifier, this.replicaTS);
    }

    public void applyGossip(DistLedgerCommonDefinitions.LedgerState otherServerState, List<Integer> otherReplicaTS) {
        // TODO - implement this!!
        System.out.println(otherServerState);
        System.out.println(otherReplicaTS);
    }

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