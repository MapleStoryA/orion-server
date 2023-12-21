package client.keymap;

import database.DatabaseConnection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;

@Slf4j
public class MapleKeyLayout implements Serializable {

    public static final int[] key = {
        2, 3, 4, 5, 6, 7, 8, 16, 17, 18, 19, 20, 23, 24, 25, 26, 27, 29, 31, 33, 34, 35, 37, 38, 39, 40, 41, 43, 44, 45,
        46, 48, 50, 56, 57, 59, 60, 61, 62, 63, 64, 65
    };
    public static final int[] KeyType = {
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 5, 5, 6, 6,
        6, 6, 6, 6, 6
    };
    public static final int[] action = {
        10, 12, 13, 18, 24, 21, 29, 8, 5, 0, 4, 28, 1, 25, 19, 14, 15, 52, 2, 26, 17, 11, 3, 20, 27, 16, 23, 9, 50, 51,
        6, 22, 7, 53, 54, 100, 101, 102, 103, 104, 105, 106
    };

    private static final long serialVersionUID = 9179541993413738569L;
    private final Map<Integer, KeyMapBinding> keyMapBindings;
    private final int characterId;

    public MapleKeyLayout(int characterId) {
        this.characterId = characterId;
        keyMapBindings = new ConcurrentHashMap<>();
    }

    public void encode(final OutPacket packet) {
        for (int x = 0; x < 90; x++) {
            var binding = keyMapBindings.get(Integer.valueOf(x));
            if (binding != null) {
                packet.write(binding.getType());
                packet.writeInt(binding.getAction());
            } else {
                packet.write(0);
                packet.writeInt(0);
            }
        }
    }

    public final void saveKeys() {
        try (var handle = DatabaseConnection.getConnector().open()) {
            for (var binding : new ArrayList<>(keyMapBindings.values())) {
                if (binding.isDeleted()) {
                    log.debug("Deleting key: {}", binding.getKey());
                    handle.createUpdate(
                                    "DELETE FROM keymap WHERE `characterid` =" + " :d.characterId AND `key` = :d.key")
                            .bindBean("d", binding)
                            .execute();
                    keyMapBindings.remove(binding.getKey());
                }
            }
            for (var binding : keyMapBindings.values()) {
                if (binding.isChanged()) {
                    log.debug("Saving key: {}", binding.getKey());
                    handle.createUpdate("INSERT INTO keymap(`characterid`, `key`, `type`,"
                                    + " `action`, `fixed`) VALUES (:d.characterId,"
                                    + " :d.key, :d.type, :d.action, :d.fixed) ON"
                                    + " DUPLICATE KEY UPDATE `type` = :d.type,"
                                    + " `action` = :d.action,`fixed` = :d.fixed")
                            .bindBean("d", binding)
                            .execute();
                    binding.setChanged(false);
                    binding.setDeleted(false);
                }
            }
        }
    }

    public void setBinding(KeyMapBinding binding) {
        keyMapBindings.put(binding.getKey(), binding);
    }

    public void setDefaultKeys() {
        for (int i = 0; i < key.length; i++) {
            if (ExcludedKeyMap.fromKeyValue(action[i]) != null) {
                continue;
            }
            KeyMapBinding binding = new KeyMapBinding(characterId, key[i], (byte) KeyType[i], action[i], 0);
            binding.setChanged(true);
            setBinding(binding);
        }
    }

    public void loadKeybindings() {
        try (var handle = DatabaseConnection.getConnector().open()) {
            var result = handle.select(
                    "SELECT `key`,`type`,`action`,`fixed` FROM keymap WHERE characterid" + " = ?", characterId);
            List<KeyMapBinding> keyMapBindings = result.map((rs, ctx) -> new KeyMapBinding(
                            characterId, rs.getInt("key"), rs.getByte("type"), rs.getInt("action"), rs.getInt("fixed")))
                    .collectIntoList();
            for (var keyBinding : keyMapBindings) {
                this.keyMapBindings.put(keyBinding.getKey(), keyBinding);
            }
        }
    }
}
