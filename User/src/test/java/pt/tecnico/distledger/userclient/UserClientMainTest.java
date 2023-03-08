package pt.tecnico.distledger.userclient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.distledger.userclient.grpc.UserService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class UserClientMainTest {

    UserService userService = new UserService("localhost:2001");
    String response;

    @BeforeEach
    public void setUp() {
        try {
            userService.deleteAccountService("test");
        } finally {
            userService.createAccountService("test");
        }
    }

    @AfterEach
    public void deleteAccount() {
        userService.deleteAccountService("test");
    }

    @Test
    void accountAlreadyCreated() {
        assert (Objects.equals(userService.createAccountService("test"), "Caught exception with description: Username already taken"));
    }

    @Test
    void deleteInvalidAccount() {
        assert (Objects.equals(userService.deleteAccountService("test2"), "Caught exception with description: User not found"));
    }

    @Test
    void deleteNonEmptyAccount() {
        assert (Objects.equals(userService.createAccountService("test2"), "OK"));
        assert (Objects.equals(userService.transferToService("broker", "test2", 100), "OK"));
        assert (Objects.equals(userService.deleteAccountService("test2"), "Caught exception with description: Balance not zero"));
        assert (Objects.equals(userService.transferToService("test2", "broker", 100), "OK"));
        assert (Objects.equals(userService.deleteAccountService("test2"), "OK"));
    }

    @Test
    void deleteBrokerAccount() {
        assert (Objects.equals(userService.deleteAccountService("broker"), "Caught exception with description: Cannot delete broker account"));
    }

    @Test
    void getEmptyBalance() {
        assert (Objects.equals(userService.getBalanceService("test"), "OK\n"));
    }

    @Test
    void transferFromBroker() {
        assert (Objects.equals(userService.transferToService("broker", "test", 100), "OK"));
        //System.out.println("R: " + userService.getBalanceService("test") + '|');
        assert (Objects.equals(userService.getBalanceService("test"), "OK\nvalue: 100\n"));
        assert (Objects.equals(userService.transferToService("test", "broker", 100), "OK"));
    }

    @Test
    void transferErrors() {
        assert (Objects.equals(userService.transferToService("broker", "test2", 100), "Caught exception with description: AccountTo not found"));
        assert (Objects.equals(userService.transferToService("broker", "test", -100), "Caught exception with description: amount has to be greater than zero"));
        assert (Objects.equals(userService.transferToService("broker", "test", 1001), "Caught exception with description: Balance lower than amount to send"));
    }

}