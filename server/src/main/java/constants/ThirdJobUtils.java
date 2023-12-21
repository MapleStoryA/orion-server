package constants;

import client.MapleCharacter;
import client.MapleJob;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        return player.getJob().equals(MapleJob.BRAWLER) || player.getJob().equals(MapleJob.GUNSLINGER);
    }

    public static boolean isBowmanSecondJob(MapleCharacter player) {
        return player.getJob().equals(MapleJob.HUNTER) || player.getJob().equals(MapleJob.CROSSBOWMAN);
    }

    public static boolean isClassicThiefSecondJob(MapleCharacter player) {
        return player.getJob().equals(MapleJob.ASSASSIN) || player.getJob().equals(MapleJob.BANDIT);
    }

    public static boolean isSecondJobWarrior(MapleCharacter player) {
        return player.getJob().equals(MapleJob.FIGHTER)
                || player.getJob().equals(MapleJob.PAGE)
                || player.getJob().equals(MapleJob.SPEARMAN);
    }

    public static boolean isSecondJobMage(MapleCharacter player) {
        return player.getJob().equals(MapleJob.FP_WIZARD)
                || player.getJob().equals(MapleJob.IL_WIZARD)
                || player.getJob().equals(MapleJob.CLERIC);
    }
}
