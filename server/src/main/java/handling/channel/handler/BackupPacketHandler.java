package handling.channel.handler;

import client.MapleClient;
import lombok.extern.slf4j.Slf4j;
import networking.data.input.InPacket;
import networking.packet.MaplePacketHandler;
import tools.helper.HexTool;

@Slf4j
public class BackupPacketHandler implements MaplePacketHandler {

    @Override
    public void handlePacket(InPacket packet, MapleClient c) {

        int nCallType = packet.readShort();
        int dwErrorCode = packet.readInt();
        int dwBackupBufferSize = packet.readShort();
        byte[] buffer = packet.read(dwBackupBufferSize);
        String error = String.format(
                "Call type: %s ErrorCode: %s, Size: %s Buffer: %s ",
                nCallType, dwErrorCode, dwBackupBufferSize, HexTool.toStringFromAscii(buffer));
        log.info(error);
    }

    @Override
    public boolean validateState(MapleClient c) {
        return false;
    }
}
