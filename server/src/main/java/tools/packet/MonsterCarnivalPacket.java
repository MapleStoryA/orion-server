package tools.packet;

import client.MapleCharacter;
import handling.packet.SendPacketOpcode;
import server.MapleCarnivalParty;
import tools.data.output.OutPacket;

@lombok.extern.slf4j.Slf4j
public class MonsterCarnivalPacket {

    public static byte[] startMonsterCarnival(
            final MapleCharacter chr, final int enemyavailable, final int enemytotal) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_START.getValue());
        final MapleCarnivalParty friendly = chr.getCarnivalParty();
        packet.write(friendly.getTeam());
        packet.writeShort(chr.getAvailableCP());
        packet.writeShort(chr.getTotalCP());
        packet.writeShort(friendly.getAvailableCP());
        packet.writeShort(friendly.getTotalCP());
        packet.writeShort(enemyavailable);
        packet.writeShort(enemytotal);
        packet.writeLong(0);
        packet.writeShort(0);

        return packet.getPacket();
    }

    public static byte[] playerDiedMessage(String name, int lostCP, int team) { // CPQ
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_DIED.getValue());
        packet.write(team);
        packet.writeMapleAsciiString(name);
        packet.write(lostCP);

        return packet.getPacket();
    }

    public static byte[] CPUpdate(boolean party, int curCP, int totalCP, int team) {
        OutPacket packet = new OutPacket();
        if (!party) {
            packet.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        } else {
            packet.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
            packet.write(team);
        }
        packet.writeShort(curCP);
        packet.writeShort(totalCP);

        return packet.getPacket();
    }

    public static byte[] playerSummoned(String name, int tab, int number) {
        OutPacket packet = new OutPacket();

        packet.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        packet.write(tab);
        packet.write(number);
        packet.writeMapleAsciiString(name);

        return packet.getPacket();
    }
}
