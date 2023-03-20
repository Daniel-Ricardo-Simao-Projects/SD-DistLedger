package pt.tecnico.distledger.namingserver.grpc;


import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.namingserver.domain.NamingServerState;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    private NamingServerState namingServerState = new NamingServerState();

    @Override
    public void register(NamingServerDistLedger.RegisterRequest request, StreamObserver<NamingServerDistLedger.RegisterResponse> responseObserver) {

        boolean flag = namingServerState.register(request.getServiceName(), request.getQualifier(), request.getServerAddress());

        namingServerState.Print();

        NamingServerDistLedger.RegisterResponse response = NamingServerDistLedger.RegisterResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}