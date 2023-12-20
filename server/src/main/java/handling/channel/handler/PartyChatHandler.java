package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.commands.CommandProcessor;
import constants.ServerConstants.CommandType;
import handling.world.alliance.AllianceManager;
import handling.world.buddy.BuddyManager;
import handling.world.guild.GuildManager;
import handling.world.party.PartyManager;
import networking.data.input.InPacket;
import networking.packet.AbstractMaplePacketHandler;

@lombok.extern.slf4j.Slf4j
public class PartyChatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        c.getPlayer().updateTick(packet.readInt());
        MapleCharacter chr = c.getPlayer();
        final int type = packet.readByte();
        final byte numRecipients = packet.readByte();
        int[] recipients = new int[numRecipients];

        for (byte i = 0; i < numRecipients; i++) {
            recipients[i] = packet.readInt();
        }
        final String chattext = packet.readMapleAsciiString();

        if (CommandProcessor.processCommand(c, chattext, CommandType.NORMAL)) {
            return;
        }
        chr.getCheatTracker().checkMsg();
        switch (type) {
            case 0:
                BuddyManager.buddyChat(recipients, chr.getId(), chr.getName(), chattext);
                break;
            case 1:
                if (chr.getParty() == null) {
                    break;
                }
                PartyManager.partyChat(chr.getParty().getId(), chattext, chr.getName());
                break;
            case 2:
                if (chr.getGuildId() <= 0) {
                    break;
                }
                GuildManager.guildChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                break;
            case 3:
                if (chr.getGuildId() <= 0) {
                    break;
                }
                AllianceManager.allianceChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                break;
            case 6:
                if (chr.getParty() == null || chr.getParty().getExpeditionId() <= 0) {
                    break;
                }
                PartyManager.expedChat(chr.getParty().getExpeditionId(), chattext, chr.getName());
        }
    }
}
