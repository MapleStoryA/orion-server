package tools;

import client.AttackPair;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleQuestStatus;
import client.MapleStat;
import client.inventory.IEquip.ScrollResult;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import client.layout.MapleKeyLayout;
import client.skill.EvanSkillPoints;
import client.skill.ExtendedSPTable;
import client.skill.ISkill;
import client.skill.SkillEntry;
import client.skill.SkillMacro;
import constants.GameConstants;
import constants.GameUI;
import handling.SendPacketOpcode;
import handling.channel.MapleGuildRanking.GuildRankingInfo;
import handling.channel.handler.PlayerInteractionHandler;
import handling.world.alliance.AllianceManager;
import handling.world.buddy.BuddyListEntry;
import handling.world.guild.GuildManager;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.guild.MapleGuildCharacter;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.MapleShopItem;
import server.MapleStatEffect;
import server.MapleTrade;
import server.config.ServerConfig;
import server.events.MapleSnowball;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.PlayerNPC;
import server.life.SummonAttackEntry;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMist;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.movement.MovePath;
import server.shops.HiredMerchant;
import server.shops.MaplePlayerShopItem;
import tools.collection.Pair;
import tools.collection.Triple;
import tools.data.output.OutPacket;
import tools.packet.PacketHelper;

@lombok.extern.slf4j.Slf4j
public class MaplePacketCreator {

    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

    public static final byte[] getServerIP(final int port, final int clientId) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        packet.writeShort(0);
        try {
            packet.write(InetAddress.getByName(
                            ServerConfig.serverConfig().getConfig().getChannel().getHost())
                    .getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        packet.writeShort(port);
        packet.writeInt(clientId);
        packet.writeZeroBytes(5);

        return packet.getPacket();
    }

    public static final byte[] getChannelChange(final int port) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        packet.write(1);
        try {
            packet.write(InetAddress.getByName(
                            ServerConfig.serverConfig().getConfig().getChannel().getHost())
                    .getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        packet.writeShort(port);

        return packet.getPacket();
    }

    public static final byte[] getCharInfo(final MapleCharacter chr) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        packet.writeShort(2);
        packet.writeLong(1);
        packet.writeLong(2);
        packet.writeLong(chr.getClient().getChannel() - 1);
        packet.write(chr.getPortalCount(true));
        packet.write(1);
        packet.writeShort(0);

        chr.CRand().connectData(packet); // Random number generator

        PacketHelper.addCharacterInfo(packet, chr);
        packet.writeInt(0); // Lucky Logout Gift packet. Received/do not show =
        // 1; not received/show = 0
        packet.writeInt(0); // SN 1
        packet.writeInt(0); // SN 2
        packet.writeInt(0); // SN 3
        packet.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

        return packet.getPacket();
    }

    public static final byte[] getWarpToMap(final MapleMap to, final int spawnPoint, final MapleCharacter chr) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        packet.writeShort(2);
        packet.writeLong(1);
        packet.writeLong(2);
        packet.writeLong(chr.getClient().getChannel() - 1);
        packet.write(chr.getPortalCount(true));
        packet.write(0); // not connect packet
        packet.writeShort(0); // Messages
        packet.write(0); // revive stuffs?..
        packet.writeInt(to.getId());
        packet.write(spawnPoint);
        packet.writeShort(chr.getStat().getHp());
        packet.write(0); // if 1, then 2 more int
        packet.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

        return packet.getPacket();
    }

    public static final byte[] enableActions() {
        return updatePlayerStats(EMPTY_STATUPDATE, true, 0);
    }

    public static final byte[] updatePlayerStats(final List<Pair<MapleStat, Integer>> stats, final int evan) {
        return updatePlayerStats(stats, false, evan);
    }

    public static final byte[] updatePlayerStats(
            final List<Pair<MapleStat, Integer>> stats, final boolean itemReaction, final int evan) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (final Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {

                @Override
                public int compare(final Pair<MapleStat, Integer> o1, final Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        packet.writeInt(updateMask);
        Integer value;

        for (final Pair<MapleStat, Integer> statupdate : mystats) {
            value = statupdate.getLeft().getValue();

            if (value >= 1) {
                if (value == MapleStat.SKIN.getValue()) {
                    packet.writeShort(statupdate.getRight().shortValue());
                } else if (value <= MapleStat.HAIR.getValue()) {
                    packet.writeInt(statupdate.getRight());
                } else if (value < MapleStat.JOB.getValue()) {
                    packet.write(statupdate.getRight().byteValue());
                } else if (value == MapleStat.AVAILABLESP.getValue()) {
                    if (GameConstants.isEvan(evan) || GameConstants.isResist(evan)) {
                        packet.writeShort(0);
                    } else {
                        packet.writeShort(statupdate.getRight().shortValue());
                    }
                } else if (value < 0xFFFF) {
                    packet.writeShort(statupdate.getRight().shortValue());
                } else {
                    packet.writeInt(statupdate.getRight().intValue());
                }
            }
        }
        packet.write(0); // v88

        return packet.getPacket();
    }

    public static final byte[] updateSp(MapleCharacter chr, final boolean itemReaction) { // this
        // will
        // do..
        return updateSp(chr, itemReaction, false);
    }

    public static final byte[] updateSp(
            MapleCharacter chr, final boolean itemReaction, final boolean overrideJob) { // this
        // will
        // do..
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(itemReaction ? 1 : 0);
        packet.writeInt(MapleStat.AVAILABLESP.getValue());
        if (overrideJob
                || GameConstants.isEvan(chr.getJob().getId())
                || GameConstants.isResist(chr.getJob().getId())) {
            packet.write(0);
        } else {
            packet.writeShort(chr.getRemainingSp());
        }
        packet.write(0); // v88

        return packet.getPacket();
    }

    public static final byte[] instantMapWarp(final byte portal) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        packet.write(0);
        packet.write(portal); // 6

        return packet.getPacket();
    }

    public static final byte[] spawnPortal(final int townId, final int targetId, final int skillId, final Point pos) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PORTAL_TOWN.getValue());
        packet.writeInt(townId);
        packet.writeInt(targetId);
        packet.writeInt(skillId);
        if (pos != null) {
            packet.writePos(pos);
        }

