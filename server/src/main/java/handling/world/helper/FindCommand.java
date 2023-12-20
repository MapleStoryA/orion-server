package handling.world.helper;

import handling.world.WorldServer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FindCommand {

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final HashMap<Integer, Integer> idToChannel = new HashMap<>();
    private static final HashMap<String, Integer> nameToChannel = new HashMap<>();

    public static boolean isConnected(String charName) {
        return FindCommand.findChannel(charName) > 0;
    }

    public static void register(int id, String name, int channel) {
        lock.writeLock().lock();
        try {
            idToChannel.put(id, channel);
            nameToChannel.put(name.toLowerCase(), channel);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void forceDeregister(int id) {
        lock.writeLock().lock();
        try {
            idToChannel.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void forceDeregister(String id) {
        lock.writeLock().lock();
        try {
            nameToChannel.remove(id.toLowerCase());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void forceDeregister(int id, String name) {
        lock.writeLock().lock();
        try {
            idToChannel.remove(id);
            nameToChannel.remove(name.toLowerCase());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static int findChannel(int id) {
        Integer ret;
        lock.readLock().lock();
        try {
            ret = idToChannel.get(id);
        } finally {
            lock.readLock().unlock();
        }
        if (ret != null) {
            if (ret != -10 && ret != -20 && WorldServer.getInstance().getChannel(ret) == null) { // wha
                forceDeregister(id);
                return -1;
            }
            return ret;
        }
        return -1;
    }

    public static int findChannel(String st) {
        Integer ret;
        lock.readLock().lock();
        try {
            ret = nameToChannel.get(st.toLowerCase());
        } finally {
            lock.readLock().unlock();
        }
        if (ret != null) {
            if (ret != -10 && ret != -20 && WorldServer.getInstance().getChannel(ret) == null) { // wha
                forceDeregister(st);
                return -1;
            }
            return ret;
        }
        return -1;
    }

    public static CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<CharacterIdChannelPair> foundsChars = new ArrayList<>(characterIds.length);
        for (int i : characterIds) {
            int channel = findChannel(i);
            if (channel > 0) {
                foundsChars.add(new CharacterIdChannelPair(i, channel));
            }
        }
        Collections.sort(foundsChars);
        return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
    }
}
