package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import java.util.*;

public class ServerState {

    private static final int ACTIVE = 1;
    private static final int INACTIVE = 0;

    private List<Operation> ledger;

    private Set<UserAccount> accounts;

    private int status;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new LinkedHashSet<>();
        this.status = ACTIVE;
        this.accounts.add(new UserAccount("broker", 1000));
    }

    public synchronized int createAccount(String userId) {
        if(status == INACTIVE) { return -4; }

        for (UserAccount userAccount : accounts) {
            if (Objects.equals(userAccount.getUserId(), userId)) {
                return 1;
            }
        }
        UserAccount newUser = new UserAccount(userId);
        accounts.add(newUser);
        CreateOp createOp = new CreateOp(userId);
        ledger.add(createOp);
        return 0;
    }

    public synchronized int deleteAccount(String userId) {
        if(status == INACTIVE) { return -4; }
        if (userId.equals("broker")) {
            return -3;
        }
        for (UserAccount userAccount : accounts) {
            if (Objects.equals(userAccount.getUserId(), userId)) {
                if (userAccount.getBalance() == 0) {
                    accounts.remove(userAccount);
                    DeleteOp deleteOp = new DeleteOp(userId);
                    ledger.add(deleteOp);
                    return 0;
                }
                return -2;
            }
        }
        return -1;
    }

    public synchronized int transferTo(String fromAccount, String destAccount, int amount) {
        if(status == INACTIVE) { return -5; }

        UserAccount sender = getUserAccount(fromAccount);
        UserAccount receiver = getUserAccount(destAccount);

        if (sender == null)
            return -1;
        if (receiver == null)
            return -2;
        if (amount < 0)
            return -3;

        if (sender.getBalance() < amount)
            return -4;

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        TransferOp transferOp = new TransferOp(fromAccount, destAccount, amount);
        ledger.add(transferOp);

        return 0;

    }

    public synchronized int getBalanceById(String userId) {
        if(status == INACTIVE) { return -4; }

        for (UserAccount userData : accounts) {
            if (Objects.equals(userData.getUserId(), userId)) {
                return userData.getBalance();
            }
        }
        // If we didn't find a UserAccount object with the given userId, return a default value or throw an exception.
        return -1;
    }

    public synchronized UserAccount getUserAccount(String userId) {
        for (UserAccount userAccount : accounts) {
            if (Objects.equals(userAccount.getUserId(), userId)) {
                return userAccount;
            }
        }
        return null;
    }

    public void activateServer() {
        this.status = ACTIVE;
    }

    public void deactivateServer() {
        this.status = INACTIVE;
    }

    public List<Operation> getLedgerState() {
        return ledger;
    }
}
