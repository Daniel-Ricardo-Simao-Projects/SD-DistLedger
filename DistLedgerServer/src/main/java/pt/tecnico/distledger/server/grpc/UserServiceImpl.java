package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.serverExceptions.*;
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
    public void createAccount(UserDistLedger.CreateAccountRequest request, StreamObserver<UserDistLedger.CreateAccountResponse> responseObserver) {

        if (DEBUG_FLAG) logger.info("Received create account request with username " + request.getUserId());

        try {
            serverState.createAccount(request.getUserId(), false);

            UserDistLedger.CreateAccountResponse response = UserDistLedger.CreateAccountResponse.newBuilder().build();
            if (DEBUG_FLAG) logger.info("Sending create account response for user " + request.getUserId());

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (AccountAlreadyExistsException e) {
            if (DEBUG_FLAG) logger.warning(e.getMessage(request.getUserId()));
            responseObserver.onError(ALREADY_EXISTS.withDescription(e.getMessage(request.getUserId())).asRuntimeException());

        } catch (ServerUnavailableException | WriteNotSupportedException | CouldNotPropagateException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage());
            responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteAccount(UserDistLedger.DeleteAccountRequest request, StreamObserver<UserDistLedger.DeleteAccountResponse> responseObserver) {

        if (DEBUG_FLAG) logger.info("Received delete account request of user " + request.getUserId());

        try {
            serverState.deleteAccount(request.getUserId(), false);

            UserDistLedger.DeleteAccountResponse response = UserDistLedger.DeleteAccountResponse.newBuilder().build();
            if (DEBUG_FLAG) logger.info("Sending delete account response for user " + request.getUserId());

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (AccountDoesntExistException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage(request.getUserId()));
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage(request.getUserId())).asRuntimeException());

        } catch (BalanceIsntZeroException | CannotRemoveBrokerException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage());
            responseObserver.onError(PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());

        } catch (ServerUnavailableException | WriteNotSupportedException | CouldNotPropagateException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage());
            responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void balance(UserDistLedger.BalanceRequest request, StreamObserver<UserDistLedger.BalanceResponse> responseObserver) {

        if (DEBUG_FLAG) logger.info("Received balance request for user " + request.getUserId());

        try {
            int value = serverState.getBalanceById(request.getUserId());

            UserDistLedger.BalanceResponse response = UserDistLedger.BalanceResponse.newBuilder()
                    .setValue(value).build();
            if (DEBUG_FLAG) logger.info("Sending balance response for user " + request.getUserId() + " : " + value);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (AccountDoesntExistException e) {
            if (DEBUG_FLAG) logger.warning(e.getMessage(request.getUserId()));
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage(request.getUserId())).asRuntimeException());

        } catch (ServerUnavailableException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage());
            responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void transferTo(UserDistLedger.TransferToRequest request, StreamObserver<UserDistLedger.TransferToResponse> responseObserver) {

        if (DEBUG_FLAG) logger.info("Received transfer request from user " + request.getAccountFrom() +
                    " to user " + request.getAccountTo());

        try {
            serverState.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount(), false);

            UserDistLedger.TransferToResponse response = UserDistLedger.TransferToResponse.newBuilder().build();
            if (DEBUG_FLAG) {
                logger.info("Sending transferTo response from user " + request.getAccountFrom() +
                        " to user " + request.getAccountTo() + " of an amount of " + request.getAmount());
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (AccountDoesntExistException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage(request.getAccountFrom()));
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage(request.getAccountFrom())).asRuntimeException());

        } catch (DestAccountEqualToFromAccountException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage(request.getAccountFrom(), request.getAccountTo()));
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage(request.getAccountFrom(), request.getAccountTo())).asRuntimeException());

        } catch (DestAccountDoesntExistException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage(request.getAccountTo()));
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage(request.getAccountTo())).asRuntimeException());

        } catch (InvalidAmountException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage(request.getAmount()));
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage(request.getAmount())).asRuntimeException());

        } catch (TransferBiggerThanBalanceException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage(request.getAmount()));
            responseObserver.onError(PERMISSION_DENIED.withDescription(e.getMessage(request.getAmount())).asRuntimeException());

        } catch (ServerUnavailableException | WriteNotSupportedException | CouldNotPropagateException e) {
            if (DEBUG_FLAG) logger.severe(e.getMessage());
            responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        }
    }

}