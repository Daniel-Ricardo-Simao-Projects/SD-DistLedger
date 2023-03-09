package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.operation.DistLedgerOperationVisitor;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

import java.util.ArrayList;
import java.util.List;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private ServerState serverState;

    public AdminServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public void activate(AdminDistLedger.ActivateRequest request, StreamObserver<AdminDistLedger.ActivateResponse> responseObserver) {

        serverState.activateServer();

        AdminDistLedger.ActivateResponse response = AdminDistLedger.ActivateResponse.newBuilder().build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    @Override
    public void deactivate(AdminDistLedger.DeactivateRequest request, StreamObserver<AdminDistLedger.DeactivateResponse> responseObserver) {

        serverState.deactivateServer();

        AdminDistLedger.DeactivateResponse response = AdminDistLedger.DeactivateResponse.newBuilder().build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    @Override
    public void getLedgerState(AdminDistLedger.getLedgerStateRequest request, StreamObserver<AdminDistLedger.getLedgerStateResponse> responseObserver) {

        List<Operation> operations = serverState.getLedgerState();

        DistLedgerOperationVisitor visitor = new DistLedgerOperationVisitor();

        for (Operation operation : operations) {
            operation.accept(visitor);
        }

        List<DistLedgerCommonDefinitions.Operation> distLedgerOperations = visitor.getDistLedgerOperations();

        DistLedgerCommonDefinitions.LedgerState ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(distLedgerOperations).build();

        AdminDistLedger.getLedgerStateResponse response = AdminDistLedger.getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }
}
