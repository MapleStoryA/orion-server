package database.state;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AccountData {
    private int id;
    private String name;
    private String password;
    private String salt;
    private byte loggedIn;
    private LocalDate lastLogin;
    private LocalDate lastLogon;
    private LocalDate createDat;
    private LocalDate birthDay;
    private int banned;
    private int gm;
    private int nxCredit;
    private int mPoints;
    private byte gender;


    public LoginState getLoginState() {
        return LoginState.fromCode(loggedIn);
    }


    public boolean isOnline() {
        return loggedIn > LoginState.LOGIN_NOTLOGGEDIN.getCode();
    }


}
