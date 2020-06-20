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

package handling.channel.handler.utils;

import client.MapleCharacter;
import tools.MaplePacketCreator;

public class GuildHandlerUtils {


  public static final boolean isGuildNameAcceptable(final String name) {
    if (name.length() < 3 || name.length() > 12) {
      return false;
    }
    for (int i = 0; i < name.length(); i++) {
      if (!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static final void respawnPlayer(final MapleCharacter mc) {
    if (mc.getMap() == null) {
      return;
    }
    mc.getMap().broadcastMessage(MaplePacketCreator.loadGuildName(mc));
    mc.getMap().broadcastMessage(MaplePacketCreator.loadGuildIcon(mc));
  }

  public static final class Invited {

    public String name;
    public int gid;
    public long expiration;

    public Invited(final String n, final int id) {
      name = n.toLowerCase();
      gid = id;
      expiration = System.currentTimeMillis() + 60 * 60 * 1000; // 1 hr expiration
    }

    @Override
    public final boolean equals(Object other) {
      if (!(other instanceof Invited)) {
        return false;
      }
      Invited oth = (Invited) other;
      return (gid == oth.gid && name.equals(oth.name));
    }
  }

  public static final java.util.List<Invited> invited = new java.util.LinkedList<Invited>();
  public static long nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;


}
