/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import handling.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.Timer.EtcTimer;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.packet.PlayerShopPacket;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class HiredMerchant extends AbstractPlayerStore {

  public ScheduledFuture<?> schedule;
  private Set<String> blacklist;
  private List<ChatEntry> chatHistory;
  private int storeid;
  private long start;

  class ChatEntry {
    private String charName;
    private String text;

    public ChatEntry(String charName, String text) {
      super();
      this.charName = charName;
      this.text = text;
    }

    public String getCharName() {
      return charName;
    }

    public String getText() {
      return text;
    }

    @Override
    public String toString() {
      return getCharName() + " : " + getText();
    }

  }

  public HiredMerchant(MapleCharacter owner, int itemId, String desc) {
    super(owner, itemId, desc, "", 3);
    start = System.currentTimeMillis();
    blacklist = new LinkedHashSet<>();
    chatHistory = new ArrayList<>();
    this.schedule = EtcTimer.getInstance().schedule(new Runnable() {

      @Override
      public void run() {
        closeShop(true, true);
      }
    }, 1000 * 60 * 60 * 24);
  }

  public byte getShopType() {
    return IMaplePlayerShop.HIRED_MERCHANT;
  }

  public final void setStoreid(final int storeid) {
    this.storeid = storeid;
  }

  public List<MaplePlayerShopItem> searchItem(final int itemSearch) {
    final List<MaplePlayerShopItem> itemz = new LinkedList<MaplePlayerShopItem>();
    for (MaplePlayerShopItem item : items) {
      if (item.item.getItemId() == itemSearch && item.bundles > 0) {
        itemz.add(item);
      }
    }
    return itemz;
  }

  private static int getFee(int meso) {
    if (meso >= 100000000) {
      return (int) (meso * (0.97)); //Removing 3%
    }
    if (meso >= 25000000) {
      return (int) (meso * (0.975)); //Removing 2.5%
    }
    if (meso >= 10000000) {
      return (int) (meso * (0.98)); //Removing 2%
    }
    if (meso >= 5000000) {
      return (int) (meso * (0.985)); //Removing 1.5%
    }
    if (meso >= 1000000) {
      return (int) (meso * (0.991)); //Removing .9%
    }
    if (meso >= 100000) {
      return (int) (meso * (0.996)); //Removing .4%
    }

    return meso;
  }

  @Override
  public void buy(MapleClient c, int item, short quantity) {
    if (quantity == -1) {
      return;
    }
    final MaplePlayerShopItem pItem = items.get(item);
    final IItem shopItem = pItem.item;
    final IItem newItem = shopItem.copy();
    final short perbundle = newItem.getQuantity();
    newItem.setQuantity((short) (quantity * perbundle));

    byte flag = newItem.getFlag();

    if (ItemFlag.KARMA_EQ.check(flag)) {
      newItem.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
    } else if (ItemFlag.KARMA_USE.check(flag)) {
      newItem.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
    }

    if (MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner()) && MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
      pItem.bundles -= quantity; // Number remaining in the store
      bought.add(new BoughtItem(newItem.getItemId(), quantity, (pItem.price * quantity), c.getPlayer().getName()));


      final int mesos = (getMesos() + (pItem.price * quantity));
      int fee = getFee(mesos);
      setMesos(fee);
      c.getPlayer().gainMeso(-pItem.price * quantity, false);
      saveItems();
    } else {
      c.getPlayer().dropMessage(1, "Your inventory is full.");
      c.getSession().write(MaplePacketCreator.enableActions());
    }
  }

  @Override
  public void closeShop(boolean saveItems, boolean remove) {
    if (schedule != null) {
      schedule.cancel(false);
    }
    if (saveItems) {
      saveItems();
    }
    if (remove) {
      ChannelServer.getInstance(channel).removeMerchant(this);
      getMap().broadcastMessage(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
    }
    getMap().removeMapObject(this);
    schedule = null;
  }

  public int getTimeLeft() {
    return (int) ((System.currentTimeMillis() - start) / 1000);
  }

  public final int getStoreId() {
    return storeid;
  }

  @Override
  public MapleMapObjectType getType() {
    return MapleMapObjectType.HIRED_MERCHANT;
  }

  @Override
  public void sendDestroyData(MapleClient client) {
    if (isAvailable()) {
      client.getSession().write(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
    }
  }

  @Override
  public void sendSpawnData(MapleClient client) {
    if (isAvailable()) {
      client.getSession().write(PlayerShopPacket.spawnHiredMerchant(this));
    }
  }

  public final boolean isInBlackList(final String bl) {
    return blacklist.contains(bl);
  }

  public final void addBlackList(final String bl) {
    blacklist.add(bl);
  }

  public final void removeBlackList(final String bl) {
    blacklist.remove(bl);
  }

  public final void sendBlackList(final MapleClient c) {
    c.getSession().write(PlayerShopPacket.MerchantBlackListView(blacklist));
  }

  public final void sendVisitor(final MapleClient c) {
    c.getSession().write(PlayerShopPacket.MerchantVisitorView(visitors));
  }

  public void addChatHistory(String characterName, String text) {
    if (text != null && !text.isEmpty()) {
      ChatEntry entry = new ChatEntry(characterName, text);
      chatHistory.add(entry);
    }
  }

  public void sendChatHistory(final MapleClient c) {
    for (ChatEntry entry : chatHistory) {
      c.getSession().write(PlayerShopPacket.shopChat(entry.toString(), 1));
    }
  }

  public int getMeso() {
    return super.getMesos();
  }
}
