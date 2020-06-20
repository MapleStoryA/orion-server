/*
This file is part of the ZeroFusion MapleStory Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>
ZeroFusion organized by "RMZero213" <RMZero213@hotmail.com>

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import tools.MaplePacketCreator;

import java.awt.*;

public class MapleDragon extends AbstractAnimatedMapleMapObject {

  private int owner;
  private int jobid;
  private Point position = new Point();

  public MapleDragon(int owner, int jobid) {
    this.owner = owner;
    this.jobid = jobid;
  }

  public MapleDragon() {
  }

  public MapleDragon(MapleCharacter owner) {
    super();
    this.owner = owner.getId();
    this.jobid = owner.getJob();
    if (jobid < 2200 || jobid > 2218) {
      throw new RuntimeException("Trying to create a dragon for a non-Evan");
    }
    int randY = (int) (Math.random() * 9);
    randY = randY < 150 ? (int) (Math.random() * 100) : 100;
    int randX = (int) (Math.random() * 9);
    randX = randX < 100 ? (int) (Math.random() * 50) : 100;
    position.y = owner.getPosition().y + randY;
    position.x = owner.getPosition().x + randX;
    setPosition(position);
    setStance(4);
  }

  @Override
  public void sendSpawnData(MapleClient client) {
    client.getSession().write(MaplePacketCreator.spawnDragon(this));
  }

  @Override
  public void sendDestroyData(MapleClient client) {
    client.getSession().write(MaplePacketCreator.removeDragon(this.owner));
  }

  public int getOwner() {
    return this.owner;
  }

  public int getJobId() {
    return this.jobid;
  }

  @Override
  public MapleMapObjectType getType() {
    return MapleMapObjectType.SUMMON;
  }
}
