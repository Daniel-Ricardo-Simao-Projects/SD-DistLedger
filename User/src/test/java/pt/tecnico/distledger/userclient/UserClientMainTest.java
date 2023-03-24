package pt.tecnico.distledger.userclient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.distledger.userclient.grpc.UserService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class UserClientMainTest {

    UserService userService = new UserService( false);
    String response;

    @BeforeEach
    public void setUp() {
        try {
            userService.deleteAccountService("test", "A");
        } finally {
            userService.createAccountService("test", "A");
        }
    }

    @AfterEach
    public void deleteAccount() {
        userService.deleteAccountService("test", "A");
    }

    @Test
    void accountAlreadyCreated() {
        assert (Objects.equals(userService.createAccountService("test", "A"), "Caught exception with description: Username already taken\n"));
    }

    @Test
    void deleteInvalidAccount() {
        assert (Objects.equals(userService.deleteAccountService("test2", "A"), "Caught exception with description: User not found\n"));
    }

    @Test
    void deleteNonEmptyAccount() {
        assert (Objects.equals(userService.createAccountService("test2", "A"), "OK\n"));
        assert (Objects.equals(userService.transferToService("broker", "test2", 100, "A"), "OK\n"));
        assert (Objects.equals(userService.deleteAccountService("test2", "A"), "Caught exception with description: Balance not zero\n"));
        assert (Objects.equals(userService.transferToService("test2", "broker", 100, "A"), "OK\n"));
        assert (Objects.equals(userService.deleteAccountService("test2", "A"), "OK\n"));
    }

    @Test
    void deleteBrokerAccount() {
        assert (Objects.equals(userService.deleteAccountService("broker", "A"), "Caught exception with description: Cannot delete broker account\n"));
    }

    @Test
    void getEmptyBalance() {
        assert (Objects.equals(userService.getBalanceService("test", "A"), "OK\n0\n"));
    }

    @Test
    void transferFromBroker() {
        assert (Objects.equals(userService.transferToService("broker", "test", 100, "A"), "OK\n"));
        //System.out.println("R: " + userService.getBalanceService("test") + '|');
        assert (Objects.equals(userService.getBalanceService("test", "A"), "OK\n100\n"));
        assert (Objects.equals(userService.transferToService("test", "broker", 100, "A"), "OK\n"));
    }

    @Test
    void transferErrors() {
        assert (Objects.equals(userService.transferToService("broker", "test2", 100, "A"), "Caught exception with description: AccountTo not found\n"));
        assert (Objects.equals(userService.transferToService("broker", "test", -100, "A"), "Caught exception with description: Amount has to be greater than zero\n"));
        assert (Objects.equals(userService.transferToService("broker", "test", 1001, "A"), "Caught exception with description: Balance lower than amount to send\n"));
    }

}