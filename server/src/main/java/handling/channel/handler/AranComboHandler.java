package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class AranComboHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    if (chr != null && chr.getJob() >= 2000 && chr.getJob() <= 2112) {
      short combo = chr.getCombo();
      final long curr = System.currentTimeMillis();

      if (combo > 0 && (curr - chr.getLastCombo()) > 7000) {
        // Official MS timing is 3.5 seconds, so 7 seconds should be
        // safe.
        chr.getCheatTracker().registerOffense(CheatingOffense.ARAN_COMBO_HACK);
        combo = 0;
      }
      if (combo < 30000) {
        combo++;
      }
      chr.setLastCombo(curr);
      chr.setCombo(combo);

      c.getSession().write(MaplePacketCreator.testCombo(combo));

      switch (combo) { // Hackish method xD
        case 10:
        case 20:
        case 30:
        case 40:
        case 50:
        case 60:
        case 70:
        case 80:
        case 90:
        case 100:
          if (chr.getSkillLevel(21000000) >= (combo / 10)) {
            SkillFactory.getSkill(21000000).getEffect(combo / 10).applyComboBuff(chr, combo);
          }
          break;
      }
    }

  }

}
