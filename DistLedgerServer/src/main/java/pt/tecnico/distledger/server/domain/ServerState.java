package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.errors.ErrorCode;

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
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        UserAccount account = getUserAccount(userId);

        if (account != null) { return ErrorCode.ACCOUNT_ALREADY_EXISTS.getCode(); }

        UserAccount newUser = new UserAccount(userId);
        accounts.add(newUser);
        CreateOp createOp = new CreateOp(userId);
        ledger.add(createOp);

        return 0;
    }

    public synchronized int deleteAccount(String userId) {
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        if (isBroker(userId)) { return ErrorCode.CANNOT_REMOVE_BROKER.getCode(); }

        UserAccount account = getUserAccount(userId);

        if (account == null) { return ErrorCode.ACCOUNT_DOESNT_EXIST.getCode(); }
        else if (account.getBalance() != 0) { return ErrorCode.BALANCE_ISNT_ZERO.getCode(); }
        else {
            accounts.remove(account);
            DeleteOp deleteOp = new DeleteOp(userId);
            ledger.add(deleteOp);
            return 0;
        }
    }

    public synchronized int getBalanceById(String userId) {
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        UserAccount account = getUserAccount(userId);

        if (account == null) { return ErrorCode.ACCOUNT_DOESNT_EXIST.getCode(); }

        return account.getBalance();
    }

    public synchronized int transferTo(String userId, String destAccount, int amount) {
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        UserAccount sender = getUserAccount(userId);
        UserAccount receiver = getUserAccount(destAccount);

        if (sender == null) { return ErrorCode.ACCOUNT_DOESNT_EXIST.getCode(); }
        if (receiver == null) { return ErrorCode.DEST_ACCOUNT_DOESNT_EXIST.getCode(); }
        if (amount < 0) { return ErrorCode.NEGATIVE_BALANCE.getCode(); }
        if (sender.getBalance() < amount) { return ErrorCode.TRANSFER_BIGGER_THAN_BALANCE.getCode(); }

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        TransferOp transferOp = new TransferOp(userId, destAccount, amount);
        ledger.add(transferOp);

        return 0;
    }

    public void activateServer() { this.status = ACTIVE; }

    public void deactivateServer() { this.status = INACTIVE; }

    public List<Operation> getLedgerState() { return ledger; }

    public synchronized UserAccount getUserAccount(String userId) {
        for (UserAccount userAccount : accounts) {
            if (Objects.equals(userAccount.getUserId(), userId)) {
                return userAccount;
            }
        }
        return null;
    }

    private boolean isInactive() { return status == INACTIVE; }

    private boolean isBroker(String userId) { return Objects.equals(userId, "broker"); }
}