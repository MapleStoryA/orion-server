package server.quest;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.MapleStat;
import client.inventory.InventoryException;
import client.inventory.MapleInventoryType;
import client.skill.ISkill;
import client.skill.SkillFactory;
import constants.GameConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.helper.Randomizer;
import tools.packet.CWVsContextOnMessagePackets;

@Slf4j
public class MapleQuestAction implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final MapleQuestActionType type;
    private final MapleData data;
    private final MapleQuest quest;

    /** Creates a new instance of MapleQuestAction */
    public MapleQuestAction(MapleQuestActionType type, MapleData data, MapleQuest quest) {
        this.type = type;
        this.data = data;
        this.quest = quest;
    }

    private static boolean canGetItem(MapleData item, MapleCharacter c) {
        if (item.getChildByPath("gender") != null) {
            final int gender = MapleDataTool.getInt(item.getChildByPath("gender"));
            if (gender != 2 && gender != c.getGender()) {
                return false;
            }
        }
        if (item.getChildByPath("job") != null) {
            final int job = MapleDataTool.getInt(item.getChildByPath("job"));
            final List<Integer> code = getJobBy5ByteEncoding(job);
            boolean jobFound = false;
            for (int codec : code) {
                if (codec / 100 == c.getJob().getId() / 100) {
                    jobFound = true;
                    break;
                }
            }
            if (!jobFound && item.getChildByPath("jobEx") != null) {
                final int jobEx = MapleDataTool.getInt(item.getChildByPath("jobEx"));
                final List<Integer> codeEx = getJobBy5ByteEncoding(jobEx);
                for (int codec : codeEx) {
                    if (codec / 100 == c.getJob().getId() / 100) {
                        jobFound = true;
                        break;
                    }
                }
            }
            return jobFound;
        }
        return true;
    }

    private static List<Integer> getJobBy5ByteEncoding(int encoded) {
        List<Integer> ret = new ArrayList<>();
        if ((encoded & 0x1) != 0) {
            ret.add(0);
        }
        if ((encoded & 0x2) != 0) {
            ret.add(100);
        }
        if ((encoded & 0x4) != 0) {
            ret.add(200);
        }
        if ((encoded & 0x8) != 0) {
            ret.add(300);
        }
        if ((encoded & 0x10) != 0) {
            ret.add(400);
        }
        if ((encoded & 0x20) != 0) {
            ret.add(500);
        }
        if ((encoded & 0x400) != 0) {
            ret.add(1000);
        }
        if ((encoded & 0x800) != 0) {
            ret.add(1100);
        }
        if ((encoded & 0x1000) != 0) {
            ret.add(1200);
        }
        if ((encoded & 0x2000) != 0) {
            ret.add(1300);
        }
        if ((encoded & 0x4000) != 0) {
            ret.add(1400);
        }
        if ((encoded & 0x8000) != 0) {
            ret.add(1500);
        }
        if ((encoded & 0x20000) != 0) {
            ret.add(2001); // im not sure of this one
            ret.add(2200);
        }
        if ((encoded & 0x100000) != 0) {
            ret.add(2000);
            ret.add(2001); // ?
        }
        if ((encoded & 0x200000) != 0) {
            ret.add(2100);
        }
        if ((encoded & 0x400000) != 0) {
            ret.add(2001); // ?
            ret.add(2200);
        }

        if ((encoded & 0x40000000) != 0) { // i haven't seen any higher than this o.o
            ret.add(3000);
            ret.add(3200);
            ret.add(3300);
            ret.add(3500);
        }
        return ret;
    }

    public final boolean RestoreLostItem(final MapleCharacter c, final int itemid) {
        if (type == MapleQuestActionType.item) {
            int retitem;

            for (final MapleData iEntry : data.getChildren()) {
                retitem = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
                if (retitem == itemid) {
                    if (!c.haveItem(retitem, 1, true, false)) {
                        MapleInventoryManipulator.addById(c.getClient(), retitem, (short) 1, "");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void runStart(MapleCharacter c, Integer extSelection) {
        MapleQuestStatus status;
        switch (type) {
            case exp:
                status = c.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                int expRate = GameConstants.getExpRate_Quest(c.getLevel());
                if (c.getLevel() < 10) {
                    expRate = 1;
                }
                c.gainExp(MapleDataTool.getInt(data, 0) * expRate, true, true, true);
                break;
            case item:
                // first check for randomness in item selection
                Map<Integer, Integer> props = new HashMap<>();
                MapleData prop;
                for (MapleData iEntry : data.getChildren()) {
                    prop = iEntry.getChildByPath("prop");
                    if (prop != null && MapleDataTool.getInt(prop) != -1 && canGetItem(iEntry, c)) {
                        for (int i = 0; i < MapleDataTool.getInt(iEntry.getChildByPath("prop")); i++) {
                            props.put(props.size(), MapleDataTool.getInt(iEntry.getChildByPath("id")));
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = props.get(Randomizer.nextInt(props.size()));
                }
                for (MapleData iEntry : data.getChildren()) {
                    if (!canGetItem(iEntry, c)) {
                        continue;
                    }
                    final int id = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
                    if (iEntry.getChildByPath("prop") != null) {
                        if (MapleDataTool.getInt(iEntry.getChildByPath("prop")) == -1) {
                            if (extSelection != extNum++) {
                                continue;
                            }
                        } else if (id != selection) {
                            continue;
                        }
                    }
                    final short count = (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 1);
                    if (count < 0) { // remove items
                        try {
                            MapleInventoryManipulator.removeById(
                                    c.getClient(), GameConstants.getInventoryType(id), id, (count * -1), true, false);
                        } catch (InventoryException ie) {
                            // it's better to catch this here so we'll atleast try to remove the
                            // other items
                            System.err.println("[h4x] Completing a quest without meeting the requirements" + ie);
                        }
                        c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    } else { // add items
                        final int period =
                                MapleDataTool.getInt(iEntry.getChildByPath("period"), 0) / 1440; // im guessing.
                        final String name =
                                MapleItemInformationProvider.getInstance().getName(id);
                        if (id / 10000 == 114 && name != null && name.length() > 0) { // medal
                            final String msg = "You have attained title <" + name + ">";
                            c.dropMessage(-1, msg);
                            c.dropMessage(5, msg);
                        }
                        MapleInventoryManipulator.addById(c.getClient(), id, count, "", null, period);
                        c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    }
                }
                break;
            case nextQuest:
                status = c.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                c.getClient()
                        .getSession()
                        .write(MaplePacketCreator.updateQuestFinish(
                                quest.getId(), status.getNpc(), MapleDataTool.getInt(data)));
                break;
            case money:
                status = c.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                c.gainMeso(MapleDataTool.getInt(data, 0), true, false, true);
                break;
            case quest:
                for (MapleData qEntry : data) {
                    c.updateQuest(new MapleQuestStatus(
                            MapleQuest.getInstance(MapleDataTool.getInt(qEntry.getChildByPath("id"))),
                            (byte) MapleDataTool.getInt(qEntry.getChildByPath("state"), 0)));
                }
                break;
            case skill:
                // TODO needs gain/lost message?
                for (MapleData sEntry : data) {
                    final int skillid = MapleDataTool.getInt(sEntry.getChildByPath("id"));
                    int skillLevel = MapleDataTool.getInt(sEntry.getChildByPath("skillLevel"), 0);
                    int masterLevel = MapleDataTool.getInt(sEntry.getChildByPath("masterLevel"), 0);
                    final ISkill skillObject = SkillFactory.getSkill(skillid);

                    for (MapleData applicableJob : sEntry.getChildByPath("job")) {
                        if (skillObject.isBeginnerSkill()
                                || c.getJob().getId() == MapleDataTool.getInt(applicableJob)) {
                            c.changeSkillLevel(
                                    skillObject, (byte) Math.max(skillLevel, c.getSkillLevel(skillObject)), (byte)
                                            Math.max(masterLevel, c.getMasterLevel(skillObject)));
                            break;
                        }
                    }
                }
                break;
            case pop:
                status = c.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                final int fameGain = MapleDataTool.getInt(data, 0);
                c.addFame(fameGain);
                c.updateSingleStat(MapleStat.FAME, c.getFame());
                c.getClient().getSession().write(MaplePacketCreator.getShowFameGain(fameGain));
                break;
            case buffItemID:
                status = c.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                final int tobuff = MapleDataTool.getInt(data, -1);
                if (tobuff == -1) {
                    break;
                }
                MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(c);
                break;
            case infoNumber: {
                //		log.info("quest : "+MapleDataTool.getInt(data, 0)+"");
                //		MapleQuest.getInstance(MapleDataTool.getInt(data, 0)).forceComplete(c, 0);
                break;
            }
            default:
                break;
        }
    }

    public boolean checkEnd(MapleCharacter c, Integer extSelection) {
        switch (type) {
            case item: {
                // first check for randomness in item selection
                final Map<Integer, Integer> props = new HashMap<>();

                for (MapleData iEntry : data.getChildren()) {
                    final MapleData prop = iEntry.getChildByPath("prop");
                    if (prop != null && MapleDataTool.getInt(prop) != -1 && canGetItem(iEntry, c)) {
                        for (int i = 0; i < MapleDataTool.getInt(iEntry.getChildByPath("prop")); i++) {
                            props.put(props.size(), MapleDataTool.getInt(iEntry.getChildByPath("id")));
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = props.get(Randomizer.nextInt(props.size()));
                }
                byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;

                for (MapleData iEntry : data.getChildren()) {
                    if (!canGetItem(iEntry, c)) {
                        continue;
                    }
                    final int id = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
                    if (iEntry.getChildByPath("prop") != null) {
                        if (MapleDataTool.getInt(iEntry.getChildByPath("prop")) == -1) {
                            if (extSelection != extNum++) {
                                continue;
                            }
                        } else if (id != selection) {
                            continue;
                        }
                    }
                    final short count = (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 1);
                    if (count < 0) { // remove items
                        if (!c.haveItem(id, count, false, true)) {
                            c.dropMessage(1, "You are short of some item to complete quest.");
                            return false;
                        }
                    } else { // add items
                        switch (GameConstants.getInventoryType(id)) {
                            case EQUIP:
                                eq++;
                                break;
                            case USE:
                                use++;
                                break;
                            case SETUP:
                                setup++;
                                break;
                            case ETC:
                                etc++;
                                break;
                            case CASH:
                                cash++;
                                break;
                        }
                    }
                }
                if (c.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq) {
                    c.dropMessage(1, "Please make space for your Equip inventory.");
                    return false;
                } else if (c.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use) {
                    c.dropMessage(1, "Please make space for your Use inventory.");
                    return false;
                } else if (c.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup) {
                    c.dropMessage(1, "Please make space for your Setup inventory.");
                    return false;
                } else if (c.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc) {
                    c.dropMessage(1, "Please make space for your Etc inventory.");
                    return false;
                } else if (c.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
                    c.dropMessage(1, "Please make space for your Cash inventory.");
                    return false;
                }
                return true;
            }
            case money: {
                final int meso = MapleDataTool.getInt(data, 0);
                if (c.getMeso() + meso < 0) { // Giving, overflow
                    c.dropMessage(1, "Meso exceed the max amount, 2147483647.");
                    return false;
                } else if (meso < 0 && c.getMeso() < Math.abs(meso)) { // remove meso
                    c.dropMessage(1, "Insufficient meso.");
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    public void runEnd(MapleCharacter c, Integer extSelection) {
        switch (type) {
            case exp: {
                int expRate = GameConstants.getExpRate_Quest(c.getLevel());
                if (c.getLevel() < 10) {
                    expRate = 1;
                }
                c.gainExp(MapleDataTool.getInt(data, 0) * expRate, true, true, true);
                break;
            }
            case item: {
                // first check for randomness in item selection
                Map<Integer, Integer> props = new HashMap<>();

                for (MapleData iEntry : data.getChildren()) {
                    final MapleData prop = iEntry.getChildByPath("prop");
                    if (prop != null && MapleDataTool.getInt(prop) != -1 && canGetItem(iEntry, c)) {
                        for (int i = 0; i < MapleDataTool.getInt(iEntry.getChildByPath("prop")); i++) {
                            props.put(props.size(), MapleDataTool.getInt(iEntry.getChildByPath("id")));
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = props.get(Randomizer.nextInt(props.size()));
                }
                for (MapleData iEntry : data.getChildren()) {
                    if (!canGetItem(iEntry, c)) {
                        continue;
                    }
                    final int id = MapleDataTool.getInt(iEntry.getChildByPath("id"), -1);
                    if (iEntry.getChildByPath("prop") != null) {
                        if (MapleDataTool.getInt(iEntry.getChildByPath("prop")) == -1) {
                            if (extSelection != extNum++) {
                                continue;
                            }
                        } else if (id != selection) {
                            continue;
                        }
                    }
                    final short count = (short) MapleDataTool.getInt(iEntry.getChildByPath("count"), 1);
                    if (count < 0) { // remove items
                        MapleInventoryManipulator.removeById(
                                c.getClient(), GameConstants.getInventoryType(id), id, (count * -1), true, false);
                        c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    } else { // add items
                        final int period = MapleDataTool.getInt(iEntry.getChildByPath("period"), 0) / 1440;
                        final String name =
                                MapleItemInformationProvider.getInstance().getName(id);
                        if (id / 10000 == 114 && name != null && name.length() > 0) { // medal
                            final String msg = "You have attained title <" + name + ">";
                            c.dropMessage(-1, msg);
                            c.dropMessage(5, msg);
                        }
                        MapleInventoryManipulator.addById(c.getClient(), id, count, "", null, period);
                        c.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, count, true));
                    }
                }
                break;
            }
            case nextQuest: {
                c.getClient()
                        .getSession()
                        .write(MaplePacketCreator.updateQuestFinish(
                                quest.getId(), c.getQuest(quest).getNpc(), MapleDataTool.getInt(data)));
                break;
            }
            case money: {
                c.gainMeso(MapleDataTool.getInt(data, 0), true, false, true);
                break;
            }
            case quest: {
                for (MapleData qEntry : data) {
                    c.updateQuest(new MapleQuestStatus(
                            MapleQuest.getInstance(MapleDataTool.getInt(qEntry.getChildByPath("id"))),
                            (byte) MapleDataTool.getInt(qEntry.getChildByPath("state"), 0)));
                }
                break;
            }
            case skill: {
                for (MapleData sEntry : data) {
                    final int skillid = MapleDataTool.getInt(sEntry.getChildByPath("id"));
                    int skillLevel = MapleDataTool.getInt(sEntry.getChildByPath("skillLevel"), 0);
                    int masterLevel = MapleDataTool.getInt(sEntry.getChildByPath("masterLevel"), 0);
                    final ISkill skillObject = SkillFactory.getSkill(skillid);

                    for (MapleData applicableJob : sEntry.getChildByPath("job")) {
                        if (skillObject.isBeginnerSkill()
                                || c.getJob().getId() == MapleDataTool.getInt(applicableJob)) {
                            c.changeSkillLevel(
                                    skillObject, (byte) Math.max(skillLevel, c.getSkillLevel(skillObject)), (byte)
                                            Math.max(masterLevel, c.getMasterLevel(skillObject)));
                            break;
                        }
                    }
                }
                break;
            }
            case pop: {
                final int fameGain = MapleDataTool.getInt(data, 0);
                c.addFame(fameGain);
                c.updateSingleStat(MapleStat.FAME, c.getFame());
                c.getClient().getSession().write(MaplePacketCreator.getShowFameGain(fameGain));
                break;
            }
            case sp: {
                for (MapleData sEntry : data) {
                    final int sp = MapleDataTool.getInt(sEntry.getChildByPath("sp_value"));
                    c.gainSp(sp);
                    c.getClient().sendPacket(CWVsContextOnMessagePackets.onIncSpMessage(c.getJob(), sp));
                    break;
                }
                break;
            }
            case buffItemID: {
                final int tobuff = MapleDataTool.getInt(data, -1);
                if (tobuff == -1) {
                    break;
                }
                MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(c);
                break;
            }
            case infoNumber: {
                //		log.info("quest : "+MapleDataTool.getInt(data, 0)+"");
                //		MapleQuest.getInstance(MapleDataTool.getInt(data, 0)).forceComplete(c, 0);
                break;
            }
            default:
                log.info("Invalid quest action");
                break;
        }
    }

    public MapleQuestActionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + ": " + data;
    }
}
