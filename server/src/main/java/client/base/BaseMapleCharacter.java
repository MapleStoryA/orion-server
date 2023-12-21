package client.base;

import client.MapleClient;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Getter;
import lombok.Setter;
import scripting.v1.event.Event;
import server.maplevar.MapleVar;
import server.maplevar.SimpleMapleVar;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.MapleMap;
import server.maps.MapleMapObject;

public abstract class BaseMapleCharacter extends AbstractAnimatedMapleMapObject {

    @Setter
    @Getter
    protected MapleClient client;

    @Getter
    protected int id;

    @Getter
    @Setter
    private String name;

    protected MapleMap map;

    private final Set<MapleMapObject> visibleMapObjects;
    private final ReentrantReadWriteLock visibleMapObjectsLock;
    private Event event;

    public BaseMapleCharacter() {
        visibleMapObjects = new LinkedHashSet<>();
        visibleMapObjectsLock = new ReentrantReadWriteLock();
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.add(mo);
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.remove(mo);
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        visibleMapObjectsLock.readLock().lock();
        try {
            return visibleMapObjects.contains(mo);
        } finally {
            visibleMapObjectsLock.readLock().unlock();
        }
    }

    public Collection<MapleMapObject> getAndWriteLockVisibleMapObjects() {
        visibleMapObjectsLock.writeLock().lock();
        return visibleMapObjects;
    }

    public void unlockWriteVisibleMapObjects() {
        visibleMapObjectsLock.writeLock().unlock();
    }

    public void leaveMap() {
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.clear();
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public Event getEvent() {
        return event;
    }

    public void registerEvent(Event event) {
        this.event = event;
    }

    public void leaveEvent() {
        this.event = null;
    }

    public void set(String key, String value) {
        MapleVar var = new SimpleMapleVar(this);
        var.set(key, value);
    }

    public String get(String key) {
        MapleVar var = new SimpleMapleVar(this);
        return var.get(key);
    }

    /**
     * Oid of players is always = the cid
     */
    @Override
    public int getObjectId() {
        return getId();
    }

    /**
     * Throws unsupported operation exception, oid of players is read only
     */
    @Override
    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getName() + " at " + getPosition() + " in map: " + map.getId();
    }
}
