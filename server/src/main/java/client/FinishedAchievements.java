package client;

import lombok.Getter;
import server.MapleAchievements;

import java.util.ArrayList;
import java.util.List;

public class FinishedAchievements {
    private final List<Integer> finishedAchievements = new ArrayList<>();
    @Getter
    private boolean changed;


    public void setAchievementFinished(int id) {
        if (!finishedAchievements.contains(id)) {
            finishedAchievements.add(id);
            changed = true;
        }
    }

    public void addAchievementFinished(int id) {
        if (!finishedAchievements.contains(id)) {
            finishedAchievements.add(id);
        }
    }

    public boolean isAchievementFinished(int achievement_id) {
        return finishedAchievements.contains(achievement_id);
    }

    public void finishAchievement(MapleCharacter player, int id) {
        if (!isAchievementFinished(id)) {
            if (player.isAlive()) {
                setAchievementFinished(id);
                MapleAchievements.getInstance().getById(id).finishAchievement(player);
                changed = true;
            }
        }
    }

    public List<Integer> getFinishedAchievements() {
        return this.finishedAchievements;
    }
}
