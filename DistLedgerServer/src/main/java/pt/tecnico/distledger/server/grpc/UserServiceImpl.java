package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {
        super.balance(request, responseObserver);
    }

    @Override
    public void createAccount(UserDistLedger.CreateAccountRequest request, StreamObserver<UserDistLedger.CreateAccountResponse> responseObserver) {
        super.createAccount(request, responseObserver);
    }

    @Override
    public void deleteAccount(UserDistLedger.DeleteAccountRequest request, StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {
        super.deleteAccount(request, responseObserver);
    }

    @Override
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {
        super.transferTo(request, responseObserver);
    }
}
