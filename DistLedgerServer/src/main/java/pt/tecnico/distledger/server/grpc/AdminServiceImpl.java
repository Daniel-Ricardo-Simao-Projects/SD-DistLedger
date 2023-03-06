package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

    @Override
    public void activate(AdminDistLedger.ActivateRequest request, StreamObserver<AdminDistLedger.ActivateResponse> responseObserver) {
        super.activate(request, responseObserver);
    }

    @Override
    public void deactivate(AdminDistLedger.DeactivateRequest request, StreamObserver<AdminDistLedger.DeactivateResponse> responseObserver) {
        super.deactivate(request, responseObserver);
    }

    @Override
    public void getLedgerState(AdminDistLedger.getLedgerStateRequest request, StreamObserver<AdminDistLedger.getLedgerStateResponse> responseObserver) {
        super.getLedgerState(request, responseObserver);
    }
}
