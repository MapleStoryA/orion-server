package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.MapConstants;
import handling.AbstractMaplePacketHandler;
import handling.ServerMigration;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.world.WorldServer;
import handling.world.helper.CharacterTransfer;
import handling.world.helper.MapleMessengerCharacter;
import handling.world.helper.PlayerBuffStorage;
import handling.world.messenger.MessengerManager;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

@lombok.extern.slf4j.Slf4j
public class EnterCashShopHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();

        if (!chr.isAlive() || chr.getEventInstance() != null || c.getChannelServer() == null
                || MapConstants.isStorylineMap(chr.getMapId())) {
            c.getSession().write(MaplePacketCreator.serverBlocked(2));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }

        final ChannelServer ch = WorldServer.getInstance().getChannel(c.getChannel());

        chr.changeRemoval();

        if (chr.getMessenger() != null) {
            var participant = new MapleMessengerCharacter(chr);
            MessengerManager.leaveMessenger(chr.getMessenger().getId(), participant);
        }
        PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
        ch.removePlayer(chr);
        chr.saveToDB(false, false);
        chr.getMap().removePlayer(chr);
        c.setPlayer(null);

        ServerMigration entry = new ServerMigration(chr.getId(), c.getAccountData(), c.getSessionIPAddress());
        entry.setCharacterTransfer(new CharacterTransfer(chr));
        WorldServer.getInstance().getMigrationService().putMigrationEntry(entry);

        c.getSession().write(MaplePacketCreator.getChannelChange(Integer.parseInt(CashShopServer.getInstance().getPublicAddress().split(":")[1])));

    }

}
