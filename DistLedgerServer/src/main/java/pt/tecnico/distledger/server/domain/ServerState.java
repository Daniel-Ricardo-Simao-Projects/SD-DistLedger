package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.*;

public class ServerState {
    private List<Operation> ledger;

    private Set<UserAccount> accounts;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new LinkedHashSet<>();
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

    public void deleteAccount(String userId) {
        for (UserAccount userAccount : accounts) {
            if (Objects.equals(userAccount.getUserId(), userId)) {
                accounts.remove(userAccount);
                DeleteOp deleteOp = new DeleteOp(userId);
                ledger.add(deleteOp);
            }
        }
        // Handle exceptions
    }

    public int getBalanceById(String userId) {
        for (UserAccount userData : accounts) {
            if (Objects.equals(userData.getUserId(), userId)) {
                return userData.getBalance();
            }
        }
        // If we didn't find a UserAccount object with the given userId, return a default value or throw an exception.
        return 0;
    }
}
