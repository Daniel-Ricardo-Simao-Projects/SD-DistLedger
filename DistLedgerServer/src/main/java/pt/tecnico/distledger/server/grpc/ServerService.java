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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerService {

    private final ManagedChannel namingChannel;

    private Map<String, DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> stubCache;
    private List<ManagedChannel> channelCache;

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;

    public ServerService(String target) {
        namingChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        namingServerStub = NamingServerServiceGrpc.newBlockingStub(namingChannel);
        this.stubCache = new HashMap<>();
        this.channelCache = new ArrayList<>();
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

        channelCache.forEach(ManagedChannel::shutdownNow);
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
            DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = addStub(response.getServerList(0));
            return stub;
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
            if (e.getStatus().getDescription().equals("UNAVAILABLE")) return false;
            else if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                try {
                    serverStub = lookupService("DistLedger", "B");
                    CrossServerDistLedger.PropagateStateResponse response = serverStub.propagateState(request);
                } catch (NoServerAvailableException exp) {
                    return false;
                }
            }
        } catch (NoServerAvailableException exp) {
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
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverEntry.getTarget()).usePlaintext().build();
        //debug("channel created: " + channel.toString());

        DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub newStub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);

        //debug("stub created" + newStub.toString());

        stubCache.put(serverEntry.getQualifier(), newStub);
        channelCache.add(channel);

        return newStub;
    }
}
