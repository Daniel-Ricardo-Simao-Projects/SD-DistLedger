package pt.tecnico.distledger.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.serverExceptions.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger;

import static io.grpc.Status.*;
import static io.grpc.Status.PERMISSION_DENIED;

public class ServerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    ServerState serverState;

    public ServerServiceImpl(ServerState serverState) {
        this.serverState = serverState;
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
            responseObserver.onError(ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
        } catch (ServerUnavailableException e) {
            responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        } catch (WriteNotSupportedException | CouldNotPropagateException e) {
            responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        } catch (AccountDoesntExistException e) {
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (DestAccountEqualToFromAccountException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (DestAccountDoesntExistException e) {
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (NegativeBalanceException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (AmountIsZeroException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (TransferBiggerThanBalanceException e) {
            responseObserver.onError(PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (BalanceIsntZeroException e) {
            responseObserver.onError(PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        } catch (CannotRemoveBrokerException e) {
            responseObserver.onError(PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
