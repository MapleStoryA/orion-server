package handling.world.messenger;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import handling.world.helper.FindCommand;
import handling.world.helper.MapleMessenger;
import handling.world.helper.MapleMessengerCharacter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import tools.MaplePacketCreator;

public class MessengerManager {

    private static final Map<Integer, MapleMessenger> messengers = new HashMap<>();
    private static final AtomicInteger runningMessengerId = new AtomicInteger();

    static {
        runningMessengerId.set(1);
    }

    public static MapleMessenger createMessenger(MapleMessengerCharacter chrfor) {
        int messengerid = runningMessengerId.getAndIncrement();
        MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
        messengers.put(messenger.getId(), messenger);
        return messenger;
    }

    public static void declineChat(String target, String namefrom) {
        int ch = FindCommand.findChannel(target);
        if (ch > 0) {
            ChannelServer cs = WorldServer.getInstance().getChannel(ch);
            MapleCharacter chr = cs.getPlayerStorage().getCharacterByName(target);
            if (chr != null) {
                MapleMessenger messenger = chr.getMessenger();
                if (messenger != null) {
                    chr.getClient().getSession().write(MaplePacketCreator.messengerNote(namefrom, 5, 0));
                }
            }
        }
    }

    public static MapleMessenger getMessenger(int messengerid) {
        return messengers.get(messengerid);
    }

    public static void leaveMessenger(int messengerid, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        int position = messenger.getPositionByName(target.getName());
        messenger.removeMember(target);

        for (MapleMessengerCharacter mmc : messenger.getMembers()) {
            if (mmc != null) {
                int ch = FindCommand.findChannel(mmc.getId());
                if (ch > 0) {
                    MapleCharacter chr = WorldServer.getInstance()
                            .getChannel(ch)
                            .getPlayerStorage()
                            .getCharacterByName(mmc.getName());
                    if (chr != null) {
                        chr.getClient().getSession().write(MaplePacketCreator.removeMessengerPlayer(position));
                    }
                }
            }
        }
    }

    public static void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.silentRemoveMember(target);
    }

    public static void silentJoinMessenger(int messengerid, MapleMessengerCharacter target) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.silentAddMember(target);
    }

    public static void updateMessenger(int messengerid, String namefrom, int fromchannel) {
        MapleMessenger messenger = getMessenger(messengerid);
        int position = messenger.getPositionByName(namefrom);

        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            if (messengerchar != null && !messengerchar.getName().equals(namefrom)) {
                int ch = FindCommand.findChannel(messengerchar.getName());
                if (ch > 0) {
                    MapleCharacter chr = WorldServer.getInstance()
                            .getChannel(ch)
                            .getPlayerStorage()
                            .getCharacterByName(messengerchar.getName());
                    if (chr != null) {
                        MapleCharacter from = WorldServer.getInstance()
                                .getChannel(fromchannel)
                                .getPlayerStorage()
                                .getCharacterByName(namefrom);
                        chr.getClient()
                                .getSession()
                                .write(MaplePacketCreator.updateMessengerPlayer(
                                        namefrom, from, position, fromchannel - 1));
                    }
                }
            }
        }
    }

    public static void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }
        messenger.addMember(target);
        int position = messenger.getPositionByName(target.getName());
        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            if (messengerchar != null) {
                int mposition = messenger.getPositionByName(messengerchar.getName());
                int ch = FindCommand.findChannel(messengerchar.getName());
                if (ch > 0) {
                    MapleCharacter chr = WorldServer.getInstance()
                            .getChannel(ch)
                            .getPlayerStorage()
                            .getCharacterByName(messengerchar.getName());
                    if (chr != null) {
                        if (!messengerchar.getName().equals(from)) {
                            MapleCharacter fromCh = WorldServer.getInstance()
                                    .getChannel(fromchannel)
                                    .getPlayerStorage()
                                    .getCharacterByName(from);
                            chr.getClient()
                                    .getSession()
                                    .write(MaplePacketCreator.addMessengerPlayer(
                                            from, fromCh, position, fromchannel - 1));
                            fromCh.getClient()
                                    .getSession()
                                    .write(MaplePacketCreator.addMessengerPlayer(
                                            chr.getName(), chr, mposition, messengerchar.getChannel() - 1));
                        } else {
                            chr.getClient().getSession().write(MaplePacketCreator.joinMessenger(mposition));
                        }
                    }
                }
            }
        }
    }

    public static void messengerChat(int messengerid, String chattext, String namefrom) {
        MapleMessenger messenger = getMessenger(messengerid);
        if (messenger == null) {
            throw new IllegalArgumentException("No messenger with the specified messengerid exists");
        }

        for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
            if (messengerchar != null && !messengerchar.getName().equals(namefrom)) {
                int ch = FindCommand.findChannel(messengerchar.getName());
                if (ch > 0) {
                    MapleCharacter chr = WorldServer.getInstance()
                            .getChannel(ch)
                            .getPlayerStorage()
                            .getCharacterByName(messengerchar.getName());
                    if (chr != null) {

                        chr.getClient().getSession().write(MaplePacketCreator.messengerChat(chattext));
                    }
                }
            } // Whisp Monitor Code
            else if (messengerchar != null) {
                int ch = FindCommand.findChannel(messengerchar.getName());
                if (ch > 0) {
                    MapleCharacter chr = WorldServer.getInstance()
                            .getChannel(ch)
                            .getPlayerStorage()
                            .getCharacterByName(messengerchar.getName());
                }
            }
            //
        }
    }

    public static void messengerInvite(String sender, int messengerid, String target, int fromchannel, boolean gm) {

        if (WorldServer.getInstance().isConnected(target)) {

            int ch = FindCommand.findChannel(target);
            if (ch > 0) {
                MapleCharacter from = WorldServer.getInstance()
                        .getChannel(fromchannel)
                        .getPlayerStorage()
                        .getCharacterByName(sender);
                MapleCharacter targeter = WorldServer.getInstance()
                        .getChannel(ch)
                        .getPlayerStorage()
                        .getCharacterByName(target);
                if (targeter != null && targeter.getMessenger() == null) {
                    if (!targeter.isGameMaster() || gm) {
                        targeter.getClient()
                                .getSession()
                                .write(MaplePacketCreator.messengerInvite(sender, messengerid));
                        from.getClient().getSession().write(MaplePacketCreator.messengerNote(target, 4, 1));
                    } else {
                        from.getClient().getSession().write(MaplePacketCreator.messengerNote(target, 4, 0));
                    }
                } else {
                    from.getClient()
                            .getSession()
                            .write(MaplePacketCreator.messengerChat(
                                    sender + " : " + target + " is already using Maple Messenger"));
                }
            }
        }
    }
}
