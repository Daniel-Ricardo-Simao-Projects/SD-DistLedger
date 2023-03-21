package pt.tecnico.distledger.namingserver.grpc;


import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.namingserver.domain.NamingServerState;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private NamingServerState namingServerState = new NamingServerState();

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {

        boolean flag = namingServerState.register(request.getServiceName(), request.getQualifier(), request.getServerAddress());

        namingServerState.Print();

        RegisterResponse response = RegisterResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
        boolean flag = namingServerState.delete(request.getServiceName(), request.getServerAddress());

        namingServerState.Print();

        DeleteResponse response = DeleteResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}