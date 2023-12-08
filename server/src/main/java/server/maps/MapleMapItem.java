package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import tools.MaplePacketCreator;

@lombok.extern.slf4j.Slf4j
public class MapleMapItem extends AbstractMapleMapObject {

    private final ReentrantLock lock = new ReentrantLock();
    protected IItem item;
    protected MapleMapObject dropper;
    protected int character_ownerid, meso = 0, questid = -1;
    protected byte type;
    protected boolean pickedUp = false, playerDrop, randDrop = false;
    protected long nextExpiry = 0, nextFFA = 0;

    public MapleMapItem(
            IItem item,
            Point position,
            MapleMapObject dropper,
            MapleCharacter owner,
            byte type,
            boolean playerDrop) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.type = type;
        this.playerDrop = playerDrop;
    }

    public MapleMapItem(
            IItem item,
            Point position,
            MapleMapObject dropper,
            MapleCharacter owner,
            byte type,
            boolean playerDrop,
            int questid) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.type = type;
        this.playerDrop = playerDrop;
        this.questid = questid;
    }

    public MapleMapItem(
            int meso,
            Point position,
            MapleMapObject dropper,
            MapleCharacter owner,
            byte type,
            boolean playerDrop) {
        setPosition(position);
        this.item = null;
        this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.meso = meso;
        this.type = type;
        this.playerDrop = playerDrop;
    }

    public MapleMapItem(Point position, IItem item) {
        setPosition(position);
        this.item = item;
        this.character_ownerid = 0;
        this.type = 2;
        this.playerDrop = false;
        this.randDrop = true;
    }

    public final IItem getItem() {
        return item;
    }

    public void setItem(IItem z) {
        this.item = z;
    }

    public final int getQuest() {
        return questid;
    }

    public final int getItemId() {
        if (getMeso() > 0) {
            return meso;
        }
        return item.getItemId();
    }

    public final MapleMapObject getDropper() {
        return dropper;
    }

    public final int getOwner() {
        return character_ownerid;
    }

    public final int getMeso() {
        return meso;
    }

    public final boolean isPlayerDrop() {
        return playerDrop;
    }

    public final boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(final boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public byte getDropType() {
        return type;
    }

    public void setDropType(byte z) {
        this.type = z;
    }

    public final boolean isRandDrop() {
        return randDrop;
    }

    public final boolean canLoot(final MapleClient client) {
        if (questid <= 0) {
            return true;
        } else return client.getPlayer().getQuestStatus(questid) == 1;
    }

    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.ITEM;
    }

    @Override
    public void sendSpawnData(final MapleClient client) {
        if (canLoot(client)) {
            client.getSession()
                    .write(
                            MaplePacketCreator.dropItemFromMapObject(
                                    this, null, getPosition(), (byte) 2));
        }
    }

    @Override
    public void sendDestroyData(final MapleClient client) {
        client.getSession().write(MaplePacketCreator.removeItemFromMap(getObjectId(), 1, 0));
    }

    public Lock getLock() {
        return lock;
    }

    public void registerExpire(final long time) {
        nextExpiry = System.currentTimeMillis() + time;
    }

    public void registerFFA(final long time) {
        nextFFA = System.currentTimeMillis() + time;
    }

    public boolean shouldExpire(long now) {
        return !pickedUp && nextExpiry > 0 && nextExpiry < now;
    }

    public boolean shouldFFA(long now) {
        return !pickedUp && type < 2 && nextFFA > 0 && nextFFA < now;
    }

    public void expire(final MapleMap map) {
        pickedUp = true;
        map.broadcastMessage(MaplePacketCreator.removeItemFromMap(getObjectId(), 0, 0));
        map.removeMapObject(this);
        if (randDrop) {
            map.spawnRandDrop();
        }
    }
}
