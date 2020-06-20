package handling.channel.handler;

import client.*;
import constants.GameConstants;
import handling.AbstractMaplePacketHandler;
import server.Randomizer;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

import java.util.ArrayList;
import java.util.List;

public class DistributeAPHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
    c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
    chr.updateTick(slea.readInt());

    final PlayerStats stat = chr.getStat();
    final int job = chr.getJob();
    if (chr.getRemainingAp() > 0) {
      switch (slea.readInt()) {
        case 64: // Str
          if (stat.getStr() >= 999) {
            return;
          }
          stat.setStr((short) (stat.getStr() + 1));
          statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, (int) stat.getStr()));
          break;
        case 128: // Dex
          if (stat.getDex() >= 999) {
            return;
          }
          stat.setDex((short) (stat.getDex() + 1));
          statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, (int) stat.getDex()));
          break;
        case 256: // Int
          if (stat.getInt() >= 999) {
            return;
          }
          stat.setInt((short) (stat.getInt() + 1));
          statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, (int) stat.getInt()));
          break;
        case 512: // Luk
          if (stat.getLuk() >= 999) {
            return;
          }
          stat.setLuk((short) (stat.getLuk() + 1));
          statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, (int) stat.getLuk()));
          break;
        case 2048: // HP
          int maxhp = stat.getMaxHp();
          if (chr.getHpApUsed() >= 10000 || maxhp >= 30000) {
            return;
          }
          if (job == 0) { // Beginner
            maxhp += Randomizer.rand(8, 12);
          } else if ((job >= 100 && job <= 132) || (job >= 3200 && job <= 3212)) { // Warrior
            ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
            int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(20, 25);
            if (improvingMaxHPLevel >= 1) {
              maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
            }
          } else if ((job >= 200 && job <= 232) || (GameConstants.isEvan(job))) { // Magician
            maxhp += Randomizer.rand(10, 20);
          } else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434) || (job >= 1300 && job <= 1312) || (job >= 1400 && job <= 1412) || (job >= 3300 && job <= 3312)) { // Bowman
            maxhp += Randomizer.rand(16, 20);
          } else if ((job >= 500 && job <= 522) || (job >= 3500 && job <= 3512)) { // Pirate
            ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
            int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(18, 22);
            if (improvingMaxHPLevel >= 1) {
              maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
            }
          } else if (job >= 1500 && job <= 1512) { // Pirate
            ISkill improvingMaxHP = SkillFactory.getSkill(15100000);
            int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(18, 22);
            if (improvingMaxHPLevel >= 1) {
              maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
            }
          } else if (job >= 1100 && job <= 1112) { // Soul Master
            ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
            int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
            maxhp += Randomizer.rand(36, 42);
            if (improvingMaxHPLevel >= 1) {
              maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
            }
          } else if (job >= 1200 && job <= 1212) { // Flame Wizard
            maxhp += Randomizer.rand(15, 21);
          } else if (job >= 2000 && job <= 2112) { // Aran
            maxhp += Randomizer.rand(40, 50);
          } else { // GameMaster
            maxhp += Randomizer.rand(50, 100);
          }
          maxhp = (short) Math.min(30000, Math.abs(maxhp));
          chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
          stat.setMaxHp(maxhp);
          statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, (int) maxhp));
          break;
        case 8192: // MP
          int maxmp = stat.getMaxMp();
          if (chr.getHpApUsed() >= 10000 || stat.getMaxMp() >= 30000) {
            return;
          }
          if (job == 0) { // Beginner
            maxmp += Randomizer.rand(6, 8);
          } else if (job >= 100 && job <= 132) { // Warrior
            maxmp += Randomizer.rand(2, 4);
          } else if ((job >= 200 && job <= 232) || (GameConstants.isEvan(job)) || (job >= 3200 && job <= 3212)) { // Magician
            ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
            int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
            maxmp += Randomizer.rand(18, 20);
            if (improvingMaxMPLevel >= 1) {
              maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getY() * 2;
            }
          } else if ((job >= 300 && job <= 322) || (job >= 400 && job <= 434) || (job >= 500 && job <= 522) || (job >= 3200 && job <= 3212) || (job >= 3500 && job <= 3512) || (job >= 1300 && job <= 1312) || (job >= 1400 && job <= 1412) || (job >= 1500 && job <= 1512)) { // Bowman
            maxmp += Randomizer.rand(10, 12);
          } else if (job >= 1100 && job <= 1112) { // Soul Master
            maxmp += Randomizer.rand(6, 9);
          } else if (job >= 1200 && job <= 1212) { // Flame Wizard
            ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
            int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
            maxmp += Randomizer.rand(18, 20);
            if (improvingMaxMPLevel >= 1) {
              maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getY() * 2;
            }
          } else if (job >= 2000 && job <= 2112) { // Aran
            maxmp += Randomizer.rand(6, 9);
          } else { // GameMaster
            maxmp += Randomizer.rand(50, 100);
          }
          maxmp = (short) Math.min(30000, Math.abs(maxmp));
          chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
          stat.setMaxMp(maxmp);
          statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, (int) maxmp));
          break;
        default:
          c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, chr.getJob()));
          return;
      }
      chr.setRemainingAp((short) (chr.getRemainingAp() - 1));
      statupdate.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, (int) chr.getRemainingAp()));
      c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
    }

  }

}
