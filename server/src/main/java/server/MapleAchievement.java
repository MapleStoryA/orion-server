/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package server;

import client.MapleCharacter;
import handling.world.Broadcast;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;

@Slf4j
public class MapleAchievement {

    private final boolean notice;
    private String name;
    private int reward;

    public MapleAchievement(String name, int reward) {
        this.name = name;
        this.reward = reward;
        this.notice = true;
    }

    public MapleAchievement(String name, int reward, boolean notice) {
        this.name = name;
        this.reward = reward;
        this.notice = notice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public boolean getNotice() {
        return notice;
    }

    public void finishAchievement(MapleCharacter chr) {
        chr.modifyAchievementCSPoints(1, reward);
        chr.getFinishedAchievements()
                .setAchievementFinished(MapleAchievements.getInstance().getByMapleAchievement(this));
        if (notice && !chr.isGameMaster()) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(
                    6,
                    "[Achievement] Congratulations to "
                            + chr.getName()
                            + " on "
                            + name
                            + " and rewarded with "
                            + reward
                            + " Nx-Credit!"));
        } else {
            chr.getClient()
                    .getSession()
                    .write(MaplePacketCreator.serverNotice(
                            5, "[Achievement] You've gained " + reward + " Nx-Credit as you " + name + "."));
        }
    }
}
