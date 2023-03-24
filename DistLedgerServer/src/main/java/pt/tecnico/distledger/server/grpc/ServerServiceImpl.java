package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.serverExceptions.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;

import java.util.logging.Logger;

import static io.grpc.Status.*;
import static io.grpc.Status.PERMISSION_DENIED;

public class ServerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    ServerState serverState;
    private static boolean DEBUG_FLAG;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            logger.info(debugMessage);
    }

    public ServerServiceImpl(ServerState serverState, final boolean DEBUG_FLAG) {
        this.serverState = serverState;
        this.DEBUG_FLAG = DEBUG_FLAG;
    }

    @Override
    public void propagateState(CrossServerDistLedger.PropagateStateRequest request,
                               StreamObserver<CrossServerDistLedger.PropagateStateResponse> responseObserver) {

        DistLedgerCommonDefinitions.LedgerState ledgerState = request.getState();

        DistLedgerCommonDefinitions.Operation operation = ledgerState.getLedger(0);

        DistLedgerCommonDefinitions.OperationType operationType = operation.getType();

        String userId = operation.getUserId();

        try {
            if (operationType.equals(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)) {
                serverState.createAccount(userId, true);
            } else if (operationType.equals(DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT)) {
                serverState.deleteAccount(userId, true);
            } else if (operationType.equals(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)) {
                serverState.transferTo(operation.getUserId(), operation.getDestUserId(), operation.getAmount(), true);
            }

            CrossServerDistLedger.PropagateStateResponse response = CrossServerDistLedger.PropagateStateResponse.getDefaultInstance();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (AccountAlreadyExistsException e) {
            debug(e.getMessage(userId));
            responseObserver.onError(ALREADY_EXISTS.withDescription(e.getMessage(userId)).asRuntimeException());

        } catch (ServerUnavailableException | WriteNotSupportedException | CouldNotPropagateException e) {
            debug(e.getMessage());
            responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());

        } catch (AccountDoesntExistException e) {
            debug(e.getMessage(userId));
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage(userId)).asRuntimeException());

        } catch (BalanceIsntZeroException | CannotRemoveBrokerException e) {
            debug(e.getMessage());
            responseObserver.onError(PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());

        } catch (DestAccountEqualToFromAccountException e) {
            debug(e.getMessage(operation.getUserId(), operation.getDestUserId()));
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage(operation.getDestUserId(), operation.getUserId())).asRuntimeException());

        } catch (DestAccountDoesntExistException e) {
            debug(e.getMessage(operation.getDestUserId()));
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage(operation.getUserId())).asRuntimeException());

        } catch (InvalidAmountException e) {
            debug(e.getMessage(operation.getAmount()));
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage(operation.getAmount())).asRuntimeException());

        } catch (TransferBiggerThanBalanceException e) {
            debug(e.getMessage(operation.getAmount()));
            responseObserver.onError(PERMISSION_DENIED.withDescription(e.getMessage(operation.getAmount())).asRuntimeException());

        }

    }

}
