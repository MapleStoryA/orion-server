package scripting.v1.game;

/**
 * Interface for managing quest records.
 * Important Notes:
 * 1. The key is 4 bytes in size.
 * 2. The maximum length of the value is 16 characters (8 in Korean).
 */
public interface UQuestRecord {

    /**
     * Adds or updates a quest in the list with the given key and value.
     *
     * @param key   The key for the quest.
     * @param value The value associated with the quest.
     */
    void set(int key, String value);

    /**
     * Marks a quest as complete.
     *
     * @param key The key of the quest to mark as complete.
     */
    void setComplete(int key);

    /**
     * Sets the state of a quest.
     * The set and setComplete methods relate to [ Key: quest ID ]
     * [ State: 1 -> Marks the quest as in progress / 2 -> Marks the quest as completed. ]
     * If the state is set to 0, it indicates that the quest is not started.
     *
     * @param key   The key of the quest.
     * @param state The state of the quest.
     */
    void setState(int key, byte state);

    /**
     * Returns the value associated with the given quest key.
     * Returns an empty string if the quest is not found.
     *
     * @param key The key of the quest.
     * @return The value associated with the quest.
     */
    String get(int key);

    /**
     * Gets the state of the quest.
     *
     * @param key The key of the quest.
     * @return The state of the quest.
     */
    int getState(int key);

    /**
     * Checks if a quest can be completed.
     * Return Values: -1 -> Quest not found or invalid / 0 -> Completion not allowed / 1 -> Completion allowed
     *
     * @param key The key of the quest.
     * @return The completion status.
     */
    int canComplete(int key);

    /**
     * Removes a quest entry with the given key.
     *
     * @param key The key of the quest to remove.
     */
    void remove(int key);

    /**
     * Records the mob selected for the quest.
     *
     * @param questId           The ID of the quest.
     * @param mobId             The ID of the mob.
     * @param locationType      The type of location (0 if not a text location, otherwise location value).
     * @param encounterLocation The encounter location.
     */
    void selectedMob(int questId, int mobId, int locationType, int encounterLocation);
}
