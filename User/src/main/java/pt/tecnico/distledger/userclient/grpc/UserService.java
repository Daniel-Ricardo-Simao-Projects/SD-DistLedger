package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;


public class UserService {

    /*TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */

    private final ManagedChannel channel;
    private final UserServiceGrpc.UserServiceBlockingStub stub;

    public UserService(String target) {

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        stub = UserServiceGrpc.newBlockingStub(channel);
    }

    public void closeChannel() {
        this.channel.shutdownNow();
    }

    public UserServiceGrpc.UserServiceBlockingStub getStub() {
        return stub;
    }

    public String createAccountService(String username) {
        try {
            UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder().setUserId(username).build();
            UserDistLedger.CreateAccountResponse response = stub.createAccount(request);
            return "OK" + response.toString();
        } catch (StatusRuntimeException e) {
            return "Caught exception with description: " +
                    e.getStatus().getDescription();
        }
    }

    public String deleteAccountService(String username) {
        try {
            UserDistLedger.DeleteAccountRequest request = UserDistLedger.DeleteAccountRequest.newBuilder().setUserId(username).build();
            UserDistLedger.DeleteAccountResponse response = stub.deleteAccount(request);
            return "OK" + response.toString();
        } catch (StatusRuntimeException e) {
            return "Caught exception with description: " + e.getStatus().getDescription();
        }
    }

    public String getBalanceService(String username) {
        UserDistLedger.BalanceRequest request = UserDistLedger.BalanceRequest.newBuilder().setUserId(username).build();
        try {
            UserDistLedger.BalanceResponse response = stub.balance(request);
            return "OK\n" + response.toString();
        }
        catch (StatusRuntimeException e){
            return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
        }
    }

    public String transferToService(String fromUsername, String toUsername, Integer amount) {
        UserDistLedger.TransferToRequest request = UserDistLedger.TransferToRequest.newBuilder()
                .setAccountFrom(fromUsername).setAccountTo(toUsername).setAmount(amount).build();
        try {
            UserDistLedger.TransferToResponse response = stub.transferTo(request);
            return "OK" + response.toString();
        }
        catch (StatusRuntimeException e) {
            return "Caught exception with description: " + e.getStatus().getDescription();
        }
    }
}
