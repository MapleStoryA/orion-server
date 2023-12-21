package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants.CommandType;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;
import server.base.commands.CommandProcessor;
import tools.MaplePacketCreator;

@Slf4j
public class GeneralChatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());
        final String text = packet.readMapleAsciiString();
        final byte unk = packet.readByte();
        MapleCharacter chr = c.getPlayer();
        if (chr != null && !CommandProcessor.processCommand(c, text, CommandType.NORMAL)) {
            if (!chr.isGameMaster() && text.length() >= 80) {
                return;
            }
            if (chr.isHidden()) {
                chr.getMap()
                        .broadcastGMMessage(
                                chr,
                                MaplePacketCreator.getChatText(
                                        chr.getId(), text, c.getPlayer().isGameMaster(), unk),
                                true);
            } else {
                chr.getCheatTracker().checkMsg();
                chr.getMap()
                        .broadcastMessage(
                                MaplePacketCreator.getChatText(
                                        chr.getId(), text, c.getPlayer().isGameMaster(), unk),
                                c.getPlayer().getPosition());
            }
        }
    }
}
