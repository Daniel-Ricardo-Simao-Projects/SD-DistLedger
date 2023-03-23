package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AdminService {

    private final boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(AdminService.class.getName());

    private Map<String, AdminServiceGrpc.AdminServiceBlockingStub> stubCache;

    private static final String namingServerTarget = "localhost:5001";

    private NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;

    public AdminService(final boolean DEBUG_FLAG) {

        this.DEBUG_FLAG = DEBUG_FLAG;

        this.stubCache = new HashMap<>();

        ManagedChannel channel = ManagedChannelBuilder.forTarget(namingServerTarget).usePlaintext().build();
        if(DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(channel);
        if(DEBUG_FLAG) { logger.info("stub created" + namingServerStub.toString()); }
    }

    public AdminServiceGrpc.AdminServiceBlockingStub addStub(NamingServerDistLedger.ServerEntry serverEntry) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverEntry.getTarget()).usePlaintext().build();
        if(DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        AdminServiceGrpc.AdminServiceBlockingStub newStub = AdminServiceGrpc.newBlockingStub(channel);

        if(DEBUG_FLAG) { logger.info("stub created" + newStub.toString()); }

        stubCache.put(serverEntry.getQualifier(), newStub);

        System.out.println(stubCache);

        return newStub;
    }

    public AdminServiceGrpc.AdminServiceBlockingStub lookupService(String qualifier) {
        NamingServerDistLedger.LookupRequest request = NamingServerDistLedger.LookupRequest
                .newBuilder()
                .setServiceName("DistLedger") //TODO: Change hardcode service name (?)
                .setQualifier(qualifier)
                .build();

        NamingServerDistLedger.LookupResponse response = namingServerStub.lookup(request);

        if (response.getServerListList().isEmpty()) {
            // TODO throw exception
        }
        else {
            AdminServiceGrpc.AdminServiceBlockingStub stub = addStub(response.getServerList(0));
            return stub;
        }

        return null;
    }

    public void closeChannel() {
        ManagedChannel namingServerChannel = (ManagedChannel) namingServerStub.getChannel();
        namingServerChannel.shutdownNow();
        for (ManagedChannel channel : stubCache.values().stream().map(stub -> (ManagedChannel) stub.getChannel()).toList()) {
            channel.shutdownNow();
        }

        if(DEBUG_FLAG) { logger.info("channel shutdown"); }
    }

    public String activateServer(String serverQualifier) {
        AdminServiceGrpc.AdminServiceBlockingStub stub = stubCache.get(serverQualifier);
        if (stub != null) {
            try {
                AdminDistLedger.ActivateRequest request = AdminDistLedger.ActivateRequest.newBuilder().build();
                AdminDistLedger.ActivateResponse response = stub.activate(request);
                return "OK\n";
            }
            catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                    try {
                        stub = lookupService(serverQualifier);
                        AdminDistLedger.ActivateRequest request = AdminDistLedger.ActivateRequest.newBuilder().build();
                        AdminDistLedger.ActivateResponse response = stub.activate(request);
                        return "OK\n";
                    }
                    catch (Exception lookUpException) {
                        // TODO Exception of lookupService
                    }
                } else {
                    // TODO maybe do some other errors handling (?)
                }
            }
        } else {
            stub = lookupService(serverQualifier);
            AdminDistLedger.ActivateRequest request = AdminDistLedger.ActivateRequest.newBuilder().build();
            AdminDistLedger.ActivateResponse response = stub.activate(request);
            return "OK\n";
        }
        return "";
    }

    public String deactivateServer(String serverQualifier) {
        AdminServiceGrpc.AdminServiceBlockingStub stub = stubCache.get(serverQualifier);
        if (stub != null) {
            try {
                AdminDistLedger.DeactivateRequest request = AdminDistLedger.DeactivateRequest.newBuilder().build();
                AdminDistLedger.DeactivateResponse response = stub.deactivate(request);
                return "OK\n";
            }
            catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                    try {
                        stub = lookupService(serverQualifier);
                        AdminDistLedger.DeactivateRequest request = AdminDistLedger.DeactivateRequest.newBuilder().build();
                        AdminDistLedger.DeactivateResponse response = stub.deactivate(request);
                        return "OK\n";
                    }
                    catch (Exception lookUpException) {
                        // TODO Exception of lookupService
                    }
                } else {
                    // TODO maybe do some other errors handling (?)
                }
            }
        } else {
            stub = lookupService(serverQualifier);
            AdminDistLedger.DeactivateRequest request = AdminDistLedger.DeactivateRequest.newBuilder().build();
            AdminDistLedger.DeactivateResponse response = stub.deactivate(request);
            return "OK\n";
        }
        return "";
    }

    public String getLedgerState(String serverQualifier) {
        AdminServiceGrpc.AdminServiceBlockingStub stub = stubCache.get(serverQualifier);
        if (stub != null) {
            try {
                AdminDistLedger.getLedgerStateRequest request = AdminDistLedger.getLedgerStateRequest.newBuilder().build();
                AdminDistLedger.getLedgerStateResponse response = stub.getLedgerState(request);
                return "OK\n" + response.toString();
            }
            catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                    try {
                        stub = lookupService(serverQualifier);
                        AdminDistLedger.getLedgerStateRequest request = AdminDistLedger.getLedgerStateRequest.newBuilder().build();
                        AdminDistLedger.getLedgerStateResponse response = stub.getLedgerState(request);
                        return "OK\n" + response.toString();
                    }
                    catch (Exception lookUpException) {
                        // TODO Exception of lookupService
                    }
                } else {
                    // TODO maybe do some other errors handling (?)
                }
            }
        } else {
            stub = lookupService(serverQualifier);
            AdminDistLedger.getLedgerStateRequest request = AdminDistLedger.getLedgerStateRequest.newBuilder().build();
            AdminDistLedger.getLedgerStateResponse response = stub.getLedgerState(request);
            return "OK\n" + response.toString();
        }
        return "";
    }
}
