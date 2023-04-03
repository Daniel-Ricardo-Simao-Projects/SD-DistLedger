package pt.tecnico.distledger.server.grpc;

import io.grpc.*;
import pt.tecnico.distledger.server.domain.operation.DistLedgerOperationVisitor;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.serverExceptions.NoServerAvailableException;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ServerService {

    private final ManagedChannel namingChannel;

    private Map<String, DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> stubCache;
    private Map<String, ManagedChannel> channelCache;

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;

    private static boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            logger.info(debugMessage);
    }

    public ServerService(String target, final boolean DEBUG_FLAG) {
        namingChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingChannel);
        this.stubCache = new HashMap<>();
        this.channelCache = new HashMap<>();
        ServerService.DEBUG_FLAG = DEBUG_FLAG;
    }

    public void closeChannel() {
        this.namingChannel.shutdownNow();
    }

    public String registerService(String service, String qualifier, String target) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setServiceName(service)
                .setQualifier(qualifier)
                .setServerAddress(target)
                .build();
        RegisterResponse response = namingServerStub.register(request);
        return response.toString();
    }

    public String deleteService(String service, String target) {
        DeleteRequest request = DeleteRequest.newBuilder()
                .setServiceName(service)
                .setServerAddress(target)
                .build();
        DeleteResponse response = namingServerStub.delete(request);

        channelCache.forEach((s, channel) -> {
            channel.shutdownNow();
        });
        channelCache.clear();

        ManagedChannel channel = (ManagedChannel) namingServerStub.getChannel();
        channel.shutdownNow();
        return response.toString();
    }

    public DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub lookupService(String service, String qualifier)
            throws NoServerAvailableException {
        LookupRequest request = LookupRequest.newBuilder()
                .setServiceName(service)
                .setQualifier(qualifier)
                .build();
        LookupResponse response = namingServerStub.lookup(request);

        if (response.getServerListList().isEmpty()) {
            throw new NoServerAvailableException();
        }
        else {
            return addStub(response.getServerList(0));
        }
    }

    public boolean propagateStateService(Operation operation) {
        ManagedChannel serverChannel;
        DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub serverStub;

        // Creating LedgerState object
        DistLedgerOperationVisitor visitor = new DistLedgerOperationVisitor();
        operation.accept(visitor);
        List<DistLedgerCommonDefinitions.Operation> distLedgerOperations = visitor.getDistLedgerOperations();
        DistLedgerCommonDefinitions.LedgerState ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(distLedgerOperations).build();
        CrossServerDistLedger.PropagateStateRequest request = CrossServerDistLedger.PropagateStateRequest.newBuilder()
                .setState(ledgerState)
                .build();

        // Propagate the operation to every server found
        try {
            serverStub = getStub("B");
            CrossServerDistLedger.PropagateStateResponse response = serverStub.propagateState(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getDescription().equals("UNAVAILABLE")) {
                debug("Secondary server is unavailable");
                return false;
            }
            else if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                debug("Secondary server either doesnt exist or port changed");
                try {
                    serverStub = lookupService("DistLedger", "B");
                    CrossServerDistLedger.PropagateStateResponse response = serverStub.propagateState(request);
                } catch (NoServerAvailableException exp) {
                    return false;
                }
            }
        } catch (NoServerAvailableException exp) {
            debug("There's no secondary server");
            return false;
        }

        return true;
    }


    private DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub getStub(String serverQualifier) throws NoServerAvailableException {
        DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = stubCache.get(serverQualifier);
        if (stub == null) {
            try {
                stub = lookupService("DistLedger", serverQualifier);
            } catch (NoServerAvailableException e) {
                throw e;
            }
            stubCache.put(serverQualifier, stub);
        }
        return stub;
    }

    public DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub addStub(NamingServerDistLedger.ServerEntry serverEntry) {
        ManagedChannel newChannel = ManagedChannelBuilder.forTarget(serverEntry.getTarget()).usePlaintext().build();
        debug("channel created: " + newChannel.toString());

        ManagedChannel channel = channelCache.get("B");
        if (channel != null) {
            channel.shutdownNow();
            channelCache.remove(channel);
        }

        DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub newStub =
                DistLedgerCrossServerServiceGrpc.newBlockingStub(newChannel);

        debug("stub created" + newStub.toString());

        stubCache.put(serverEntry.getQualifier(), newStub);
        channelCache.put(serverEntry.getQualifier(), newChannel);

        return newStub;
    }
}
