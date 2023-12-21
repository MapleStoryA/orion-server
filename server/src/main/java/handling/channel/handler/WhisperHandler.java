package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.world.FindCommand;
import handling.world.WorldServer;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;

@Slf4j
public class WhisperHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        final byte mode = packet.readByte();
        packet.readInt(); // ticks
        switch (mode) {
            case 68: // buddy
            case 5: { // Find
                final String recipient = packet.readMapleAsciiString();
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    if (!player.isGameMaster() || c.getPlayer().isGameMaster() && player.isGameMaster()) {

                        c.getSession()
                                .write(MaplePacketCreator.getFindReplyWithMap(
                                        player.getName(), player.getMap().getId(), mode == 68));
                    } else {
                        c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                    }
                } else { // Not found
                    int ch = FindCommand.findChannel(recipient);
                    if (ch > 0) {
                        player = WorldServer.getInstance()
                                .getChannel(ch)
                                .getPlayerStorage()
                                .getCharacterByName(recipient);
                        if (player == null) {
                            break;
                        }
                        if (player != null) {
                            if (!player.isGameMaster() || (c.getPlayer().isGameMaster() && player.isGameMaster())) {
                                c.getSession().write(MaplePacketCreator.getFindReply(recipient, (byte) ch, mode == 68));
                            } else {
                                c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                            }
                            return;
                        }
                    }
                    if (ch == -10) {
                        c.getSession().write(MaplePacketCreator.getFindReplyWithCS(recipient, mode == 68));
                    } else if (ch == -20) {
                        c.getSession().write(MaplePacketCreator.getFindReplyWithMTS(recipient, mode == 68));
                    } else {
                        c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                    }
                }
                break;
            }
            case 6:
                { // Whisper
                    c.getPlayer().getCheatTracker().checkMsg();
                    final String recipient = packet.readMapleAsciiString();
                    final String text = packet.readMapleAsciiString();
                    final int ch = FindCommand.findChannel(recipient);
                    if (ch > 0) {
                        MapleCharacter player = WorldServer.getInstance()
                                .getChannel(ch)
                                .getPlayerStorage()
                                .getCharacterByName(recipient);
                        if (player == null) {
                            break;
                        }
                        player.getClient()
                                .getSession()
                                .write(MaplePacketCreator.getWhisper(
                                        c.getPlayer().getName(), c.getChannel(), text));
                        if (!c.getPlayer().isGameMaster() && player.isGameMaster() && player.isHidden()) {
                            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                        } else {
                            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
                        }
                    } else {
                        c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                    }
                }
                break;
        }
    }
}
