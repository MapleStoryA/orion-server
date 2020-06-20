package handling.channel.handler;

import client.ISkill;
import client.MapleClient;
import client.SkillFactory;
import handling.AbstractMaplePacketHandler;
import server.maps.MapleMist;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

public class ThrowSkillHandler extends AbstractMaplePacketHandler {


  private static Rectangle calculateBoundingBox(Point posFrom) {
    Point mylt = new Point(-100 + posFrom.x, -82 + posFrom.y);
    Point myrb = new Point(100 + posFrom.x, 83 + posFrom.y);
    return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
  }

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    // Poisonbomb, flashbang, monsterbomb, grenade
    final int x = slea.readInt(); // bomb
    final int y = slea.readInt(); // bomb
    slea.readInt(); // player's y pos (place to land for bomb)
    final int charge = slea.readInt(); // 1200 for monster bomb and 800 for
    // flashbang
    final int skillid = slea.readInt();
    final int skillLevel = slea.readInt();
    if (skillid == 4341003) {
      c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.skillCancel(c.getPlayer(), skillid));
      return;
    }
    if (skillid == 4321002) {
      int left = c.getPlayer().getFacingDirection();
      c.getPlayer().getMap().broadcastMessage(c.getPlayer(),
          MaplePacketCreator.skillEffect(c.getPlayer(), skillid, (byte) skillLevel, (byte) 0, (byte) 1, (byte) left), false);

    }
    if (skillid == 14111006) { // Poison bomb
      int left = c.getPlayer().isFacingLeft() ? -1 : 1;
      final ISkill skill = SkillFactory.getSkill(skillid);
      if (skill != null && c.getPlayer().getSkillLevel(skill) > 0) {
        Point newp = null; // equation is something like -x^2/360+x for
        // fully charged projectile, but i'm not
        // doing rectline thing again.
        try {
          newp = c.getPlayer().getMap().getGroundBelow(new Point(x + left * charge / 3, y - 30));
        } catch (NullPointerException e) {
          newp = c.getPlayer().getPosition();
        }
        final MapleMist mist = new MapleMist(calculateBoundingBox(newp), c.getPlayer(),
            skill.getEffect(c.getPlayer().getSkillLevel(skill)));
        c.getPlayer().getMap().spawnMist(mist, (int) (4 * (Math.ceil(skillLevel / 3))) * 1000, false);
      }
    } // apply monster status effect for monster bomb here? :O
  }

}
