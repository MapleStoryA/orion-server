package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.data.input.SeekableLittleEndianAccessor;

public class HypnotizeDamageHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
    slea.skip(4); // Player ID
    final int to = slea.readInt(); // mobto
    slea.skip(1); // Same as player damage, -1 = bump, integer = skill ID
    final int damage = slea.readInt();
    // slea.skip(1); // Facing direction
    // slea.skip(4); // Some type of pos, damage display, I think

    final MapleMonster mob_to = chr.getMap().getMonsterByOid(to);

    if (mob_from != null && mob_to != null && mob_to.getStats().isFriendly()) { // temp
      // for
      // now
      if (damage > 30000) {
        return;
      }
      mob_to.damage(chr, damage, true);
      MobHandlerUtils.checkShammos(chr, mob_to, chr.getMap());
    }

  }

}
