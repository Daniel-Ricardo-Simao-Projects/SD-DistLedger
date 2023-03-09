package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

public class AdminService {

    private final ManagedChannel channel;
    private final AdminServiceGrpc.AdminServiceBlockingStub stub;

    public AdminService(String target) {

        channel = ManagedChannelBuilder.forTarget("localhost:2001").usePlaintext().build();

        stub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public void closeChannel() {
        this.channel.shutdownNow();
    }

    public String activateServer() {
        AdminDistLedger.ActivateRequest request = AdminDistLedger.ActivateRequest.newBuilder().build();
        AdminDistLedger.ActivateResponse response = stub.activate(request);
        return "OK";
    }

    public String deactivateServer() {
        AdminDistLedger.DeactivateRequest request = AdminDistLedger.DeactivateRequest.newBuilder().build();
        AdminDistLedger.DeactivateResponse response = stub.deactivate(request);
        return "OK";
    }

    public String getLedgerState() {
        AdminDistLedger.getLedgerStateRequest request = AdminDistLedger.getLedgerStateRequest.newBuilder().build();
        AdminDistLedger.getLedgerStateResponse response = stub.getLedgerState(request);
        return "OK\n" + response.toString();
    }
}
