package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import constants.MapConstants;
import handling.AbstractMaplePacketHandler;
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
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
            MessengerManager.leaveMessenger(chr.getMessenger().getId(), messengerplayer);
        }
        PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
        WorldServer.getInstance().getChangeChannelData(new CharacterTransfer(chr), chr.getId(), -10);
        ch.removePlayer(chr);
        c.updateLoginState(MapleClient.CHANGE_CHANNEL, c.getSessionIPAddress());
        chr.saveToDB(false, false);
        chr.getMap().removePlayer(chr);
        c.setPlayer(null);
        c.setReceiving(false);
        c.getSession().write(MaplePacketCreator.getChannelChange(Integer.parseInt(CashShopServer.getIP().split(":")[1])));

    }

}
