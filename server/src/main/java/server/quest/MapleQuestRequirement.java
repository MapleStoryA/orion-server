package server.quest;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.skill.ISkill;
import client.skill.SkillFactory;
import constants.GameConstants;
import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import provider.MapleData;
import provider.MapleDataTool;
import tools.collection.Pair;

@Slf4j
public class MapleQuestRequirement implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final MapleQuest quest;
    private final MapleQuestRequirementType type;
    private int intStore;
    private String stringStore;
    private List<Pair<Integer, Integer>> dataStore;

    /** Creates a new instance of MapleQuestRequirement */
    public MapleQuestRequirement(MapleQuest quest, MapleQuestRequirementType type, MapleData data) {
        this.type = type;
        this.quest = quest;

        switch (type) {
            case job: {
                final List<MapleData> child = data.getChildren();
                dataStore = new LinkedList<>();

                for (int i = 0; i < child.size(); i++) {
                    dataStore.add(new Pair<>(i, MapleDataTool.getInt(child.get(i), -1)));
                }
                break;
            }
            case skill: {
                final List<MapleData> child = data.getChildren();
                dataStore = new LinkedList<>();

                for (int i = 0; i < child.size(); i++) {
                    final MapleData childdata = child.get(i);
                    dataStore.add(new Pair<>(
                            MapleDataTool.getInt(childdata.getChildByPath("id"), 0),
                            MapleDataTool.getInt(childdata.getChildByPath("acquire"), 0)));
                }
                break;
            }
            case quest: {
                final List<MapleData> child = data.getChildren();
                dataStore = new LinkedList<>();

                for (int i = 0; i < child.size(); i++) {
                    final MapleData childdata = child.get(i);
                    dataStore.add(new Pair<>(
                            MapleDataTool.getInt(childdata.getChildByPath("id")),
                            MapleDataTool.getInt(childdata.getChildByPath("state"), 0)));
                }
                break;
            }
            case item: {
                final List<MapleData> child = data.getChildren();
                dataStore = new LinkedList<>();

                for (int i = 0; i < child.size(); i++) {
                    final MapleData childdata = child.get(i);
                    dataStore.add(new Pair<>(
                            MapleDataTool.getInt(childdata.getChildByPath("id")),
                            MapleDataTool.getInt(childdata.getChildByPath("count"), 0)));
                }
                break;
            }
            case pettamenessmin:
            case npc:
            case questComplete:
            case pop:
            case interval:
            case mbmin:
            case lvmax:
            case lvmin: {
                intStore = MapleDataTool.getInt(data, -1);
                break;
            }
            case end: {
                stringStore = MapleDataTool.getString(data, null);
                break;
            }
            case mob: {
                final List<MapleData> child = data.getChildren();
                dataStore = new LinkedList<>();

                for (int i = 0; i < child.size(); i++) {
                    final MapleData childdata = child.get(i);
                    dataStore.add(new Pair<>(
                            MapleDataTool.getInt(childdata.getChildByPath("id"), 0),
                            MapleDataTool.getInt(childdata.getChildByPath("count"), 0)));
                }
                break;
            }
            case fieldEnter: {
                final MapleData zeroField = data.getChildByPath("0");
                if (zeroField != null) {
                    intStore = MapleDataTool.getInt(zeroField);
                } else {
                    intStore = -1;
                }
                break;
            }
            case mbcard: {
                final List<MapleData> child = data.getChildren();
                dataStore = new LinkedList<>();

                for (int i = 0; i < child.size(); i++) {
                    final MapleData childdata = child.get(i);
                    dataStore.add(new Pair<>(
                            MapleDataTool.getInt(childdata.getChildByPath("id"), 0),
                            MapleDataTool.getInt(childdata.getChildByPath("min"), 0)));
                }
                break;
            }
            case pet: {
                dataStore = new LinkedList<>();

                for (MapleData child : data) {
                    dataStore.add(new Pair<>(-1, MapleDataTool.getInt("id", child, 0)));
                }
                break;
            }
        }
    }

    public boolean check(MapleCharacter c, Integer npcId) {
        switch (type) {
            case job:
                for (Pair<Integer, Integer> a : dataStore) {
                    if (a.getRight() == c.getJob().getId() || c.isGameMaster()) {
                        return true;
                    }
                }
                return false;
            case skill: {
                for (Pair<Integer, Integer> a : dataStore) {
                    final boolean acquire = a.getRight() > 0;
                    final int skill = a.getLeft();
                    final ISkill skil = SkillFactory.getSkill(skill);
                    if (acquire) {
                        if (skil.isFourthJob()) {
                            if (c.getMasterLevel(skil) == 0) {
                                return false;
                            }
                        } else {
                            if (c.getSkillLevel(skil) == 0) {
                                return false;
                            }
                        }
                    } else {
                        if (c.getSkillLevel(skil) > 0 || c.getMasterLevel(skil) > 0) {
                            return false;
                        }
                    }
                }
                return true;
            }
            case quest:
                for (Pair<Integer, Integer> a : dataStore) {
                    final MapleQuestStatus q = c.getQuest(MapleQuest.getInstance(a.getLeft()));
                    final int state = a.getRight();
                    if (state != 0) {
                        if (q == null && state == 0) {
                            continue;
                        }
                        if (q == null || q.getStatus() != state) {
                            return false;
                        }
                    }
                }
                return true;
            case item:
                MapleInventoryType iType;
                int itemId;
                short quantity;

                for (Pair<Integer, Integer> a : dataStore) {
                    itemId = a.getLeft();
                    quantity = 0;
                    iType = GameConstants.getInventoryType(itemId);
                    for (IItem item : c.getInventory(iType).listById(itemId)) {
                        quantity += item.getQuantity();
                    }
                    final int count = a.getRight();
                    if (quantity < count || count <= 0 && quantity > 0) {
                        return false;
                    }
                }
                return true;
            case lvmin:
                return c.getLevel() >= intStore;
            case lvmax:
                return c.getLevel() <= intStore;
            case end:
                final String timeStr = stringStore;
                final Calendar cal = Calendar.getInstance();
                cal.set(
                        Integer.parseInt(timeStr.substring(0, 4)),
                        Integer.parseInt(timeStr.substring(4, 6)),
                        Integer.parseInt(timeStr.substring(6, 8)),
                        Integer.parseInt(timeStr.substring(8, 10)),
                        0);
                return cal.getTimeInMillis() >= System.currentTimeMillis();
            case mob:
                for (Pair<Integer, Integer> a : dataStore) {
                    final int mobId = a.getLeft();
                    final int killReq = a.getRight();
                    if (c.getQuest(quest).getMobKills(mobId) < killReq) {
                        return false;
                    }
                }
                return true;
            case npc:
                return npcId == null || npcId == intStore;
            case fieldEnter:
                if (intStore != -1) {
                    return intStore == c.getMapId();
                }
                return false;
            case mbmin:
                return c.getMonsterBook().getTotalCards() >= intStore;
            case mbcard:
                for (Pair<Integer, Integer> a : dataStore) {
                    final int cardId = a.getLeft();
                    final int killReq = a.getRight();
                    if (c.getMonsterBook().getLevelByCard(cardId) < killReq) {
                        return false;
                    }
                }
                return true;
            case pop:
                return c.getFame() <= intStore;
            case questComplete:
                return c.getNumQuest() >= intStore;
            case interval:
                return c.getQuest(quest).getStatus() != 2
                        || c.getQuest(quest).getCompletionTime() <= System.currentTimeMillis() - intStore * 60 * 1000L;
            case pet:
                for (Pair<Integer, Integer> a : dataStore) {
                    if (c.getPetById(a.getRight()) == -1) {
                        return false;
                    }
                }
                return true;
            case pettamenessmin:
                for (MaplePet pet : c.getPets()) {
                    if (pet.getSummoned() && pet.getCloseness() >= intStore) {
                        return true;
                    }
                }
                return false;
            default:
                return true;
        }
    }

    public MapleQuestRequirementType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
