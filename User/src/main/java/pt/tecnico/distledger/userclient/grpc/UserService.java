package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import java.util.logging.Logger;


public class UserService {

    private final ManagedChannel channel;

    private final UserServiceGrpc.UserServiceBlockingStub stub;

    private static boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    public UserService(String target, final boolean DEBUG_FLAG) {
        this.DEBUG_FLAG = DEBUG_FLAG;

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        if(DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        stub = UserServiceGrpc.newBlockingStub(channel);
        if(DEBUG_FLAG) { logger.info("stub created" + stub.toString()); }
    }

    public void closeChannel() {
        this.channel.shutdownNow();

        if(DEBUG_FLAG) { logger.info("channel shutdown"); }
    }

    public String createAccountService(String username) {
        try {
            UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder().setUserId(username).build();
            UserDistLedger.CreateAccountResponse response = stub.createAccount(request);
            return "OK" + response.toString() + "\n";
        } catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received createAccount error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }

    public String deleteAccountService(String username) {
        try {
            UserDistLedger.DeleteAccountRequest request = UserDistLedger.DeleteAccountRequest
                    .newBuilder()
                    .setUserId(username)
                    .build();
            UserDistLedger.DeleteAccountResponse response = stub.deleteAccount(request);
            return "OK" + response.toString() + "\n";
        } catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received deleteAccount error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }

    public String getBalanceService(String username) {
        UserDistLedger.BalanceRequest request = UserDistLedger.BalanceRequest.newBuilder().setUserId(username).build();
        try {
            UserDistLedger.BalanceResponse response = stub.balance(request);
            return "OK\n" + response.getValue() + "\n";
        }
        catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received getBalance error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }

    public String transferToService(String fromUsername, String toUsername, Integer amount) {
        UserDistLedger.TransferToRequest request = UserDistLedger.TransferToRequest
                .newBuilder()
                .setAccountFrom(fromUsername)
                .setAccountTo(toUsername)
                .setAmount(amount)
                .build();
        try {
            UserDistLedger.TransferToResponse response = stub.transferTo(request);
            return "OK" + response.toString() + "\n";
        }
        catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received transferTo error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }
}
