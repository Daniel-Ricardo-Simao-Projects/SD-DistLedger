package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import static io.grpc.Status.NOT_FOUND;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    ServerState serverState = new ServerState();

    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {

        int value = serverState.getBalanceById(request.getUserId());

        if(value == -1) {
            responseObserver.onError(NOT_FOUND.withDescription("User not found").asRuntimeException());
        }
        else {
            UserDistLedger.BalanceResponse response = UserDistLedger.BalanceResponse.newBuilder().
                    setValue(value).build();

            responseObserver.onNext(response);

            responseObserver.onCompleted();
        }
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

        int flag = serverState.deleteAccount(request.getUserId());

        if (flag == -1) {
            responseObserver.onError(NOT_FOUND.withDescription("User not found").asRuntimeException());
        }
        else if (flag == -2) {
            responseObserver.onError(PERMISSION_DENIED.withDescription("Balance not zero").asRuntimeException());
        }
        else {
            UserDistLedger.DeleteAccountResponse response = UserDistLedger.DeleteAccountResponse.newBuilder().build();

            responseObserver.onNext(response);

            responseObserver.onCompleted();
        }
    }

    @Override
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {
        super.transferTo(request, responseObserver);
    }
}
