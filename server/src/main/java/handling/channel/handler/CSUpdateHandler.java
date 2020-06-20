package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.cashshop.handler.CashShopOperationUtils;
import tools.data.input.SeekableLittleEndianAccessor;

public class CSUpdateHandler extends AbstractMaplePacketHandler {

  @Override
  public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    CashShopOperationUtils.CSUpdate(c);

  }

}
