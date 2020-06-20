package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.Iterator;

public class DamageSummonHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    slea.skip(4);
    final int unkByte = slea.readByte();
    final int damage = slea.readInt();
    final int monsterIdFrom = slea.readInt();
    // slea.readByte(); // stance
    MapleCharacter chr = c.getPlayer();
    final Iterator<MapleSummon> iter = chr.getSummons().values().iterator();
    MapleSummon summon;


    while (iter.hasNext()) {
      summon = iter.next();
      if (summon.isPuppet() && summon.getOwnerId() == chr.getId()) { // We
        // can
        // only
        // have
        // one
        // puppet(AFAIK
        // O.O)
        // so
        // this
        // check
        // is
        // safe.
        summon.addHP((short) -damage);
        if (summon.getHP() <= 0) {
          chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        chr.getMap().broadcastMessage(chr,
            MaplePacketCreator.damageSummon(chr.getId(), summon.getSkill(), damage, unkByte, monsterIdFrom),
            summon.getPosition());
        break;
      }
    }

  }

}
