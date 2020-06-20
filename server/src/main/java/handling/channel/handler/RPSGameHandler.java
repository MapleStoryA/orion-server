package handling.channel.handler;

import client.MapleClient;
import client.RockPaperScissors;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class RPSGameHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (slea.available() == 0 || !c.getPlayer().getMap().containsNPC(9000019)) {
      if (c.getPlayer().getRPS() != null) {
        c.getPlayer().getRPS().dispose(c);
      }
      return;
    }
    final byte mode = slea.readByte();
    switch (mode) {
      case 0: // start game
      case 5: // retry
        if (c.getPlayer().getRPS() != null) {
          c.getPlayer().getRPS().reward(c);
        }
        if (c.getPlayer().getMeso() >= 1000) {
          c.getPlayer().setRPS(new RockPaperScissors(c, mode));
        } else {
          c.getSession().write(MaplePacketCreator.getRPSMode((byte) 0x08, -1, -1, -1));
        }
        break;
      case 1: // answer
        if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().answer(c, slea.readByte())) {
          c.getSession().write(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
        }
        break;
      case 2: // time over
        if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().timeOut(c)) {
          c.getSession().write(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
        }
        break;
      case 3: // continue
        if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().nextRound(c)) {
          c.getSession().write(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
        }
        break;
      case 4: // leave
        if (c.getPlayer().getRPS() != null) {
          c.getPlayer().getRPS().dispose(c);
        } else {
          c.getSession().write(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
        }
        break;
    }

  }

}
