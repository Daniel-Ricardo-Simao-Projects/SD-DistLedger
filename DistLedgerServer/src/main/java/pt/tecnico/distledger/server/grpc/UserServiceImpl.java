package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    ServerState serverState = new ServerState();

    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {

        UserDistLedger.BalanceResponse response = UserDistLedger.BalanceResponse.newBuilder().
                setValue(serverState.getBalanceById(request.getUserId())).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void createAccount(UserDistLedger.CreateAccountRequest request, StreamObserver<UserDistLedger.CreateAccountResponse> responseObserver) {

        int flag = serverState.createAccount(request.getUserId());

        if (flag == 1) {
            responseObserver.onError(ALREADY_EXISTS.withDescription("Username already taken").asRuntimeException());
        } else {
            UserDistLedger.CreateAccountResponse response = UserDistLedger.CreateAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteAccount(UserDistLedger.DeleteAccountRequest request, StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {

        serverState.deleteAccount(request.getUserId());

        UserDistLedger.DeleteAccountResponse response = UserDistLedger.DeleteAccountResponse.newBuilder().build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {
        super.transferTo(request, responseObserver);
    }
}
