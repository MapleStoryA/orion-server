/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package handling;

public enum RecvPacketOpcode {
    PONG(25, false),
    LOGIN_PASSWORD(1, false),
    CHARLIST_REQUEST(5),
    SERVERSTATUS_REQUEST(6),
    AFTER_LOGIN(9),
    SERVERLIST_REQUEST(11),
    SERVERLIST_REQUEST_2(12),
    SERVERLIST_REQUEST_3(4),
    PLAYER_DC(12),
    VIEW_ALL_CHAR(13),
    PICK_ALL_CHAR(15),
    CHECK_CHAR_NAME(21),
    CREATE_CHAR(22),
    DELETE_CHAR(24),
    BACKUP_PACKET(0x26),
    STRANGE_DATA(27),
    CHAR_SELECT(19),
    RELOG(28),
    REGISTER_PIC(30),
    CHAR_SELECT_WITH_PIC(31),
    VIEW_ALL_PIC_REGISTER(32),
    VIEW_ALL_WITH_PIC(0x0E),
    CLIENT_START(36),
    PACKET_ERROR(38),
    PLAYER_LOGGEDIN(20, false),
    CHANGE_MAP(43),
    CHANGE_CHANNEL(44),
    ENTER_CASH_SHOP(45),
    MOVE_PLAYER(46),
    CANCEL_CHAIR(47),
    USE_CHAIR(48),
    CLOSE_RANGE_ATTACK(49),
    RANGED_ATTACK(50),
    MAGIC_ATTACK(51),
    PASSIVE_ENERGY(52),
    TAKE_DAMAGE(53),
    GENERAL_CHAT(55),
    CLOSE_CHALKBOARD(56),
    FACE_EXPRESSION(57),
    USE_ITEMEFFECT(58),
    WHEEL_OF_FORTUNE(59),
    MONSTER_BOOK_COVER(63),
    NPC_TALK(64),
    NPC_TALK_MORE(66),
    NPC_SHOP(67),
    STORAGE(68),
    USE_HIRED_MERCHANT(69),
    MERCH_ITEM_STORE(70),
    REMOTE_STORE_REQUEST(65),
    DUEY_ACTION(71),
    OWL(73),
    OWL_WARP(74),
    ITEM_SORT(76),
    ITEM_GATHER(77),
    ITEM_MOVE(78),
    USE_ITEM(79),
    CANCEL_ITEM_EFFECT(80),
    USE_SUMMON_BAG(82),
    PET_FOOD(83),
    USE_MOUNT_FOOD(84),
    USE_SCRIPTED_NPC_ITEM(85),
    USE_CASH_ITEM(86),
    USE_CATCH_ITEM(88),
    USE_SKILL_BOOK(89),
    USE_OWL_MINERVA(90),
    USE_RETURN_SCROLL(92),
    USE_UPGRADE_SCROLL(93),
    USE_EQUIP_SCROLL(94),
    USE_POTENTIAL_SCROLL(95),
    USE_MAGNIFY_GLASS(96),
    DISTRIBUTE_AP(97),
    AUTO_ASSIGN_AP(98),
    HEAL_OVER_TIME(99),
    DISTRIBUTE_SP(101),
    SPECIAL_MOVE(102),
    CANCEL_BUFF(103),
    SKILL_EFFECT(104),
    MESO_DROP(105),
    GIVE_FAME(106),
    CHAR_INFO_REQUEST(108),
    SPAWN_PET(109),
    CANCEL_DEBUFF(110),
    CHANGE_MAP_SPECIAL(111),
    USE_INNER_PORTAL(112),
    TROCK_ADD_MAP(113),
    QUEST_ACTION(118),
    //119 : Skills with buffstats
// skill with actions?
    THROW_SKILL(120),
    SKILL_MACRO(121),
    REWARD_ITEM(123),
    ITEM_MAKER(124),
    REPAIR_ALL(0x81),
    REPAIR(0x999),
    SOLOMON(178),
    GACH_EXP(0x999),
    TORNADO_SPIN(132),
    FOLLOW_REQUEST(133),
    FOLLOW_REPLY(135),
    AUTO_FOLLOW_REPLY(0x999),
    USE_TREASUER_CHEST(126),
    PARTYCHAT(137),
    WHISPER(138),
    MESSENGER(140),
    PLAYER_INTERACTION(141),
    PARTY_OPERATION(142),
    DENY_PARTY_REQUEST(143),
    EXPEDITION_OPERATION(144),
    PARTY_LISTING(145),
    GUILD_OPERATION(146),
    DENY_GUILD_REQUEST(147),
    BUDDYLIST_MODIFY(150),
    NOTE_ACTION(151),
    USE_DOOR(153),
    CHANGE_KEYMAP(156),
    RPS_GAME(157),
    ENTER_MTS(177),
    RING_ACTION(159),
    ALLIANCE_OPERATION(0x999),
    DENY_ALLIANCE_REQUEST(0x999),
    REQUEST_FAMILY(166),
    OPEN_FAMILY(167),
    FAMILY_OPERATION(168),
    DELETE_JUNIOR(169),
    DELETE_SENIOR(170),
    ACCEPT_FAMILY(171),
    USE_FAMILY(172),
    FAMILY_PRECEPT(173),
    FAMILY_SUMMON(174),
    BBS_OPERATION(176),
    CYGNUS_SUMMON(0x999),
    TRANSFORM_PLAYER(181),
    CS_SURPRISE(182),
    ARAN_COMBO(186),
    ESCORT_RESULT(188),
    FIRST_LOGIN(189),
    GAME_POLL(0x999),
    MOVE_PET(196),
    PET_CHAT(197),
    PET_COMMAND(198),
    PET_LOOT(199),
    PET_AUTO_POT(200),
    PET_IGNORE(201),
    PET_NAME_CHANGE(1999),//TODO THIS IS WRONG!
    MOVE_SUMMON(204),
    SUMMON_ATTACK(205),
    DAMAGE_SUMMON(206),
    SUB_SUMMON(207),
    REMOVE_SUMMON(208),
    MOVE_DRAGON(211),
    QUICK_SLOT(213),
    PAMS_SONG(215),
    MOVE_LIFE(220),
    AUTO_AGGRO(221),
    FRIENDLY_DAMAGE(226),
    SELF_DESTRUCT(227),
    MONSTER_BOMB(228),
    HYPNOTIZE_DMG(0x999),
    MOB_NODE(0x999),
    DISPLAY_NODE(0x999),
    NPC_ACTION(234),
    ITEM_PICKUP(239),
    DAMAGE_REACTOR(242),
    TOUCH_REACTOR(243),
    UPDATE_CHARACTER(244),
    SNOWBALL(0x999),
    LEFT_KNOCK_BACK(0x999),
    COCONUT(0xFA),
    MONSTER_CARNIVAL(255),
    SHIP_OBJECT(0x999),
    PARTY_SEARCH_START(259),
    PARTY_SEARCH_STOP(260),
    ENTER_MAP(260),
    RELOAD_MAP(262),
    CS_UPDATE(267),
    BUY_CS_ITEM(268),
    LUCKY_LOGOUT_GIFT(303),
    COUPON_CODE(269),
    VICIOUS_HAMMER(289),
    MAPLETV(0x999),
    UPDATE_QUEST(0x999),
    QUEST_ITEM(0x999),
    USE_ITEM_QUEST(0x999),
    REQUEST_BOAT_STATUS(257),
    QUEST_SUMMON_PARTNER_CLICK(0xB9),
    PLAYER_DISCONECT(0x24),
    REMOTE_GACHAPON(0x7F),
    MONSTER_BOMB_DB(0x84),
    TWIN_DRAGON_EGG(10001),
    REPORT(0x75),
    ADMIN_COMMAND(148),
    ADMIN_LOG(149),
    ADMIN_CHAT(136);


    private boolean checkState;
    private short code = -1;


    RecvPacketOpcode(int code) {
        this.code = (short) code;
        this.checkState = true;
    }

    RecvPacketOpcode(int code, final boolean checkState) {
        this.code = (short) code;
        this.checkState = checkState;
    }

    public short getValue() {
        return this.code;
    }

    public final boolean needsChecking() {
        return checkState;
    }
}
