package database;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

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
    private byte greason;
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

    public boolean isGameMaster() {
        return this.gm > 0;
    }

    public int getGMLevel() {
        return this.gm;
    }
}
