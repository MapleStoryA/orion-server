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

package client.anticheat;

@lombok.extern.slf4j.Slf4j
public class CheatingOffenseEntry {

    private final CheatingOffense offense;
    private final int characterid;
    private final long firstOffense;
    private int count = 0;
    private long lastOffense;
    private String param;
    private int dbid = -1;

    public CheatingOffenseEntry(CheatingOffense offense, int characterid) {
        super();
        this.offense = offense;
        this.characterid = characterid;
        firstOffense = System.currentTimeMillis();
    }

    public CheatingOffense getOffense() {
        return offense;
    }

    public int getCount() {
        return count;
    }

    public int getChrfor() {
        return characterid;
    }

    public void incrementCount() {
        this.count++;
        lastOffense = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return lastOffense < (System.currentTimeMillis() - offense.getValidityDuration());
    }

    public int getPoints() {
        return count * offense.getPoints();
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public long getLastOffenseTime() {
        return lastOffense;
    }

    public int getDbId() {
        return dbid;
    }

    public void setDbId(int dbid) {
        this.dbid = dbid;
    }
}
