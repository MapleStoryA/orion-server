package database;

public enum LoginState {
    LOGIN_NOTLOGGEDIN(0),
    LOGIN_SERVER_TRANSITION(1),
    LOGIN_LOGGEDIN(2),
    LOGIN_WAITING(3),
    CASH_SHOP_TRANSITION(4),
    LOGIN_CS_LOGGEDIN(5),
    CHANGE_CHANNEL(6);
    ;

    private final int code;

    LoginState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static LoginState fromCode(int code) {
        for (var state : LoginState.values()) {
            if (code == state.code) {
                return state;
            }
        }
        return null;
    }
}
