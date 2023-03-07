package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;


public class UserService {

    /*TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */

    private final ManagedChannel channel;
    private final UserServiceGrpc.UserServiceBlockingStub stub;

    public UserService(String target) {

        channel = ManagedChannelBuilder.forTarget("localhost:2001").usePlaintext().build();

        stub = UserServiceGrpc.newBlockingStub(channel);
    }

    public void closeChannel() {
        this.channel.shutdownNow();
    }

    public UserServiceGrpc.UserServiceBlockingStub getStub() {
        return stub;
    }

    public String createAccountService(String username) {
        UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder().setUserId(username).build();
        UserDistLedger.CreateAccountResponse response = stub.createAccount(request);
        return "OK"; // TODO try catch when errors are implemented
    }

    public String deleteAccountService(String username) {
        UserDistLedger.DeleteAccountRequest request = UserDistLedger.DeleteAccountRequest.newBuilder().setUserId(username).build();
        UserDistLedger.DeleteAccountResponse response = stub.deleteAccount(request);
        return "OK"; // TODO try catch when errors are implemented
    }

    public int getBalanceService(String username) {
        UserDistLedger.BalanceRequest request = UserDistLedger.BalanceRequest.newBuilder().setUserId(username).build();
        UserDistLedger.BalanceResponse response = stub.balance(request);
        return response.getValue();
    }
    // TODO implement all functions of command parser of user
}
