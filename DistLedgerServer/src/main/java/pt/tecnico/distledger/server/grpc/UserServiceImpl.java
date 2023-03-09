package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.tecnico.distledger.server.domain.ServerState;
import static io.grpc.Status.NOT_FOUND;
import java.util.logging.Logger;

import static io.grpc.Status.*;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private ServerState serverState;
    private static boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    public UserServiceImpl(ServerState serverState, final boolean DEBUG_FLAG) {
        this.serverState = serverState;
        this.DEBUG_FLAG = DEBUG_FLAG;
    }

    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {

        if(DEBUG_FLAG)
            logger.info("Received balance request for user " + request.getUserId());

        int value = serverState.getBalanceById(request.getUserId());

        if(value == -1) {
            if(DEBUG_FLAG)
                logger.warning("User " + request.getUserId() + " not found");

            responseObserver.onError(NOT_FOUND.withDescription("User not found").asRuntimeException());
        }
        else if(value == -4) {
            if(DEBUG_FLAG)
                logger.severe("Server unavailable");

            responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
        }
        else {
            UserDistLedger.BalanceResponse response = UserDistLedger.BalanceResponse.newBuilder().
                    setValue(value).build();

            if(DEBUG_FLAG)
                logger.info("Sending balance response for user " + request.getUserId() + " : " + value);

            responseObserver.onNext(response);

            responseObserver.onCompleted();
        }
    }

    @Override
    public void createAccount(UserDistLedger.CreateAccountRequest request, StreamObserver<UserDistLedger.CreateAccountResponse> responseObserver) {

        if(DEBUG_FLAG)
            logger.info("Received create account request with username " + request.getUserId());

        int flag = serverState.createAccount(request.getUserId());

        if (flag == -1) {
            if(DEBUG_FLAG)
                logger.warning("User " + request.getUserId() + " already exists");

            responseObserver.onError(ALREADY_EXISTS.withDescription("Username already taken").asRuntimeException());

        }
        else if(flag == -4) {
            if(DEBUG_FLAG)
                logger.severe("Server unavailable");

            responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
        }
        else {
            UserDistLedger.CreateAccountResponse response = UserDistLedger.CreateAccountResponse.newBuilder().build();
            if(DEBUG_FLAG)
                logger.info("Sending create account response for user " + request.getUserId());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteAccount(UserDistLedger.DeleteAccountRequest request, StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {

        if(DEBUG_FLAG)
            logger.info("Received delete account request of user " + request.getUserId());

        int flag = serverState.deleteAccount(request.getUserId());

        switch (flag) {
            case -1 -> {
                if(DEBUG_FLAG)
                    logger.severe("User " + request.getUserId() + " not found");
                responseObserver.onError(NOT_FOUND.withDescription("User not found").asRuntimeException());
            }
            case -2 -> {
                if(DEBUG_FLAG)
                    logger.severe("Balance of user " + request.getUserId() + " is not zero");
                responseObserver.onError(PERMISSION_DENIED.withDescription("Balance not zero").asRuntimeException());
            }
            case -3 -> {
                if(DEBUG_FLAG)
                    logger.severe("User tried to remove broker");
                responseObserver.onError(PERMISSION_DENIED.withDescription("Cannot delete broker account").asRuntimeException());
            }
            case -4 -> {
                if(DEBUG_FLAG)
                    logger.severe("Server unavailable");
                responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
            }
            default -> {
                UserDistLedger.DeleteAccountResponse response = UserDistLedger.DeleteAccountResponse.newBuilder().build();
                if(DEBUG_FLAG)
                    logger.info("Sending delete account response for user " + request.getUserId());
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
    }

    @Override
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {

        if(DEBUG_FLAG)
            logger.info("Received transfer request from user " + request.getAccountFrom() +
                    " to user " + request.getAccountTo());

        int flag = serverState.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount());

        switch (flag) {
            case -1 -> {
                if(DEBUG_FLAG)
                    logger.severe("AccountFrom " + request.getAccountFrom() + " not found");
                responseObserver.onError(NOT_FOUND.withDescription("AccountFrom not found").asRuntimeException());
            }
            case -2 -> {
                if(DEBUG_FLAG)
                    logger.severe("AccountTo " + request.getAccountTo() + " not found");
                responseObserver.onError(NOT_FOUND.withDescription("AccountTo not found").asRuntimeException());
            }
            case -3 -> {
                if(DEBUG_FLAG)
                    logger.severe("The amount <" + request.getAmount() + "> is negative");
                responseObserver.onError(INVALID_ARGUMENT.withDescription("Amount has to be greater than zero").asRuntimeException());
            }
            case -4 -> {
                if(DEBUG_FLAG)
                    logger.severe("The amount <" + request.getAmount() + "> is bigger than the AccountFrom balance");
                responseObserver.onError(PERMISSION_DENIED.withDescription("Balance lower than amount to send").asRuntimeException());
            }
            case -5 -> {
                if(DEBUG_FLAG)
                    logger.severe("Server unavailable");
                responseObserver.onError(UNAVAILABLE.withDescription("UNAVAILABLE").asRuntimeException());
            }
            default -> {
                UserDistLedger.TransferToResponse response = UserDistLedger.TransferToResponse.newBuilder().build();
                if(DEBUG_FLAG)
                    logger.info("Sending transferTo response from user " + request.getAccountFrom() +
                            " to user " + request.getAccountTo() + " of an amount of " + request.getAmount());
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
    }
}
