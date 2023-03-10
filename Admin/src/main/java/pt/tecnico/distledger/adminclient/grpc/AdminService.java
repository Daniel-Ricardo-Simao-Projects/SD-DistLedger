package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

import java.util.logging.Logger;

public class AdminService {

    private final ManagedChannel channel;

    private final AdminServiceGrpc.AdminServiceBlockingStub stub;

    private final boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(AdminService.class.getName());

    public AdminService(String target, final boolean DEBUG_FLAG) {

        this.DEBUG_FLAG = DEBUG_FLAG;

        channel = ManagedChannelBuilder.forTarget("localhost:2001").usePlaintext().build();
        if(DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        stub = AdminServiceGrpc.newBlockingStub(channel);
        if(DEBUG_FLAG) { logger.info("stub created" + stub.toString()); }
    }

    public void closeChannel() {
        this.channel.shutdownNow();
    }

    public String activateServer() {
        AdminDistLedger.ActivateRequest request = AdminDistLedger.ActivateRequest.newBuilder().build();
        AdminDistLedger.ActivateResponse response = stub.activate(request);
        return "OK\n";
    }

    public String deactivateServer() {
        AdminDistLedger.DeactivateRequest request = AdminDistLedger.DeactivateRequest.newBuilder().build();
        AdminDistLedger.DeactivateResponse response = stub.deactivate(request);
        return "OK\n";
    }

    public String getLedgerState() {
        AdminDistLedger.getLedgerStateRequest request = AdminDistLedger.getLedgerStateRequest.newBuilder().build();
        AdminDistLedger.getLedgerStateResponse response = stub.getLedgerState(request);
        return "OK\n" + response.toString();
    }
}
