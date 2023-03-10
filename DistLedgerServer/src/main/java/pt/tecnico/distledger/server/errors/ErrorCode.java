package pt.tecnico.distledger.server.errors;

public enum ErrorCode {

    ACCOUNT_ALREADY_EXISTS(-1),
    ACCOUNT_DOESNT_EXIST(-2),
    DEST_ACCOUNT_DOESNT_EXIST(-3),
    SERVER_UNAVAILABLE(-4),
    BALANCE_ISNT_ZERO (-5),
    TRANSFER_BIGGER_THAN_BALANCE(-6),
    NEGATIVE_BALANCE(-7),
    CANNOT_REMOVE_BROKER(-8);

    private final int code;

    ErrorCode(int code) { this.code = code; }

    public int getCode() {
        return code;
    }
}
