package constants;

@lombok.extern.slf4j.Slf4j
public class ServerConstants {

    public static final int ONE_DAY_ITEM = 5062000; // cube
    // Event Constants
    // Allows all mobs to drop EXP Item Card
    public static final boolean EXPItemDrop = false;
    // Bonus EXP every 3rd mob killed
    public static final boolean TRIPLE_TRIO = true;
    // Shop discount for potions
    public static final boolean SHOP_DISCOUNT = false;

    public static final float SHOP_DISCOUNT_PERCENT = 5f; // float = round up.
    //
    public static final boolean SPEED_QUIZ = true;

    // Default is 500. If the map contains > this amount, it will automatically clear drops
    public static final int MAX_ITEMS = 600;
    // End of Poll
    public static final short MAPLE_VERSION = 90;

    public static final String MAPLE_PATCH = "3";

    public static final String WORLD_MESSAGE = "Welcome to Maple Story Global ";

    public static final String RECOMMENDED_MESSAGE = "We are still in Tespia testing! Report bugs on our forums.";

    /*
     * Specifics which job gives an additional EXP to party
     * returns the percentage of EXP to increase
     */
    public static byte calculate_bonus_exp(final int job) {
        switch (job) {
            case 3000: // whenever these arrive, they'll give bonus
            case 3200:
            case 3210:
            case 3211:
            case 3212:
            case 3300:
            case 3310:
            case 3311:
            case 3312:
            case 3500:
            case 3510:
            case 3511:
            case 3512:
                return 10;
        }
        return 0;
    }

    public static int getRespawnRate(final int mapid) {
        return 1;
    }

    public enum PlayerGMRank {
        NORMAL('@', 0),
        DONOR('!', 1),
        GM('!', 2),
        ADMIN('!', 3);
        private final char commandPrefix;
        private final int level;

        PlayerGMRank(char ch, int level) {
            commandPrefix = ch;
            this.level = level;
        }

        public char getCommandPrefix() {
            return commandPrefix;
        }

        public int getLevel() {
            return level;
        }
    }

    public enum CommandType {
        NORMAL(0),
        TRADE(1);
        private final int level;

        CommandType(int level) {
            this.level = level;
        }

        public int getType() {
            return level;
        }
    }
}
