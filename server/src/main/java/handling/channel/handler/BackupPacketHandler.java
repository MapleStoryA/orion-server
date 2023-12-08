package handling.channel.handler;

import client.MapleClient;
import handling.MaplePacketHandler;
import tools.HexTool;
import tools.data.input.InPacket;

@lombok.extern.slf4j.Slf4j
public class BackupPacketHandler implements MaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {

        int nCallType = packet.readShort();
        int dwErrorCode = packet.readInt();
        int dwBackupBufferSize = packet.readShort();
        byte[] buffer = packet.read(dwBackupBufferSize);
        String.format(
                "Call type: %s ErrorCode: %s, Size: %s Buffer: %s ",
                nCallType, dwErrorCode, dwBackupBufferSize, HexTool.toStringFromAscii(buffer));
    }

    @Override
    public boolean validateState(MapleClient c) {
        return false;
    }
}
