package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.commands.v1.CommandProcessor;
import constants.ServerConstants.CommandType;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
public class GeneralChatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        final String text = slea.readMapleAsciiString();
        final byte unk = slea.readByte();
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
