/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General  License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General  License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General  License for more details.

You should have received a copy of the GNU Affero General  License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package client;

import server.MapleStatEffect;
import server.life.Element;

public interface ISkill {

  int getId();

  MapleStatEffect getEffect(int level);

  byte getMaxLevel();

  int getAnimationTime();

  boolean canBeLearnedBy(int job);

  boolean isFourthJob();

  boolean getAction();

  boolean isTimeLimited();

  int getMasterLevel();

  Element getElement();

  boolean isBeginnerSkill();

  boolean hasRequiredSkill();

  boolean isInvisible();

  boolean isChargeSkill();

  int getRequiredSkillLevel();

  int getRequiredSkillId();

  String getName();

  boolean hasMastery();
}
