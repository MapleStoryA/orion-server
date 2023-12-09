package constants;

public enum BlockReason {
    HACK(1, "Your account has been blocked for hacking or illegal use of third-party programs."),
    BOT(2, "Your account has been blocked for using macro / auto-keyboard."),
    AD(3, "Your account has been blocked for illicit promotion and advertising."),
    HARASS(4, "Your account has been blocked for harassment."),
    CURSE(5, "Your account has been blocked for using profane language."),
    SCAM(6, "Your account has been blocked for scamming."),
    MISCONDUCT(7, "Your account has been blocked for misconduct."),
    SELL(8, "Your account has been blocked for illegal cash transaction"),
    ILLEGAL_FUNDING(
            9,
            "Your account has been blocked for illegal charging/funding. Please contact customer support for further details."),
    TEMP(
            10,
            "Your account has been blocked for temporary request. Please contact customer support for further details."),
    GM(11, "Your account has been blocked for impersonating GM."),
    ILLEGAL_PROGRAM(12, "Your account has been blocked for using illegal programs or violating the game policy."),
    MEGAPHONE(13, "Your account has been blocked for one of cursing, scamming, or illegal trading via Megaphones.");

    private final int id;
    private final String message;

    BlockReason(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
