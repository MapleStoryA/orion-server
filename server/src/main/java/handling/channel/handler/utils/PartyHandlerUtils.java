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

package handling.channel.handler.utils;

public class PartyHandlerUtils {

  public enum PartyOperation {

    JOIN, LEAVE, MOVE_MEMBER, EXPEL, DISBAND, DISBAND_IN_EXPEDITION, SILENT_UPDATE, LOG_ONOFF, CHANGE_LEADER, CHANGE_LEADER_DC
  }

  public static final byte // Party actions
      BEGINNER_NO_PARTY = 0x0A, // A beginner can't create a party.
      NOT_IN_PARTY = 0x0D, // You have yet to join a party.
      JOINED_PARTY = 0x10, // You have joined the party.
      ALREADY_JOINED = 0x11, // Already have joined a party.
      PARTY_FULL = 0x12, // The party you're trying to join is already in full capacity.
      INVITE_MSG = 0x16, // You have invited <name> to your party. (Popup)
      NO_EXPEL = 0x1D, // Cannot kick another user in this map | Expel function is not available in this map.
      NOT_SAME_MAP = 0x20, // This can only be given to a party member within the vicinity. | The Party Leader can only be handed over to the party member in the same map.
      FAILED_TO_HAND_OVER = 0x21, // Unable to hand over the leadership post; No party member is currently within the vicinity of the party leader | There is no party member in the same field with party leader for the hand over.
      NOT_SAME_MAP1 = 0x22, // You may only change with the party member that's on the same channel. | You can only hand over to the party member within the same map.
      NO_GM_CREATES = 0x24, // As a GM, you're forbidden from creating a party.
      NON_EXISTANT = 0x25; // Unable to find the character.


}
