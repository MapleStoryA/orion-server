package client.layout;


import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.annotation.JdbiProperty;

public class KeyMapBinding {

    @Getter
    private final int characterId;
    @Getter
    @Setter
    private int key;
    @Getter
    private final byte type;
    @Getter
    private final int action;

    @Getter
    private final int fixed;

    @Getter
    @Setter
    @JdbiProperty(map = false)
    private boolean changed = false;

    @Getter
    @Setter
    @JdbiProperty(map = false)
    private boolean deleted = false;

    public KeyMapBinding(int characterId, int key, byte type, int action, int fixed) {
        this.characterId = characterId;
        this.key = key;
        this.type = type;
        this.action = action;
        this.fixed = fixed;
    }

    private boolean isRemove() {
        return type == 0;
    }
}
