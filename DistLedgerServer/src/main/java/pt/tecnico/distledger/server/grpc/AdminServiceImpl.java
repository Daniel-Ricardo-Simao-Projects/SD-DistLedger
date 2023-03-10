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
import java.util.logging.Logger;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    private ServerState serverState;

    private static boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    public AdminServiceImpl(ServerState serverState, final boolean DEBUG_FLAG) {
        this.serverState = serverState;
        this.DEBUG_FLAG = DEBUG_FLAG;
    }

    @Override
    public void activate(AdminDistLedger.ActivateRequest request, StreamObserver<AdminDistLedger.ActivateResponse> responseObserver) {

        if(DEBUG_FLAG)
            logger.info("Received activate server request from admin");

        serverState.activateServer();

        AdminDistLedger.ActivateResponse response = AdminDistLedger.ActivateResponse.newBuilder().build();

        if(DEBUG_FLAG)
            logger.info("Sending server activated response for admin");

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    @Override
    public void deactivate(AdminDistLedger.DeactivateRequest request, StreamObserver<AdminDistLedger.DeactivateResponse> responseObserver) {

        if(DEBUG_FLAG)
            logger.info("Received deactivate server request from admin");

        serverState.deactivateServer();

        AdminDistLedger.DeactivateResponse response = AdminDistLedger.DeactivateResponse.newBuilder().build();

        if(DEBUG_FLAG)
            logger.info("Sending server deactivated response for admin");

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    @Override
    public void getLedgerState(AdminDistLedger.getLedgerStateRequest request, StreamObserver<AdminDistLedger.getLedgerStateResponse> responseObserver) {

        if(DEBUG_FLAG)
            logger.info("Received get ledger state request from admin");

        List<Operation> operations = serverState.getLedgerState();

        DistLedgerOperationVisitor visitor = new DistLedgerOperationVisitor();

        for (Operation operation : operations) {
            operation.accept(visitor);
        }

        List<DistLedgerCommonDefinitions.Operation> distLedgerOperations = visitor.getDistLedgerOperations();

        DistLedgerCommonDefinitions.LedgerState ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(distLedgerOperations).build();

        AdminDistLedger.getLedgerStateResponse response = AdminDistLedger.getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build();

        if(DEBUG_FLAG)
            logger.info("Sending getLedgerState response for admin");

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }
}
