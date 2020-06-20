/*
 * This file is part of the OdinMS MapleStory Private Server
 * Copyright (C) 2011 Patrick Huy and Matthias Butz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package handling.world.buddy;

public class BuddyListEntry {

  private String name, group;
  private int cid, channel;

  public BuddyListEntry(final String name, final int characterId, final String group, final int channel) {
    this.name = name;
    this.cid = characterId;
    this.group = group;
    this.channel = channel;
  }

  public final String getName() {
    return name;
  }

  public final String getGroup() {
    return group;
  }

  public final void setGroup(final String g) {
    this.group = g;
  }

  public final int getCharacterId() {
    return cid;
  }

  public final int getChannel() {
    return channel;
  }

  public final void setChannel(final int channel) {
    this.channel = channel;
  }
}
