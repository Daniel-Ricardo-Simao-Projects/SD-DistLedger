package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

public class UserService {

    /*TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */

    private String target;

    public UserService(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    // Channel is the abstraction to connect to a service endpoint.
    // Let us use plaintext communication because we do not have certificates.
    final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 2001).usePlaintext().build();

    // It is up to the client to determine whether to block the call.
    // Here we create a blocking stub, but an async stub,
    // or an async stub with Future are always possible.
    UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);


    // A Channel should be shutdown before stopping the process.
    // channel.shutdownNow();
}
