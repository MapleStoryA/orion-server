package client;

public class ThirdJobUtils {

  public static int getMapIdForJob(MapleCharacter player) {
    int mapId = 910000000;
    if (isSecondJobMage(player)) {
      mapId = 108010200;

    } else if (isSecondJobWarrior(player)) {
      mapId = 108010300;

    } else if (isClassicThiefSecondJob(player)) {
      mapId = 108010400;

    } else if (isBowmanSecondJob(player)) {
      mapId = 108010100;

    } else if (isPirateSecondJob(player)) {
      mapId = 108010500;
    }
    return mapId;
  }

  public static boolean isPirateSecondJob(MapleCharacter player) {
    return player.getJobValue().equals(MapleJob.BRAWLER) || player.getJobValue().equals(MapleJob.GUNSLINGER);
  }

  public static boolean isBowmanSecondJob(MapleCharacter player) {
    return player.getJobValue().equals(MapleJob.HUNTER) || player.getJobValue().equals(MapleJob.CROSSBOWMAN);
  }

  public static boolean isClassicThiefSecondJob(MapleCharacter player) {
    return player.getJobValue().equals(MapleJob.ASSASSIN) || player.getJobValue().equals(MapleJob.BANDIT);
  }

  public static boolean isSecondJobWarrior(MapleCharacter player) {
    return player.getJobValue().equals(MapleJob.FIGHTER) || player.getJobValue().equals(MapleJob.PAGE)
        || player.getJobValue().equals(MapleJob.SPEARMAN);
  }

  public static boolean isSecondJobMage(MapleCharacter player) {
    return player.getJobValue().equals(MapleJob.FP_WIZARD) || player.getJobValue().equals(MapleJob.IL_WIZARD)
        || player.getJobValue().equals(MapleJob.CLERIC);
  }

}
