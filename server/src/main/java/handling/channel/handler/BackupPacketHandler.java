package handling.channel.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import tools.HexTool;
import tools.data.input.SeekableLittleEndianAccessor;

public class BackupPacketHandler implements MaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

    int nCallType = slea.readShort();
    int dwErrorCode = slea.readInt();
    int dwBackupBufferSize = slea.readShort();
    byte[] buffer = slea.read(dwBackupBufferSize);
    String.format("Call type: %s ErrorCode: %s, Size: %s Buffer: %s ",
        nCallType, dwErrorCode, dwBackupBufferSize, HexTool.toStringFromAscii(buffer));

  }

  @Override
  public boolean validateState(MapleClient c) {
    return false;
  }

}
