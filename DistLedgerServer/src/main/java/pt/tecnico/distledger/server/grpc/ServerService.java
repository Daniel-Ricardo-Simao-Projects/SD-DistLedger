package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.server.domain.operation.DistLedgerOperationVisitor;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

import java.util.ArrayList;
import java.util.List;

public class ServerService {

    private final ManagedChannel channel;

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    public ServerService(String target) {
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = NamingServerServiceGrpc.newBlockingStub(channel);
    }

    public void closeChannel() {
        this.channel.shutdownNow();
    }

    public String registerService(String service, String qualifier, String target) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setServiceName(service)
                .setQualifier(qualifier)
                .setServerAddress(target)
                .build();
        RegisterResponse response = stub.register(request);
        return response.toString();
    }

    public String deleteService(String service, String target) {
        DeleteRequest request = DeleteRequest.newBuilder()
                .setServiceName(service)
                .setServerAddress(target)
                .build();
        DeleteResponse response = stub.delete(request);
        return response.toString();
    }

    public List<ServerEntry> lookupService(String service, String qualifier) {
        LookupRequest request = LookupRequest.newBuilder()
                .setServiceName(service)
                .setQualifier(qualifier)
                .build();
        LookupResponse response = stub.lookup(request);
        return response.getServerListList();
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

        // Look for servers to propagate the operation
        List<ServerEntry> serverEntries = new ArrayList<>();
        serverEntries.addAll(this.lookupService("DistLedger", ""));

        // Propagate the operation to every server found
        for(ServerEntry se : serverEntries) {
            if(!se.getQualifier().equals("A")) {
                serverChannel = ManagedChannelBuilder.forTarget(se.getTarget()).usePlaintext().build();
                serverStub = DistLedgerCrossServerServiceGrpc.newBlockingStub(serverChannel);
                try {
                    CrossServerDistLedger.PropagateStateResponse response = serverStub.propagateState(request);
                } catch (StatusRuntimeException e) {
                    return false;
                }

                serverChannel.shutdownNow();
            }
        }
        return true;
    }

}
