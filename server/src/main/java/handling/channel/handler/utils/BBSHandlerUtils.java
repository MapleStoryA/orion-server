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

import client.MapleClient;
import handling.world.World;
import handling.world.guild.MapleBBSThread;
import tools.MaplePacketCreator;

import java.util.List;

public class BBSHandlerUtils {

  public static final String correctLength(final String in, final int maxSize) {
    if (in.length() > maxSize) {
      return in.substring(0, maxSize);
    }
    return in;
  }


  public static void listBBSThreads(MapleClient c, int start) {
    if (c.getPlayer().getGuildId() <= 0) {
      return;
    }
    c.getSession().write(MaplePacketCreator.BBSThreadList(World.Guild.getBBS(c.getPlayer().getGuildId()), start));
  }

  public static final void newBBSReply(final MapleClient c, final int localthreadid, final String text) {
    if (c.getPlayer().getGuildId() <= 0) {
      return;
    }
    World.Guild.addBBSReply(c.getPlayer().getGuildId(), localthreadid, text, c.getPlayer().getId());
    displayThread(c, localthreadid);
  }

  public static final void editBBSThread(final MapleClient c, final String title, final String text, final int icon, final int localthreadid) {
    if (c.getPlayer().getGuildId() <= 0) {
      return; // expelled while viewing?
    }
    World.Guild.editBBSThread(c.getPlayer().getGuildId(), localthreadid, title, text, icon, c.getPlayer().getId(), c.getPlayer().getGuildRank());
    displayThread(c, localthreadid);
  }

  public static final void newBBSThread(final MapleClient c, final String title, final String text, final int icon, final boolean bNotice) {
    if (c.getPlayer().getGuildId() <= 0) {
      return; // expelled while viewing?
    }
    displayThread(c, World.Guild.addBBSThread(c.getPlayer().getGuildId(), title, text, icon, bNotice, c.getPlayer().getId()));
  }

  public static final void deleteBBSThread(final MapleClient c, final int localthreadid) {
    if (c.getPlayer().getGuildId() <= 0) {
      return;
    }
    World.Guild.deleteBBSThread(c.getPlayer().getGuildId(), localthreadid, c.getPlayer().getId(), (int) c.getPlayer().getGuildRank());
  }

  public static final void deleteBBSReply(final MapleClient c, final int localthreadid, final int replyid) {
    if (c.getPlayer().getGuildId() <= 0) {
      return;
    }

    World.Guild.deleteBBSReply(c.getPlayer().getGuildId(), localthreadid, replyid, c.getPlayer().getId(), (int) c.getPlayer().getGuildRank());
    displayThread(c, localthreadid);
  }

  public static final void displayThread(final MapleClient c, final int localthreadid) {
    if (c.getPlayer().getGuildId() <= 0) {
      return;
    }
    final List<MapleBBSThread> bbsList = World.Guild.getBBS(c.getPlayer().getGuildId());
    if (bbsList != null) {
      for (MapleBBSThread t : bbsList) {
        if (t != null && t.localthreadID == localthreadid) {
          c.getSession().write(MaplePacketCreator.showThread(t));
        }
      }
    }
  }
}