        return packet.getPacket();
    }

    public static final byte[] spawnDoor(final int oid, final Point pos, final boolean town) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        packet.write(town ? 1 : 0);
        packet.writeInt(oid);
        packet.writePos(pos);

        return packet.getPacket();
    }

    public static byte[] removeDoor(int oid, boolean town) {
        OutPacket packet = new OutPacket();

        if (town) {
            packet.writeShort(SendPacketOpcode.PORTAL_TOWN.getValue());
            packet.writeInt(999999999);
            packet.writeLong(999999999);
        } else {
            packet.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
            packet.write(/* town ? 1 : */ 0);
            packet.writeLong(oid);
        }

        return packet.getPacket();
    }

    public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
        OutPacket packet = new OutPacket(25);
        packet.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
        packet.writeInt(summon.getOwner().getId());
        packet.writeInt(summon.getObjectId());
        packet.writeInt(summon.getSkill());
        packet.write(summon.getOwner().getLevel());
        packet.write(summon.getSkillLevel());
        packet.writeShort(summon.getPosition().x);
        packet.writeShort(summon.getPosition().y);
        packet.write(4);
        packet.write(summon.getStance());
        packet.write(0);
        packet.write(summon.getMovementType().getValue());
        packet.write(summon.isMirrorTarget() ? 0 : 1);
        packet.write((animated) || (summon.isMirrorTarget()) ? 0 : 1);
        if (!summon.isMirrorTarget()) {
            packet.writeShort(0);
        } else {
            packet.write(1);
            PacketHelper.addCharLook(packet, summon.getOwner(), true);
        }
        return packet.getPacket();
    }

    public static byte[] removeSummon(MapleSummon summon, boolean animated) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
        packet.writeInt(summon.getOwnerId());
        packet.writeInt(summon.getObjectId());
        packet.write(animated ? 4 : 1);

        return packet.getPacket();
    }

    public static byte[] getRelogResponse() {
        OutPacket packet = new OutPacket(3);

        packet.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
        packet.write(1);

        return packet.getPacket();
    }

    /**
     * Possible values for <code>type</code>:<br>
     * 1: You cannot move that channel. Please try again later.<br>
     * 2: You cannot go into the cash shop. Please try again later.<br>
     * 3: The Item-Trading shop is currently unavailable, please try again later.<br>
     * 4: You cannot go into the trade shop, due to the limitation of user count.<br>
     * 5: You do not meet the minimum level requirement to access the Trade Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static byte[] serverBlocked(int type) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SERVER_BLOCKED.getValue());
        packet.write(type);

        return packet.getPacket();
    }

    public static byte[] mapBlocked(int type) {
        OutPacket packet = new OutPacket();
        // 1: The portal is closed for now.
        // 2: You cannot go to that place.
        // 3: Unable to approach due to the force of the ground.
        // 4; You cannot teleport to or on this map.
        // 5; Unable to approach due to the force of the ground.
        // 6: This map can only be entered by party members.
        // 7: Only members of an expedition can enter this map.
        // 8: The cash shop is currently not available. Stay tuned...
        packet.writeShort(SendPacketOpcode.MAP_BLOCKED.getValue());
        packet.write(type);
        return packet.getPacket();
    }

    public static byte[] serverMessage(String message) {
        return serverMessage(4, 0, message, false);
    }

    public static byte[] serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, smegaEar);
    }

    public static byte[] serverMessage(int type, int channel, String message, boolean whisper) {
        OutPacket packet = new OutPacket();

        // 0: [Notice] <Msg>
        // 1: Popup <Msg>
        // 2: Megaphone
        // 3: Super Megaphone
        // 4: Server Message
        // 5: Pink Text
        // 6: LightBlue Text ({} as Item)
        // 7: [int] -> Keep Wz Error
        // 8: Item Megaphone
        // 9: Item Megaphone
        // 10: Three Line Megaphone
        // 11: Weather Effect
        // 12: Green Gachapon Message
        // 13: Yellow Twin Dragon's Egg
        // 14: Green Twin Dragon's Egg
        // 15: Lightblue Text
        // 16: Lightblue Text
        // 18: LightBlue Text ({} as Item)
        // 20: (Red Message) : Skull?

        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(type);
        if (type == 4) { // Server Message
            packet.write(1);
        }
        packet.writeMapleAsciiString(message);
        switch (type) {
            case 3: // Super Megaphone
            case 20: // Skull Megaphone
                packet.write(channel - 1);
                packet.write(whisper ? 1 : 0);
                break;
            case 9: // Like Item Megaphone (Without Item)
                packet.write(channel - 1);
                break;
            case 11: // Weather Effect
                packet.writeInt(channel); // item id
                break;
            case 13: // Yellow Twin Dragon's Egg
            case 14: // Green Twin Dragon's Egg
                packet.writeMapleAsciiString("NULL"); // Name
                PacketHelper.addItemInfo(packet, null, true, true);
                break;
            case 6:
            case 18:
                packet.writeInt(channel >= 1000000 && channel < 6000000 ? channel : 0); // Item
                // Id
                // E.G. All new EXP coupon {Ruby EXP Coupon} is now available in the
                // Cash Shop!
                break;
        }
        return packet.getPacket();
    }

    public static byte[] getGachaponMega(
            final String name, final String message, final IItem item, final byte rareness) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(12);
        packet.writeMapleAsciiString(name + " : got a(n) ");
        packet.writeInt(1); // 0~3 i think
        packet.writeMapleAsciiString(message);
        PacketHelper.addItemInfo(packet, item, true, true);

        return packet.getPacket();
    }

    public static byte[] tripleSmega(List<String> message, boolean ear, int channel) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(10);
        if (message.get(0) != null) {
            packet.writeMapleAsciiString(message.get(0));
        }
        packet.write(message.size());
        for (int i = 1; i < message.size(); i++) {
            if (message.get(i) != null) {
                packet.writeMapleAsciiString(message.get(i));
            }
        }
        packet.write(channel - 1);
        packet.write(ear ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] getAvatarMega(
            MapleCharacter chr, int channel, int itemId, final List<String> text, boolean ear) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        packet.writeInt(itemId);
        packet.writeMapleAsciiString(chr.getName());
        for (String i : text) {
            packet.writeMapleAsciiString(i);
        }
        packet.writeInt(channel - 1); // channel
        packet.write(ear ? 1 : 0);
        PacketHelper.addCharLook(packet, chr, true);

        return packet.getPacket();
    }

    public static byte[] itemMegaphone(String msg, boolean whisper, int channel, IItem item) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        packet.write(8);
        packet.writeMapleAsciiString(msg);
        packet.write(channel - 1);
        packet.write(whisper ? 1 : 0);

        if (item == null) {
            packet.write(0);
        } else {
            PacketHelper.addItemInfo(packet, item, false, false, true);
        }
        return packet.getPacket();
    }

    public static byte[] spawnNPC(MapleNPC life, boolean show) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
        packet.writeInt(life.getObjectId());
        packet.writeInt(life.getId());
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getCy());
        packet.write(life.getF() == 1 ? 0 : 1);
        packet.writeShort(life.getFh());
        packet.writeShort(life.getRx0());
        packet.writeShort(life.getRx1());
        packet.write((show && !life.isHidden()) ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] removeNPC(final int objectid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
        packet.writeInt(objectid); // TODO: is this correct?

        return packet.getPacket();
    }

    public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        packet.write(1);
        packet.writeInt(life.getObjectId());
        packet.writeInt(life.getId());
        packet.writeShort(life.getPosition().x);
        packet.writeShort(life.getCy());
        packet.write(life.getF() == 1 ? 0 : 1);
        packet.writeShort(life.getFh());
        packet.writeShort(life.getRx0());
        packet.writeShort(life.getRx1());
        packet.write(MiniMap ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPC npc) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        packet.write(1); // 0 = hide, 1 = show
        packet.writeInt(npc.getId());
        packet.writeMapleAsciiString(npc.getName());
        packet.write(npc.getGender());
        packet.write(npc.getSkin());
        packet.writeInt(npc.getFace());
        packet.write(0);
        packet.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (Entry<Byte, Integer> position : equip.entrySet()) {
            byte pos = (byte) (position.getKey() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, position.getValue());
            } else if ((pos > 100 || pos == -128) && pos != 111) { // don't ask.
                // o.o
                pos = (byte) (pos == -128 ? 28 : pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, position.getValue());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, position.getValue());
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            packet.write(entry.getKey());
            packet.writeInt(entry.getValue());
        }
        packet.write(0xFF);
        for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            packet.write(entry.getKey());
            packet.writeInt(entry.getValue());
        }
        packet.write(0xFF);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            packet.writeInt(cWeapon);
        } else {
            packet.writeInt(0);
        }
        for (int i = 0; i < 3; i++) {
            packet.writeInt(npc.getPet(i));
        }

        return packet.getPacket();
    }

    public static byte[] getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        packet.writeInt(cidfrom);
        packet.write(whiteBG ? 1 : 0);
        packet.writeMapleAsciiString(text);
        packet.write(show);

        return packet.getPacket();
    }

    public static byte[] GameMaster_Func(int value, int mode) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
        packet.write(value);
        packet.write(mode);

        return packet.getPacket();
    }

    public static byte[] testCombo(int value) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
        packet.writeInt(value);

        return packet.getPacket();
    }

    public static byte[] getPacketFromHexString(String hex) {
        return HexTool.getByteArrayFromHexString(hex);
    }

    public static final byte[] GainEXP_Monster(
            int gain,
            boolean white,
            int partyinc,
            int Class_Bonus_EXP,
            int Equipment_Bonus_EXP,
            int Premium_Bonus_EXP,
            byte percentage,
            double hoursFromLogin) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        packet.write(white ? 1 : 0);
        packet.writeInt(gain);
        packet.write(0); // Not in chat
        packet.writeInt(0); // Bonus Event EXP
        packet.write(percentage);
        packet.write(0);
        packet.writeInt(0); // Bonus Wedding EXP
        // A bonus EXP <percentage>% is awarded for every 3rd monster defeated.
        // Bonus EXP for hunting over <> hours
        packet.write((byte) hoursFromLogin);
        if (percentage > 0) {
            packet.write(0); // Party bonus rate. x 0.01
        }
        packet.writeInt(partyinc); // Bonus EXP for PARTY
        packet.writeInt(Equipment_Bonus_EXP); // Equip Item Bonus EXP
        packet.writeInt(0); // Internet Cafe EXP Bonus
        packet.writeInt(0); // Rainbow Week Bonus EXP
        packet.writeInt(Premium_Bonus_EXP); // Party Ring Bonus EXP
        packet.writeInt(0); // Cake vs Pie Bonus EXP

        return packet.getPacket();
    }

    public static final byte[] GainEXP_Others(final int gain, final boolean inChat, final boolean white) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        packet.write(white ? 1 : 0);
        packet.writeInt(gain);
        packet.write(inChat ? 1 : 0);
        packet.writeInt(0); // Bonus Event EXP
        packet.write(0);
        packet.write(0);
        packet.writeInt(0); // Bonus Wedding EXP
        if (inChat) {
            int bonus = 0, applied = 0; // will code this later on.
            // Earned 'Spirit Week Event' bonus EXP.
            packet.write(bonus);
            if (bonus > 0) {
                // The next <applied> completed quests will include additional
                // Event bonus EXP
                packet.write(applied);
            }
        }
        packet.write(0); // Party bonus rate. x 0.01
        packet.writeInt(0); // Bonus EXP for PARTY
        packet.writeInt(0); // Equip Item Bonus EXP
        packet.writeInt(0); // Internet Cafe EXP Bonus
        packet.writeInt(0); // Rainbow Week Bonus EXP
        packet.writeInt(0); // Party Ring Bonus EXP
        packet.writeInt(0); // Cake vs Pie Bonus EXP

        return packet.getPacket();
    }

    public static final byte[] getShowFameGain(final int gain) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(5);
        packet.writeInt(gain);

        return packet.getPacket();
    }

    public static final byte[] showMesoGain(final int gain, final boolean inChat) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            packet.write(0);
            packet.write(1);
            packet.write(0);
            packet.writeInt(gain);
            packet.writeShort(0); // inet cafe meso gain ?.o
        } else {
            packet.write(6);
            packet.writeInt(gain);
        }

        return packet.getPacket();
    }

    public static byte[] getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
        OutPacket packet = new OutPacket();

        if (inChat) {
            packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            packet.write(3);
            packet.write(1); // item count
            packet.writeInt(itemId);
            packet.writeInt(quantity);
            /*
             * for (int i = 0; i < count; i++) { // if ItemCount is handled.
             * packet.writeInt(itemId); packet.writeInt(quantity); }
             */
        } else {
            packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            packet.writeShort(0);
            packet.writeInt(itemId);
            packet.writeInt(quantity);
        }
        return packet.getPacket();
    }

    public static byte[] showRewardItemAnimation(int itemId, String effect) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(14);
        packet.writeInt(itemId);
        packet.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            packet.writeMapleAsciiString(effect);
        }

        return packet.getPacket();
    }

    public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(from_playerid);
        packet.write(14);
        packet.writeInt(itemId);
        packet.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            packet.writeMapleAsciiString(effect);
        }

        return packet.getPacket();
    }

    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        packet.write(mod); // 1 animation, 2 no animation, 3 spawn disappearing
        // item [Fade], 4 spawn disappearing item
        packet.writeInt(drop.getObjectId()); // item owner id
        packet.write(drop.getMeso() > 0 ? 1 : 0); // 1 mesos, 0 item, 2 and above
        // all item meso bag,
        packet.writeInt(drop.getItemId()); // drop object ID
        packet.writeInt(drop.getOwner()); // owner charid
        packet.write(drop.getDropType()); // 0 = timeout for non-owner, 1 =
        // timeout for non-owner's party, 2
        // = FFA, 3 = explosive/FFA
        packet.writePos(dropto);
        packet.writeInt(0);

        if (mod != 2) {
            packet.writePos(dropfrom);
            packet.writeShort(0);
        }
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(packet, drop.getItem().getExpiration());
        }
        packet.writeShort(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup

        return packet.getPacket();
    }

    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        packet.writeInt(chr.getId());
        packet.write(chr.getLevel());
        packet.writeMapleAsciiString(chr.getName());

        if (chr.getGuildId() <= 0) {
            packet.writeInt(0);
            packet.writeInt(0);
        } else {
            final MapleGuild gs = GuildManager.getGuild(chr.getGuildId());
            if (gs != null) {
                packet.writeMapleAsciiString(gs.getName());
                packet.writeShort(gs.getLogoBG());
                packet.write(gs.getLogoBGColor());
                packet.writeShort(gs.getLogo());
                packet.write(gs.getLogoColor());
            } else {
                packet.writeMapleAsciiString("");
                packet.write(new byte[6]);
            }
        }
        // packet.writeInt(3); after aftershock
        List<Pair<Integer, Integer>> buffvalue = new ArrayList<Pair<Integer, Integer>>();
        long fbuffmask = 0x3F80000L; // becomes F8000000 after bb?
        /** TODO: Removed for now, it d/c's other people */
        if (chr.getBuffedValue(MapleBuffStat.FINAL_CUT) != null) {
            fbuffmask |= MapleBuffStat.FINAL_CUT.getValue();
            buffvalue.add(new Pair<>(
                    Integer.valueOf(chr.getBuffedValue(MapleBuffStat.FINAL_CUT).intValue()), 3));
        }
        if (chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT) != null) {
            fbuffmask |= MapleBuffStat.OWL_SPIRIT.getValue();
            buffvalue.add(new Pair<>(
                    Integer.valueOf(chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT).intValue()), 3));
        }

        if (chr.getBuffedValue(MapleBuffStat.SOARING) != null) {
            fbuffmask |= MapleBuffStat.SOARING.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MIRROR_IMAGE) != null) {
            fbuffmask |= MapleBuffStat.MIRROR_IMAGE.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.DARK_AURA) != null) {
            fbuffmask |= MapleBuffStat.DARK_AURA.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.BLUE_AURA) != null) {
            fbuffmask |= MapleBuffStat.BLUE_AURA.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.YELLOW_AURA) != null) {
            fbuffmask |= MapleBuffStat.YELLOW_AURA.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ) != null) {
            fbuffmask |= MapleBuffStat.PYRAMID_PQ.getValue();
            buffvalue.add(new Pair<>(
                    Integer.valueOf(chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ).intValue()), 1)); // idk
        }
        if (chr.getBuffedValue(MapleBuffStat.MAGIC_SHIELD) != null) {
            fbuffmask |= MapleBuffStat.MAGIC_SHIELD.getValue();
            buffvalue.add(new Pair<>(
                    Integer.valueOf(
                            chr.getBuffedValue(MapleBuffStat.MAGIC_SHIELD).intValue()),
                    1)); // idk
        }
        packet.writeLong(fbuffmask);
        long buffmask = 0;

        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            buffmask |= MapleBuffStat.COMBO.getValue();
            buffvalue.add(new Pair<Integer, Integer>(
                    Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue()),
                    MapleBuffStat.COMBO.isFirst()));
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
            buffmask |= MapleBuffStat.DIVINE_BODY.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {
            buffmask |= MapleBuffStat.BERSERK_FURY.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            buffmask |= MapleBuffStat.MORPH.getValue();
            buffvalue.add(new Pair<Integer, Integer>(
                    Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue()), 2));
        }

        packet.writeLong(buffmask);
        for (Pair<Integer, Integer> i : buffvalue) {
            if (i.right == 2) {
                packet.writeShort(i.left.shortValue());
            } else if (i.right == 3) {
                packet.writeInt(i.left.shortValue());
            } else {
                packet.write(i.left.byteValue());
            }
        }
        final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        // CHAR_MAGIC_SPAWN is really just tickCount
        // this is here as it explains the 7 "dummy" buffstats which are placed
        // into every character
        // these 7 buffstats are placed because they have irregular packet
        // structure.
        // they ALL have writeShort(0); first, then a long as their variables,
        // then server tick count
        // 0x80000, 0x100000, 0x200000, 0x400000, 0x800000, 0x1000000, 0x2000000

        packet.writeShort(0); // start of energy charge
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.writeShort(0); // start of dash_speed
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.writeShort(0); // start of dash_jump
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.writeShort(0); // start of Monster Riding
        int buffSrc = chr.getBuffSource(MapleBuffStat.MONSTER_RIDING);
        if (buffSrc > 0) {
            final IItem c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118);
            final IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
            if (GameConstants.getMountItem(buffSrc) == 0 && c_mount != null) {
                packet.writeInt(c_mount.getItemId());
            } else if (GameConstants.getMountItem(buffSrc) == 0 && mount != null) {
                packet.writeInt(mount.getItemId());
            } else {
                packet.writeInt(GameConstants.getMountItem(buffSrc));
            }
            packet.writeInt(buffSrc);
        } else {
            packet.writeLong(0);
        }
        packet.write(1);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.writeLong(0); // speed infusion behaves differently here
        packet.write(1);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.writeInt(1);
        packet.writeLong(0); // homing beacon
        packet.write(0);
        packet.writeShort(0);
        packet.write(1);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.writeInt(0); // and finally, something ive no idea
        packet.writeLong(0);
        packet.write(1);
        packet.writeInt(CHAR_MAGIC_SPAWN);
        packet.writeShort(0);
        packet.writeShort(chr.getJob().getId());
        PacketHelper.addCharLook(packet, chr, false);
        packet.writeInt(0); // this is CHARID to follow
        packet.writeInt(0); // probably charid following
        packet.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); // max
        // is
        // like
        // 100.
        // but
        // w/e
        packet.writeInt(chr.getItemEffect());
        packet.writeInt(0);
        packet.writeInt(
                GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
        packet.writePos(chr.getPosition());
        packet.write(chr.getStance());
        packet.writeShort(0); // FH
        packet.write(0);
        packet.write(0); // end of pets
        packet.writeInt(chr.getMount().getLevel()); // mount lvl
        packet.writeInt(chr.getMount().getExp()); // exp
        packet.writeInt(chr.getMount().getFatigue()); // tiredness
        PacketHelper.addAnnounceBox(packet, chr);
        packet.write(chr.getChalkboard() != null && chr.getChalkboard().length() > 0 ? 1 : 0);
        if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
            packet.writeMapleAsciiString(chr.getChalkboard());
        }
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(packet, rings.getLeft());
        addRingInfo(packet, rings.getMid());
        addMRingInfo(packet, rings.getRight(), chr);
        // packet.write(0); // 3 ints
        packet.write(chr.getStat().isBersek() ? 1 : 0);
        packet.write(0); // if this is 1, then 1 int(size), each size = another
        // int.
        packet.writeInt(0);
        if (chr.getCarnivalParty() != null) {
            packet.write(chr.getCarnivalParty().getTeam());
        } else {
            packet.write(0);
        }
        return packet.getPacket();
    }

    public static byte[] removePlayerFromMap(int cid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        packet.writeInt(cid);

        return packet.getPacket();
    }

    public static byte[] facialExpression(MapleCharacter from, int expression) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        packet.writeInt(from.getId());
        packet.writeInt(expression);
        packet.writeInt(-1); // itemid of expression use
        packet.write(0);

        return packet.getPacket();
    }

    public static byte[] movePlayer(int cid, MovePath moves) { // CUserRemote::OnMove
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        packet.writeInt(cid);
        moves.encode(packet);
        return packet.getPacket();
    }

    public static byte[] moveSummon(int cid, int oid, MovePath path) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
        packet.writeInt(cid);
        packet.writeInt(oid);
        path.encode(packet);

        return packet.getPacket();
    }

    public static byte[] summonAttack(
            final int cid,
            final int summonSkillId,
            final byte animation,
            final List<SummonAttackEntry> allDamage,
            final int level) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
        packet.writeInt(cid);
        packet.writeInt(summonSkillId);
        packet.write(level - 1); // ? guess
        packet.write(animation);
        packet.write(allDamage.size());

        for (final SummonAttackEntry attackEntry : allDamage) {
            packet.writeInt(attackEntry.getMonster().getObjectId()); // oid
            packet.write(7); // who knows
            packet.writeInt(attackEntry.getDamage()); // damage
        }
        return packet.getPacket();
    }

    public static byte[] closeRangeAttack(
            int cid,
            int tbyte,
            int skill,
            int level,
            byte display,
            byte animation,
            byte speed,
            List<AttackPair> damage,
            final boolean energy,
            int lvl,
            byte mastery,
            byte unk,
            int charge) {
        OutPacket packet = new OutPacket();

        packet.writeShort(
                energy ? SendPacketOpcode.ENERGY_ATTACK.getValue() : SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        packet.writeInt(cid);
        packet.write(tbyte);
        packet.write(lvl); // ?
        if (skill > 0) {
            packet.write(level);
            packet.writeInt(skill);
        } else {
            packet.write(0);
        }
        packet.write(unk); // Added on v.82
        // a short actually
        packet.write(display);
        packet.write(animation);

        packet.write(speed);
        packet.write(mastery); // Mastery
        packet.writeInt(0); // E9 03 BE FC

        if (skill == 4211006) {
            for (AttackPair oned : damage) {
                if (oned.getAttack() != null) {
                    packet.writeInt(oned.getObjectId());
                    packet.write(0x07);
                    packet.write(oned.getAttack().size());
                    for (Pair<Integer, Boolean> eachd : oned.getAttack()) {
                        packet.write(eachd.right ? 1 : 0);
                        packet.writeInt(eachd.left); // m.e. is never crit
                    }
                }
            }
        } else {
            for (AttackPair oned : damage) {
                if (oned.getAttack() != null) {
                    packet.writeInt(oned.getObjectId());
                    packet.write(0x07);
                    for (Pair<Integer, Boolean> eachd : oned.getAttack()) {
                        packet.write(eachd.right ? 1 : 0);
                        packet.writeInt(eachd.left.intValue());
                    }
                }
            }
        }
        // if (charge > 0) {
        // packet.writeInt(charge); //is it supposed to be here
        // }
        return packet.getPacket();
    }

    public static byte[] rangedAttack(
            int cid,
            byte tbyte,
            int skill,
            int level,
            byte display,
            byte animation,
            byte speed,
            int itemid,
            List<AttackPair> damage,
            final Point pos,
            int lvl,
            byte mastery,
            byte unk) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        packet.writeInt(cid);
        packet.write(tbyte);
        packet.write(lvl); // ?
        if (skill > 0) {
            packet.write(level);
            packet.writeInt(skill);
        } else {
            packet.write(0);
        }
        packet.write(unk); // Added on v.82
        packet.write(display);
        packet.write(animation);
        packet.write(speed);
        packet.write(mastery); // Mastery level, who cares
        packet.writeInt(itemid);

        for (AttackPair oned : damage) {
            if (oned.getAttack() != null) {
                packet.writeInt(oned.getObjectId());
                packet.write(0x07);
                for (Pair<Integer, Boolean> eachd : oned.getAttack()) {
                    packet.write(eachd.right ? 1 : 0);
                    packet.writeInt(eachd.left.intValue());
                }
            }
        }
        packet.writePos(pos); // Position

        return packet.getPacket();
    }

    public static byte[] magicAttack(
            int cid,
            int tbyte,
            int skill,
            int level,
            byte display,
            byte animation,
            byte speed,
            List<AttackPair> damage,
            int charge,
            int lvl,
            byte unk) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        packet.writeInt(cid);
        packet.write(tbyte);
        packet.write(lvl); // ?
        packet.write(level);
        packet.writeInt(skill);

        packet.write(unk); // Added on v.82
        packet.write(display);
        packet.write(animation);
        packet.write(speed);
        packet.write(0); // Mastery byte is always 0 because spells don't have a
        // swoosh
        packet.writeInt(0);

        for (AttackPair oned : damage) {
            if (oned.getAttack() != null) {
                packet.writeInt(oned.getObjectId());
                packet.write(7);
                for (Pair<Integer, Boolean> eachd : oned.getAttack()) {
                    packet.write(eachd.right ? 1 : 0);
                    packet.writeInt(eachd.left.intValue());
                }
            }
        }
        if (charge > 0) {
            packet.writeInt(charge);
        }
        return packet.getPacket();
    }

    public static byte[] getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {

        OutPacket packet = new OutPacket();
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        packet.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        packet.writeInt(sid);
        packet.writeShort(items.size()); // item count
        for (MapleShopItem item : items) {
            packet.writeInt(item.getItemId());
            packet.writeInt(item.getPrice());
            packet.write(0); // ??
            packet.writeInt(item.getReqItem());
            packet.writeInt(item.getReqItemQ());
            packet.writeInt(GameConstants.isPet(item.getItemId()) ? 0 : item.getExpiration()); // Can
            // be
            // used
            // x
            // minutes
            // after
            // purchase
            packet.writeInt(item.getReqLevel()); // minimum level to purchase
            // item ("Your level must be
            // over lv.X to purchase this
            // item"), the level stated here
            // = can buy already.
            if (GameConstants.isRechargable(item.getItemId())) {
                packet.writeShort(0);
                packet.writeInt(0);
            }
            packet.writeShort(
                    GameConstants.isRechargable(item.getItemId())
                            ? ii.getSlotMax(c, item.getItemId())
                            : item.getQuantity()); // Quantity sold
            packet.writeShort(ii.getSlotMax(c, item.getItemId())); // Maximum
            // quantity
            // which can
            // be bought
            // at a time
            // / Max
            // recharged
        }
        return packet.getPacket();
    }

    public static byte[] confirmShopTransaction(byte code) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        packet.write(code); // 8 = sell, 0 = buy, 0x20 = due to an error

        return packet.getPacket();
    }

    public static byte[] addInventorySlot(MapleInventoryType type, IItem item) {
        return addInventorySlot(type, item, false);
    }

    public static byte[] addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(fromDrop ? 1 : 0);
        packet.writeShort(1); // add mode
        packet.write(type.getType()); // iv type
        packet.write(item.getPosition()); // slot id
        PacketHelper.addItemInfo(packet, item, true, false);

        return packet.getPacket();
    }

    public static byte[] updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(fromDrop ? 1 : 0);
        // packet.write((slot2 > 0 ? 1 : 0) + 1);
        packet.write(1);
        packet.write(1);
        packet.write(type.getType()); // iv type
        packet.writeShort(item.getPosition()); // slot id
        packet.writeShort(item.getQuantity());
        /*
         * if (slot2 > 0) { packet.write(1); packet.write(type.getType());
         * packet.writeShort(slot2); packet.writeShort(amt2); }
         */
        return packet.getPacket();
    }

    public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst) {
        return moveInventoryItem(type, src, dst, (byte) -1);
    }

    public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, short equipIndicator) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(HexTool.getByteArrayFromHexString("01 01 02"));
        packet.write(type.getType());
        packet.writeShort(src);
        packet.writeShort(dst);
        if (equipIndicator != -1) {
            packet.write(equipIndicator);
        }
        return packet.getPacket();
    }

    public static byte[] moveAndMergeInventoryItem(MapleInventoryType type, short src, short dst, short total) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(HexTool.getByteArrayFromHexString("01 02 03"));
        packet.write(type.getType());
        packet.writeShort(src);
        packet.write(1); // merge mode?
        packet.write(type.getType());
        packet.writeShort(dst);
        packet.writeShort(total);

        return packet.getPacket();
    }

    public static byte[] moveAndMergeWithRestInventoryItem(
            MapleInventoryType type, short src, short dst, short srcQ, short dstQ) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(HexTool.getByteArrayFromHexString("01 02 01"));
        packet.write(type.getType());
        packet.writeShort(src);
        packet.writeShort(srcQ);
        packet.write(HexTool.getByteArrayFromHexString("01"));
        packet.write(type.getType());
        packet.writeShort(dst);
        packet.writeShort(dstQ);

        return packet.getPacket();
    }

    public static byte[] clearInventoryItem(MapleInventoryType type, short slot, boolean fromDrop) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(fromDrop ? 1 : 0);
        packet.write(HexTool.getByteArrayFromHexString("01 03"));
        packet.write(type.getType());
        packet.writeShort(slot);

        return packet.getPacket();
    }

    public static byte[] updateSpecialItemUse(IItem item, byte invType) {
        return updateSpecialItemUse(item, invType, item.getPosition());
    }

    public static byte[] updateSpecialItemUse(IItem item, byte invType, short pos) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(0); // could be from drop
        packet.write(2); // always 2
        packet.write(3); // quantity > 0 (?)
        packet.write(invType); // Inventory type
        packet.writeShort(pos); // item slot
        packet.write(0);
        packet.write(invType);
        if (item.getType() == 1) {
            packet.writeShort(pos);
        } else {
            packet.write(pos);
        }
        PacketHelper.addItemInfo(packet, item, true, true);
        if (item.getPosition() < 0) {
            packet.write(2); // ?
        }

        return packet.getPacket();
    }

    public static byte[] updateSpecialItemUse_(IItem item, byte invType) {
        return updateSpecialItemUse_(item, invType, item.getPosition());
    }

    public static byte[] updateSpecialItemUse_(IItem item, byte invType, short pos) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(0); // could be from drop
        packet.write(1); // always 2
        packet.write(0); // quantity > 0 (?)
        packet.write(invType); // Inventory type
        if (item.getType() == 1) {
            packet.writeShort(pos);
        } else {
            packet.write(pos);
        }
        PacketHelper.addItemInfo(packet, item, true, true);
        if (item.getPosition() < 0) {
            packet.write(1); // ?
        }

        return packet.getPacket();
    }

    public static byte[] scrolledItem(IItem scroll, IItem item, boolean destroyed, boolean potential) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(1); // fromdrop always true
        packet.write(destroyed ? 2 : 3);
        packet.write(scroll.getQuantity() > 0 ? 1 : 3);
        packet.write(GameConstants.getInventoryType(scroll.getItemId()).getType()); // can
        // be
        // cash
        packet.writeShort(scroll.getPosition());

        if (scroll.getQuantity() > 0) {
            packet.writeShort(scroll.getQuantity());
        }
        packet.write(3);
        if (!destroyed) {
            packet.write(MapleInventoryType.EQUIP.getType());
            packet.writeShort(item.getPosition());
            packet.write(0);
        }
        packet.write(MapleInventoryType.EQUIP.getType());
        packet.writeShort(item.getPosition());
        if (!destroyed) {
            PacketHelper.addItemInfo(packet, item, true, true);
        }
        packet.write(potential ? 2 : 1);

        return packet.getPacket();
    }

    public static byte[] getNormalScrollEffect(
            int chr, ScrollResult scrollSuccess, boolean legendarySpirit, boolean whiteScroll) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_UPGRADE_EFFECT.getValue());
        packet.writeInt(chr);

        switch (scrollSuccess) {
            case SUCCESS:
                packet.write(1);
                packet.write(0);
                break;
            case FAIL:
                packet.write(0);
                packet.write(0);
                break;
            case CURSE:
                packet.write(0);
                packet.write(1);
                break;
        }
        packet.write(legendarySpirit ? 1 : 0);
        packet.writeInt(0); // if this is 2, then below 2 bytes are not used.
        // 1 + 0 : You are successful in upgrading the equipment.
        // 0 + 0 : You fail to upgrade the equipment.
        // 0 + 1 : Your equipment is destroyed since you failed to upgrade.
        packet.write(legendarySpirit ? 0 : (whiteScroll ? 1 : 0)); // this is not
        // used when
        // got
        // legendaryspirit
        packet.write(/* legendarySpirit ? (whiteScroll ? 1 : 0) : */ 0); // pams
        // song?

        return packet.getPacket();
    }

    public static byte[] getPotentialScrollEffect(
            boolean isPotential, int chr, ScrollResult scrollSuccess, boolean legendarySpirit) {
        OutPacket packet = new OutPacket();

        packet.writeShort(
                isPotential
                        ? SendPacketOpcode.SHOW_ITEM_OPTION_UPGRADE_EFFECT.getValue()
                        : SendPacketOpcode.SHOW_ITEM_HYPER_UPGRADE_EFFECT.getValue());
        packet.writeInt(chr);
        packet.write(scrollSuccess == ScrollResult.SUCCESS ? 1 : 0);
        packet.write(scrollSuccess == ScrollResult.CURSE ? 1 : 0);
        packet.write(legendarySpirit ? 1 : 0);
        packet.writeInt(0); // ??

        return packet.getPacket();
    }

    public static byte[] getMagnifyingEffect(final int chr, final short pos) {
        OutPacket packet = new OutPacket(8);

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_RELEASE_EFFECT.getValue());
        packet.writeInt(chr);
        packet.writeShort(pos);

        return packet.getPacket();
    }

    public static byte[] getMiracleCubeEffect(final int chr, final boolean pass) {
        // 0x00 : Resetting Potential has failed due to insufficient space in
        // the Use item.
        // 0x01 : Potential successfully reset.
        OutPacket packet = new OutPacket(7);

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_UNRELEASE_EFFECT.getValue());
        packet.writeInt(chr);
        packet.write(pass ? 1 : 0);

        return packet.getPacket();
    }

    public static final byte[] ItemMaker_Success(final boolean pass) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(16);
        packet.writeInt(pass ? 0 : 1); // 0 = pass, 1 = fail

        return packet.getPacket();
    }

    public static final byte[] ItemMaker_Success_3rdParty(final int from_playerid, final boolean pass) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(from_playerid);
        packet.write(16);
        packet.writeInt(pass ? 0 : 1); // 0 = pass, 1 = fail

        return packet.getPacket();
    }

    public static byte[] explodeDrop(int oid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        packet.write(4); // 4 = Explode
        packet.writeInt(oid);
        packet.writeShort(655);

        return packet.getPacket();
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, 0);
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid, int slot) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        packet.write(animation); // 0 = Expire, 1 = without animation, 2 =
        // pickup, 4 = explode, 5 = pet pickup
        packet.writeInt(oid);
        if (animation >= 2) {
            packet.writeInt(cid);
            if (animation == 5) { // allow pet pickup?
                packet.writeInt(slot);
            }
        }
        return packet.getPacket();
    }

    public static byte[] updateCharLook(MapleCharacter chr) {
        return updateCharLook(chr, (byte) 1);
    }

    public static byte[] updateCharLook(MapleCharacter chr, byte mode) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        packet.writeInt(chr.getId());
        packet.write(mode); // flags actually |
        switch (mode) {
            case 1:
                PacketHelper.addCharLook(packet, chr, false);
                break;
            case 2:
                packet.write(0); // ?
                break;
            case 4: // Carry Item effect
                packet.write(0);
                break;
        }
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(packet, rings.getLeft());
        addRingInfo(packet, rings.getMid());
        addMRingInfo(packet, rings.getRight(), chr);
        packet.writeInt(0); // charid to follow

        return packet.getPacket();
    }

    public static void addRingInfo(OutPacket packet, List<MapleRing> rings) {
        packet.write(rings.size());
        for (MapleRing ring : rings) {
            packet.writeLong(ring.getRingId());
            packet.writeLong(ring.getPartnerRingId());
            packet.writeInt(ring.getItemId());
        }
    }

    public static void addMRingInfo(OutPacket packet, List<MapleRing> rings, MapleCharacter chr) {
        packet.write(rings.size());
        for (MapleRing ring : rings) {
            packet.writeInt(chr.getId());
            packet.writeInt(ring.getPartnerChrId());
            packet.writeInt(ring.getItemId());
        }
    }

    public static byte[] dropInventoryItem(MapleInventoryType type, short src) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(HexTool.getByteArrayFromHexString("01 01 03"));
        packet.write(type.getType());
        packet.writeShort(src);
        if (src < 0) {
            packet.write(1);
        }
        return packet.getPacket();
    }

    public static byte[] dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(HexTool.getByteArrayFromHexString("01 01 01"));
        packet.write(type.getType());
        packet.writeShort(item.getPosition());
        packet.writeShort(item.getQuantity());

        return packet.getPacket();
    }

    public static byte[] damagePlayer(
            int skill,
            int monsteridfrom,
            int cid,
            int damage,
            int fake,
            byte direction,
            int reflect,
            boolean is_pg,
            int oid,
            int pos_x,
            int pos_y) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        packet.writeInt(cid);
        packet.write(skill);
        packet.writeInt(damage);
        packet.writeInt(monsteridfrom);
        packet.write(direction);

        if (reflect > 0) {
            packet.write(reflect);
            packet.write(is_pg ? 1 : 0);
            packet.writeInt(oid);
            packet.write(6);
            packet.writeShort(pos_x);
            packet.writeShort(pos_y);
            packet.write(0);
        } else {
            packet.writeShort(0);
        }
        packet.writeInt(damage);
        if (fake > 0) {
            packet.writeInt(fake);
        }
        return packet.getPacket();
    }

    public static final byte[] updateInfoQuest(final int quest, final String data) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(11);
        packet.writeShort(quest);
        packet.writeMapleAsciiString(data);

        return packet.getPacket();
    }

    public static byte[] updateQuestInfo(MapleCharacter c, int quest, int npc, byte progress) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        packet.write(progress);
        packet.writeShort(quest);
        packet.writeInt(npc);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static byte[] updateQuestFinish(int quest, int npc, int nextquest) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        packet.write(8);
        packet.writeShort(quest);
        packet.writeInt(npc);
        packet.writeInt(nextquest);
        return packet.getPacket();
    }

    public static final byte[] charInfo(final MapleCharacter chr, final boolean isSelf) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CHARACTER_INFO.getValue());
        packet.writeInt(chr.getId());
        packet.write(chr.getLevel());
        packet.writeShort(chr.getJob().getId());
        packet.writeShort(chr.getFame());
        packet.write(chr.getMarriageId() > 0 ? 1 : 0); // heart red or gray

        if (chr.getGuildId() <= 0) {
            packet.writeMapleAsciiString("-");
            packet.writeMapleAsciiString("");
        } else {
            final MapleGuild gs = GuildManager.getGuild(chr.getGuildId());
            if (gs != null) {
                packet.writeMapleAsciiString(gs.getName());
                if (gs.getAllianceId() > 0) {
                    final MapleGuildAlliance allianceName = AllianceManager.getAlliance(gs.getAllianceId());
                    if (allianceName != null) {
                        packet.writeMapleAsciiString(allianceName.getName());
                    } else {
                        packet.writeMapleAsciiString("");
                    }
                } else {
                    packet.writeMapleAsciiString("");
                }
            } else {
                packet.writeMapleAsciiString("-");
                packet.writeMapleAsciiString("");
            }
        }
        packet.write(isSelf ? 1 : 0);

        final IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
        final int peteqid = inv != null ? inv.getItemId() : 0;

        for (final MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                packet.write(pet.getUniqueId()); // o-o byte ?
                packet.writeInt(pet.getPetItemId()); // petid
                packet.writeMapleAsciiString(pet.getName());
                packet.write(pet.getLevel()); // pet level
                packet.writeShort(pet.getCloseness()); // pet closeness
                packet.write(pet.getFullness()); // pet fullness
                packet.writeShort(0);
                packet.writeInt(peteqid);
            }
        }
        packet.write(0); // End of pet

        if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -22) != null) {
            final int itemid = chr.getInventory(MapleInventoryType.EQUIPPED)
                    .getItem((byte) -22)
                    .getItemId();
            final MapleMount mount = chr.getMount();
            final boolean canwear = MapleItemInformationProvider.getInstance().getReqLevel(itemid) <= chr.getLevel();
            packet.write(canwear ? 1 : 0);
            if (canwear) {
                packet.writeInt(mount.getLevel());
                packet.writeInt(mount.getExp());
                packet.writeInt(mount.getFatigue());
            }
        } else {
            packet.write(0);
        }

        chr.getWishlist().encodeToCharInfo(packet);
        chr.getMonsterBook().addCharInfoPacket(chr.getMonsterBookCover(), packet);

        IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
        packet.writeInt(medal == null ? 0 : medal.getItemId());
        List<Integer> medalQuests = new ArrayList<Integer>();
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (MapleQuestStatus q : completed) {
            if (q.getQuest().getMedalItem() > 0
                    && GameConstants.getInventoryType(q.getQuest().getMedalItem())
                            == MapleInventoryType.EQUIP) { // chair
                // kind
                // medal
                // viewmedal
                // is
                // weird
                medalQuests.add(q.getQuest().getId());
            }
        }
        packet.writeShort(medalQuests.size());
        for (int x : medalQuests) {
            packet.writeShort(x);
        }
        // v90 New
        List<Integer> chairs = new ArrayList<Integer>();
        for (IItem item : chr.getInventory(MapleInventoryType.SETUP)) {
            if (item.getItemId() / 10000 == 301 && !chairs.contains(item.getItemId())) {
                chairs.add(item.getItemId());
            }
        }
        packet.writeInt(chairs.size());
        for (Integer ch : chairs) {
            packet.writeInt(ch);
        }
        return packet.getPacket();
    }

    private static void writeLongMask(OutPacket packet, List<Pair<MapleBuffStat, Integer>> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<MapleBuffStat, Integer> statup : statups) { // TODO: implement
            // isFirst
            if (statup.getLeft().isFirst() == 2) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        packet.writeLong(firstmask);
        packet.writeLong(secondmask);
    }

    // List<Pair<MapleDisease, Integer>>
    private static void writeLongDiseaseMask(OutPacket packet, List<Pair<MapleDisease, Integer>> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (Pair<MapleDisease, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        packet.writeLong(firstmask);
        packet.writeLong(secondmask);
    }

    private static void writeLongMaskFromList(OutPacket packet, List<MapleBuffStat> statups) {
        long firstmask = 0;
        long secondmask = 0;
        for (MapleBuffStat statup : statups) {
            if (statup.isFirst() == 2) { // TODO: implement isFirst
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        packet.writeLong(firstmask);
        packet.writeLong(secondmask);
    }

    public static byte[] giveMount(int buffid, int skillid, List<Pair<MapleBuffStat, Integer>> statups) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        writeLongMask(packet, statups);

        packet.writeShort(0);
        packet.writeInt(buffid); // 1902000 saddle
        packet.writeInt(skillid); // skillid
        packet.writeInt(0); // Server tick value
        packet.writeShort(0);
        packet.write(0);
        packet.write(2); // Total buffed times

        return packet.getPacket();
    }

    public static byte[] givePirate(List<Pair<MapleBuffStat, Integer>> statups, int duration, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        writeLongMask(packet, statups);

        packet.writeShort(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            packet.writeInt(stat.getRight().intValue());
            packet.writeLong(skillid);
            packet.writeZeroBytes(infusion ? 6 : 1);
            packet.writeShort(duration);
        }
        packet.writeShort(infusion ? 600 : 0);
        if (!infusion) {
            packet.write(1); // does this only come in dash?
        }
        return packet.getPacket();
    }

    public static byte[] giveForeignPirate(
            List<Pair<MapleBuffStat, Integer>> statups, int duration, int cid, int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        writeLongMask(packet, statups);
        packet.writeShort(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            packet.writeInt(stat.getRight().intValue());
            packet.writeLong(skillid);
            packet.writeZeroBytes(infusion ? 7 : 1);
            packet.writeShort(duration); // duration... seconds
        }
        packet.writeShort(infusion ? 600 : 0);
        return packet.getPacket();
    }

    public static byte[] giveHoming(int skillid, int mobid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        packet.writeLong(MapleBuffStat.HOMING_BEACON.getValue());
        packet.writeLong(0);

        packet.writeShort(0);
        packet.writeInt(1);
        packet.writeLong(skillid);
        packet.write(0);
        packet.writeInt(mobid);
        packet.writeShort(0);
        return packet.getPacket();
    }

    public static byte[] giveEnergyChargeTest(int bar, int bufflength) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        packet.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        packet.writeLong(0);
        packet.writeShort(0);
        packet.writeInt(0);
        packet.writeInt(1555445060); // ?
        packet.writeShort(0);
        packet.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
        packet.writeLong(0); // skillid, but its 0 here
        packet.write(0);
        packet.writeInt(bar >= 10000 ? bufflength : 0); // short - bufflength...50
        return packet.getPacket();
    }

    public static byte[] giveEnergyChargeTest(int cid, int bar, int bufflength) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        packet.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        packet.writeLong(0);
        packet.writeShort(0);
        packet.writeInt(0);
        packet.writeInt(1555445060); // ?
        packet.writeShort(0);
        packet.writeInt(Math.min(bar, 10000)); // 0 = no bar, 10000 = full bar
        packet.writeLong(0); // skillid, but its 0 here
        packet.write(0);
        packet.writeInt(bar >= 10000 ? bufflength : 0); // short - bufflength...50
        return packet.getPacket();
    }

    public static byte[] giveEnergyCharge(int barammount) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue());
        packet.writeZeroBytes(10);
        packet.writeShort(barammount);
        packet.writeZeroBytes(11);
        packet.writeInt(50);
        return packet.getPacket();
    }

    public static byte[] giveBuff(
            int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, MapleStatEffect effect) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        writeLongMask(packet, statups);

        for (Pair<MapleBuffStat, Integer> statup : statups) {
            packet.writeShort(statup.getRight().shortValue());
            packet.writeInt(buffid);
            packet.writeInt(bufflength);
        }
        packet.writeShort(0); // delay, wk charges have 600 here o.o
        packet.writeShort(0); // combo 600, too
        if (effect == null || (!effect.isCombo() && !effect.isFinalAttack())) {
            packet.write(0); // Test
        }

        return packet.getPacket();
    }

    public static byte[] giveDebuff(
            final List<Pair<MapleDisease, Integer>> statups, int skillid, int level, int duration) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS.getValue());

        writeLongDiseaseMask(packet, statups);

        for (Pair<MapleDisease, Integer> statup : statups) {
            packet.writeShort(statup.getRight().shortValue());
            packet.writeShort(skillid);
            packet.writeShort(level);
            packet.writeInt(duration);
        }
        packet.writeShort(0); // ??? wk charges have 600 here o.o
        packet.writeShort(900); // Delay
        packet.write(1);

        return packet.getPacket();
    }

    public static byte[] giveForeignDebuff(
            int cid, final List<Pair<MapleDisease, Integer>> statups, int skillid, int level) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);

        writeLongDiseaseMask(packet, statups);

        if (skillid == 125) {
            packet.writeShort(0);
        }
        packet.writeShort(skillid);
        packet.writeShort(level);
        packet.writeShort(0); // same as give_buff
        packet.writeShort(900); // Delay

        return packet.getPacket();
    }

    public static byte[] cancelForeignDebuff(int cid, long mask, boolean first) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);
        packet.writeLong(first ? mask : 0);
        packet.writeLong(first ? 0 : mask);

        return packet.getPacket();
    }

    public static byte[] showMonsterRiding(
            int cid, List<Pair<MapleBuffStat, Integer>> statups, int itemId, int skillId) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);

        writeLongMask(packet, statups);

        packet.writeShort(0);
        packet.writeInt(itemId);
        packet.writeInt(skillId);
        packet.writeInt(0);
        packet.writeShort(0);
        packet.write(0);
        packet.write(0);

        return packet.getPacket();
    }

    public static byte[] giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups, MapleStatEffect effect) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);

        writeLongMask(packet, statups);

        for (Pair<MapleBuffStat, Integer> statup : statups) {
            packet.writeShort(statup.getRight().shortValue());
        }
        packet.writeShort(0); // same as give_buff
        if (effect.isMorph()) {
            packet.write(0);
        }
        packet.write(0);

        return packet.getPacket();
    }

    public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        packet.writeInt(cid);

        writeLongMaskFromList(packet, statups);

        return packet.getPacket();
    }

    public static byte[] cancelBuff(List<MapleBuffStat> statups) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        if (statups != null) {
            writeLongMaskFromList(packet, statups);
            packet.write(3);
        } else {
            packet.writeLong(0);
            packet.writeInt(0x40);
            packet.writeInt(0x1000);
        }

        return packet.getPacket();
    }

    public static byte[] cancelHoming() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        packet.writeLong(MapleBuffStat.HOMING_BEACON.getValue());
        packet.writeLong(0);

        return packet.getPacket();
    }

    public static byte[] cancelDebuff(long mask, boolean first) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        packet.writeLong(first ? mask : 0);
        packet.writeLong(first ? 0 : mask);
        packet.write(1);

        return packet.getPacket();
    }

    public static byte[] updateMount(MapleCharacter chr, boolean levelup) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        packet.writeInt(chr.getId());
        packet.writeInt(chr.getMount().getLevel());
        packet.writeInt(chr.getMount().getExp());
        packet.writeInt(chr.getMount().getFatigue());
        packet.write(levelup ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        PacketHelper.addCharLook(packet, c, false);
        packet.writeMapleAsciiString(c.getName());
        packet.writeShort(c.getJob().getId());

        return packet.getPacket();
    }

    public static byte[] getPlayerShopRemoveVisitor(int slot) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(PlayerInteractionHandler.EXIT);
        if (slot > 0) {
            packet.writeShort(slot);
        }

        return packet.getPacket();
    }

    public static byte[] getTradePartnerAdd(MapleCharacter c) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(4);
        packet.write(1);
        PacketHelper.addCharLook(packet, c, false);
        packet.writeMapleAsciiString(c.getName());
        packet.writeShort(c.getJob().getId());

        return packet.getPacket();
    }

    public static byte[] getTradeInvite(MapleCharacter c) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(2);
        packet.write(3);
        packet.writeMapleAsciiString(c.getName());
        packet.writeInt(0); // Trade ID

        return packet.getPacket();
    }

    public static byte[] getTradeMesoSet(byte number, int meso) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x10);
        packet.write(number);
        packet.writeInt(meso);

        return packet.getPacket();
    }

    public static byte[] getTradeItemAdd(byte number, IItem item) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0xF);
        packet.write(number);
        PacketHelper.addItemInfo(packet, item, false, false, true);

        return packet.getPacket();
    }

    public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(5);
        packet.write(3);
        packet.write(2);
        packet.write(number);

        if (number == 1) {
            packet.write(0);
            PacketHelper.addCharLook(packet, trade.getPartner().getChr(), false);
            packet.writeMapleAsciiString(trade.getPartner().getChr().getName());
            packet.writeShort(trade.getPartner().getChr().getJob().getId());
        }
        packet.write(number);
        PacketHelper.addCharLook(packet, c.getPlayer(), false);
        packet.writeMapleAsciiString(c.getPlayer().getName());
        packet.writeShort(c.getPlayer().getJob().getId());
        packet.write(0xFF);

        return packet.getPacket();
    }

    public static byte[] getTradeConfirmation() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0x11); // or 7? what

        return packet.getPacket();
    }

    public static byte[] TradeMessage(final byte UserSlot, final byte message) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0xA);
        packet.write(UserSlot);
        packet.write(message);
        // 0x02 = cancelled
        // 0x07 = success [tax is automated]
        // 0x08 = unsuccessful
        // 0x09 = "You cannot make the trade because there are some items which
        // you cannot carry more than one."
        // 0x0A = "You cannot make the trade because the other person's on a
        // different map."

        return packet.getPacket();
    }

    public static byte[] getTradeCancel(final byte UserSlot, final int unsuccessful) { // 0
        // =
        // canceled
        // 1
        // =
        // invent
        // space
        // 2
        // =
        // pickuprestricted
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        packet.write(0xA);
        packet.write(UserSlot);
        packet.write(unsuccessful == 0 ? 2 : (unsuccessful == 1 ? 8 : 9));

        return packet.getPacket();
    }

    public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
        return getNPCTalk(npc, msgType, talk, endBytes, type, 0);
    }

    public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type, int OtherNPC) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npc);
        packet.write(msgType);
        packet.write(type); // 1 = No ESC, 3 = show character + no sec
        if (type >= 4 && type <= 5) {
            packet.writeInt(OtherNPC);
        }
        packet.writeMapleAsciiString(talk);
        packet.write(HexTool.getByteArrayFromHexString(endBytes));

        return packet.getPacket();
    }

    public static final byte[] getMapSelection(final int npcid, final String sel) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npcid);
        packet.writeShort(15);

        packet.writeInt(0); // type, usually is 0, or 1 // If type is 1, then sel
        // is 0-6, else is 0-7 and 99
        packet.writeInt(5);
        packet.writeMapleAsciiString(sel);

        return packet.getPacket();
    }

    public static final byte[] getSpeedQuiz(int npc, byte type, int oid, int points, int questionNo, int time) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npc);
        packet.writeShort(7); // Speed quiz
        packet.write(0); // 1 = close
        packet.writeInt(type); // Type: 0 = NPC, 1 = Mob, 2 = Item
        packet.writeInt(oid); // Object id
        packet.writeInt(points); // points
        packet.writeInt(questionNo); // questions
        packet.writeInt(time); // time in seconds

        return packet.getPacket();
    }

    public static final byte[] getQuiz() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(9010010);
        packet.write(6); // quiz
        packet.write(0);

        packet.write(0); // 1 = close
        packet.writeMapleAsciiString("123"); // Main topic
        packet.writeMapleAsciiString("     123"); // Question
        packet.writeMapleAsciiString(" none"); // Clue
        packet.writeInt(10); // min characters
        packet.writeInt(20); // max characters
        packet.writeInt(30); // time in seconds

        return packet.getPacket();
    }

    public static byte[] getNPCTalkStyle(int npc, String talk, int... args) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npc);
        packet.writeShort(8);
        packet.writeMapleAsciiString(talk);
        packet.write(args.length);

        for (int i = 0; i < args.length; i++) {
            packet.writeInt(args[i]);
        }
        return packet.getPacket();
    }

    public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npc);
        packet.writeShort(4);
        packet.writeMapleAsciiString(talk);
        packet.writeInt(def);
        packet.writeInt(min);
        packet.writeInt(max);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static byte[] getNPCTalkText(int npc, String talk) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        packet.write(4);
        packet.writeInt(npc);
        packet.writeShort(3);
        packet.writeMapleAsciiString(talk);
        packet.writeInt(0);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static byte[] showForeignEffect(int cid, int effect) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(effect); // 0 = Level up, 8 = job change

        return packet.getPacket();
    }

    public static byte[] showBuffeffect(int cid, int skillid, int effectid) {
        return showBuffeffect(cid, skillid, effectid, (byte) 3);
    }

    public static byte[] showBuffeffect(int cid, int skillid, int effectid, byte direction) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(effectid); // ehh?
        packet.writeInt(skillid);
        packet.write(1); // skill level = 1 for the lulz
        packet.write(1); // actually skill level ? 0 = dosnt show
        if (direction != (byte) 3) {
            packet.write(direction);
        }
        return packet.getPacket();
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid) {
        return showOwnBuffEffect(skillid, effectid, (byte) 3);
    }

    public static byte[] showOwnBuffEffect(int skillid, int effectid, byte direction) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(effectid);
        packet.writeInt(skillid);
        packet.write(1); // skill level = 1 for the lulz
        packet.write(1); // 0 = doesnt show? or is this even here
        if (direction != (byte) 3) {
            packet.write(direction);
        }

        return packet.getPacket();
    }

    public static byte[] showItemLevelupEffect() {
        return showSpecialEffect(15);
    }

    public static byte[] showForeignItemLevelupEffect(int cid) {
        return showSpecialEffect(cid, 15);
    }

    public static byte[] showSpecialEffect(int effect) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(effect);

        return packet.getPacket();
    }

    public static byte[] showSpecialEffect(int cid, int effect) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(effect);

        return packet.getPacket();
    }

    public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SKILLS_UPDATE.getValue());
        packet.write(1);
        packet.writeShort(1);
        packet.writeInt(skillid);
        packet.writeInt(level);
        packet.writeInt(masterlevel);
        PacketHelper.addExpirationTime(packet, expiration);
        packet.write(4);

        return packet.getPacket();
    }

    public static byte[] updateSkill(final Map<ISkill, SkillEntry> skills) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SKILLS_UPDATE.getValue());
        packet.write(1);
        packet.writeShort(skills.size());
        for (final Entry<ISkill, SkillEntry> sk : skills.entrySet()) {
            packet.writeInt(sk.getKey().getId());
            packet.writeInt(sk.getValue().skillevel);
            packet.writeInt(sk.getValue().masterlevel);
            PacketHelper.addExpirationTime(packet, sk.getValue().expiration);
        }
        packet.write(4);

        return packet.getPacket();
    }

    public static final byte[] updateQuestMobKills(final MapleQuestStatus status) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(1);
        packet.writeShort(status.getQuest().getId());
        packet.write(1);

        final StringBuilder sb = new StringBuilder();
        for (final int kills : status.getMobKills().values()) {
            sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
        }
        packet.writeMapleAsciiString(sb.toString());
        packet.writeZeroBytes(8);

        return packet.getPacket();
    }

    public static byte[] getShowQuestCompletion(int id) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        packet.writeShort(id);

        return packet.getPacket();
    }

    public static byte[] getKeymap(MapleKeyLayout layout) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.KEYMAP.getValue());
        packet.write(0);
        layout.encode(packet);

        return packet.getPacket();
    }

    public static byte[] getQuickSlot(final String qs) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
        packet.write((qs == null || qs.equals("")) ? 0 : 1);
        if (qs != null && !qs.equals("")) {
            final String[] slots = qs.split(",");
            for (int i = 0; i < 8; i++) {
                packet.writeInt(Integer.parseInt(slots[i]));
            }
        }

        return packet.getPacket();
    }

    public static byte[] getWhisper(String sender, int channel, String text) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(0x12);
        packet.writeMapleAsciiString(sender);
        packet.writeShort(channel - 1);
        packet.writeMapleAsciiString(text);

        return packet.getPacket();
    }

    public static byte[] getWhisperReply(String target, byte reply) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(0x0A); // whisper?
        packet.writeMapleAsciiString(target);
        packet.write(reply); // 0x0 = cannot find char, 0x1 = success

        return packet.getPacket();
    }

    public static byte[] getFindReplyWithMap(String target, int mapid, final boolean buddy) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(buddy ? 72 : 9);
        packet.writeMapleAsciiString(target);
        packet.write(1);
        packet.writeInt(mapid);
        packet.writeZeroBytes(8); // ?? official doesn't send zeros here but
        // whatever

        return packet.getPacket();
    }

    public static byte[] getFindReply(String target, int channel, final boolean buddy) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(buddy ? 72 : 9);
        packet.writeMapleAsciiString(target);
        packet.write(3);
        packet.writeInt(channel - 1);

        return packet.getPacket();
    }

    public static byte[] getInventoryFull() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        packet.write(1);
        packet.write(0);

        return packet.getPacket();
    }

    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public static byte[] showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }

    public static byte[] getShowInventoryStatus(int mode) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(0);
        packet.write(mode);
        packet.writeInt(0);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static byte[] getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x16);
        packet.writeInt(npcId);
        packet.write(slots);
        packet.writeShort(0x7E);
        packet.writeShort(0);
        packet.writeInt(0);
        packet.writeInt(meso);
        packet.writeShort(0);
        packet.write((byte) items.size());
        for (IItem item : items) {
            PacketHelper.addItemInfo(packet, item, true, true);
        }
        packet.writeShort(0);
        packet.write(0);

        return packet.getPacket();
    }

    public static byte[] getStorageFull() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x11);

        return packet.getPacket();
    }

    public static byte[] mesoStorage(byte slots, int meso) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x13);
        packet.write(slots);
        packet.writeShort(2);
        packet.writeShort(0);
        packet.writeInt(0);
        packet.writeInt(meso);

        return packet.getPacket();
    }

    public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x0D);
        packet.write(slots);
        packet.writeShort(type.getBitfieldEncoding());
        packet.writeShort(0);
        packet.writeInt(0);
        packet.write(items.size());
        for (IItem item : items) {
            PacketHelper.addItemInfo(packet, item, true, true);
        }
        return packet.getPacket();
    }

    public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        packet.write(0x9);
        packet.write(slots);
        packet.writeShort(type.getBitfieldEncoding());
        packet.writeShort(0);
        packet.writeInt(0);
        packet.write(items.size());
        for (IItem item : items) {
            PacketHelper.addItemInfo(packet, item, true, true);
        }
        return packet.getPacket();
    }

    public static byte[] fairyPendantMessage(int percent) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BONUS_EXP_CHANGED.getValue());
        packet.writeInt(0x11); // 0x11 = pendant, 0x31 = evan medal
        packet.writeInt(0); // GMS doens't send hour here.
        packet.writeInt(percent);

        return packet.getPacket();
    }

    public static byte[] giveFameResponse(int mode, String charname, int newfame) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        packet.write(0);
        packet.writeMapleAsciiString(charname);
        packet.write(mode); // 1 or 0
        packet.writeInt(newfame);

        return packet.getPacket();
    }

    public static byte[] giveFameErrorResponse(int status) {
        OutPacket packet = new OutPacket();
        // 1: The user name is incorrectly entered.
        // 2: Users under leve l5 are unable to toggle with fame.
        // 3: You can't raise or drop a level anymore for today.
        // 4: You can't raise or drop a level of fame of that character anymore
        // for this month.
        // 6: The level of fame has neither been raise or dropped due to an
        // unexpected error.
        packet.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        packet.write(status);

        return packet.getPacket();
    }

    public static byte[] receiveFame(int mode, String charnameFrom) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        packet.write(5);
        packet.writeMapleAsciiString(charnameFrom);
        packet.write(mode); // 1 : Raised, 0 : Dropped

        return packet.getPacket();
    }

    public static byte[] multiChat(String name, String chattext, int mode) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MULTICHAT.getValue());
        packet.write(mode); // 0 buddychat; 1 partychat; 2 guildchat
        packet.writeMapleAsciiString(name);
        packet.writeMapleAsciiString(chattext);

        return packet.getPacket();
    }

    public static byte[] getClock(int time) { // time in seconds
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CLOCK.getValue());
        packet.write(2); // clock type. if you send 3 here you have to send
        // another byte (which does not matter at all) before
        // the timestamp
        packet.writeInt(time);

        return packet.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) { // Current
        // Time
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CLOCK.getValue());
        packet.write(1); // Clock-Type
        packet.write(hour);
        packet.write(min);
        packet.write(sec);

        return packet.getPacket();
    }

    public static byte[] spawnMist(final MapleMist mist) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        packet.writeInt(mist.getObjectId());
        packet.writeInt(mist.isMobMist() ? 0 : (mist.isPoisonMist() != 0 ? 1 : 2));
        packet.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            packet.writeInt(mist.getSourceSkill().getId());
        } else {
            packet.writeInt(mist.getMobSkill().getSkillId());
        }
        packet.write(mist.getSkillLevel());
        packet.writeShort(mist.getSkillDelay());
        packet.writeInt(mist.getBox().x);
        packet.writeInt(mist.getBox().y);
        packet.writeInt(mist.getBox().x + mist.getBox().width);
        packet.writeInt(mist.getBox().y + mist.getBox().height);
        packet.writeInt(0);
        packet.writeInt(0);

        return packet.getPacket();
    }

    public static byte[] removeMist(final int oid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        packet.writeInt(oid);

        return packet.getPacket();
    }

    public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        packet.writeInt(cid);
        packet.writeInt(summonSkillId);
        packet.write(unkByte);
        packet.writeInt(damage);
        packet.writeInt(monsterIdFrom);
        packet.write(0);

        return packet.getPacket();
    }

    public static byte[] buddylistMessage(byte message) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(message);

        return packet.getPacket();
    }

    public static byte[] updateBuddylist(byte action, Collection<BuddyListEntry> buddylist) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(action);
        packet.write(buddylist.size());

        for (BuddyListEntry buddy : buddylist) {
            packet.writeInt(buddy.getCharacterId());
            packet.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getName(), '\0', 13));
            packet.write(0);
            packet.writeInt(buddy.getChannel() == -1 ? -1 : buddy.getChannel() - 1);
            packet.writeAsciiString(StringUtil.getRightPaddedStr(buddy.getGroup(), '\0', 17));
        }
        for (int x = 0; x < buddylist.size(); x++) {
            packet.writeInt(0);
        }
        return packet.getPacket();
    }

    public static byte[] requestBuddylistAdd(int cidFrom, String nameFrom, int levelFrom, int jobFrom) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(9);
        packet.writeInt(cidFrom);
        packet.writeMapleAsciiString(nameFrom);
        packet.writeInt(levelFrom);
        packet.writeInt(jobFrom);
        packet.writeInt(cidFrom);
        packet.writeAsciiString(StringUtil.getRightPaddedStr(nameFrom, '\0', 13));
        packet.write(1);
        packet.writeInt(0);
        packet.writeAsciiString(StringUtil.getRightPaddedStr("Default Group", '\0', 16));
        packet.writeShort(1);

        return packet.getPacket();
    }

    public static byte[] updateBuddyChannel(int characterid, int channel) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(0x14);
        packet.writeInt(characterid);
        packet.write(0);
        packet.writeInt(channel);

        return packet.getPacket();
    }

    public static byte[] itemEffect(int characterid, int itemid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        packet.writeInt(characterid);
        packet.writeInt(itemid);

        return packet.getPacket();
    }

    public static byte[] updateBuddyCapacity(int capacity) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        packet.write(0x15);
        packet.write(capacity);

        return packet.getPacket();
    }

    public static byte[] showChair(int characterid, int itemid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
        packet.writeInt(characterid);
        packet.writeInt(itemid);

        return packet.getPacket();
    }

    public static byte[] cancelChair(int id) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            packet.write(0);
        } else {
            packet.write(1);
            packet.writeShort(id);
        }
        return packet.getPacket();
    }

    public static byte[] spawnReactor(MapleReactor reactor) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        packet.writeInt(reactor.getObjectId());
        packet.writeInt(reactor.getReactorId());
        packet.write(reactor.getState());
        packet.writePos(reactor.getPosition());
        packet.write(reactor.getFacingDirection()); // stance
        packet.writeMapleAsciiString(reactor.getName());

        return packet.getPacket();
    }

    public static byte[] triggerReactor(MapleReactor reactor, int stance) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        packet.writeInt(reactor.getObjectId());
        packet.write(reactor.getState());
        packet.writePos(reactor.getPosition());
        packet.writeShort(stance);
        packet.write(0);
        packet.write(4); // frame delay, set to 5 since there doesn't appear to
        // be a fixed formula for it

        return packet.getPacket();
    }

    public static byte[] destroyReactor(MapleReactor reactor) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        packet.writeInt(reactor.getObjectId());
        packet.write(reactor.getState());
        packet.writePos(reactor.getPosition());

        return packet.getPacket();
    }

    public static byte[] musicChange(String song) {
        return environmentChange(song, 6);
    }

    public static byte[] showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public static byte[] playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public static byte[] environmentChange(String env, int mode) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        packet.write(mode);
        packet.writeMapleAsciiString(env);

        return packet.getPacket();
    }

    public static byte[] environmentMove(String env, int mode) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
        packet.writeMapleAsciiString(env);
        packet.writeInt(mode);

        return packet.getPacket();
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        packet.write(active ? 0 : 1);

        packet.writeInt(itemid);
        if (active) {
            packet.writeMapleAsciiString(msg);
        }
        return packet.getPacket();
    }

    public static byte[] removeMapEffect() {
        return startMapEffect(null, 0, false);
    }

    public static byte[] showGuildInfo(MapleCharacter c) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x1A); // signature for showing guild info

        if (c == null || c.getMGC() == null) { // show empty guild (used for
            // leaving, expelled)
            packet.write(0);
            return packet.getPacket();
        }
        MapleGuild g = GuildManager.getGuild(c.getGuildId());
        if (g == null) { // failed to read from DB - don't show a guild
            packet.write(0);
            return packet.getPacket();
        }
        packet.write(1); // bInGuild
        getGuildInfo(packet, g);

        return packet.getPacket();
    }

    private static void getGuildInfo(OutPacket packet, MapleGuild guild) {
        packet.writeInt(guild.getId());
        packet.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            packet.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(packet);
        packet.writeInt(guild.getCapacity());
        packet.writeShort(guild.getLogoBG());
        packet.write(guild.getLogoBGColor());
        packet.writeShort(guild.getLogo());
        packet.write(guild.getLogoColor());
        packet.writeMapleAsciiString(guild.getNotice());
        packet.writeInt(guild.getGP());
        packet.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
    }

    public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x3d);
        packet.writeInt(gid);
        packet.writeInt(cid);
        packet.write(bOnline ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] guildInvite(int gid, String charName, int levelFrom, int jobFrom) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x05);
        packet.writeInt(gid);
        packet.writeMapleAsciiString(charName);
        packet.writeInt(levelFrom);
        packet.writeInt(jobFrom);

        return packet.getPacket();
    }

    public static byte[] denyGuildInvitation(String charname) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x37);
        packet.writeMapleAsciiString(charname);

        return packet.getPacket();
    }

    public static byte[] genericGuildMessage(byte code) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(code);

        return packet.getPacket();
    }

    public static byte[] newGuildMember(MapleGuildCharacter mgc) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x27);
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeAsciiString(StringUtil.getRightPaddedStr(mgc.getName(), '\0', 13));
        packet.writeInt(mgc.getJobId());
        packet.writeInt(mgc.getLevel());
        packet.writeInt(mgc.getGuildRank()); // should be always 5 but whatevs
        packet.writeInt(mgc.isOnline() ? 1 : 0); // should always be 1 too
        packet.writeInt(1); // ? could be guild signature, but doesn't seem to
        // matter
        packet.writeInt(mgc.getAllianceRank()); // should always 3

        return packet.getPacket();
    }

    // someone leaving, mode == 0x2c for leaving, 0x2f for expelled
    public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(bExpelled ? 0x2f : 0x2c);

        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeMapleAsciiString(mgc.getName());

        return packet.getPacket();
    }

    public static byte[] changeRank(MapleGuildCharacter mgc) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x40);
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.write(mgc.getGuildRank());

        return packet.getPacket();
    }

    public static byte[] guildNotice(int gid, String notice) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x44);
        packet.writeInt(gid);
        packet.writeMapleAsciiString(notice);

        return packet.getPacket();
    }

    public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x3C);
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeInt(mgc.getLevel());
        packet.writeInt(mgc.getJobId());

        return packet.getPacket();
    }

    public static byte[] rankTitleChange(int gid, String[] ranks) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x3e);
        packet.writeInt(gid);

        for (String r : ranks) {
            packet.writeMapleAsciiString(r);
        }
        return packet.getPacket();
    }

    public static byte[] guildDisband(int gid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x32);
        packet.writeInt(gid);
        packet.write(1);

        return packet.getPacket();
    }

    public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x42);
        packet.writeInt(gid);
        packet.writeShort(bg);
        packet.write(bgcolor);
        packet.writeShort(logo);
        packet.write(logocolor);

        return packet.getPacket();
    }

    public static byte[] guildCapacityChange(int gid, int capacity) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x3a);
        packet.writeInt(gid);
        packet.write(capacity);

        return packet.getPacket();
    }

    public static byte[] removeGuildFromAlliance(
            MapleGuildAlliance alliance, MapleGuild expelledGuild, boolean expelled) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x10);
        addAllianceInfo(packet, alliance);
        getGuildInfo(packet, expelledGuild);
        packet.write(expelled ? 1 : 0); // 1 = expelled, 0 = left
        return packet.getPacket();
    }

    public static byte[] changeAlliance(MapleGuildAlliance alliance, final boolean in) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x01);
        packet.write(in ? 1 : 0);
        packet.writeInt(in ? alliance.getId() : 0);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < noGuilds; i++) {
            g[i] = GuildManager.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        packet.write(noGuilds);
        for (int i = 0; i < noGuilds; i++) {
            packet.writeInt(g[i].getId());
            // must be world
            Collection<MapleGuildCharacter> members = g[i].getMembers();
            packet.writeInt(members.size());
            for (MapleGuildCharacter mgc : members) {
                packet.writeInt(mgc.getId());
                packet.write(in ? mgc.getAllianceRank() : 0);
            }
        }
        return packet.getPacket();
    }

    public static byte[] changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x02);
        packet.writeInt(allianceid);
        packet.writeInt(oldLeader);
        packet.writeInt(newLeader);
        return packet.getPacket();
    }

    public static byte[] updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x19);
        packet.writeInt(allianceid);
        packet.writeInt(oldLeader);
        packet.writeInt(newLeader);
        return packet.getPacket();
    }

    public static byte[] sendAllianceInvite(String allianceName, MapleCharacter inviter) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x03);
        packet.writeInt(inviter.getGuildId());
        packet.writeMapleAsciiString(inviter.getName());
        // alliance invite did NOT change
        packet.writeMapleAsciiString(allianceName);
        return packet.getPacket();
    }

    public static byte[] changeGuildInAlliance(MapleGuildAlliance alliance, MapleGuild guild, final boolean add) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x04);
        packet.writeInt(add ? alliance.getId() : 0);
        packet.writeInt(guild.getId());
        Collection<MapleGuildCharacter> members = guild.getMembers();
        packet.writeInt(members.size());
        for (MapleGuildCharacter mgc : members) {
            packet.writeInt(mgc.getId());
            packet.write(add ? mgc.getAllianceRank() : 0);
        }
        return packet.getPacket();
    }

    public static byte[] changeAllianceRank(int allianceid, MapleGuildCharacter player) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x05);
        packet.writeInt(allianceid);
        packet.writeInt(player.getId());
        packet.writeInt(player.getAllianceRank());
        return packet.getPacket();
    }

    public static byte[] createGuildAlliance(MapleGuildAlliance alliance) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0F);
        addAllianceInfo(packet, alliance);
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = GuildManager.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        for (MapleGuild gg : g) {
            getGuildInfo(packet, gg);
        }
        return packet.getPacket();
    }

    public static byte[] getAllianceInfo(MapleGuildAlliance alliance) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0C);
        packet.write(alliance == null ? 0 : 1); // in an alliance
        if (alliance != null) {
            addAllianceInfo(packet, alliance);
        }
        return packet.getPacket();
    }

    public static byte[] getAllianceUpdate(MapleGuildAlliance alliance) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x17);
        addAllianceInfo(packet, alliance);
        return packet.getPacket();
    }

    public static byte[] getGuildAlliance(MapleGuildAlliance alliance) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0D);
        if (alliance == null) {
            packet.writeInt(0);
            return packet.getPacket();
        }
        final int noGuilds = alliance.getNoGuilds();
        MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            g[i] = GuildManager.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        packet.writeInt(noGuilds);
        for (MapleGuild gg : g) {
            getGuildInfo(packet, gg);
        }
        return packet.getPacket();
    }

    public static byte[] addGuildToAlliance(MapleGuildAlliance alliance, MapleGuild newGuild) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x12);
        addAllianceInfo(packet, alliance);
        packet.writeInt(newGuild.getId()); // ???
        getGuildInfo(packet, newGuild);
        packet.write(0); // ???
        return packet.getPacket();
    }

    private static void addAllianceInfo(OutPacket packet, MapleGuildAlliance alliance) {
        packet.writeInt(alliance.getId());
        packet.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            packet.writeMapleAsciiString(alliance.getRank(i));
        }
        packet.write(alliance.getNoGuilds());
        for (int i = 0; i < alliance.getNoGuilds(); i++) {
            packet.writeInt(alliance.getGuildId(i));
        }
        packet.writeInt(alliance.getCapacity()); // ????
        packet.writeMapleAsciiString(alliance.getNotice());
    }

    public static byte[] allianceMemberOnline(int alliance, int gid, int id, boolean online) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x0E);
        packet.writeInt(alliance);
        packet.writeInt(gid);
        packet.writeInt(id);
        packet.write(online ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] updateAlliance(MapleGuildCharacter mgc, int allianceid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x18);
        packet.writeInt(allianceid);
        packet.writeInt(mgc.getGuildId());
        packet.writeInt(mgc.getId());
        packet.writeInt(mgc.getLevel());
        packet.writeInt(mgc.getJobId());

        return packet.getPacket();
    }

    public static byte[] updateAllianceRank(int allianceid, MapleGuildCharacter mgc) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x1B);
        packet.writeInt(allianceid);
        packet.writeInt(mgc.getId());
        packet.writeInt(mgc.getAllianceRank());

        return packet.getPacket();
    }

    public static byte[] disbandAlliance(int alliance) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        packet.write(0x1D);
        packet.writeInt(alliance);

        return packet.getPacket();
    }

    public static byte[] BBSThreadList(final List<MapleBBSThread> bbs, int start) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        packet.write(6);

        if (bbs == null) {
            packet.write(0);
            packet.writeLong(0);
            return packet.getPacket();
        }
        int threadCount = bbs.size();
        MapleBBSThread notice = null;
        for (MapleBBSThread b : bbs) {
            if (b.isNotice()) { // notice
                notice = b;
                break;
            }
        }
        final int ret = (notice == null ? 0 : 1);
        packet.write(ret);
        if (notice != null) { // has a notice
            addThread(packet, notice);
            threadCount--; // one thread didn't count (because it's a notice)
        }
        if (threadCount < start) { // seek to the thread before where we start
            // uh, we're trying to start at a place past possible
            start = 0;
        }
        // each page has 10 threads, start = page # in packet but not here
        packet.writeInt(threadCount);
        final int pages = Math.min(10, threadCount - start);
        packet.writeInt(pages);

        for (int i = 0; i < pages; i++) {
            addThread(packet, bbs.get(start + i + ret)); // because 0 = notice
        }
        return packet.getPacket();
    }

    private static void addThread(OutPacket packet, MapleBBSThread rs) {
        packet.writeInt(rs.localthreadID);
        packet.writeInt(rs.ownerID);
        packet.writeMapleAsciiString(rs.name);
        packet.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
        packet.writeInt(rs.icon);
        packet.writeInt(rs.getReplyCount());
    }

    public static byte[] showThread(MapleBBSThread thread) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        packet.write(7);

        packet.writeInt(thread.localthreadID);
        packet.writeInt(thread.ownerID);
        packet.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
        packet.writeMapleAsciiString(thread.name);
        packet.writeMapleAsciiString(thread.text);
        packet.writeInt(thread.icon);
        packet.writeInt(thread.getReplyCount());
        for (MapleBBSReply reply : thread.replies.values()) {
            packet.writeInt(reply.replyid);
            packet.writeInt(reply.ownerID);
            packet.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
            packet.writeMapleAsciiString(reply.content);
        }
        return packet.getPacket();
    }

    public static byte[] showGuildRanks(int npcid, List<GuildRankingInfo> all) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x49);
        packet.writeInt(npcid);
        packet.writeInt(all.size());

        for (GuildRankingInfo info : all) {
            packet.writeMapleAsciiString(info.getName());
            packet.writeInt(info.getGP());
            packet.writeInt(info.getLogo());
            packet.writeInt(info.getLogoColor());
            packet.writeInt(info.getLogoBg());
            packet.writeInt(info.getLogoBgColor());
        }

        return packet.getPacket();
    }

    public static byte[] updateGP(int gid, int GP) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        packet.write(0x48);
        packet.writeInt(gid);
        packet.writeInt(GP);

        return packet.getPacket();
    }

    public static byte[] skillEffect(
            MapleCharacter from, int skillId, byte level, byte direction, byte speed, byte unk) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        packet.writeInt(from.getId());
        packet.writeInt(skillId);
        packet.write(level);
        packet.write(direction);
        packet.write(speed);
        packet.writeZeroBytes(5); // Direction ??

        return packet.getPacket();
    }

    public static byte[] skillCancel(MapleCharacter from, int skillId) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        packet.writeInt(from.getId());
        packet.writeInt(skillId);

        return packet.getPacket();
    }

    public static byte[] showMagnet(int mobid, byte success) { // Monster Magnet
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        packet.writeInt(mobid);
        packet.write(success);

        return packet.getPacket();
    }

    public static byte[] sendHint(String hint, int width, int height) {
        OutPacket packet = new OutPacket();

        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        packet.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        packet.writeMapleAsciiString(hint);
        packet.writeShort(width);
        packet.writeShort(height);
        packet.write(1);

        return packet.getPacket();
    }

    public static byte[] messengerInvite(String from, int messengerid) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x03);
        packet.writeMapleAsciiString(from);
        packet.write(0x00);
        packet.writeInt(messengerid);
        packet.write(0x00);

        return packet.getPacket();
    }

    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x00);
        packet.write(position);
        PacketHelper.addCharLook(packet, chr, true);
        packet.writeMapleAsciiString(from);
        packet.writeShort(channel);

        return packet.getPacket();
    }

    public static byte[] removeMessengerPlayer(int position) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x02);
        packet.write(position);

        return packet.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x07);
        packet.write(position);
        PacketHelper.addCharLook(packet, chr, true);
        packet.writeMapleAsciiString(from);
        packet.writeShort(channel);

        return packet.getPacket();
    }

    public static byte[] joinMessenger(int position) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x01);
        packet.write(position);

        return packet.getPacket();
    }

    public static byte[] messengerChat(String text) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(0x06);
        packet.writeMapleAsciiString(text);

        return packet.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MESSENGER.getValue());
        packet.write(mode);
        packet.writeMapleAsciiString(text);
        packet.write(mode2);

        return packet.getPacket();
    }

    public static byte[] getFindReplyWithCS(String target, final boolean buddy) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(buddy ? 72 : 9);
        packet.writeMapleAsciiString(target);
        packet.write(2);
        packet.writeInt(-1);

        return packet.getPacket();
    }

    public static byte[] getFindReplyWithMTS(String target, final boolean buddy) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.WHISPER.getValue());
        packet.write(buddy ? 72 : 9);
        packet.writeMapleAsciiString(target);
        packet.write(0);
        packet.writeInt(-1);

        return packet.getPacket();
    }

    public static byte[] showEquipEffect() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

        return packet.getPacket();
    }

    public static byte[] showEquipEffect(int team) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        packet.writeShort(team);
        return packet.getPacket();
    }

    public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
        packet.writeInt(cid);
        packet.writeInt(summonSkillId);
        packet.write(newStance);

        return packet.getPacket();
    }

    public static byte[] skillCooldown(int sid, int time) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        packet.writeInt(sid);
        packet.writeShort(time);

        return packet.getPacket();
    }

    public static byte[] useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        packet.write(0); // ?
        packet.writeInt(chr.getId());
        packet.write(1);
        packet.writeInt(skillid);
        packet.writeInt(maxlevel);
        packet.write(canuse ? 1 : 0);
        packet.write(success ? 1 : 0);

        return packet.getPacket();
    }

    public static byte[] getMacros(SkillMacro[] macros) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        packet.write(count); // number of macros
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                packet.writeMapleAsciiString(macro.getName());
                packet.write(macro.getShout());
                packet.writeInt(macro.getSkill1());
                packet.writeInt(macro.getSkill2());
                packet.writeInt(macro.getSkill3());
            }
        }
        return packet.getPacket();
    }

    public static byte[] updateAriantPQRanking(String name, int score, boolean empty) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ARIANT_PQ_START.getValue());
        packet.write(empty ? 0 : 1);
        if (!empty) {
            packet.writeMapleAsciiString(name);
            packet.writeInt(score);
        }
        return packet.getPacket();
    }

    public static byte[] catchMonster(int mobid, int itemid, byte success) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
        packet.writeInt(mobid);
        packet.writeInt(itemid);
        packet.write(success);

        return packet.getPacket();
    }

    public static byte[] showAriantScoreBoard() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

        return packet.getPacket();
    }

    public static byte[] boatPacket(int effect) {
        OutPacket packet = new OutPacket();

        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        packet.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        packet.writeShort(effect); // 0A 04 balrog
        // this packet had 3: boat leaves

        return packet.getPacket();
    }

    public static byte[] Mulung_DojoUp2() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(7);

        return packet.getPacket();
    }

    public static byte[] showQuestMsg(final String msg) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(10);
        packet.writeMapleAsciiString(msg);
        return packet.getPacket();
    }

    public static byte[] Mulung_Pts(int recv, int total) {
        return showQuestMsg("You have received "
                + recv
                + " training points, for the accumulated total of "
                + total
                + " training points.");
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
        packet.write(askQuestion ? 1 : 0);
        packet.write(questionSet);
        packet.writeShort(questionId);
        return packet.getPacket();
    }

    public static byte[] leftKnockBack() {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());
        return packet.getPacket();
    }

    public static byte[] rollSnowball(
            int type, MapleSnowball.MapleSnowballs ball1, MapleSnowball.MapleSnowballs ball2) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.ROLL_SNOWBALL.getValue());
        packet.write(type); // 0 = normal, 1 = rolls from start to end, 2 = down
        // disappear, 3 = up disappear, 4 = move
        packet.writeInt(ball1 == null ? 0 : (ball1.getSnowmanHP() / 75));
        packet.writeInt(ball2 == null ? 0 : (ball2.getSnowmanHP() / 75));
        packet.writeShort(ball1 == null ? 0 : ball1.getPosition());
        packet.write(0);
        packet.writeShort(ball2 == null ? 0 : ball2.getPosition());
        packet.writeZeroBytes(11);
        return packet.getPacket();
    }

    public static byte[] enterSnowBall() {
        return rollSnowball(0, null, null);
    }

    public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.HIT_SNOWBALL.getValue());
        packet.write(team); // 0 is down, 1 is up
        packet.writeShort(damage);
        packet.write(distance);
        packet.write(delay);
        return packet.getPacket();
    }

    public static byte[] snowballMessage(int team, int message) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
        packet.write(team); // 0 is down, 1 is up
        packet.writeInt(message);
        return packet.getPacket();
    }

    public static byte[] finishedSort(int type) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.FINISH_SORT.getValue());
        packet.write(1);
        packet.write(type);
        return packet.getPacket();
    }

    // 00 01 00 00 00 00
    public static byte[] coconutScore(int[] coconutscore) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.COCONUT_SCORE.getValue());
        packet.writeShort(coconutscore[0]);
        packet.writeShort(coconutscore[1]);
        return packet.getPacket();
    }

    public static byte[] hitCoconut(boolean spawn, int id, int type) {
        // FF 00 00 00 00 00 00
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.HIT_COCONUT.getValue());
        if (spawn) {
            packet.write(0);
            packet.writeInt(0x80);
        } else {
            packet.writeInt(id);
            packet.write(type); // What action to do for the coconut.
        }
        return packet.getPacket();
    }

    public static byte[] finishedGather(int type) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.FINISH_GATHER.getValue());
        packet.write(1);
        packet.write(type);
        return packet.getPacket();
    }

    public static byte[] yellowChat(String msg) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.YELLOW_CHAT.getValue());
        packet.write(-1); // could be something like mob displaying message.
        packet.writeMapleAsciiString(msg);
        return packet.getPacket();
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int itemId2, short quantity2) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.PIGMI_REWARD.getValue());
        packet.writeInt(itemId);
        packet.writeShort(quantity);
        packet.writeInt(5060003);
        packet.writeInt(itemId2);
        packet.writeInt(quantity2);

        return packet.getPacket();
    }

    public static byte[] sendLevelup(boolean family, int level, String name) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.LEVEL_UPDATE.getValue());
        packet.write(family ? 1 : 2);
        packet.writeInt(level);
        packet.writeMapleAsciiString(name);

        return packet.getPacket();
    }

    public static byte[] sendMarriage(boolean family, String name) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MARRIAGE_UPDATE.getValue());
        packet.write(family ? 1 : 0);
        packet.writeMapleAsciiString(name);

        return packet.getPacket();
    }

    public static byte[] sendJobup(boolean family, int jobid, String name) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.JOB_UPDATE.getValue());
        packet.write(family ? 1 : 0);
        packet.writeInt(jobid); // or is this a short
        packet.writeMapleAsciiString(name);

        return packet.getPacket();
    }

    public static byte[] showZakumShrine(boolean spawned, int time) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.ZAKUM_SHRINE.getValue());
        packet.write(spawned ? 1 : 0);
        packet.writeInt(time);
        return packet.getPacket();
    }

    public static byte[] showHorntailShrine(boolean spawned, int time) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
        packet.write(spawned ? 1 : 0);
        packet.writeInt(time);
        return packet.getPacket();
    }

    public static byte[] showChaosZakumShrine(boolean spawned, int time) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
        packet.write(spawned ? 1 : 0);
        packet.writeInt(time);
        return packet.getPacket();
    }

    public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.CHAOS_HORNTAIL_SHRINE.getValue());
        packet.write(spawned ? 1 : 0);
        packet.writeInt(time);
        return packet.getPacket();
    }

    public static byte[] stopClock() {
        return getPacketFromHexString("A9 00"); // does the header not work?
    }

    public static byte[] spawnDragon(MapleDragon d) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
        packet.writeInt(d.getOwner());
        packet.writeInt(d.getPosition().x);
        packet.writeInt(d.getPosition().y);
        packet.write(d.getStance()); // stance?
        packet.writeShort(0);
        packet.writeShort(d.getJobId());
        return packet.getPacket();
    }

    public static byte[] removeDragon(int chrid) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
        packet.writeInt(chrid);
        return packet.getPacket();
    }

    public static byte[] moveDragon(MapleDragon d, MovePath path) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue()); // not sure
        packet.writeInt(d.getOwner());
        path.encode(packet);

        return packet.getPacket();
    }

    public static final byte[] temporaryStats_Aran() {
        final List<Pair<MapleStat.TemporaryMapleStat, Integer>> stats =
                new ArrayList<Pair<MapleStat.TemporaryMapleStat, Integer>>();
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.STR, 999));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.DEX, 999));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.INT, 999));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.LUK, 999));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.WATK, 255));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.ACC, 999));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.AVOID, 999));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.SPEED, 140));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(MapleStat.TemporaryMapleStat.JUMP, 120));
        return temporaryStats(stats);
    }

    public static final byte[] temporaryStats_Balrog(final MapleCharacter chr) {
        final List<Pair<MapleStat.TemporaryMapleStat, Integer>> stats =
                new ArrayList<Pair<MapleStat.TemporaryMapleStat, Integer>>();
        int offset = 1 + (chr.getLevel() - 90) / 20;
        // every 20 levels above 90, +1
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(
                MapleStat.TemporaryMapleStat.STR, chr.getStat().getTotalStr() / offset));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(
                MapleStat.TemporaryMapleStat.DEX, chr.getStat().getTotalDex() / offset));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(
                MapleStat.TemporaryMapleStat.INT, chr.getStat().getTotalInt() / offset));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(
                MapleStat.TemporaryMapleStat.LUK, chr.getStat().getTotalLuk() / offset));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(
                MapleStat.TemporaryMapleStat.WATK, chr.getStat().getTotalWatk() / offset));
        stats.add(new Pair<MapleStat.TemporaryMapleStat, Integer>(
                MapleStat.TemporaryMapleStat.MATK, chr.getStat().getTotalMagic() / offset));
        return temporaryStats(stats);
    }

    public static final byte[] temporaryStats(final List<Pair<MapleStat.TemporaryMapleStat, Integer>> stats) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.FORCED_STATS.getValue());
        // str 0x1, dex 0x2, int 0x4, luk 0x8
        // level 0x10 = 255
        // 0x100 = 999
        // 0x200 = 999
        // 0x400 = 120
        // 0x800 = 140
        int updateMask = 0;
        for (final Pair<MapleStat.TemporaryMapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat.TemporaryMapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat.TemporaryMapleStat, Integer>>() {

                @Override
                public int compare(
                        final Pair<MapleStat.TemporaryMapleStat, Integer> o1,
                        final Pair<MapleStat.TemporaryMapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        packet.writeInt(updateMask);
        Integer value;

        for (final Pair<MapleStat.TemporaryMapleStat, Integer> statupdate : mystats) {
            value = statupdate.getLeft().getValue();

            if (value >= 1) {
                if (value <= 0x200) { // level 0x10 - is this really short or
                    // some other? (FF 00)
                    packet.writeShort(statupdate.getRight().shortValue());
                } else {
                    packet.write(statupdate.getRight().byteValue());
                }
            }
        }
        return packet.getPacket();
    }

    public static final byte[] temporaryStats_Reset() {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.FORCED_STATS_RESET.getValue());
        return packet.getPacket();
    }

    public static final byte[] showHpHealed(final int cid, final int amount) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        packet.writeInt(cid);
        packet.write(10);
        packet.writeInt(amount);

        return packet.getPacket();
    }

    public static final byte[] showOwnHpHealed(final int amount) {
        final OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(10);
        packet.writeInt(amount);

        return packet.getPacket();
    }

    public static final byte[] sendRepairWindow(int npc) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.REPAIR_WINDOW.getValue());
        packet.writeInt(GameUI.REPAIR_WINDOW);
        packet.writeInt(npc);
        return packet.getPacket();
    }

    public static final byte[] sendPyramidUpdate(final int amount) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
        packet.writeInt(amount); // 1-132 ?
        return packet.getPacket();
    }

    public static final byte[] sendPyramidResult(final byte rank, final int amount) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
        packet.write(rank);
        packet.writeInt(amount); // 1-132 ?
        return packet.getPacket();
    }

    // show_status_info - 01 53 1E 01
    // 10/08/14/19/11
    // update_quest_info - 08 53 1E 00 00 00 00 00 00 00 00
    // show_status_info - 01 51 1E 01 01 00 30
    // update_quest_info - 08 51 1E 00 00 00 00 00 00 00 00
    public static final byte[] sendPyramidEnergy(final String type, final String amount) {
        return sendString(1, type, amount);
    }

    public static final byte[] sendString(final int type, final String object, final String amount) {
        final OutPacket packet = new OutPacket();
        switch (type) {
            case 1:
                packet.writeShort(SendPacketOpcode.ENERGY.getValue());
                break;
            case 2:
                packet.writeShort(SendPacketOpcode.GHOST_POINT.getValue());
                break;
            case 3:
                packet.writeShort(SendPacketOpcode.GHOST_STATUS.getValue());
                break;
        }
        packet.writeMapleAsciiString(object); // massacre_hit, massacre_cool,
        // massacre_miss,
        // massacre_party,
        // massacre_laststage,
        // massacre_skill
        packet.writeMapleAsciiString(amount);
        return packet.getPacket();
    }

    public static final byte[] sendGhostPoint(final String type, final String amount) {
        return sendString(2, type, amount); // PRaid_Point (0-1500???)
    }

    public static final byte[] sendGhostStatus(final String type, final String amount) {
        return sendString(3, type, amount); // Red_Stage(1-5), Blue_Stage,
        // blueTeamDamage, redTeamDamage
    }

    public static byte[] MulungEnergy(int energy) {
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    public static byte[] getEvanTutorial(String data) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());

        packet.writeInt(8);
        packet.write(0);
        packet.write(1);
        packet.write(1);
        packet.write(1);
        packet.writeMapleAsciiString(data);

        return packet.getPacket();
    }

    public static byte[] showEventInstructions() {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] getOwlOpen() { // best items! hardcoded
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        packet.write(7);
        packet.write(GameConstants.owlItems.length);
        for (int i : GameConstants.owlItems) {
            packet.writeInt(i);
        } // these are the most searched items. too lazy to actually make
        return packet.getPacket();
    }

    public static byte[] getOwlSearched(final int itemSearch, final List<HiredMerchant> hms) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        packet.write(6);
        packet.writeInt(0);
        packet.writeInt(itemSearch);
        int size = 0;

        for (HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }
        packet.writeInt(size);
        for (HiredMerchant hm : hms) {
            final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
            for (MaplePlayerShopItem item : items) {
                packet.writeMapleAsciiString(hm.getOwnerName());
                packet.writeInt(hm.getMap().getId());
                packet.writeMapleAsciiString(hm.getDescription());
                packet.writeInt(item.getItem().getQuantity()); // I THINK.
                packet.writeInt(item.getBundles()); // I THINK.
                packet.writeInt(item.getPrice());
                packet.writeInt(hm.getOwnerId());
                packet.write(hm.getFreeSlot() == -1 ? 1 : 0);
                if (item.getItem().getItemId() / 1000000 == 1) {
                    packet.write(1);
                    PacketHelper.addItemInfo(packet, item.getItem(), true, true);
                } else {
                    packet.write(2);
                }
            }
        }
        return packet.getPacket();
    }

    public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.RPS_GAME.getValue());
        packet.write(mode);
        switch (mode) {
            case 6: { // not enough mesos
                if (mesos != -1) {
                    packet.writeInt(mesos);
                }
                break;
            }
            case 8: { // open (npc)
                packet.writeInt(9000019);
                break;
            }
            case 11: { // selection vs answer
                packet.write(selection);
                packet.write(answer); // FF = lose, or if selection = answer then
                // lose ???
                break;
            }
        }
        return packet.getPacket();
    }

    public static final byte[] getSlotUpdate(byte invType, byte newSlots) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.INVENTORY_GROW.getValue());
        packet.write(invType);
        packet.write(newSlots);
        return packet.getPacket();
    }

    public static byte[] followRequest(int chrid) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.FOLLOW_REQUEST.getValue());
        packet.writeInt(chrid);
        return packet.getPacket();
    }

    public static byte[] followEffect(int initiator, int replier, Point toMap) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
        packet.writeInt(initiator);
        packet.writeInt(replier);
        if (replier == 0) { // cancel
            packet.write(toMap == null ? 0 : 1); // 1 -> x (int) y (int) to
            // change map
            if (toMap != null) {
                packet.writeInt(toMap.x);
                packet.writeInt(toMap.y);
            }
        }
        return packet.getPacket();
    }

    public static byte[] getFollowMsg(int opcode) {
        final OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
        packet.writeLong(opcode); // 5 = canceled request.
        return packet.getPacket();
    }

    // TODO FIX follow
    public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, MovePath path) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
        packet.writePos(otherStart);
        packet.writePos(myStart);
        path.encode(packet);
        packet.write(17);
        for (int i = 0; i < 8; i++) {
            packet.write(0);
        }
        packet.write(0);
        packet.writePos(otherEnd);
        packet.writePos(otherStart);

        return packet.getPacket();
    }

    public static final byte[] getFollowMessage(final String msg) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.FOLLOW_MESSAGE.getValue());
        packet.writeShort(0x0B); // ?
        packet.writeMapleAsciiString(msg); // white in gms, but msea just makes
        // it pink.. waste
        return packet.getPacket();
    }

    public static final byte[] getNodeProperties(final MapleMonster objectid, final MapleMap map) {
        // idk.
        if (objectid.getNodePacket() != null) {
            return objectid.getNodePacket();
        }
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MONSTER_PROPERTIES.getValue());
        packet.writeInt(objectid.getObjectId()); // ?
        packet.writeInt(map.getNodes().size());
        packet.writeInt(objectid.getPosition().x);
        packet.writeInt(objectid.getPosition().y);
        for (MapleNodeInfo mni : map.getNodes()) {
            packet.writeInt(mni.x);
            packet.writeInt(mni.y);
            packet.writeInt(mni.attr);
            if (mni.attr == 2) { // msg
                packet.writeInt(500); // ? talkMonster
            }
        }
        packet.writeZeroBytes(6);
        objectid.setNodePacket(packet.getPacket());
        return objectid.getNodePacket();
    }

    public static final byte[] getMovingPlatforms(final MapleMap map) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
        packet.writeInt(map.getPlatforms().size());
        for (MaplePlatform mp : map.getPlatforms()) {
            packet.writeMapleAsciiString(mp.name);
            packet.writeInt(mp.start);
            packet.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); x++) {
                packet.writeInt(mp.SN.get(x));
            }
            packet.writeInt(mp.speed);
            packet.writeInt(mp.x1);
            packet.writeInt(mp.x2);
            packet.writeInt(mp.y1);
            packet.writeInt(mp.y2);
            packet.writeInt(mp.x1); // ?
            packet.writeInt(mp.y1);
            packet.writeShort(mp.r);
        }
        return packet.getPacket();
    }

    public static final byte[] getUpdateEnvironment(final MapleMap map) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
        packet.writeInt(map.getEnvironment().size());
        for (Entry<String, Integer> mp : map.getEnvironment().entrySet()) {
            packet.writeMapleAsciiString(mp.getKey());
            packet.writeInt(mp.getValue());
        }
        return packet.getPacket();
    }

    public static byte[] sendEngagementRequest(String name, int cid) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.ENGAGE_REQUEST.getValue());
        packet.write(0); // mode, 0 = engage, 1 = cancel, 2 = answer.. etc
        packet.writeMapleAsciiString(name); // name
        packet.writeInt(cid); // playerid
        return packet.getPacket();
    }

    /**
     * @param type  - (0:Light&Long 1:Heavy&Short)
     * @param delay - seconds
     * @return
     */
    public static byte[] trembleEffect(int type, int delay) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        packet.write(1);
        packet.write(type);
        packet.writeInt(delay);
        return packet.getPacket();
    }

    public static byte[] sendEngagement(
            final byte msg, final int item, final MapleCharacter male, final MapleCharacter female) {
        OutPacket packet = new OutPacket();
        // 0B = Engagement has been concluded.
        // 0D = The engagement is cancelled.
        // 0E = The divorce is concluded.
        // 10 = The marriage reservation has been successsfully made.
        // 12 = Wrong character name
        // 13 = The party in not in the same map.
        // 14 = Your inventory is full. Please empty your E.T.C window.
        // 15 = The person's inventory is full.
        // 16 = The person cannot be of the same gender.
        // 17 = You are already engaged.
        // 18 = The person is already engaged.
        // 19 = You are already married.
        // 1A = The person is already married.
        // 1B = You are not allowed to propose.
        // 1C = The person is not allowed to be proposed to.
        // 1D = Unfortunately, the one who proposed to you has cancelled his
        // proprosal.
        // 1E = The person had declined the proposal with thanks.
        // 1F = The reservation has been cancelled. Try again later.
        // 20 = You cannot cancel the wedding after reservation.
        // 22 = The invitation card is ineffective.
        packet.writeShort(SendPacketOpcode.ENGAGE_RESULT.getValue());
        packet.write(msg); // 1103 custom quest
        if (msg == 11) {
            packet.writeInt(0); // ringid or uniqueid
            packet.writeInt(male.getId());
            packet.writeInt(female.getId());
            packet.writeShort(1); // always
            packet.writeInt(item);
            packet.writeInt(item); // wtf?repeat?
            packet.writeAsciiString(male.getName(), 13);
            packet.writeAsciiString(female.getName(), 13);
        }
        return packet.getPacket();
    }

    public static byte[] iDontKnow(final MapleCharacter chr, int type) {
        OutPacket packet = new OutPacket();
        packet.writeShort(396);
        packet.write(type);
        packet.write(1);
        PacketHelper.addCharLook(packet, chr, false);
        packet.writeMapleAsciiString("1");
        packet.writeMapleAsciiString("2");
        packet.writeMapleAsciiString("3");
        packet.writeMapleAsciiString("4");
        packet.writeMapleAsciiString("5");
        packet.writeMapleAsciiString("6");
        packet.writeMapleAsciiString("7");
        packet.writeInt(0); // other char id?
        if (type == 2) {
            PacketHelper.addCharLook(packet, chr, false);
        }
        return packet.getPacket();
    }

    // [29 00] [01] [1A 6F]-28442 questid [01] [08 00 37 30 30 30 30 33 39 33]
    public static byte[] luckyLogoutGift(byte progress_mode, String progress_info) {
        OutPacket packet = new OutPacket(16);
        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(1);
        packet.writeShort(28442); // questid
        packet.write(progress_mode);
        packet.writeMapleAsciiString(progress_info); // serial number
        return packet.getPacket();
    }

    public static byte[] enableShopDiscount(byte percent) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOP_DISCOUNT.getValue());
        packet.write(percent);

        return packet.getPacket();
    }

    public static byte[] showVisitorEffect() {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.VISITOR.getValue());
        packet.writeMapleAsciiString("Visitor");
        packet.write(1); // 0, 1, 2, 3

        return packet.getPacket();
    }

    public static byte[] itemExpired(int itemid) {
        return expiredMessage((byte) 2, itemid, null, null);
    }

    public static byte[] sealExpired(List<Integer> expire) {
        return expiredMessage((byte) 12, 0, expire, null);
    }

    public static byte[] itemReplaced(List<String> replaceMsg) {
        return expiredMessage((byte) 13, 0, null, replaceMsg);
    }

    public static byte[] skillExpired(List<Integer> expire) {
        return expiredMessage((byte) 14, 0, expire, null);
    }

    public static byte[] expiredMessage(byte type, int itemid, List<Integer> expire, List<String> replaceMsg) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        packet.write(type);
        switch (type) {
            case 2: // OnCashItemExpireMessage
                // [<item name>] has passed its expiration date and will be removed
                // from your inventory
                packet.writeInt(itemid);
                break;
            case 12: // OnItemProtectExpireMessage
                // <Item name>'s seal has expired.
                packet.write(expire.size()); // Size
                for (Integer it : expire) {
                    packet.writeInt(it.intValue());
                }
                break;
            case 13: // OnItemExpireReplaceMessage
                packet.write(replaceMsg.size()); // Size
                for (String x : replaceMsg) {
                    packet.writeMapleAsciiString(x);
                }
                break;
            case 14: // OnSkillExpireMessage
                // <Skill Name> has disappeared as the time limit has passed.
                packet.write(expire.size());
                for (Integer i : expire) {
                    packet.writeInt(i); // Skill Id
                }
                break;
        }

        return packet.getPacket();
    }

    public static byte[] showMonsterBombEffect(int x, int y, int level) {
        OutPacket packet = new OutPacket(23);

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(5);
        packet.writeInt(4341003);
        packet.writeInt(x);
        packet.writeInt(y);
        packet.writeInt(1);
        packet.writeInt(level);

        return packet.getPacket();
    }

    public static byte[] setNPCScriptable(List<Integer> npcId) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
        packet.write(npcId.size());
        for (Integer i : npcId) {
            packet.writeInt(i);
            packet.writeMapleAsciiString(".");
            packet.writeInt(0); // start time
            packet.writeInt(Integer.MAX_VALUE); // end time
        }

        return packet.getPacket();
    }

    public static byte[] setNPCScriptable(int npcId, String message) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
        packet.write(1);
        packet.writeInt(npcId);
        packet.writeMapleAsciiString(message);
        packet.writeInt(0); // start time
        packet.writeInt(Integer.MAX_VALUE); // end time

        return packet.getPacket();
    }

    public static byte[] loadGuildName(MapleCharacter chr) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
        packet.writeInt(chr.getId());

        if (chr.getGuildId() <= 0) {
            packet.writeShort(0);
        } else {
            final MapleGuild gs = GuildManager.getGuild(chr.getGuildId());
            packet.writeMapleAsciiString(gs != null ? gs.getName() : "");
        }

        return packet.getPacket();
    }

    public static byte[] loadGuildIcon(MapleCharacter chr) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
        packet.writeInt(chr.getId());

        if (chr.getGuildId() <= 0) {
            packet.writeZeroBytes(6);
        } else {
            final MapleGuild gs = GuildManager.getGuild(chr.getGuildId());
            if (gs != null) {
                packet.writeShort(gs.getLogoBG());
                packet.write(gs.getLogoBGColor());
                packet.writeShort(gs.getLogo());
                packet.write(gs.getLogoColor());
            } else {
                packet.writeZeroBytes(6);
            }
        }

        return packet.getPacket();
    }

    /**
     * 0 : Level up Effect 7 : Enter portal 8 : Job Change 9 : Complete quest 13 : Monster Book 15 :
     * Equipment level up 17 : EXP Card gain 26 : Soul stone effect i think..(You have revived on
     * the current map through the effect of the Spirit Stone.)
     */
    public static byte[] showSpecialEffect_(int effect) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(effect);
        switch (effect) {
            case 2:
                break;
            case 10:
                packet.write(0); // 0 = Miss, 1 and above is heal (blue colour hp)
                break;
            case 11: // some kind of shine effect
                packet.writeInt(0); // ??
                break;
            case 12: // show path
                packet.writeMapleAsciiString("");
                break;
            case 16: // maker skill
                packet.writeInt(0); // 0 = pass, 1 = fail
                break;
            case 21: // Wheel of Destiny
                // You have used 1 Wheel of Destiny in order to revive at the
                // current map. (<left> left)
                packet.write(0); // left
                break;
        }

        return packet.getPacket();
    }

    public static final byte[] getGameMessage(final int code, final String msg) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.FOLLOW_MESSAGE.getValue());
        packet.writeShort(code);
        packet.writeMapleAsciiString(msg);

        return packet.getPacket();
    }

    public static final byte[] sendBrowser(final String site) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MAPLE_ADMIN.getValue());
        packet.writeAsciiString(site);

        return packet.getPacket();
    }

    public static byte[] damageMonster(int oid, int damage) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.writeInt(oid);
        packet.write(0);
        packet.writeInt(damage);
        packet.write(0);
        packet.write(0);
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] damageMonster(int skill, int x, int y) {
        OutPacket packet = new OutPacket(19);
        packet.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        packet.write(skill);
        packet.writeInt(x);
        packet.writeInt(y);
        packet.writeLong(0L);
        return packet.getPacket();
    }

    public static byte[] updateExtendedSP(EvanSkillPoints esp) {
        OutPacket packet = new OutPacket(11);
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(0);
        packet.write(0);
        packet.write(128);
        packet.writeShort(0);
        packet.write(esp.getSkillPoints().keySet().size());
        for (Iterator<?> i = esp.getSkillPoints().keySet().iterator(); i.hasNext(); ) {
            int val = ((Integer) i.next()).intValue();
            packet.write(val == 2200 ? 1 : val - 2208);
            packet.write(esp.getSkillPoints(val));
        }
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] updatePlayerStats(
            List<Pair<MapleStat, Integer>> stats, boolean itemReaction, boolean extendSPJob, ExtendedSPTable table) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        packet.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
                @Override
                public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        packet.writeInt(updateMask);
        for (Pair<MapleStat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    packet.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft() == MapleStat.AVAILABLESP) {
                    if (extendSPJob) {
                        table.addSPData(packet);
                    } else {
                        packet.writeShort(statupdate.getRight().shortValue());
                    }
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    packet.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x20) {
                    packet.write(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                    packet.writeShort(statupdate.getRight().shortValue());
                } else {
                    packet.writeInt(statupdate.getRight().intValue());
                }
            }
        }
        packet.write(0); // v88 new
        return packet.getPacket();
    }

    public static byte[] viewAllCharCustomMessage(String message) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        packet.write(6); // 2: Already connected
        packet.write(1);
        packet.writeMapleAsciiString(message);
        return packet.getPacket();
    }

    /*
     * header: 8 se
     *
     */
    public static byte[] viewAllChar(int serverCount, int numCharacter) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        packet.write(1); // 2: Already connected
        packet.writeInt(serverCount); // m_nCountRelatedSvrs
        packet.writeInt(numCharacter); // m_nCountCharacters
        return packet.getPacket();
    }

    public static byte[] viewAllCharShowChars(int nWorldID, List<MapleCharacter> chars) {
        OutPacket packet = new OutPacket();
        packet.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        packet.write(0); // nType
        packet.write(nWorldID);
        packet.write(chars.size());
        for (final MapleCharacter chr : chars) {
            PacketHelper.addCharStats(packet, chr);
            PacketHelper.addCharLook(packet, chr, true);
            boolean ranking = !chr.isGameMaster();
            packet.write(ranking ? 1 : 0);
            if (ranking) {
                packet.writeInt(0);
                packet.writeInt(0);
                packet.writeInt(0);
                packet.writeInt(0);
            }
        }
        packet.write(2); // second pw request

        return packet.getPacket();
    }

    public static byte[] openVoteWebpage() {
        OutPacket packet = new OutPacket();
        packet.writeShort(41);
        return packet.getPacket();
    }

    public static byte[] openGMWindowBoard(String url) {
        OutPacket packet = new OutPacket();
        packet.writeShort(131);
        packet.writeInt(Integer.MAX_VALUE);
        packet.writeMapleAsciiString(url);
        return packet.getPacket();
    }

    public byte[] blockedAccessGMMessage(boolean blockAccess) {
        OutPacket packet = new OutPacket();
        packet.writeShort(159);
        int type = GMResulMessages.SUCCESSFULLY_BLOCKED_ACCESS.getType();
        if (!blockAccess) {
            type = GMResulMessages.UNBLOCKING_SUCCESSFULL.getType();
        }
        packet.write(type);
        packet.write(4);
        return packet.getPacket();
    }

    public byte[] channelAndServerMessage(String channel, String world, String msg) {
        OutPacket packet = new OutPacket();
        packet.writeShort(159);
        packet.write(0xB);
        packet.writeMapleAsciiString(channel);
        packet.writeMapleAsciiString(world);
        packet.writeMapleAsciiString(msg);

        return packet.getPacket();
    }

    public byte[] writeGMMessageWithoutName(String msg, boolean isRed) {
        OutPacket packet = new OutPacket();
        packet.writeShort(159);
        if (isRed) {
            packet.write(GMResulMessages.INVISIVLE_GM_MESSAGE_RED.getType());
        } else {
            packet.write(GMResulMessages.INVISIVLE_GM_MESSAGE_LIGHT.getType());
        }

        packet.writeMapleAsciiString(msg);
        return packet.getPacket();
    }

    enum GMResulMessages {
        SUCCESSFULLY_BLOCKED_ACCESS(4),
        UNBLOCKING_SUCCESSFULL(5),
        INVALID_CHAR_NAME(6),
        REMOVED_FROM_RANKS(6),
        INVISIVLE_GM_MESSAGE_RED(0x3A),
        INVISIVLE_GM_MESSAGE_LIGHT(0x39);

        int type;

        GMResulMessages(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
