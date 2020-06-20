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

import java.util.Objects;

/**
 * @author AuroX
 */
public class BuddyInvitedEntry {

  public String name;
  public int inviter;
  public long expiration;

  public BuddyInvitedEntry(final String n, final int inviterid) {
    name = n.toLowerCase();
    inviter = inviterid;
    expiration = System.currentTimeMillis() + 10 * 60 * 1000; // 10 minutes expiration
  }

  @Override
  public final boolean equals(Object other) {
    if (!(other instanceof BuddyInvitedEntry)) {
      return false;
    }
    BuddyInvitedEntry oth = (BuddyInvitedEntry) other;
    return (inviter == oth.inviter && name.equals(oth.name));
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + Objects.hashCode(this.name);
    hash = 97 * hash + this.inviter;
    hash = 97 * hash + (int) (this.expiration ^ (this.expiration >>> 32));
    return hash;
  }
}
