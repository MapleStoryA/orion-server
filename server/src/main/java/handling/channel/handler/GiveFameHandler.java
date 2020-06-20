package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.anticheat.CheatingOffense;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class GiveFameHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final int who = slea.readInt();
    final int mode = slea.readByte();

    final int famechange = mode == 0 ? -1 : 1;
    final MapleCharacter target = (MapleCharacter) chr.getMap().getMapObject(who, MapleMapObjectType.PLAYER);

    if (target == chr) { // faming self
      chr.getCheatTracker().registerOffense(CheatingOffense.FAMING_SELF);
      return;
    } else if (chr.getLevel() < 15) {
      chr.getCheatTracker().registerOffense(CheatingOffense.FAMING_UNDER_15);
      return;
    }
    switch (chr.canGiveFame(target)) {
      case OK:
        if (Math.abs(target.getFame() + famechange) <= 30000) {
          target.addFame(famechange);
          target.updateSingleStat(MapleStat.FAME, target.getFame());
        }
        if (!chr.isGM()) {
          chr.hasGivenFame(target);
        }
        c.getSession().write(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
        target.getClient().getSession().write(MaplePacketCreator.receiveFame(mode, chr.getName()));
        break;
      case NOT_TODAY:
        c.getSession().write(MaplePacketCreator.giveFameErrorResponse(3));
        break;
      case NOT_THIS_MONTH:
        c.getSession().write(MaplePacketCreator.giveFameErrorResponse(4));
        break;
    }

  }

}
