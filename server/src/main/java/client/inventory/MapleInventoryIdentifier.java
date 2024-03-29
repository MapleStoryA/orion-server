package client.inventory;

import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MapleInventoryIdentifier implements Serializable {

    private static final long serialVersionUID = 21830921831301L;
    private static final MapleInventoryIdentifier instance = new MapleInventoryIdentifier();
    private final AtomicInteger runningUID;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public MapleInventoryIdentifier() {
        this.runningUID = new AtomicInteger(0);
        getNextUniqueId();
    }

    public static int getInstance() {
        return instance.getNextUniqueId();
    }

    public int getNextUniqueId() {
        if (grabRunningUID() <= 0) {
            setRunningUID(initUID());
        }
        incrementRunningUID();
        return grabRunningUID();
    }

    public int grabRunningUID() {
        readLock.lock();
        try {
            return runningUID.get();
        } finally {
            readLock.unlock();
        }
    }

    public void incrementRunningUID() {
        setRunningUID(grabRunningUID() + 1);
    }

    public void setRunningUID(int rUID) {
        if (rUID < grabRunningUID()) {
            return;
        }
        writeLock.lock();
        try {
            runningUID.set(rUID);
        } finally {
            writeLock.unlock();
        }
    }

    public int initUID() {
        int ret = 0;
        if (grabRunningUID() > 0) {
            return grabRunningUID();
        }
        try (var con = DatabaseConnection.getConnection()) {
            int[] ids = new int[4];
            PreparedStatement ps = con.prepareStatement("SELECT MAX(uniqueid) FROM inventoryitems");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ids[0] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT MAX(petid) FROM pets");
            rs = ps.executeQuery();
            if (rs.next()) {
                ids[1] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT MAX(ringid) FROM rings");
            rs = ps.executeQuery();
            if (rs.next()) {
                ids[2] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT MAX(partnerringid) FROM rings");
            rs = ps.executeQuery();
            if (rs.next()) {
                ids[3] = rs.getInt(1) + 1;
            }
            rs.close();
            ps.close();

            for (int i = 0; i < ids.length; i++) {
                if (ids[i] > ret) {
                    ret = ids[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
