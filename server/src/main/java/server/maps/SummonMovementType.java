package server.maps;

public enum SummonMovementType {
    STATIONARY(0), // octo etc
    FOLLOW(1), // 4th job mage
    WALK_STATIONARY(2),
    CIRCLE_FOLLOW(4), // bowman summons
    CIRCLE_STATIONARY(5); // gavi only
    // 3, 6,7, etc is tele follow. idk any skills that use
    private final int val;

    SummonMovementType(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }
}
