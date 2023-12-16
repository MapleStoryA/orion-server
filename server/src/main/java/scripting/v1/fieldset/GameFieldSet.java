package scripting.v1.fieldset;

public class GameFieldSet implements FieldSet {

    private final String name;

    public GameFieldSet(String name) {
        this.name = name;
    }


    @Override
    public String getVar(String name) {
        return null;
    }

    @Override
    public void setVar(String name, String value) {

    }

    @Override
    public int getUserCount() {
        return 0;
    }

    public int enter(int characterID, int fieldInfo) {
        // Load script
        return 0;
    }

    @Override
    public int incExpAll(int exp, Object... additionalParams) {
        return 0;
    }

    @Override
    public int getReactorState(int fieldIndex, String name) {
        return 0;
    }

    @Override
    public void setReactorState(int fieldIndex, String name, int state, int delay) {

    }

    @Override
    public int getQuestTime() {
        return 0;
    }

    @Override
    public void resetQuestTime() {

    }

    @Override
    public void transferFieldAll(int fieldId, String additionalParams) {

    }

    @Override
    public void broadcastMsg(int type, String message, Object... additionalParams) {

    }

    @Override
    public int startManually() {
        return 0;
    }

    @Override
    public int resetTimeOut(int timeout) {
        return 0;
    }

    @Override
    public void setTargetFieldID(int targetFieldId) {

    }
}
