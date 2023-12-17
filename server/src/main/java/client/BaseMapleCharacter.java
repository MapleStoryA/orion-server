package client;

import scripting.v1.event.Event;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.MapleMapObject;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class BaseMapleCharacter extends AbstractAnimatedMapleMapObject {

    private Set<MapleMapObject> visibleMapObjects;
    private ReentrantReadWriteLock visibleMapObjectsLock;
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
}
