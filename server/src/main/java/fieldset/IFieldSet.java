package fieldset;

public interface IFieldSet {


    /**
     * Retrieves a variable value by its name.
     *
     * @param name The name of the variable.
     * @return The value of the variable.
     */
    String getVar(String name);

    /**
     * Sets a variable with the specified name and value.
     *
     * @param name  The name of the variable.
     * @param value The value to set for the variable.
     */
    void setVar(String name, String value);

    /**
     * Gets the count of users in the field set.
     *
     * @return The count of users.
     */
    int getUserCount();

    /**
     * Processes entry of a character into the field set.
     *
     * @param characterId The ID of the character.
     * @param fieldInfo   Field information.
     * @return An integer result of the operation.
     */
    int enter(int characterId, int fieldInfo);

    /**
     * Increases experience for all characters in the field set.
     *
     * @param incExp The amount of experience to increase.
     * @param args   Additional arguments (e.g., quest repetition count).
     * @return An integer result of the operation.
     */
    int incExpAll(int incExp, Object... args);

    /**
     * Gets the state of a reactor in the field set.
     * Returns -1 in case of an error.
     *
     * @param fieldIndex The index of the field.
     * @param name       The name of the reactor.
     * @return The state of the reactor, or -1 in case of an error.
     */
    int getReactorState(int fieldIndex, String name);

    /**
     * Sets the state of a reactor in the field set.
     *
     * @param fieldIndex The index of the field.
     * @param name       The name of the reactor.
     * @param state      The state to set for the reactor.
     * @param delay      The delay before the state change takes effect.
     */
    void setReactorState(int fieldIndex, String name, int state, int delay);

    // Other methods follow the same pattern...
    int getQuestTime();

    void resetQuestTime();

    void transferFieldAll(int mapCode, String portalName);

    void broadcastMsg(int type, String message, Object... args);

    int startManually();

    int resetTimeOut(int setting);

    void setTargetFieldID(int fieldId);

    // Other methods as per the original structure...
}

