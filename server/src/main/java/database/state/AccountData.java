package database.state;

import lombok.Getter;
import lombok.Setter;

import java.io.Externalizable;
import java.time.LocalDate;
import java.util.Calendar;

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
    private Long tempBan;
    private int banned;
    private int gm;
    private int points;
    private int nxCredit;
    private int mPoints;
    private byte gender;


    public LoginState getLoginState() {
        return LoginState.fromCode(loggedIn);
    }


    public boolean isOnline() {
        return loggedIn > LoginState.LOGIN_NOTLOGGEDIN.getCode();
    }

    public Calendar getTempBanCalendar() {
        Calendar lTempban = Calendar.getInstance();
        if (tempBan == 0) { // basically if timestamp in db is 0000-00-00
            lTempban.setTimeInMillis(0);
            return lTempban;
        }
        Calendar today = Calendar.getInstance();
        lTempban.setTimeInMillis(tempBan);
        if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
        }

        lTempban.setTimeInMillis(0);
        return lTempban;
    }


    public boolean isGameMaster() {
        return this.gm > 0;
    }

    public int getGMLevel() {
        return this.getGm();
    }


}
