package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

public class UserService {

    /*TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */

    private final UserServiceGrpc.UserServiceBlockingStub stub;

    public UserService(String target) {

        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        // It is up to the client to determine whether to block the call.
        // Here we create a blocking stub, but an async stub,
        // or an async stub with Future are always possible.
        stub = UserServiceGrpc.newBlockingStub(channel);

        // A Channel should be shutdown before stopping the process.
        channel.shutdownNow();
    }

    public UserServiceGrpc.UserServiceBlockingStub getStub() {
        return stub;
    }

    public String createAccountService(String username) {
        UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder().setUserId(username).build();
        UserDistLedger.CreateAccountResponse response = stub.createAccount(request);
        return "OK"; // TODO try catch when errors are implemented
    }
    // TODO implement all functions of command parser of user
}
