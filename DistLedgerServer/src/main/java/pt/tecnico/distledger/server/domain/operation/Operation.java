package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class Operation {
    private String account;

    private final List<Integer> prevTS;

    private final List<Integer> TS;

    public Operation(String fromAccount, List<Integer> prevTS, List<Integer> TS) {
        this.account = fromAccount;
        this.prevTS = prevTS;
        this.TS = TS;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public List<Integer> getPrevTS() {
        return prevTS;
    }

    public List<Integer> getTS() {
        return TS;
    }

    public void accept(OperationVisitor visitor) {
        visitor.visitOperation(this);
    }
}
