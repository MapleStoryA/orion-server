package fieldset;

public class FieldSet implements IFieldSet {

    private final String name;

    /**
     * Constructor for FieldSet with a specific name.
     *
     * @param name The name of the field set.
     */
    public FieldSet(String name) {
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

    @Override
    public int enter(int characterId, int fieldInfo) {
        return 0;
    }

    @Override
    public int incExpAll(int incExp, Object... args) {
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
    public void transferFieldAll(int mapCode, String portalName) {

    }

    @Override
    public void broadcastMsg(int type, String message, Object... args) {

    }

    @Override
    public int startManually() {
        return 0;
    }

    @Override
    public int resetTimeOut(int setting) {
        return 0;
    }

    @Override
    public void setTargetFieldID(int fieldId) {

    }
}
