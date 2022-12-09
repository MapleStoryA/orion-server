package client;

import client.crypto.LoginCrypto;
import client.crypto.LoginCryptoLegacy;
import database.AccountData;
import database.CharacterService;
import database.DatabaseConnection;
import database.DatabaseException;
import database.LoginService;
import database.LoginState;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.handler.utils.PartyHandlerUtils.PartyOperation;
import handling.session.NetworkSession;
import handling.world.WorldServer;
import handling.world.buddy.BuddyManager;
import handling.world.buddy.MapleBuddyList;
import handling.world.guild.GuildManager;
import handling.world.guild.MapleGuildCharacter;
import handling.world.helper.FindCommand;
import handling.world.helper.MapleMessengerCharacter;
import handling.world.messenger.MessengerManager;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import scripting.v1.game.NpcScripting;
import server.ClientStorage;
import server.config.ServerEnvironment;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.shops.IMaplePlayerShop;
import tools.FileOutputUtil;

import javax.script.ScriptEngine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class MapleClient extends BaseMapleClient {


    public static final int DEFAULT_CHAR_SLOT = 6;

    private final static Lock login_mutex = new ReentrantLock(true);
    private final Map<String, ScriptEngine> engines = new HashMap<>();
    @Getter
    @Setter
    private NpcScripting currentNpcScript;
    private final Lock npc_mutex = new ReentrantLock();

    // Account and player fields
    private MapleCharacter player;
    @Getter
    @Setter
    private AccountData accountData;
    private int charSlots = DEFAULT_CHAR_SLOT;

    // Channel and world related fields.
    private int world;
    private int channel = 1;
    private boolean serverTransition = false;
    private boolean loggedIn = false;
    @Getter
    @Setter
    private long lastNPCTalk;
    private ScheduledFuture<?> idleTask = null;


    public MapleClient(byte[] ivSend, byte[] ivRecv, NetworkSession session) {
        super(ivSend, ivRecv, session);
    }


    public final Lock getNPCLock() {
        return npc_mutex;
    }

    public MapleCharacter getPlayer() {
        return player;
    }

    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }


    public boolean isLoggedIn() {
        return loggedIn;
    }

    public int finishLogin() {
        login_mutex.lock();
        try {
            final LoginState state = getLoginState();
            if ((state.getCode() > LoginState.LOGIN_NOTLOGGEDIN.getCode()) && !state.equals(LoginState.LOGIN_WAITING)) {
                if (!ClientStorage.isConnected(this)) {
                    updateLoginState(LoginState.LOGIN_NOTLOGGEDIN, getSessionIPAddress());
                    return 0;
                }
                loggedIn = false;
                return 7;
            }
            updateLoginState(LoginState.LOGIN_WAITING, getSessionIPAddress());
        } finally {
            login_mutex.unlock();
        }
        return 0;
    }

    public int login(String name, String pwd) {

        int loginOk = 5;
        try {
            this.accountData = LoginService.loadAccountDataByName(name);

            if (this.accountData != null) {
                final int banned = accountData.getBanned();
                final String passhash = accountData.getPassword();
                final String salt = accountData.getSalt();


                if (banned > 0 && !this.accountData.isGameMaster()) {
                    loginOk = 3;
                } else {
                    if (banned == -1) {
                        CharacterService.unban(this.accountData.getId());
                    }
                    LoginState loginState = getLoginState();
                    if (loginState.getCode() > LoginState.LOGIN_NOTLOGGEDIN.getCode() || ClientStorage.isConnected(this)) {
                        // already loggedin
                        loggedIn = false;
                        loginOk = 7;
                    } else {
                        boolean updatePasswordHash = false;
                        // Check if the passwords are correct here. :B
                        if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
                            // Check if a password upgrade is needed.
                            loginOk = 0;
                            updatePasswordHash = true;
                        } else if (LoginCrypto.checkSha1Hash(passhash, pwd)) {
                            loginOk = 0;
                            updatePasswordHash = true;
                        } else if (LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
                            loginOk = 0;
                        } else if (passhash.equals(pwd)) {
                            loginOk = 0;
                        } else {
                            loggedIn = false;
                            loginOk = 4;
                        }
                        if (updatePasswordHash) {
                            Connection con = DatabaseConnection.getConnection();
                            PreparedStatement pss = con.prepareStatement("UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?");
                            try {
                                final String newSalt = LoginCrypto.makeSalt();
                                pss.setString(1, LoginCrypto.makeSaltedSha512Hash(pwd, newSalt));
                                pss.setString(2, newSalt);
                                pss.setInt(3, accountData.getId());
                                pss.executeUpdate();
                            } finally {
                                pss.close();
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR" + e);
        }
        if (ServerEnvironment.isDebugEnabled()) {
            return 0;
        }
        return loginOk;
    }


    public void loadAccountData(int id) {
        this.accountData = LoginService.loadAccountDataById(id);
    }

    public final void updateLoginState(final LoginState loginState, final String SessionID) { // TODO hide?
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
            ps.setInt(1, loginState.getCode());
            ps.setString(2, SessionID);
            ps.setInt(3, getAccountData().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("error updating login state" + e);
        }
        if (LoginState.LOGIN_NOTLOGGEDIN.equals(loginState) || LoginState.LOGIN_WAITING.equals(loginState)) {
            loggedIn = false;
            serverTransition = false;
        } else {
            var states = List.of(LoginState.LOGIN_SERVER_TRANSITION, LoginState.CHANGE_CHANNEL);
            serverTransition = states.contains(loginState);
            loggedIn = !serverTransition;
        }
    }

    public final LoginState getLoginState() {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT loggedin, lastlogin, `birthday` + 0 AS `bday` FROM accounts WHERE id = ?");
            ps.setInt(1, getAccountData().getId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                throw new DatabaseException("Everything sucks");
            }
            LoginState state = LoginState.fromCode(rs.getInt("loggedin"));

            if (LoginState.LOGIN_SERVER_TRANSITION.equals(state) || LoginState.CHANGE_CHANNEL.equals(state)) {
                if (rs.getTimestamp("lastlogin").getTime() + 20000 < System.currentTimeMillis()) {
                    // connecting to channel server timeout
                    state = LoginState.LOGIN_NOTLOGGEDIN;
                    updateLoginState(state, getSessionIPAddress());
                }
            }
            rs.close();
            ps.close();
            loggedIn = LoginState.LOGIN_LOGGEDIN.equals(state);
            return state;
        } catch (SQLException e) {
            loggedIn = false;
            throw new DatabaseException("error getting login state", e);
        }
    }

    public void removalTask() {
        try {
            player.cancelAllBuffs_();
            player.cancelAllDebuffs();
            if (player.getMarriageId() > 0) {
                final MapleQuestStatus stat1 = player.getQuestNAdd(MapleQuest.getInstance(160001));
                final MapleQuestStatus stat2 = player.getQuestNAdd(MapleQuest.getInstance(160002));
                if (stat1.getCustomData() != null && (stat1.getCustomData().equals("2_") || stat1.getCustomData().equals("2"))) {
                    //dc in process of marriage
                    if (stat2.getCustomData() != null) {
                        stat2.setCustomData("0");
                    }
                    stat1.setCustomData("3");
                }
            }
            player.changeRemoval(true);
            if (player.getEventInstance() != null) {
                player.getEventInstance().playerDisconnected(player, player.getId());
            }
            if (player.getNewEventInstance() != null) {
                player.getNewEventInstance().onPlayerDisconnected(player);
            }
            if (player.getMap() != null) {
                switch (player.getMapId()) {
                    case 541010100: //latanica
                    case 541020800: //scar/targa
                    case 551030200: //krexel
                    case 220080001: //pap
                        player.getMap().addDisconnected(player.getId());
                        break;
                }
                player.getMap().removePlayer(player);
            }

            final IMaplePlayerShop shop = player.getPlayerShop();
            if (shop != null) {
                shop.removeVisitor(player);
                if (shop.isOwner(player)) {
                    if (shop.getShopType() == 1 && shop.isAvailable()) {
                        shop.setOpen(true);
                    } else {
                        shop.closeShop(true, true);
                    }
                }
            }
            player.setMessenger(null);
        } catch (final Throwable e) {
            FileOutputUtil.outputFileError(FileOutputUtil.Acc_Stuck, e);
        }
    }

    public final void disconnect(final boolean RemoveInChannelServer, final boolean fromCS) {
        disconnect(RemoveInChannelServer, fromCS, false);
    }

    public final void disconnect(final boolean RemoveInChannelServer, final boolean fromCS, final boolean shutdown) {
        if (player != null && isLoggedIn()) {
            ClientStorage.removeClient(this);
            MapleMap map = player.getMap();
            final MapleParty party = player.getParty();
            final String name = player.getName();
            final boolean hidden = player.isHidden();
            final int gmLevel = player.getGMLevel();
            final int id = player.getId();
            int messengerId = player.getMessenger() == null ? 0 : player.getMessenger().getId();
            int gid = player.getGuildId();
            final MapleBuddyList bl = player.getBuddylist();
            final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
            final MapleMessengerCharacter chrm = new MapleMessengerCharacter(player);
            final MapleGuildCharacter chrg = player.getMGC();


            removalTask();
            player.saveToDB(true, fromCS);
            if (shutdown) {
                player = null;
                return;
            }

            if (!fromCS) {
                final ChannelServer ch = WorldServer.getInstance().getChannel(map == null ? channel : map.getChannel());

                try {
                    if (ch == null || ch.isShutdown()) {
                        player = null;
                        return;//no idea
                    }
                    if (messengerId > 0) {
                        MessengerManager.leaveMessenger(messengerId, chrm);
                    }

                    if (party != null) {
                        chrp.setOnline(false);
                        PartyManager.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                        if (map != null && party.getLeader().getId() == player.getId()) {
                            MaplePartyCharacter lchr = null;
                            for (final MaplePartyCharacter pchr : party.getMembers()) {
                                if (pchr.getId() != chrp.getId() && pchr != null && map.getCharacterById(pchr.getId()) != null && (lchr == null || lchr.getLevel() < pchr.getLevel())) {
                                    lchr = pchr;
                                }
                            }
                            if (party.getMembers().size() > 0) {
                                if (lchr != null) {
                                    PartyManager.updateParty(party.getId(), PartyOperation.CHANGE_LEADER_DC, lchr);
                                } else {
                                    for (MaplePartyCharacter partychar : party.getMembers()) {
                                        if (partychar.getChannel() == getChannel()) {
                                            final MapleCharacter chr = getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                                            if (chr != null) {
                                                chr.dropMessage(5, "There is no party member in the same field with party leader for the hand over.");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (bl != null) {
                        if (!serverTransition && isLoggedIn()) {
                            BuddyManager.loggedOff(name, id, channel, bl.getBuddyIds(), gmLevel, hidden);
                        } else { // Change channel
                            BuddyManager.loggedOn(name, id, channel, bl.getBuddyIds(), gmLevel, hidden);
                        }
                    }
                    if (gid > 0) {
                        GuildManager.setGuildMemberOnline(chrg, false, -1);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    FileOutputUtil.outputFileError(FileOutputUtil.Acc_Stuck, e);
                    setNotLoggedIn();
                } finally {
                    if (RemoveInChannelServer && ch != null) {
                        ch.removePlayer(id, name);
                    }
                    player = null;
                }
            } else {
                final int ch = FindCommand.findChannel(id);
                if (ch > 0) {
                    disconnect(RemoveInChannelServer, false);
                    return;
                }
                try {
                    if (party != null) {
                        chrp.setOnline(false);
                        PartyManager.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                    }
                    if (!serverTransition && isLoggedIn()) {
                        BuddyManager.loggedOff(name, id, channel, bl.getBuddyIds(), gmLevel, hidden);
                    } else { // Change channel
                        BuddyManager.loggedOn(name, id, channel, bl.getBuddyIds(), gmLevel, hidden);
                    }
                    if (gid > 0) {
                        GuildManager.setGuildMemberOnline(chrg, false, -1);
                    }
                    if (player != null) {
                        player.setMessenger(null);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    FileOutputUtil.outputFileError(FileOutputUtil.Acc_Stuck, e);
                } finally {
                    if (RemoveInChannelServer && ch > 0) {
                        CashShopServer.getInstance().getPlayerStorage().deregisterPlayer(id, name);
                    }
                    player = null;
                }
            }
        }
        if (!serverTransition && isLoggedIn()) {
            updateLoginState(LoginState.LOGIN_NOTLOGGEDIN, getSessionIPAddress());
        }
    }

    private void setNotLoggedIn() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            ps = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE id = ?");
            ps.setInt(1, getAccountData().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            log.info("Error while setting player not logged in", ex);
        }
    }


    public final boolean CheckIPAddress() {
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT SessionIP FROM accounts WHERE id = ?");
            ps.setInt(1, this.accountData.getId());
            final ResultSet rs = ps.executeQuery();

            boolean canLogin = false;

            if (rs.next()) {
                final String sessionIP = rs.getString("SessionIP");

                if (sessionIP != null) { // Probably a login proced skipper?
                    canLogin = getSessionIPAddress().equals(sessionIP.split(":")[0]);
                }
            }
            rs.close();
            ps.close();

            return canLogin;
        } catch (final SQLException e) {
            log.info("Failed in checking IP address for client.");
        }
        return true;
    }

    public final int getChannel() {
        return channel;
    }

    public final void setChannel(final int channel) {
        this.channel = channel;
    }

    public final int getWorld() {
        return world;
    }

    public final void setWorld(final int world) {
        this.world = world;
    }

    public final ChannelServer getChannelServer() {
        return WorldServer.getInstance().getChannel(channel);
    }

    public final void setScriptEngine(final String name, final ScriptEngine e) {
        engines.put(name, e);
    }

    public final void removeScriptEngine(final String name) {
        engines.remove(name);
    }

    public final ScheduledFuture<?> getIdleTask() {
        return idleTask;
    }

    public final void setIdleTask(final ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }

    public int getCharacterSlots() {
        if (this.getAccountData().isGameMaster()) {
            return 15;
        }
        if (charSlots != DEFAULT_CHAR_SLOT) {
            return charSlots; //save a sql
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?");
            ps.setInt(1, this.accountData.getId());
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                charSlots = rs.getInt("charslots");
            } else {
                PreparedStatement psu = con.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)");
                psu.setInt(1, this.accountData.getId());
                psu.setInt(2, world);
                psu.setInt(3, charSlots);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }

        return charSlots;
    }

    public long getLastNPCTalk() {
        return lastNPCTalk;
    }

    public void setLastNPCTalk() {
        lastNPCTalk = System.currentTimeMillis();
    }


}
