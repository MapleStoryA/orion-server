package tools.packet;

import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import client.MapleQuestStatus;
import client.inventory.Equip;
import client.inventory.IEquip;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import client.skill.EvanSkillPoints;
import client.skill.ISkill;
import client.skill.SkillEntry;
import constants.GameConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.shops.AbstractPlayerStore;
import server.shops.IMaplePlayerShop;
import tools.KoreanDateUtil;
import tools.Triple;
import tools.data.output.COutPacket;

@lombok.extern.slf4j.Slf4j
public class PacketHelper {

    public static final long MAX_TIME = 150842304000000000L;
    public static final long ZERO_TIME = 94354848000000000L;
    public static final long PERMANENT = 150841440000000000L;
    private static final long FT_UT_OFFSET = 116444592000000000L; // EDT

    public static final long getKoreanTimestamp(final long realTimestamp) {
        return getTime(realTimestamp);
    }

    public static final long getTime(final long realTimestamp) {
        if (realTimestamp == -1) {
            return MAX_TIME;
        } else if (realTimestamp == -2) {
            return ZERO_TIME;
        } else if (realTimestamp == -3) {
            return PERMANENT;
        }
        long time = (realTimestamp / 1000); // convert to seconds
        return ((time * 10000000) + FT_UT_OFFSET);
    }

    public static void addQuestInfo(
            final COutPacket packet, final MapleCharacter chr) {
        final List<MapleQuestStatus> started = chr.getStartedQuests();
        packet.writeShort(started.size());

        for (final MapleQuestStatus q : started) {
            packet.writeShort(q.getQuest().getId());
            packet.writeMapleAsciiString(q.getCustomData() != null ? q.getCustomData() : "");
        }
        final List<MapleQuestStatus> completed = chr.getCompletedQuests();
        int time;
        packet.writeShort(completed.size());

        for (final MapleQuestStatus q : completed) {
            packet.writeShort(q.getQuest().getId());
            time = KoreanDateUtil.getQuestTimestamp(q.getCompletionTime());
            packet.writeInt(time); // maybe start time? no effect.
            packet.writeInt(time); // completion time
        }
    }

    public static final void addSkillInfo(
            final COutPacket packet, final MapleCharacter chr) {
        final Map<ISkill, SkillEntry> skills = chr.getSkills();
        packet.writeShort(skills.size());
        for (final Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
            packet.writeInt(skill.getKey().getId());
            packet.writeInt(skill.getValue().skillevel);
            addExpirationTime(packet, skill.getValue().expiration);
            if (skill.getKey().hasMastery()) {
                packet.writeInt(skill.getValue().masterlevel);
            }
        }
    }

    public static final void addCoolDownInfo(
            final COutPacket packet, final MapleCharacter chr) {
        final List<MapleCoolDownValueHolder> cd = chr.getCooldowns();
        packet.writeShort(cd.size());
        for (final MapleCoolDownValueHolder cooling : cd) {
            packet.writeInt(cooling.getSkillId());
            packet.writeShort(
                    (int)
                                    (cooling.getLength()
                                            + cooling.getStartTime()
                                            - System.currentTimeMillis())
                            / 1000);
        }
    }

    public static final void addRocksInfo(
            final COutPacket packet, final MapleCharacter chr) {
        chr.getRegTeleportRock().encode(packet);
        chr.getVipTeleportRock().encode(packet);
    }

    public static final void addMonsterBookInfo(
            final COutPacket packet, final MapleCharacter chr) {
        packet.writeInt(chr.getMonsterBookCover());
        packet.write(0);
        chr.getMonsterBook().addCardPacket(packet);
    }

    public static final void addRingInfo(
            final COutPacket packet, final MapleCharacter chr) {
        packet.writeShort(0);
        // 01 00 = size
        // 01 00 00 00 = gametype?
        // 03 00 00 00 = win
        // 00 00 00 00 = tie/loss
        // 01 00 00 00 = tie/loss
        // 16 08 00 00 = points
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
        List<MapleRing> cRing = aRing.getLeft();
        packet.writeShort(cRing.size()); // Couple
        for (MapleRing ring : cRing) {
            packet.writeInt(ring.getPartnerChrId());
            packet.writeAsciiString(ring.getPartnerName(), 13);
            packet.writeLong(ring.getRingId());
            packet.writeLong(ring.getPartnerRingId());
        }
        List<MapleRing> fRing = aRing.getMid();
        packet.writeShort(fRing.size()); // Friends
        for (MapleRing ring : fRing) {
            packet.writeInt(ring.getPartnerChrId());
            packet.writeAsciiString(ring.getPartnerName(), 13);
            packet.writeLong(ring.getRingId());
            packet.writeLong(ring.getPartnerRingId());
            packet.writeInt(ring.getItemId());
        }
        List<MapleRing> mRing = aRing.getRight();
        packet.writeShort(mRing.size()); // Marriage [48]
        int marriageId = 30000;
        for (MapleRing ring : mRing) { // We only can have 1 marriage ring, so yeah..
            packet.writeInt(marriageId); // Engagement id.
            packet.writeInt(chr.getId());
            packet.writeInt(ring.getPartnerChrId());
            packet.writeShort(0 /*ring.getStatus()*/);
            packet.writeInt(ring.getItemId());
            packet.writeInt(ring.getItemId());
            packet.writeAsciiString(chr.getName(), 13);
            packet.writeAsciiString(ring.getPartnerName(), 13);
        }
    }

