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

    private Map<String, Integer> accounts;

    private int status;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.status = ACTIVE;
        this.accounts.put("broker", 1000);
    }

    public synchronized int createAccount(String userId) {
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        if (accounts.containsKey(userId)) { return ErrorCode.ACCOUNT_ALREADY_EXISTS.getCode(); }

        accounts.put(userId, 0);
        CreateOp createOp = new CreateOp(userId);
        ledger.add(createOp);

        return 0;
    }

    public synchronized int deleteAccount(String userId) {
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        if (isBroker(userId)) { return ErrorCode.CANNOT_REMOVE_BROKER.getCode(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { return ErrorCode.ACCOUNT_DOESNT_EXIST.getCode(); }
        else if (balance != 0) { return ErrorCode.BALANCE_ISNT_ZERO.getCode(); }
        else {
            accounts.remove(userId);
            DeleteOp deleteOp = new DeleteOp(userId);
            ledger.add(deleteOp);
            return 0;
        }
    }

    public synchronized int getBalanceById(String userId) {
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        Integer balance = accounts.get(userId);

        if (balance == null) { return ErrorCode.ACCOUNT_DOESNT_EXIST.getCode(); }

        return balance;
    }

    public synchronized int transferTo(String userId, String destAccount, int amount) {
        if(isInactive()) { return ErrorCode.SERVER_UNAVAILABLE.getCode(); }

        Integer senderBalance = accounts.get(userId);
        Integer receiverBalance = accounts.get(destAccount);

        if (senderBalance == null) { return ErrorCode.ACCOUNT_DOESNT_EXIST.getCode(); }
        if (receiverBalance == null) { return ErrorCode.DEST_ACCOUNT_DOESNT_EXIST.getCode(); }
        if (amount < 0) { return ErrorCode.NEGATIVE_BALANCE.getCode(); }
        if (senderBalance < amount) { return ErrorCode.TRANSFER_BIGGER_THAN_BALANCE.getCode(); }

        accounts.put(userId, senderBalance - amount);
        accounts.put(destAccount, receiverBalance + amount);

        TransferOp transferOp = new TransferOp(userId, destAccount, amount);
        ledger.add(transferOp);

        return 0;
    }

    public void activateServer() { this.status = ACTIVE; }

    public void deactivateServer() { this.status = INACTIVE; }

    public List<Operation> getLedgerState() { return ledger; }

    private boolean isInactive() { return status == INACTIVE; }

    private boolean isBroker(String userId) { return "broker".equals(userId); }
}