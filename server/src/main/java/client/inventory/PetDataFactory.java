package client.inventory;

import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import server.config.ServerConfig;
import tools.collection.Pair;

@lombok.extern.slf4j.Slf4j
public class PetDataFactory {

    private static final MapleDataProvider dataRoot =
            ServerConfig.serverConfig().getDataProvider("wz/Item");
    private static final Map<Pair<Integer, Integer>, PetCommand> petCommands =
            new HashMap<Pair<Integer, Integer>, PetCommand>();
    private static final Map<Integer, Integer> petHunger = new HashMap<Integer, Integer>();

    public static final PetCommand getPetCommand(final int petId, final int skillId) {
        PetCommand ret = petCommands.get(new Pair<Integer, Integer>(Integer.valueOf(petId), Integer.valueOf(skillId)));
        if (ret != null) {
            return ret;
        }
        final MapleData skillData = dataRoot.getData("Pet/" + petId + ".img");
        int prob = 0;
        int inc = 0;
        if (skillData != null) {
            prob = MapleDataTool.getInt("interact/" + skillId + "/prob", skillData, 0);
            inc = MapleDataTool.getInt("interact/" + skillId + "/inc", skillData, 0);
        }
        ret = new PetCommand(petId, skillId, prob, inc);
        petCommands.put(new Pair<Integer, Integer>(Integer.valueOf(petId), Integer.valueOf(skillId)), ret);

        return ret;
    }

    public static final int getHunger(final int petId) {
        Integer ret = petHunger.get(Integer.valueOf(petId));
        if (ret != null) {
            return ret;
        }
        final MapleData hungerData = dataRoot.getData("Pet/" + petId + ".img").getChildByPath("info/hungry");
        ret = Integer.valueOf(MapleDataTool.getInt(hungerData, 1));
        petHunger.put(petId, ret);

        return ret;
    }
}