    public static void addInventoryInfo(COutPacket packet, MapleCharacter chr) {
        packet.writeInt(chr.getMeso()); // mesos
        packet.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit()); // equip slots
        packet.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit()); // use slots
        packet.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit()); // set-up slots
        packet.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit()); // etc slots
        packet.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit()); // cash slots

        packet.writeLong(getTime(-2)); // extra pendant slot
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<>(equippedC.size());

        for (IItem item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);
        for (Item item : equipped) {
            if (item.getPosition() < 0 && item.getPosition() > -100) {
                addItemInfo(packet, item, false, false);
            }
        }
        packet.writeShort(0); // start of equipped nx
        for (Item item : equipped) {
            if (item.getPosition() <= -100 && item.getPosition() > -1000) {
                addItemInfo(packet, item, false, false);
            }
        }
        packet.writeShort(0); // start of equip inventory
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (IItem item : iv.list()) {
            addItemInfo(packet, item, false, false);
        }
        packet.writeShort(0); // start of other equips
        for (Item item : equipped) {
            if (item.getPosition() <= -1000 && item.getPosition() > -1100) {
                addItemInfo(packet, item, false, false);
            }
        }
        packet.writeShort(0); // start of use inventory
        iv = chr.getInventory(MapleInventoryType.USE);
        for (IItem item : iv.list()) {
            addItemInfo(packet, item, false, false);
        }
        packet.write(0); // start of set-up inventory
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (IItem item : iv.list()) {
            addItemInfo(packet, item, false, false);
        }
        packet.write(0); // start of etc inventory
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (IItem item : iv.list()) {
            addItemInfo(packet, item, false, false);
        }
        packet.write(0); // start of cash inventory
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (IItem item : iv.list()) {
            addItemInfo(packet, item, false, false);
        }
        packet.write(0); // start of extended slots
    }

    public static final void addCharStats(
            final COutPacket packet, final MapleCharacter chr) {
        packet.writeInt(chr.getId()); // character id
        packet.writeAsciiString(chr.getName(), 13);
        packet.write(chr.getGender()); // gender (0 = male, 1 = female)
        packet.write(chr.getSkinColor()); // skin color
        packet.writeInt(chr.getFace()); // face
        packet.writeInt(chr.getHair()); // hair
        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) {
                packet.writeLong(chr.getPet(i).getUniqueId());
            } else {
                packet.writeLong(0);
            }
        }
        packet.write(chr.getLevel()); // level
        packet.writeShort(chr.getJob().getId()); // job
        chr.getStat().connectData(packet);
        packet.writeShort(Math.min(199, chr.getRemainingAp())); // Avoid Popup
        if (chr.getJob().isEvan() && (chr.getLevel() >= 10) && (chr.getJob().getId() != 2001)) {
            EvanSkillPoints esp;
            esp = chr.getEvanSP();
            packet.write(esp.getSkillPoints().keySet().size());
            for (Iterator i$ = esp.getSkillPoints().keySet().iterator(); i$.hasNext(); ) {
                int i = ((Integer) i$.next()).intValue();
                packet.write(i == 2200 ? 1 : i - 2208);
                packet.write(esp.getSkillPoints(i));
            }
        } else if (chr.getJob().getId() == 2001) {
            packet.write(0);
        } else {
            packet.writeShort(chr.getRemainingSp()); // remaining sp
        }
        packet.writeInt(chr.getExp()); // exp
        packet.writeShort(chr.getFame()); // fame
        packet.writeInt(0); // Gachapon exp
        packet.writeInt(chr.getMapId()); // current map id
        packet.write(chr.getInitialSpawnpoint()); // spawnpoint
        packet.writeInt(0);
        packet.writeShort(chr.getSubCategoryField()); // 1 here = db
    }

    public static final void addCharLook(
            final COutPacket packet,
            final MapleCharacter chr,
            final boolean mega) {
        packet.write(chr.getGender());
        packet.write(chr.getSkinColor());
        packet.writeInt(chr.getFace());
        packet.write(mega ? 0 : 1);
        packet.writeInt(chr.getHair());

        final Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);

        for (final IItem item : equip.list()) {
            if (item instanceof Equip) {
                Equip currentEquip = (Equip) item;
                if (!chr.getJob().isGameMasterJob()) {
                    if (currentEquip.getRequiredStr() > chr.getStat().getTotalStr()) {
                        continue;
                    }
                    if (currentEquip.getRequiredDex() > chr.getStat().getTotalDex()) {
                        continue;
                    }
                    if (currentEquip.getRequiredInt() > chr.getStat().getTotalInt()) {
                        continue;
                    }
                    if (currentEquip.getRequiredLuk() > chr.getStat().getTotalLuk()) {
                        continue;
                    }
                    boolean isLevel0 = currentEquip.getRequiredLevel() == 0;
                    if (!isLevel0) {
                        if (currentEquip.getRequiredLevel() > chr.getLevel()) {
                            continue;
                        }
                    }
                }
            }

            if (item.isNotVisible()) {
                continue;
            }
            byte pos = (byte) (item.getPosition() * -1);

            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if ((pos > 100 || pos == -128) && pos != 111) {
                pos = (byte) (pos == -128 ? 28 : pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (final Entry<Byte, Integer> entry : myEquip.entrySet()) {
            packet.write(entry.getKey());
            packet.writeInt(entry.getValue());
        }

        packet.write(0xFF);

        // end of visible itens
        // masked itens
        for (final Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            packet.write(entry.getKey());
            packet.writeInt(entry.getValue());
        }
        packet.write(0xFF); // ending markers

        final IItem cWeapon = equip.getItem((byte) -111);
        packet.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
        for (int i = 0; i < 3; i++) {
            if (chr.getPet(i) != null) {
                packet.writeInt(chr.getPet(i).getPetItemId());
            } else {
                packet.writeInt(0);
            }
        }
    }

    public static final void addExpirationTime(
            final COutPacket packet, final long time) {
        packet.write(0);
        packet.writeShort(1408); // 80 05
        if (time != -1) {
            packet.writeInt(KoreanDateUtil.getItemTimestamp(time));
            packet.write(1);
        } else {
            packet.writeInt(400967355);
            packet.write(2);
        }
    }

    public static final void addItemInfo(
            final COutPacket packet,
            final IItem item,
            final boolean zeroPosition,
            final boolean leaveOut) {
        addItemInfo(packet, item, zeroPosition, leaveOut, false);
    }

    public static final void addItemInfo(
            final COutPacket packet,
            final IItem item,
            final boolean zeroPosition,
            final boolean leaveOut,
            final boolean trade) {
        short pos = item.getPosition();
        if (zeroPosition) {
            if (!leaveOut) {
                packet.write(0);
            }
        } else {
            if (pos <= -1) {
                pos *= -1;
                if (pos > 100 && pos < 1000) {
                    pos -= 100;
                }
            }
            if (!trade && item.getType() == 1) {
                packet.writeShort(pos);
            } else {
                packet.write(pos);
            }
        }
        packet.write(item.getPet() != null ? 3 : item.getType());
        packet.writeInt(item.getItemId());
        boolean hasUniqueId = item.getSN() > 0;
        // marriage rings arent cash items so dont have uniqueids, but we assign them anyway for the
        // sake of rings
        packet.write(hasUniqueId ? 1 : 0);
        if (hasUniqueId) {
            packet.writeLong(item.getSN());
        }

        if (item.getPet() != null) { // Pet
            addPetItemInfo(packet, item, item.getPet());
        } else {
            addExpirationTime(packet, item.getExpiration());
            if (item.getType() == 1) {
                final IEquip equip = (IEquip) item;
                packet.write(equip.getUpgradeSlots());
                packet.write(equip.getLevel());
                packet.writeShort(equip.getStr());
                packet.writeShort(equip.getDex());
                packet.writeShort(equip.getInt());
                packet.writeShort(equip.getLuk());
                packet.writeShort(equip.getHp());
                packet.writeShort(equip.getMp());
                packet.writeShort(equip.getWatk());
                packet.writeShort(equip.getMatk());
                packet.writeShort(equip.getWdef());
                packet.writeShort(equip.getMdef());
                packet.writeShort(equip.getAcc());
                packet.writeShort(equip.getAvoid());
                packet.writeShort(equip.getHands());
                packet.writeShort(equip.getSpeed());
                packet.writeShort(equip.getJump());
                packet.writeMapleAsciiString(equip.getOwner());
                packet.writeShort(equip.getFlag());
                packet.write(0); // skills
                packet.write(Math.max(equip.getBaseLevel(), equip.getEquipLevel())); // Item level
                packet.writeInt(equip.getExpPercentage() * 100000);
                packet.writeInt(equip.getDurability());
                packet.writeInt(equip.getViciousHammer());
                if (!hasUniqueId) {
                    packet.write(equip.getState()); // 7 = unique for the lulz
                    packet.write(equip.getEnhance());
                    packet.writeShort(equip.getPotential1()); // potential stuff 1. total damage
                    packet.writeShort(equip.getPotential2()); // potential stuff 2. critical rate
                    packet.writeShort(equip.getPotential3()); // potential stuff 3. all stats
                }
                packet.writeShort(equip.getHpR());
                packet.writeShort(equip.getMpR());
                packet.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId());
                packet.writeLong(getTime(-2));
                packet.writeInt(-1);
            } else {
                packet.writeShort(item.getQuantity());
                packet.writeMapleAsciiString(item.getOwner());
                packet.writeShort(item.getFlag());
                if (GameConstants.isThrowingStar(item.getItemId())
                        || GameConstants.isBullet(item.getItemId())) {
                    packet.writeLong(item.getInventoryId() <= 0 ? -1 : item.getInventoryId());
                }
            }
        }
    }

    public static final void addAnnounceBox(
            final COutPacket packet, final MapleCharacter chr) {
        if (chr.getPlayerShop() != null
                && chr.getPlayerShop().isOwner(chr)
                && chr.getPlayerShop().getShopType() != 1
                && chr.getPlayerShop().isAvailable()) {
            addInteraction(packet, chr.getPlayerShop());
        } else {
            packet.write(0);
        }
    }

    public static final void addInteraction(
            final COutPacket packet, IMaplePlayerShop shop) {
        packet.write(shop.getGameType());
        packet.writeInt(((AbstractPlayerStore) shop).getObjectId());
        packet.writeMapleAsciiString(shop.getDescription());
        if (shop.getShopType() != 1) {
            packet.write(shop.getPassword().length() > 0 ? 1 : 0); // password = false
        }
        packet.write(shop.getItemId() % 10);
        packet.write(shop.getSize()); // current size
        packet.write(shop.getMaxSize()); // full slots... 4 = 4-1=3 = has slots, 1-1=0 = no slots
        if (shop.getShopType() != 1) {
            packet.write(shop.isOpen() ? 0 : 1);
        }
    }

    public static final void addCharacterInfo(
            final COutPacket packet, final MapleCharacter chr) {
        packet.writeLong(-1);
        packet.write(0);
        addCharStats(packet, chr);
        packet.write(chr.getBuddyList().getCapacity());
        if (chr.getBlessOfFairyOrigin() != null) {
            packet.write(1);
            packet.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
        } else {
            packet.write(0);
        }
        addInventoryInfo(packet, chr);
        addSkillInfo(packet, chr);
        addCoolDownInfo(packet, chr);
        addQuestInfo(packet, chr);

        addRingInfo(packet, chr);
        addRocksInfo(packet, chr);
        addMonsterBookInfo(packet, chr);
        packet.writeShort(0);
        chr.QuestInfoPacket(packet); // for every questinfo: int16_t questid, string questdata
        packet.writeInt(0); // PQ rank
    }

    public static final void addPetItemInfo(
            final COutPacket packet, final IItem item, final MaplePet pet) {
        if (item == null) {
            packet.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(
                    packet,
                    item.getExpiration() <= System.currentTimeMillis() ? -1 : item.getExpiration());
        }
        packet.writeAsciiString(pet.getName(), 13);
        packet.write(pet.getLevel());
        packet.writeShort(pet.getCloseness());
        packet.write(pet.getFullness());
        if (item == null) {
            packet.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(
                    packet,
                    item.getExpiration() <= System.currentTimeMillis() ? -1 : item.getExpiration());
        }
        packet.writeShort(0);
        packet.writeShort(0); // pet flags
        packet.writeInt(
                (pet.getPetItemId() == 5000054 && pet.getSecondsLeft() > 0)
                        ? pet.getSecondsLeft()
                        : 0);
        packet.writeShort(0);
    }
}
