package database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * * 3: ID deleted or blocked 4: Incorrect password 5: Not a registered id 6: System error 7:
 * Already logged in 8: System error 9: System error 10: Cannot process so many connections 11: Only
 * users older than 20 can use this channel 13: Unable to log on as master at this ip 14: Wrong
 * gateway or personal info and weird korean button 15: Processing request with that korean button!
 * 16: Please verify your account through email... 17: Wrong gateway or personal info 21: Please
 * verify your account through email... 23: License agreement 25: Maple Europe notice 27: Some weird
 * full client notice, probably for trial versions 32: IP blocked 84: please revisit website for
 * pass change --> 0x07 recv with response 00/01
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginResult {

    public static final int INCORRECT_PASSWORD = 4;
    public static final int NOT_REGISTERED_ID = 5;
    public static final int ALREADY_LOGGED_IN = 7;
    public static final int USER_BANNED = 9;

    private int result;
    private AccountData accountData;

    public boolean isLoginError() {
        return this.result != 0;
    }

    public boolean isPermanentBan() {
        return this.result == USER_BANNED;
    }
}
