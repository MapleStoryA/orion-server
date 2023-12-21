package provider.drop;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import tools.collection.Pair;

@Slf4j
public class DropDataProvider implements MapleDropProvider {

    private final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();

    private final MapleItemInformationProvider itemProvider = MapleItemInformationProvider.getInstance();

    @Override
    public List<MapleDropData> search(MapleMonster monster) {
        List<MapleDropData> dataList = new ArrayList<>();
        List<MonsterDropEntry> entries = mi.retrieveDrop(monster.getId());

        for (MonsterDropEntry entry : entries) {
            String name = "";
            for (Pair<Integer, String> pair : itemProvider.getAllItems()) {
                if (pair.getLeft() == entry.itemId) {
                    name = pair.getRight();
                    break;
                }
            }
            dataList.add(new MapleDropData(name, monster, entry.chance));
        }

        return dataList;
    }
}
