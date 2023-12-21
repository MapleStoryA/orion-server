package server.maps;

import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import server.config.ServerConfig;
import tools.collection.Pair;
import tools.helper.StringUtil;

@lombok.extern.slf4j.Slf4j
public class MapleReactorFactory {

    private static final MapleDataProvider data = ServerConfig.serverConfig().getDataProvider("wz/Reactor");
    private static final Map<Integer, MapleReactorStats> reactorStats = new HashMap<Integer, MapleReactorStats>();

    public static final MapleReactorStats getReactor(int rid) {
        MapleReactorStats stats = reactorStats.get(Integer.valueOf(rid));
        if (stats == null) {
            int infoId = rid;
            MapleData reactorData = data.getData(StringUtil.getLeftPaddedStr(infoId + ".img", '0', 11));
            MapleData link = reactorData.getChildByPath("info/link");
            if (link != null) {
                infoId = MapleDataTool.getIntConvert("info/link", reactorData);
                stats = reactorStats.get(Integer.valueOf(infoId));
            }
            if (stats == null) {
                stats = new MapleReactorStats();
                reactorData = data.getData(StringUtil.getLeftPaddedStr(infoId + ".img", '0', 11));
                if (reactorData == null) {
                    return stats;
                }
                boolean areaSet = false;
                boolean foundState = false;
                for (byte i = 0; true; i++) {
                    MapleData reactorD = reactorData.getChildByPath(String.valueOf(i));
                    if (reactorD == null) {
                        break;
                    }
                    MapleData reactorInfoData_ = reactorD.getChildByPath("event");
                    if (reactorInfoData_ != null && reactorInfoData_.getChildByPath("0") != null) {
                        MapleData reactorInfoData = reactorInfoData_.getChildByPath("0");
                        Pair<Integer, Integer> reactItem = null;
                        int type = MapleDataTool.getIntConvert("type", reactorInfoData);
                        if (rid == 5022000) {
                            stats.setTL(MapleDataTool.getPoint("lt", reactorInfoData));
                            stats.setBR(MapleDataTool.getPoint("rb", reactorInfoData));
                            areaSet = true;
                        } else if (type == 100) { // reactor waits for item
                            reactItem = new Pair<Integer, Integer>(
                                    MapleDataTool.getIntConvert("0", reactorInfoData),
                                    MapleDataTool.getIntConvert("1", reactorInfoData, 1));
                            if (!areaSet) { // only set area of effect for item-triggered reactors
                                // once
                                stats.setTL(MapleDataTool.getPoint("lt", reactorInfoData));
                                stats.setBR(MapleDataTool.getPoint("rb", reactorInfoData));
                                areaSet = true;
                            }
                        }
                        foundState = true;
                        stats.addState(
                                i,
                                type,
                                reactItem,
                                (byte) MapleDataTool.getIntConvert("state", reactorInfoData),
                                MapleDataTool.getIntConvert("timeOut", reactorInfoData_, -1));
                    } else {
                        stats.addState(i, 999, null, (byte) (foundState ? -1 : (i + 1)), 0);
                    }
                }
                reactorStats.put(Integer.valueOf(infoId), stats);
                if (rid != infoId) {
                    reactorStats.put(Integer.valueOf(rid), stats);
                }
            } else { // stats exist at infoId but not rid; add to map
                reactorStats.put(Integer.valueOf(rid), stats);
            }
        }
        return stats;
    }
}
