package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerState {
    private List<Operation> ledger;

    private Set<UserAccount> accounts = new HashSet<UserAccount>();

    public ServerState() {
        this.ledger = new ArrayList<>();
    }

    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */
    public void AddCreateOp(CreateOp createOp) {
        ledger.add(createOp);
    }

    public void deleteAccount(String userId) {
        for (UserAccount userAccount : accounts) {
            if (userAccount.getUserId() == userId) {
                accounts.remove(userAccount);
                DeleteOp deleteOp = new DeleteOp(userId);
                ledger.add(deleteOp);
            }
        }
        // Handle exceptions
    }

    public int getBalanceById(String userId) {
        for (UserAccount userAccount : accounts) {
            if (userAccount.getUserId() == userId) {
                return userAccount.getBalance();
            }
        }
        // If we didn't find a UserAccount object with the given userId, return a default value or throw an exception.
        return 0;
    }
}
