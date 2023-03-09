package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import static io.grpc.Status.NOT_FOUND;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private ServerState serverState;

    public UserServiceImpl(ServerState serverState) {
        this.serverState = serverState;
    }

    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {

        int value = serverState.getBalanceById(request.getUserId());

        if(value == -1) {
            responseObserver.onError(NOT_FOUND.withDescription("User not found").asRuntimeException());
        }
        else if(value == -4) {
            responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
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

        if (flag == -1) {
            responseObserver.onError(ALREADY_EXISTS.withDescription("Username already taken").asRuntimeException());

        }
        else if(flag == -4) {
            responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
        }
        else {
            UserDistLedger.CreateAccountResponse response = UserDistLedger.CreateAccountResponse.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteAccount(UserDistLedger.DeleteAccountRequest request, StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {

        int flag = serverState.deleteAccount(request.getUserId());

        switch (flag) {
            case -1 -> responseObserver.onError(NOT_FOUND.withDescription("User not found").asRuntimeException());
            case -2 -> responseObserver.onError(PERMISSION_DENIED.withDescription("Balance not zero").asRuntimeException());
            case -3 -> responseObserver.onError(PERMISSION_DENIED.withDescription("Cannot delete broker account").asRuntimeException());
            case -4 -> responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
            default -> {
                UserDistLedger.DeleteAccountResponse response = UserDistLedger.DeleteAccountResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
    }

    @Override
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {

        int flag = serverState.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount());

        switch (flag) {
            case -1 -> responseObserver.onError(NOT_FOUND.withDescription("AccountFrom not found").asRuntimeException());
            case -2 -> responseObserver.onError(NOT_FOUND.withDescription("AccountTo not found").asRuntimeException());
            case -3 -> responseObserver.onError(INVALID_ARGUMENT.withDescription("Amount has to be greater than zero").asRuntimeException());
            case -4 -> responseObserver.onError(PERMISSION_DENIED.withDescription("Balance lower than amount to send").asRuntimeException());
            case -5 -> responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
            default -> {
                UserDistLedger.TransferToResponse response = UserDistLedger.TransferToResponse.newBuilder().build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
    }
}
