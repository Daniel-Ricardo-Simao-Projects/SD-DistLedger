package pt.tecnico.distledger.server.domain;

public class UserAccount {

    private String userId;

    private int balance = 0;

    public UserAccount(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public int getBalance() {
        return balance;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "UserAccount {" +
                "userId='" + userId + '\'' +
                ", balance=" + balance +
                '}';
    }
}
