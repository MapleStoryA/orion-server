/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
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

package client;

import java.util.concurrent.ScheduledFuture;
import server.MapleStatEffect;

@lombok.extern.slf4j.Slf4j
public class MapleBuffStatValueHolder {

    private MapleStatEffect effect;
    private long startTime;
    private int value;
    private ScheduledFuture<?> schedule;

    public MapleBuffStatValueHolder(
            MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
        super();
        this.setEffect(effect);
        this.setStartTime(startTime);
        this.setSchedule(schedule);
        this.setValue(value);
    }

    public MapleStatEffect getEffect() {
        return effect;
    }

    public void setEffect(MapleStatEffect effect) {
        this.effect = effect;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }
}
