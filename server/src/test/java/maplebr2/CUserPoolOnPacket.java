package maplebr2;

import handling.SendPacketOpcode;
import org.junit.Test;

public class CUserPoolOnPacket {

  private static String test(short a3) {
    if (a3 == 177) {
      return " CUserPool::OnUserEnterField((int)a1, a2, (int)Format";
    } else if (a3 == 178) {
      return " CUserPool::OnUserLeaveField((int)Format";
    } else if (a3 < 179 || a3 > 212) {
      if (a3 < 213 || a3 > 232) {
        if (a3 >= 233 && a3 <= 274) {
          return " CUserPool::OnUserLocalPacket((int)a1, a3 - 178, a3, (wchar_t *)Format";
        }
      } else {
        return " CUserPool::OnUserRemotePacket(a3, (int)Format";
      }
    } else {
      return " (unsigned int)CUserPool::OnUserCommonPacket(a1, a3, Format";
    }
    return "";
  }

  @Test
  public void testPacket() {
    for (SendPacketOpcode opcode : SendPacketOpcode.values()) {
      if (opcode == SendPacketOpcode.SHOW_STATUS_INFO) {
        System.out.println(opcode + " : " + test(opcode.getValue()));
      }
    }
  }

}
