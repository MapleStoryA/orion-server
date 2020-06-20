package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class NoteActionHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    MapleCharacter chr = c.getPlayer();
    final byte type = slea.readByte();

    switch (type) {
      case 0:
        String name = slea.readMapleAsciiString();
        String msg = slea.readMapleAsciiString();
        boolean fame = slea.readByte() > 0;
        slea.readInt(); // 0?
        IItem itemz = chr.getCashInventory().findByCashId((int) slea.readLong());
        if (itemz == null || !itemz.getGiftFrom().equalsIgnoreCase(name)
            || !chr.getCashInventory().canSendNote(itemz.getSN())) {
          return;
        }
        try {
          chr.sendNote(name, msg, fame ? 1 : 0);
          chr.getCashInventory().sendedNote(itemz.getSN());
        } catch (Exception e) {
          e.printStackTrace();
        }
        break;
      case 1:
        final byte num = slea.readByte();
        slea.skip(2);

        for (int i = 0; i < num; i++) {
          final int id = slea.readInt();
          chr.deleteNote(id, slea.readByte() > 0 ? 1 : 0);
        }
        break;
      default:
        System.out.println("Unhandled note action, " + type + "");
    }

  }

}
