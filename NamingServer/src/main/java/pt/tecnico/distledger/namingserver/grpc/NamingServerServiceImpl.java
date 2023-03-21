package pt.tecnico.distledger.namingserver.grpc;


import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.namingserver.domain.NamingServerState;
import pt.tecnico.distledger.namingserver.domain.ServerEntry;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

import java.util.List;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private NamingServerState namingServerState = new NamingServerState();

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {

        boolean flag = namingServerState.register(request.getServiceName(), request.getQualifier(), request.getServerAddress());

        RegisterResponse response = RegisterResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        boolean flag = namingServerState.delete(request.getServiceName(), request.getServerAddress());

        DeleteResponse response = DeleteResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        List<ServerEntry> serverEntryList = namingServerState.lookup(request.getServiceName(), request.getQualifier());

        //namingServerState.Print();

        LookupResponse response = LookupResponse.newBuilder()
                .addAllServerList(serverEntryList.stream().map(ServerEntry::toString).toList())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
