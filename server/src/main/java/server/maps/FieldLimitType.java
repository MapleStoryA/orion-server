package server.maps;

public enum FieldLimitType {
    Jump(0x1),
    MovementSkills(0x2),
    SummoningBag(0x04),
    MysticDoor(0x08),
    ChannelSwitch(0x10),
    RegularExpLoss(0x20),
    VipRock(0x40),
    Minigames(0x80),
    NoClue1(0x100), // APQ and a couple quest maps have this
    Mount(0x200),
    NoClue2(0x400), // Monster carnival?
    NoClue3(0x800), // Monster carnival?
    PotionUse(0x1000),
    NoClue4(0x2000), // No notes
    Unused(0x4000),
    NoClue5(0x8000), // Ariant colosseum-related?
    NoClue6(0x10000), // No notes
    DropDown(0x20000), //   NoClue7(0x40000) // Seems to .. disable Rush if 0x2 is set
    ;
    private final int i;

    FieldLimitType(int i) {
        this.i = i;
    }

    public final int getValue() {
        return i;
    }

    public final boolean check(int fieldlimit) {
        return (fieldlimit & i) == i;
    }
}
