package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.Randomizer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;

public class FriendlyDamageHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final MapleMap map = chr.getMap();
    if (map == null) {
      return;
    }
    final MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
    slea.skip(4); // Player ID
    final MapleMonster mobto = map.getMonsterByOid(slea.readInt());

    if (mobfrom != null && mobto != null && mobto.getStats().isFriendly()) {
      final int damage = (mobto.getStats().getLevel() * Randomizer.nextInt(99)) / 2; // Temp
      mobto.damage(chr, damage, true);
      MobHandlerUtils.checkShammos(chr, mobto, map);
    }

  }

}
