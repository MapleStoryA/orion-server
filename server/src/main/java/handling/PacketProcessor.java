/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package handling;

import handling.channel.handler.AcceptFamilyHandler;
import handling.channel.handler.AlianceOperationHandler;
import handling.channel.handler.AranComboHandler;
import handling.channel.handler.AutoAggroHandler;
import handling.channel.handler.AutoAssignAPHandler;
import handling.channel.handler.BackupPacketHandler;
import handling.channel.handler.BbsOperationHandler;
import handling.channel.handler.BuddyListModifyHandler;
import handling.channel.handler.BuyCSItemHandler;
import handling.channel.handler.CancelBuffHandler;
import handling.channel.handler.CancelChairHandler;
import handling.channel.handler.CancelItemEffectHandler;
import handling.channel.handler.ChangeChannelHandler;
import handling.channel.handler.ChangeKeyMapHandler;
import handling.channel.handler.ChangeMapSpecialHandler;
import handling.channel.handler.ChangeMonsterCoverHandler;
import handling.channel.handler.ChangeSkillMacroHandler;
import handling.channel.handler.CharInfoRequestHandler;
import handling.channel.handler.CloseChalkBoard;
import handling.channel.handler.CloseRangeDamageHandler;
import handling.channel.handler.CouponCodeHandler;
import handling.channel.handler.CsSurpriseHandler;
import handling.channel.handler.CygnusSummonHandler;
import handling.channel.handler.DamageReactorHandler;
import handling.channel.handler.DamageSummonHandler;
import handling.channel.handler.DeleteJuniorHandler;
import handling.channel.handler.DeleteSeniorHandler;
import handling.channel.handler.DenyAllianceRequest;
import handling.channel.handler.DenyGuildRequestHandler;
import handling.channel.handler.DenyPartyRequestHandler;
import handling.channel.handler.DisplayNodeHandler;
import handling.channel.handler.DistributeAPHandler;
import handling.channel.handler.DistributeSPHandler;
import handling.channel.handler.EnterCashShopHandler;
import handling.channel.handler.EnterMTSHandler;
import handling.channel.handler.EnterMapRequestHandler;
import handling.channel.handler.EscortResultHandler;
import handling.channel.handler.ExpeditionOperationHandler;
import handling.channel.handler.FaceExpressionHandler;
import handling.channel.handler.FamilyOperationHandler;
import handling.channel.handler.FamilyPreAcceptHandler;
import handling.channel.handler.FamilySummonHandler;
import handling.channel.handler.FollowRequestHandler;
import handling.channel.handler.FriendlyDamageHandler;
import handling.channel.handler.GeneralChatHandler;
import handling.channel.handler.GiveFameHandler;
import handling.channel.handler.GuildOperationHandler;
import handling.channel.handler.HealOverTimeHandler;
import handling.channel.handler.HitCoconutHandler;
import handling.channel.handler.HypnotizeDamageHandler;
import handling.channel.handler.ItemGatherHandler;
import handling.channel.handler.ItemMakerHandler;
import handling.channel.handler.ItemMoveHandler;
import handling.channel.handler.ItemPickupHandler;
import handling.channel.handler.ItemSortHandler;
import handling.channel.handler.LeftKnockBackHandler;
import handling.channel.handler.LuckyLogoutHandler;
import handling.channel.handler.MagicDamageHandler;
import handling.channel.handler.MerchantItemStoreHandler;
import handling.channel.handler.MesoDropHandler;
import handling.channel.handler.MessengerHandler;
import handling.channel.handler.MobNodeHandler;
import handling.channel.handler.MonsterBombHandler;
import handling.channel.handler.MonsterCarnivalHandler;
import handling.channel.handler.MoveDragonHandler;
import handling.channel.handler.MoveLifeHandler;
import handling.channel.handler.MovePetHandler;
import handling.channel.handler.MovePlayerHandler;
import handling.channel.handler.MoveSummonHandler;
import handling.channel.handler.EnableActionOpHandler;
import handling.channel.handler.NoteActionHandler;
import handling.channel.handler.NpcAnimationHandler;
import handling.channel.handler.NpcShopHandler;
import handling.channel.handler.NpcTalkHandler;
import handling.channel.handler.NpcTalkMoreHandler;
import handling.channel.handler.OpenFamilyHandler;
import handling.channel.handler.OwlHandler;
import handling.channel.handler.OwlMinervaHandler;
import handling.channel.handler.OwlWarpHandler;
import handling.channel.handler.PartyChatHandler;
import handling.channel.handler.PartyListingHandler;
import handling.channel.handler.PartyOperationHandler;
import handling.channel.handler.PetAutoPotHandler;
import handling.channel.handler.PetChatHandler;
import handling.channel.handler.PetCommandHandler;
import handling.channel.handler.PetFoodHandler;
import handling.channel.handler.PetIgnoreHandler;
import handling.channel.handler.PetLootHandler;
import handling.channel.handler.PlayerDisconectHandler;
import handling.channel.handler.PlayerInteractionHandler;
import handling.channel.handler.QuestSummonHandler;
import handling.channel.handler.QuestionActionHandler;
import handling.channel.handler.QuickSlotHandler;
import handling.channel.handler.RPSGameHandler;
import handling.channel.handler.RangedAttackHandler;
import handling.channel.handler.RemoteGachaponHandler;
import handling.channel.handler.RemoteStoreHandler;
import handling.channel.handler.RepairAllHandler;
import handling.channel.handler.RepairHandler;
import handling.channel.handler.ReportHandler;
import handling.channel.handler.RequestBoatStatusHandler;
import handling.channel.handler.RequestFamilyHandler;
import handling.channel.handler.RewardItemHandler;
import handling.channel.handler.RingActionHandler;
import handling.channel.handler.SelfDestructHandler;
import handling.channel.handler.SkillEffectHandler;
import handling.channel.handler.SnowballHandler;
import handling.channel.handler.SpawnPetHandler;
import handling.channel.handler.SpecialMoveHandler;
import handling.channel.handler.StorageHandler;
import handling.channel.handler.SummonAttackHandler;
import handling.channel.handler.TakeDamageHandler;
import handling.channel.handler.TeleportRockAddMapHandler;
import handling.channel.handler.ThrowSkillHandler;
import handling.channel.handler.TouchReactorHandler;
import handling.channel.handler.TransformPlayerHandler;
import handling.channel.handler.TwinDragonEggHandler;
import handling.channel.handler.UpdateCharacterHandler;
import handling.channel.handler.UpdateQuestHandler;
import handling.channel.handler.UseCashItemHandler;
import handling.channel.handler.UseCatchItemHandler;
import handling.channel.handler.UseChairHandler;
import handling.channel.handler.UseDoorHandler;
import handling.channel.handler.UseEquipScrollHandler;
import handling.channel.handler.UseFamilyHandler;
import handling.channel.handler.UseHiredMerchantHandler;
import handling.channel.handler.UseInnerPortalHandler;
import handling.channel.handler.UseItemEffectHandler;
import handling.channel.handler.UseItemHandler;
import handling.channel.handler.UseItemQuestHandler;
import handling.channel.handler.UseMagnifyGlassHandler;
import handling.channel.handler.UseMountFoodHandler;
import handling.channel.handler.UsePotencialHandler;
import handling.channel.handler.UseReturnScrollHandler;
import handling.channel.handler.UseScriptedNpcItemHandler;
import handling.channel.handler.UseScrollHandler;
import handling.channel.handler.UseSkillBookHandler;
import handling.channel.handler.UseSummonBagHandler;
import handling.channel.handler.UseTreasureChestHandler;
import handling.channel.handler.ViciousHammerHandler;
import handling.channel.handler.ViewAllCharPacket;
import handling.channel.handler.WhisperHandler;
import handling.channel.handler.admin.AdminChatHandler;
import handling.channel.handler.admin.AdminCommandHandler;
import handling.channel.handler.admin.AdminLogHandler;
import handling.login.handler.AfterLoginHandler;
import handling.login.handler.CharListRequestHandler;
import handling.login.handler.CharLoginPasswordHandler;
import handling.login.handler.CharSelectedHandler;
import handling.login.handler.CharSelectedViewAllHandler;
import handling.login.handler.CharlistViewAllHandler;
import handling.login.handler.CheckCharNameHandler;
import handling.login.handler.CreateCharHandler;
import handling.login.handler.DeleteCharHandler;
import handling.login.handler.NoOpHandler;
import handling.login.handler.InvalidPacketRequestHandler;
import handling.login.handler.KeepAliveHandler;
import handling.login.handler.RelogRequestHandler;
import handling.login.handler.ServerListRequestHandler;
import handling.login.handler.ServerStatusRequestHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PacketProcessor {

    private static PacketProcessor instance;
    private final MaplePacketHandler[] handlers;

    private PacketProcessor() {
        int maxRecvOp = 0;
        for (RecvPacketOpcode op : RecvPacketOpcode.values()) {
            if (op.getValue() > maxRecvOp) {
                maxRecvOp = op.getValue();
            }
        }
        handlers = new MaplePacketHandler[maxRecvOp + 1];
    }

    public static synchronized PacketProcessor getProcessor(Mode mode) {
        if (instance == null) {
            instance = new PacketProcessor();
        }
        instance.reset(mode);
        return instance;
    }

    public MaplePacketHandler getHandler(short packetId) {
        if (packetId > handlers.length) {
            return null;
        }
        MaplePacketHandler handler = handlers[packetId];
        return handler;
    }

    public void registerHandler(RecvPacketOpcode code, MaplePacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
            log.info("Error registering handler - " + code.name());
        }
    }

    public void reset(Mode mode) {
        registerHandler(RecvPacketOpcode.PONG, new KeepAliveHandler());
        registerHandler(RecvPacketOpcode.BACKUP_PACKET, new BackupPacketHandler());
        if (mode == Mode.LOGINSERVER) {
            registerHandler(RecvPacketOpcode.LOGIN_PASSWORD, new CharLoginPasswordHandler());
            registerHandler(RecvPacketOpcode.AFTER_LOGIN, new AfterLoginHandler());
            registerHandler(RecvPacketOpcode.SERVER_LIST_REQUEST, new ServerListRequestHandler());
            registerHandler(RecvPacketOpcode.SERVER_LIST_REQUEST_2, new ServerListRequestHandler());
            registerHandler(RecvPacketOpcode.SERVER_LIST_REQUEST_3, new ServerListRequestHandler());
            registerHandler(RecvPacketOpcode.CHAR_LIST_REQUEST, new CharListRequestHandler());
            registerHandler(
                    RecvPacketOpcode.SERVER_STATUS_REQUEST, new ServerStatusRequestHandler());
            registerHandler(RecvPacketOpcode.CHECK_CHAR_NAME, new CheckCharNameHandler());
            registerHandler(RecvPacketOpcode.CREATE_CHAR, new CreateCharHandler());
            registerHandler(RecvPacketOpcode.DELETE_CHAR, new DeleteCharHandler());
            registerHandler(RecvPacketOpcode.CHAR_SELECT, new CharSelectedHandler());
            registerHandler(RecvPacketOpcode.CHAR_SELECT_WITH_PIC, new CharSelectedHandler());
            registerHandler(RecvPacketOpcode.PICK_ALL_CHAR, new ViewAllCharPacket());
            registerHandler(RecvPacketOpcode.VIEW_ALL_WITH_PIC, new CharSelectedViewAllHandler());
            registerHandler(
                    RecvPacketOpcode.VIEW_ALL_PIC_REGISTER, new InvalidPacketRequestHandler());
            registerHandler(RecvPacketOpcode.RELOG, new RelogRequestHandler());
            registerHandler(RecvPacketOpcode.VIEW_ALL_CHAR, new CharlistViewAllHandler());
        } else if (mode == Mode.CHANNELSERVER) {
            registerHandler(RecvPacketOpcode.CHANGE_CHANNEL, new ChangeChannelHandler());
            registerHandler(RecvPacketOpcode.ENTER_CASH_SHOP, new EnterCashShopHandler());
            registerHandler(RecvPacketOpcode.ENTER_MTS, new EnterMTSHandler());
            registerHandler(RecvPacketOpcode.REMOTE_STORE_REQUEST, new RemoteStoreHandler());
            registerHandler(RecvPacketOpcode.MOVE_PLAYER, new MovePlayerHandler());
            registerHandler(RecvPacketOpcode.REMOTE_GACHAPON, new RemoteGachaponHandler());
            registerHandler(RecvPacketOpcode.CHAR_INFO_REQUEST, new CharInfoRequestHandler());
            registerHandler(
                    RecvPacketOpcode.CLOSE_RANGE_ATTACK, new CloseRangeDamageHandler(false));
            registerHandler(RecvPacketOpcode.PASSIVE_ENERGY, new CloseRangeDamageHandler(true));
            registerHandler(RecvPacketOpcode.RANGED_ATTACK, new RangedAttackHandler());
            registerHandler(RecvPacketOpcode.MAGIC_ATTACK, new MagicDamageHandler());
            registerHandler(RecvPacketOpcode.SPECIAL_MOVE, new SpecialMoveHandler());
            registerHandler(RecvPacketOpcode.ENTER_MAP, new EnterMapRequestHandler());
            registerHandler(RecvPacketOpcode.FACE_EXPRESSION, new FaceExpressionHandler());
            registerHandler(RecvPacketOpcode.TAKE_DAMAGE, new TakeDamageHandler());
            registerHandler(RecvPacketOpcode.HEAL_OVER_TIME, new HealOverTimeHandler());
            registerHandler(RecvPacketOpcode.CANCEL_BUFF, new CancelBuffHandler());
            registerHandler(RecvPacketOpcode.CANCEL_ITEM_EFFECT, new CancelItemEffectHandler());
            registerHandler(RecvPacketOpcode.USE_CHAIR, new UseChairHandler());
            registerHandler(RecvPacketOpcode.CANCEL_CHAIR, new CancelChairHandler());
            registerHandler(RecvPacketOpcode.USE_ITEM_EFFECT, new UseItemEffectHandler());
            registerHandler(RecvPacketOpcode.WHEEL_OF_FORTUNE, new UseItemEffectHandler());
            registerHandler(RecvPacketOpcode.SKILL_EFFECT, new SkillEffectHandler());
            registerHandler(RecvPacketOpcode.MESO_DROP, new MesoDropHandler());
            registerHandler(RecvPacketOpcode.MONSTER_BOOK_COVER, new ChangeMonsterCoverHandler());
            registerHandler(RecvPacketOpcode.CHANGE_KEYMAP, new ChangeKeyMapHandler());
            registerHandler(RecvPacketOpcode.CHANGE_MAP_SPECIAL, new ChangeMapSpecialHandler());
            registerHandler(RecvPacketOpcode.USE_INNER_PORTAL, new UseInnerPortalHandler());
            registerHandler(RecvPacketOpcode.SKILL_MACRO, new ChangeSkillMacroHandler());
            registerHandler(RecvPacketOpcode.QUICK_SLOT, new QuickSlotHandler());
            registerHandler(RecvPacketOpcode.UPDATE_CHARACTER, new UpdateCharacterHandler());
            registerHandler(
                    RecvPacketOpcode.TELEPORT_ROCK_ADD_MAP, new TeleportRockAddMapHandler());
            registerHandler(RecvPacketOpcode.ARAN_COMBO, new AranComboHandler());
            registerHandler(RecvPacketOpcode.GIVE_FAME, new GiveFameHandler());
            registerHandler(RecvPacketOpcode.TRANSFORM_PLAYER, new TransformPlayerHandler());
            registerHandler(RecvPacketOpcode.NOTE_ACTION, new NoteActionHandler());
            registerHandler(RecvPacketOpcode.USE_DOOR, new UseDoorHandler());
            registerHandler(RecvPacketOpcode.DAMAGE_REACTOR, new DamageReactorHandler());
            registerHandler(RecvPacketOpcode.TOUCH_REACTOR, new TouchReactorHandler());
            registerHandler(RecvPacketOpcode.COCONUT, new HitCoconutHandler());
            registerHandler(RecvPacketOpcode.FOLLOW_REQUEST, new FollowRequestHandler());
            registerHandler(RecvPacketOpcode.FOLLOW_REPLY, new FollowRequestHandler());
            registerHandler(RecvPacketOpcode.RING_ACTION, new RingActionHandler());
            registerHandler(RecvPacketOpcode.PLAYER_DISCONECT, new PlayerDisconectHandler());
            registerHandler(RecvPacketOpcode.QUEST_SUMMON_PARTNER_CLICK, new QuestSummonHandler());
            registerHandler(RecvPacketOpcode.RELOAD_MAP, new EnableActionOpHandler());
            registerHandler(RecvPacketOpcode.FIRST_LOGIN, new EnableActionOpHandler());
            registerHandler(RecvPacketOpcode.REQUEST_BOAT_STATUS, new RequestBoatStatusHandler());
            registerHandler(RecvPacketOpcode.CLOSE_CHALKBOARD, new CloseChalkBoard());
            registerHandler(RecvPacketOpcode.ITEM_MAKER, new ItemMakerHandler());
            registerHandler(RecvPacketOpcode.ITEM_SORT, new ItemSortHandler());
            registerHandler(RecvPacketOpcode.ITEM_GATHER, new ItemGatherHandler());
            registerHandler(RecvPacketOpcode.ITEM_MOVE, new ItemMoveHandler());
            registerHandler(RecvPacketOpcode.ITEM_PICKUP, new ItemPickupHandler());
            registerHandler(RecvPacketOpcode.USE_CASH_ITEM, new UseCashItemHandler());
            registerHandler(RecvPacketOpcode.USE_ITEM, new UseItemHandler());
            registerHandler(RecvPacketOpcode.USE_MAGNIFY_GLASS, new UseMagnifyGlassHandler());
            registerHandler(
                    RecvPacketOpcode.USE_SCRIPTED_NPC_ITEM, new UseScriptedNpcItemHandler());
            registerHandler(RecvPacketOpcode.USE_RETURN_SCROLL, new UseReturnScrollHandler());
            registerHandler(RecvPacketOpcode.ESCORT_RESULT, new EscortResultHandler());
            registerHandler(RecvPacketOpcode.SELF_DESTRUCT, new SelfDestructHandler());
            registerHandler(RecvPacketOpcode.MONSTER_BOMB, new MonsterBombHandler());
            registerHandler(RecvPacketOpcode.THROW_SKILL, new ThrowSkillHandler());
            registerHandler(RecvPacketOpcode.VICIOUS_HAMMER, new ViciousHammerHandler());
            registerHandler(RecvPacketOpcode.PARTY_LISTING, new PartyListingHandler());
            registerHandler(RecvPacketOpcode.USE_SUMMON_BAG, new UseSummonBagHandler());
            registerHandler(
                    RecvPacketOpcode.EXPEDITION_OPERATION, new ExpeditionOperationHandler());
            registerHandler(RecvPacketOpcode.NPC_ACTION, new NpcAnimationHandler());
            registerHandler(RecvPacketOpcode.NPC_SHOP, new NpcShopHandler());
            registerHandler(RecvPacketOpcode.NPC_TALK, new NpcTalkHandler());
            registerHandler(RecvPacketOpcode.NPC_TALK_MORE, new NpcTalkMoreHandler());
            registerHandler(RecvPacketOpcode.QUEST_ACTION, new QuestionActionHandler());
            registerHandler(RecvPacketOpcode.STORAGE, new StorageHandler());
            registerHandler(RecvPacketOpcode.REPAIR_ALL, new RepairAllHandler());
            registerHandler(RecvPacketOpcode.REPAIR, new RepairHandler());
            registerHandler(RecvPacketOpcode.UPDATE_QUEST, new UpdateQuestHandler());
            registerHandler(RecvPacketOpcode.USE_ITEM_QUEST, new UseItemQuestHandler());
            registerHandler(RecvPacketOpcode.RPS_GAME, new RPSGameHandler());
            registerHandler(RecvPacketOpcode.ACCEPT_FAMILY, new AcceptFamilyHandler());
            registerHandler(RecvPacketOpcode.USE_TREASUER_CHEST, new UseTreasureChestHandler());
            registerHandler(RecvPacketOpcode.FAMILY_SUMMON, new FamilySummonHandler());
            registerHandler(RecvPacketOpcode.HYPNOTIZE_DMG, new HypnotizeDamageHandler());
            registerHandler(RecvPacketOpcode.MOB_NODE, new MobNodeHandler());
            registerHandler(RecvPacketOpcode.MOVE_LIFE, new MoveLifeHandler());
            registerHandler(RecvPacketOpcode.AUTO_AGGRO, new AutoAggroHandler());
            registerHandler(RecvPacketOpcode.DISPLAY_NODE, new DisplayNodeHandler());
            registerHandler(RecvPacketOpcode.FRIENDLY_DAMAGE, new FriendlyDamageHandler());
            registerHandler(RecvPacketOpcode.FAMILY_PRECEPT, new FamilyPreAcceptHandler());
            registerHandler(RecvPacketOpcode.USE_FAMILY, new UseFamilyHandler());
            registerHandler(RecvPacketOpcode.MAPLETV, new EnableActionOpHandler());
            registerHandler(RecvPacketOpcode.CANCEL_DEBUFF, new EnableActionOpHandler());
            registerHandler(RecvPacketOpcode.DELETE_SENIOR, new DeleteSeniorHandler());
            registerHandler(RecvPacketOpcode.DELETE_JUNIOR, new DeleteJuniorHandler());
            registerHandler(RecvPacketOpcode.FAMILY_OPERATION, new FamilyOperationHandler());
            registerHandler(RecvPacketOpcode.OPEN_FAMILY, new OpenFamilyHandler());
            registerHandler(RecvPacketOpcode.REQUEST_FAMILY, new RequestFamilyHandler());
            registerHandler(RecvPacketOpcode.LUCKY_LOGOUT_GIFT, new LuckyLogoutHandler());
            registerHandler(RecvPacketOpcode.USE_OWL_MINERVA, new OwlMinervaHandler());
            registerHandler(RecvPacketOpcode.OWL_WARP, new OwlWarpHandler());
            registerHandler(RecvPacketOpcode.USE_CATCH_ITEM, new UseCatchItemHandler());
            registerHandler(RecvPacketOpcode.USE_MOUNT_FOOD, new UseMountFoodHandler());
            registerHandler(RecvPacketOpcode.PARTYCHAT, new PartyChatHandler());
            registerHandler(RecvPacketOpcode.OWL, new OwlHandler());
            registerHandler(RecvPacketOpcode.SNOWBALL, new SnowballHandler());
            registerHandler(RecvPacketOpcode.LEFT_KNOCK_BACK, new LeftKnockBackHandler());
            registerHandler(RecvPacketOpcode.MERCH_ITEM_STORE, new MerchantItemStoreHandler());
            registerHandler(RecvPacketOpcode.USE_HIRED_MERCHANT, new UseHiredMerchantHandler());
            registerHandler(RecvPacketOpcode.MONSTER_CARNIVAL, new MonsterCarnivalHandler());
            registerHandler(RecvPacketOpcode.PET_IGNORE, new PetIgnoreHandler());
            registerHandler(RecvPacketOpcode.PET_AUTO_POT, new PetAutoPotHandler());
            registerHandler(RecvPacketOpcode.USE_UPGRADE_SCROLL, new UseScrollHandler());
            registerHandler(RecvPacketOpcode.USE_POTENTIAL_SCROLL, new UsePotencialHandler());
            registerHandler(RecvPacketOpcode.PET_LOOT, new PetLootHandler());
            registerHandler(RecvPacketOpcode.PET_COMMAND, new PetCommandHandler());
            registerHandler(RecvPacketOpcode.PET_FOOD, new PetFoodHandler());
            registerHandler(RecvPacketOpcode.PET_CHAT, new PetChatHandler());
            registerHandler(RecvPacketOpcode.MOVE_PET, new MovePetHandler());
            registerHandler(RecvPacketOpcode.SPAWN_PET, new SpawnPetHandler());
            registerHandler(RecvPacketOpcode.MOVE_DRAGON, new MoveDragonHandler());
            registerHandler(RecvPacketOpcode.USE_EQUIP_SCROLL, new UseEquipScrollHandler());
            registerHandler(RecvPacketOpcode.USE_SKILL_BOOK, new UseSkillBookHandler());
            registerHandler(RecvPacketOpcode.REWARD_ITEM, new RewardItemHandler());
            registerHandler(RecvPacketOpcode.SUMMON_ATTACK, new SummonAttackHandler());
            registerHandler(RecvPacketOpcode.MOVE_SUMMON, new MoveSummonHandler());
            registerHandler(RecvPacketOpcode.DAMAGE_SUMMON, new DamageSummonHandler());
            registerHandler(RecvPacketOpcode.TWIN_DRAGON_EGG, new TwinDragonEggHandler());
            registerHandler(RecvPacketOpcode.CS_SURPRISE, new CsSurpriseHandler());
            registerHandler(RecvPacketOpcode.CS_UPDATE, new CsSurpriseHandler());
            registerHandler(RecvPacketOpcode.COUPON_CODE, new CouponCodeHandler());
            registerHandler(RecvPacketOpcode.BUY_CS_ITEM, new BuyCSItemHandler());
            registerHandler(RecvPacketOpcode.CYGNUS_SUMMON, new CygnusSummonHandler());
            registerHandler(RecvPacketOpcode.BUDDYLIST_MODIFY, new BuddyListModifyHandler());
            registerHandler(RecvPacketOpcode.DENY_PARTY_REQUEST, new DenyPartyRequestHandler());
            registerHandler(RecvPacketOpcode.PARTY_OPERATION, new PartyOperationHandler());
            registerHandler(RecvPacketOpcode.BBS_OPERATION, new BbsOperationHandler());
            registerHandler(RecvPacketOpcode.DENY_ALLIANCE_REQUEST, new DenyAllianceRequest());
            registerHandler(RecvPacketOpcode.ALLIANCE_OPERATION, new AlianceOperationHandler());
            registerHandler(RecvPacketOpcode.DENY_GUILD_REQUEST, new DenyGuildRequestHandler());
            registerHandler(RecvPacketOpcode.GUILD_OPERATION, new GuildOperationHandler());
            registerHandler(RecvPacketOpcode.PLAYER_INTERACTION, new PlayerInteractionHandler());
            registerHandler(RecvPacketOpcode.DISTRIBUTE_SP, new DistributeSPHandler());
            registerHandler(RecvPacketOpcode.DISTRIBUTE_AP, new DistributeAPHandler());
            registerHandler(RecvPacketOpcode.AUTO_ASSIGN_AP, new AutoAssignAPHandler());
            registerHandler(RecvPacketOpcode.MESSENGER, new MessengerHandler());
            registerHandler(RecvPacketOpcode.WHISPER, new WhisperHandler());
            registerHandler(RecvPacketOpcode.GENERAL_CHAT, new GeneralChatHandler());
            registerHandler(RecvPacketOpcode.REPORT, new ReportHandler());
            registerHandler(RecvPacketOpcode.ADMIN_CHAT, new AdminChatHandler());
            registerHandler(RecvPacketOpcode.ADMIN_COMMAND, new AdminCommandHandler());
            registerHandler(RecvPacketOpcode.ADMIN_LOG, new AdminLogHandler());
        }
    }

    public enum Mode {
        LOGINSERVER,
        CHANNELSERVER,
        CASHSHOP
    }
}
