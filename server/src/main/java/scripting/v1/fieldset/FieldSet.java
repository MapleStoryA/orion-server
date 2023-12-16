package scripting.v1.fieldset;

public interface FieldSet {
    String getVar(String name); // name
    void setVar(String name, String value); // name, value
    int getUserCount();
    int enter(int characterId, int fieldInfo); // CharacterID, Field Info
    int incExpAll(int exp, Object... additionalParams); // incExp, additional parameters (quest repetition count, etc.)
    int getReactorState(int fieldIndex, String name); // fieldIndex, name. Returns -1 on error
    void setReactorState(int fieldIndex, String name, int state, int delay); // fieldIndex, name, state, delay
    int getQuestTime();
    void resetQuestTime();
    void transferFieldAll(int fieldId, String additionalParams);
    void broadcastMsg(int type, String message, Object... additionalParams); // Type(0: normal, 1: alert, 4: slide, 7: NPCSay), Message, additional parameters
    int startManually(); // return [0]: FieldSet still in operation. Restart failed. [1]: Restart successful.
    int resetTimeOut(int timeout); // return [0] : Reset failed [1]: Reset successful. Do not use if resetQuestTime is needed. Outcome not guaranteed.
    void setTargetFieldID(int targetFieldId);
}

