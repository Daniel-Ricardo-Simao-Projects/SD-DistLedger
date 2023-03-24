package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.userclient.UserExceptions.NoServerAvailableException;
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

    private final NamingServerServiceGrpc.NamingServerServiceBlockingStub namingServerStub;

    private static void debug(String debugMessage) {
        if (DEBUG_FLAG) logger.info(debugMessage);
    }

    public UserService(final boolean DEBUG_FLAG) {
        UserService.DEBUG_FLAG = DEBUG_FLAG;

        this.stubCache = new HashMap<>();

        ManagedChannel channel = ManagedChannelBuilder.forTarget(namingServerTarget).usePlaintext().build();
        if(DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        this.namingServerStub = NamingServerServiceGrpc.newBlockingStub(channel);
        if(DEBUG_FLAG) { logger.info("stub created" + namingServerStub.toString()); }

    }

    public UserServiceGrpc.UserServiceBlockingStub addStub(NamingServerDistLedger.ServerEntry serverEntry) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverEntry.getTarget()).usePlaintext().build();
        if (DEBUG_FLAG) { logger.info("channel created: " + channel.toString()); }

        UserServiceGrpc.UserServiceBlockingStub newStub = UserServiceGrpc.newBlockingStub(channel);
        if (DEBUG_FLAG) { logger.info("stub created" + newStub.toString()); }

        stubCache.put(serverEntry.getQualifier(), newStub);
        debug("channel created: " + channel.toString());

        if (DEBUG_FLAG) { logger.info("stubCache:"+ stubCache.toString()); }
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
        debug("stub created" + stub.toString());

        return newStub;
    }

    private UserServiceGrpc.UserServiceBlockingStub getStub(String serverQualifier) throws NoServerAvailableException {
        UserServiceGrpc.UserServiceBlockingStub stub = stubCache.get(serverQualifier);
        if (stub == null) {
            stub = lookupService(serverQualifier);
            stubCache.put(serverQualifier, stub);
        }
        debug("getStub returned: " + stub.toString());
        return stub;
    }

    public UserServiceGrpc.UserServiceBlockingStub lookupService(String qualifier) throws NoServerAvailableException {
        NamingServerDistLedger.LookupRequest request = NamingServerDistLedger.LookupRequest.newBuilder()
                .setServiceName("DistLedger")
                .setQualifier(qualifier)
                .build();

        NamingServerDistLedger.LookupResponse response = namingServerStub.lookup(request);

        if (response.getServerListList().isEmpty()) {
            throw new NoServerAvailableException();
        }
        return addStub(response.getServerList(0));
    }

    public void closeChannel() {
        ManagedChannel namingServerChannel = (ManagedChannel) namingServerStub.getChannel();
        namingServerChannel.shutdownNow();
        debug("NamingServer channel shutdown");

        for (ManagedChannel channel : stubCache.values().stream().map(stub -> (ManagedChannel) stub.getChannel()).toList()) {
            debug("channel shutdown" + channel.toString());
            channel.shutdownNow();
        }

        debug("channel shutdown");
    }

    public String createAccountService(String username, String serverQualifier) {
        UserServiceGrpc.UserServiceBlockingStub stub;
        try {
            stub = getStub(serverQualifier);
            return createAccountRequest(stub, username);
        } catch (StatusRuntimeException e) {
            debug("user received createAccount error status: " + e.getStatus());
            if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                try {
                    stub = lookupService(serverQualifier);
                    return createAccountRequest(stub, username);
                } catch (NoServerAvailableException noServer) {
                    return e.getMessage() + "\n";
                } catch (StatusRuntimeException exception) {
                    return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
                }
            } else {
                return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
            }
        } catch (NoServerAvailableException e) {
            return e.getMessage() + "\n";
        }
    }

    public String deleteAccountService(String username, String serverQualifier) {
        UserServiceGrpc.UserServiceBlockingStub stub;
        try {
            stub = getStub(serverQualifier);
            return deleteAccountRequest(stub, username);
        } catch (StatusRuntimeException e) {
            debug("user received deleteAccount error status: " + e.getStatus());
            if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                try {
                    stub = lookupService(serverQualifier);
                    return deleteAccountRequest(stub, username);
                } catch (NoServerAvailableException noServer) {
                    return e.getMessage() + "\n";
                } catch (StatusRuntimeException exception) {
                    return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
                }
            } else {
                return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
            }
        } catch (NoServerAvailableException e) {
            return e.getMessage() + "\n";
        }
    }

    public String getBalanceService(String username, String serverQualifier) {
        UserServiceGrpc.UserServiceBlockingStub stub;
        try {
            stub = getStub(serverQualifier);
            return getBalanceRequest(stub, username);
        } catch (StatusRuntimeException e) {
            debug("user received getBalance error status: " + e.getStatus());
            if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                try {
                    stub = lookupService(serverQualifier);
                    return getBalanceRequest(stub, username);
                } catch (NoServerAvailableException noServer) {
                    return e.getMessage() + "\n";
                } catch (StatusRuntimeException exception) {
                    return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
                }
            } else {
                return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
            }
        } catch (NoServerAvailableException e) {
            return e.getMessage() + "\n";
        }
    }

    public String transferToService(String fromUsername, String toUsername, Integer amount, String serverQualifier) {
        UserServiceGrpc.UserServiceBlockingStub stub;
        try {
            stub = getStub(serverQualifier);
            return transferToRequest(stub, fromUsername, toUsername, amount);
        } catch (StatusRuntimeException e) {
            debug("user received transferTo error status: " + e.getStatus());
            if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
                try {
                    stub = lookupService(serverQualifier);
                    return transferToRequest(stub, fromUsername, toUsername, amount);
                } catch (NoServerAvailableException noServer) {
                    return e.getMessage() + "\n";
                } catch (StatusRuntimeException exception) {
                    return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
                }
            } else {
                return "Caught exception with description: " + e.getStatus().getDescription() + "\n";
            }
        } catch (NoServerAvailableException e) {
            return e.getMessage() + "\n";
        }
    }

    public String createAccountRequest(UserServiceGrpc.UserServiceBlockingStub stub, String username) {
        UserDistLedger.CreateAccountRequest request = UserDistLedger.CreateAccountRequest.newBuilder()
                .setUserId(username)
                .build();
        UserDistLedger.CreateAccountResponse response = stub.createAccount(request);
        return "OK" + response.toString() + "\n";
    }

    public String deleteAccountRequest(UserServiceGrpc.UserServiceBlockingStub stub, String username) {
        UserDistLedger.DeleteAccountRequest request = UserDistLedger.DeleteAccountRequest.newBuilder()
                .setUserId(username)
                .build();
        UserDistLedger.DeleteAccountResponse response = stub.deleteAccount(request);
        return "OK" + response.toString() + "\n";
    }

    public String getBalanceRequest(UserServiceGrpc.UserServiceBlockingStub stub, String username) {
        UserDistLedger.BalanceRequest request = UserDistLedger.BalanceRequest.newBuilder().setUserId(username).build();
        UserDistLedger.BalanceResponse response = stub.balance(request);
        return "OK\n" + response.getValue() + "\n";
    }

    public String transferToRequest(UserServiceGrpc.UserServiceBlockingStub stub, String fromUsername, String toUsername, int amount) {
        UserDistLedger.TransferToRequest request = UserDistLedger.TransferToRequest
                .newBuilder()
                .setAccountFrom(fromUsername)
                .setAccountTo(toUsername)
                .setAmount(amount)
                .build();

        UserDistLedger.TransferToResponse response = stub.transferTo(request);
        return "OK" + response.toString() + "\n";
    }

}
