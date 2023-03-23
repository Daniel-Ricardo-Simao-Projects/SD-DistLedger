package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.AbstractStub;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class UserService {

    private static boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private Map<String, UserServiceGrpc.UserServiceBlockingStub> stubCache;

    private static final String namingServerTarget = "localhost:5001";

    private NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;

    public UserService(final boolean DEBUG_FLAG) {
        this.DEBUG_FLAG = DEBUG_FLAG;

        this.stubCache = new HashMap<>();

        ManagedChannel channel = ManagedChannelBuilder.forTarget(namingServerTarget).usePlaintext().build();
        if(DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(channel);
        if(DEBUG_FLAG) { logger.info("stub created" + namingServerStub.toString()); }

    }

    public void addStub(NamingServerDistLedger.ServerEntry serverEntry) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverEntry.getTarget()).usePlaintext().build();
        if(DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
        if(DEBUG_FLAG) { logger.info("stub created" + stub.toString()); }

        stubCache.put(serverEntry.getQualifier(), stub);

        System.out.println(stubCache);
    }

    public void lookupService(String qualifier) {
        NamingServerDistLedger.LookupRequest request = NamingServerDistLedger.LookupRequest.newBuilder()
                .setServiceName("DistLedger").setQualifier(qualifier).build(); // TODO change hardcode serviceName?
        NamingServerDistLedger.LookupResponse response = namingServerStub.lookup(request);

        if (response.getServerListList().isEmpty()) {
            // TODO create exception
        }
        if (!stubCache.containsKey(response.getServerList(0).getQualifier())) {
            addStub(response.getServerList(0));
            // TODO check other cases;
        }
    }

    public void closeChannel() {
        ManagedChannel namingServerChannel = (ManagedChannel) namingServerStub.getChannel();
        namingServerChannel.shutdownNow();
        for (ManagedChannel channel : stubCache.values().stream().map(stub -> (ManagedChannel) stub.getChannel()).toList()) {
            channel.shutdownNow();
        }

        if(DEBUG_FLAG) { logger.info("channel shutdown"); }
    }

    public String createAccountService(String username, String serverQualifier) {
        lookupService(serverQualifier);
        try {
            UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder().setUserId(username).build();
            UserDistLedger.CreateAccountResponse response = stubCache.get(serverQualifier).createAccount(request);
            return "OK" + response.toString() + "\n";
        } catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received createAccount error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }

    public String deleteAccountService(String username, String serverQualifier) {
        lookupService(serverQualifier);
        try {
            UserDistLedger.DeleteAccountRequest request = UserDistLedger.DeleteAccountRequest
                    .newBuilder()
                    .setUserId(username)
                    .build();
            UserDistLedger.DeleteAccountResponse response = stubCache.get(serverQualifier).deleteAccount(request);
            return "OK" + response.toString() + "\n";
        } catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received deleteAccount error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }

    public String getBalanceService(String username, String serverQualifier) {
        lookupService(serverQualifier);
        UserDistLedger.BalanceRequest request = UserDistLedger.BalanceRequest.newBuilder().setUserId(username).build();
        try {
            UserDistLedger.BalanceResponse response = stubCache.get(serverQualifier).balance(request);
            return "OK\n" + response.getValue() + "\n";
        }
        catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received getBalance error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }

    public String transferToService(String fromUsername, String toUsername, Integer amount, String serverQualifier) {
        lookupService(serverQualifier);
        UserDistLedger.TransferToRequest request = UserDistLedger.TransferToRequest
                .newBuilder()
                .setAccountFrom(fromUsername)
                .setAccountTo(toUsername)
                .setAmount(amount)
                .build();
        try {
            UserDistLedger.TransferToResponse response = stubCache.get(serverQualifier).transferTo(request);
            return "OK" + response.toString() + "\n";
        }
        catch (StatusRuntimeException e) {
            if(DEBUG_FLAG) { logger.severe("user received transferTo error status: " + e.getStatus()); }
            if(e.getStatus().getDescription().equals("UNAVAILABLE")) { return e.getStatus().getDescription() + "\n"; }
            else { return "Caught exception with description: " + e.getStatus().getDescription() + "\n"; }
        }
    }
}
