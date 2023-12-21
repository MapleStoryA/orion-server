package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.world.WorldServer;
import handling.world.helper.MapleMessenger;
import handling.world.helper.MapleMessengerCharacter;
import handling.world.messenger.MessengerManager;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import tools.MaplePacketCreator;

@Slf4j
public class MessengerHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        String input;
        MapleMessenger messenger = c.getPlayer().getMessenger();

        switch (packet.readByte()) {
            case 0x00: // open
                if (messenger == null) {
                    int messengerid = packet.readInt();
                    if (messengerid == 0) { // create
                        c.getPlayer()
                                .setMessenger(
                                        MessengerManager.createMessenger(new MapleMessengerCharacter(c.getPlayer())));
                    } else { // join
                        messenger = MessengerManager.getMessenger(messengerid);
                        if (messenger != null) {
                            final int position = messenger.getLowestPosition();
                            if (position > -1 && position < 4) {
                                c.getPlayer().setMessenger(messenger);
                                MessengerManager.joinMessenger(
                                        messenger.getId(),
                                        new MapleMessengerCharacter(c.getPlayer()),
                                        c.getPlayer().getName(),
                                        c.getChannel());
                            }
                        }
                    }
                }
                break;
            case 0x02: // exit
                if (messenger != null) {
                    final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
                    MessengerManager.leaveMessenger(messenger.getId(), messengerplayer);
                    c.getPlayer().setMessenger(null);
                }
                break;
            case 0x03: // invite
                if (messenger != null) {
                    final int position = messenger.getLowestPosition();
                    if (position <= -1 || position >= 4) {
                        return;
                    }
                    input = packet.readMapleAsciiString();
                    final MapleCharacter target =
                            c.getChannelServer().getPlayerStorage().getCharacterByName(input);

                    if (target != null) {
                        if (target.getMessenger() == null) {
                            if (!target.isGameMaster() || c.getPlayer().isGameMaster()) {
                                c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 1));
                                target.getClient()
                                        .getSession()
                                        .write(MaplePacketCreator.messengerInvite(
                                                c.getPlayer().getName(), messenger.getId()));
                            } else {
                                c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 0));
                            }
                        } else {
                            c.getSession()
                                    .write(MaplePacketCreator.messengerChat(
                                            c.getPlayer().getName()
                                                    + " : "
                                                    + target.getName()
                                                    + " is already using Maple"
                                                    + " Messenger."));
                        }
                    } else {
                        if (WorldServer.getInstance().isConnected(input)) {
                            MessengerManager.messengerInvite(
                                    c.getPlayer().getName(),
                                    messenger.getId(),
                                    input,
                                    c.getChannel(),
                                    c.getPlayer().isGameMaster());
                        } else {
                            c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 0));
                        }
                    }
                }
                break;
            case 0x05: // decline
                final String targeted = packet.readMapleAsciiString();
                final MapleCharacter target =
                        c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
                if (target != null) { // This channel
                    if (target.getMessenger() != null) {
                        target.getClient()
                                .getSession()
                                .write(MaplePacketCreator.messengerNote(
                                        c.getPlayer().getName(), 5, 0));
                    }
                } else { // Other channel
                    if (!c.getPlayer().isGameMaster()) {
                        MessengerManager.declineChat(targeted, c.getPlayer().getName());
                    }
                }
                break;
            case 0x06: // message
                if (messenger != null) {
                    MessengerManager.messengerChat(
                            messenger.getId(),
                            packet.readMapleAsciiString(),
                            c.getPlayer().getName());
                }
                break;
        }
    }
}
