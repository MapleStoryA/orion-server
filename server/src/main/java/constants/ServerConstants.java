package constants;

import lombok.extern.slf4j.Slf4j;
import java.util.Set;

/**
 * Class containing server constants for the game.
 */
@Slf4j
public class ServerConstants {

    // Constants for items
    public static final int ONE_DAY_ITEM = 5062000; // Cube

    // Event Feature Constants
    public enum EventFeature {
        EXP_ITEM_DROP(false),
        TRIPLE_TRIO(true),
        SHOP_DISCOUNT(false);

        private final boolean isEnabled;

        EventFeature(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        public boolean isEnabled() {
            return isEnabled;
        }
    }

    public static final float SHOP_DISCOUNT_PERCENT = 5f; // Discount percent for shop
    public static final boolean SPEED_QUIZ = true;

    // Constants for game mechanics
    public static final int MAX_ITEMS = 600; // Max items before auto-clearing
    public static final short MAPLE_VERSION = 90; // Game version
    public static final String MAPLE_PATCH = "3"; // Patch number

    // Messages
    public static final String WORLD_MESSAGE = "Welcome to Maple Story Global ";
    public static final String RECOMMENDED_MESSAGE = "We are still in Tespia testing! Report bugs on our forums.";

    private static final Set<Integer> BONUS_EXP_JOBS = Set.of(
            3000, 3200, 3210, 3211, 3212, 3300, 3310, 3311, 3312, 3500, 3510, 3511, 3512
    );

    /**
     * Calculates the bonus experience based on the job.
     *
     * @param job The job identifier.
     * @return The percentage of EXP to increase.
     */
    public static byte calculateBonusExp(final int job) {
        return BONUS_EXP_JOBS.contains(job) ? (byte) 10 : 0;
    }

    /**
     * Gets the respawn rate based on the map ID.
     *
     * @param mapid The map identifier.
     * @return The respawn rate.
     */
    public static int getRespawnRate(final int mapid) {
        return 1; // Consider making this configurable if it varies
    }

    /**
     * Enumeration for player GM rank.
     */
    public enum PlayerGMRank {
        NORMAL('@', 0),
        DONOR('!', 1),
        GM('!', 2),
        ADMIN('!', 3);

        private final char commandPrefix;
        private final int level;

        PlayerGMRank(char commandPrefix, int level) {
            this.commandPrefix = commandPrefix;
            this.level = level;
        }

        public char getCommandPrefix() {
            return commandPrefix;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Enumeration for command types.
     */
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
