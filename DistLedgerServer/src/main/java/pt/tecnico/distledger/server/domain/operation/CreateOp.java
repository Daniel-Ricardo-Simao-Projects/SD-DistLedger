package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class CreateOp extends Operation {

    public CreateOp(String account, List<Integer> prevTS,List<Integer> TS) {
        super(account, prevTS, TS);
    }

    @Override
    public void accept(OperationVisitor visitor) {
        visitor.visitCreateOp(this);
    }

}
