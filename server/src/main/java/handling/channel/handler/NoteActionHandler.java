package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import networking.packet.AbstractMaplePacketHandler;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class NoteActionHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        final byte type = packet.readByte();

        switch (type) {
            case 0:
                String name = packet.readMapleAsciiString();
                String msg = packet.readMapleAsciiString();
                boolean fame = packet.readByte() > 0;
                packet.readInt(); // 0?
                IItem itemz = chr.getCashInventory().findByCashId((int) packet.readLong());
                if (itemz == null
                        || !itemz.getGiftFrom().equalsIgnoreCase(name)
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
                final byte num = packet.readByte();
                packet.skip(2);

                for (int i = 0; i < num; i++) {
                    final int id = packet.readInt();
                    chr.deleteNote(id, packet.readByte() > 0 ? 1 : 0);
                }
                break;
            default:
                log.info("Unhandled note action, " + type + "");
        }
    }
}
