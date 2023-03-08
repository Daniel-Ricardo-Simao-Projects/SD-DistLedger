package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;

import java.lang.invoke.LambdaMetafactory;
import java.util.*;

public class ServerState {
    private List<Operation> ledger;

    private Set<UserAccount> accounts;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new LinkedHashSet<>();
        this.accounts.add(new UserAccount("broker", 1000));
    }

    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */
    public int createAccount(String userId) {
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

    public int deleteAccount(String userId) {
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

    public int transferTo(String fromAccount, String destAccount, int amount) {

        UserAccount sender = getUserAccount(fromAccount);
        UserAccount receiver = getUserAccount(destAccount);

        if (sender == null)
            return 1;
        if (receiver == null)
            return 2;

        if (sender.getBalance() < amount)
            return 3;

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        return 0;

    }

    public int getBalanceById(String userId) {
        for (UserAccount userData : accounts) {
            if (Objects.equals(userData.getUserId(), userId)) {
                return userData.getBalance();
            }
        }
        // If we didn't find a UserAccount object with the given userId, return a default value or throw an exception.
        return -1;
    }

    public UserAccount getUserAccount(String userId) {
        for (UserAccount userAccount : accounts) {
            if (Objects.equals(userAccount.getUserId(), userId)) {
                return userAccount;
            }
        }
        return null;
    }
}
