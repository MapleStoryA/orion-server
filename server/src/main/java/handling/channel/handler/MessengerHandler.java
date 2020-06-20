package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.World;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MessengerHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    String input;
    MapleMessenger messenger = c.getPlayer().getMessenger();

    switch (slea.readByte()) {
      case 0x00: // open
        if (messenger == null) {
          int messengerid = slea.readInt();
          if (messengerid == 0) { // create
            c.getPlayer()
                .setMessenger(World.Messenger.createMessenger(new MapleMessengerCharacter(c.getPlayer())));
          } else { // join
            messenger = World.Messenger.getMessenger(messengerid);
            if (messenger != null) {
              final int position = messenger.getLowestPosition();
              if (position > -1 && position < 4) {
                c.getPlayer().setMessenger(messenger);
                World.Messenger.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()),
                    c.getPlayer().getName(), c.getChannel());
              }
            }
          }
        }
        break;
      case 0x02: // exit
        if (messenger != null) {
          final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
          World.Messenger.leaveMessenger(messenger.getId(), messengerplayer);
          c.getPlayer().setMessenger(null);
        }
        break;
      case 0x03: // invite

        if (messenger != null) {
          final int position = messenger.getLowestPosition();
          if (position <= -1 || position >= 4) {
            return;
          }
          input = slea.readMapleAsciiString();
          final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);

          if (target != null) {
            if (target.getMessenger() == null) {
              if (!target.isGM() || c.getPlayer().isGM()) {
                c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 1));
                target.getClient().getSession().write(
                    MaplePacketCreator.messengerInvite(c.getPlayer().getName(), messenger.getId()));
              } else {
                c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 0));
              }
            } else {
              c.getSession().write(MaplePacketCreator.messengerChat(c.getPlayer().getName() + " : "
                  + target.getName() + " is already using Maple Messenger."));
            }
          } else {
            if (World.isConnected(input)) {
              World.Messenger.messengerInvite(c.getPlayer().getName(), messenger.getId(), input,
                  c.getChannel(), c.getPlayer().isGM());
            } else {
              c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 0));
            }
          }
        }
        break;
      case 0x05: // decline
        final String targeted = slea.readMapleAsciiString();
        final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
        if (target != null) { // This channel
          if (target.getMessenger() != null) {
            target.getClient().getSession()
                .write(MaplePacketCreator.messengerNote(c.getPlayer().getName(), 5, 0));
          }
        } else { // Other channel
          if (!c.getPlayer().isGM()) {
            World.Messenger.declineChat(targeted, c.getPlayer().getName());
          }
        }
        break;
      case 0x06: // message
        if (messenger != null) {
          World.Messenger.messengerChat(messenger.getId(), slea.readMapleAsciiString(), c.getPlayer().getName());

        }
        break;
    }
  }


}
