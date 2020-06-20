package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.handler.utils.BBSHandlerUtils;
import tools.data.input.SeekableLittleEndianAccessor;

public class BbsOperationHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    if (c.getPlayer().getGuildId() <= 0) {
      return; // expelled while viewing bbs or hax
    }
    int localthreadid = 0;
    final byte action = slea.readByte();
    switch (action) {
      case 0: // start a new post
        final boolean bEdit = slea.readByte() > 0;
        if (bEdit) {
          localthreadid = slea.readInt();
        }
        final boolean bNotice = slea.readByte() > 0;
        final String title = BBSHandlerUtils.correctLength(slea.readMapleAsciiString(), 25);
        String text = BBSHandlerUtils.correctLength(slea.readMapleAsciiString(), 600);
        final int icon = slea.readInt();
        if (icon >= 0x64 && icon <= 0x6a) {
          if (!c.getPlayer().haveItem(5290000 + icon - 0x64, 1, false, true)) {
            return; // hax, using an nx icon that s/he doesn't have
          }
        } else if (icon < 0 || icon > 2) {
          return; // hax, using an invalid icon
        }
        if (!bEdit) {
          BBSHandlerUtils.newBBSThread(c, title, text, icon, bNotice);
        } else {
          BBSHandlerUtils.editBBSThread(c, title, text, icon, localthreadid);
        }
        break;
      case 1: // delete a thread
        localthreadid = slea.readInt();
        BBSHandlerUtils.deleteBBSThread(c, localthreadid);
        break;
      case 2: // list threads
        int start = slea.readInt();
        BBSHandlerUtils.listBBSThreads(c, start * 10);
        break;
      case 3: // list thread + reply, followed by id (int)
        localthreadid = slea.readInt();
        BBSHandlerUtils.displayThread(c, localthreadid);
        break;
      case 4: // reply
        localthreadid = slea.readInt();
        text = BBSHandlerUtils.correctLength(slea.readMapleAsciiString(), 25);
        BBSHandlerUtils.newBBSReply(c, localthreadid, text);
        break;
      case 5: // delete reply
        localthreadid = slea.readInt();
        int replyid = slea.readInt();
        BBSHandlerUtils.deleteBBSReply(c, localthreadid, replyid);
        break;
    }

  }

}
