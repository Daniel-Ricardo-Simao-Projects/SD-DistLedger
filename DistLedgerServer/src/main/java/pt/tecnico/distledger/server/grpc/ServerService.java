package pt.tecnico.distledger.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

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

    public String lookupService(String service, String qualifier) {
        LookupRequest request = LookupRequest.newBuilder()
                .setServiceName(service)
                .setQualifier(qualifier)
                .build();
        LookupResponse response = stub.lookup(request);
        return response.toString();
    }
}
