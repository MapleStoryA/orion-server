/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.skill;

import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import networking.data.output.OutPacket;

@Slf4j
public class ExtendedSPTable {

    private final ExtendedSPType SPType; // since resistance etc use this too, future compat
    private final HashMap<Integer, Integer> SPTable;
    private final int baseJob;

    public ExtendedSPTable(HashMap<Integer, Integer> SPTable, int jobID) {
        this.SPTable = SPTable;
        this.SPType = ExtendedSPType.getFromJobID(jobID);
        if (SPType == ExtendedSPType.EVAN) {
            baseJob = 2210;
        } else {
            baseJob = 3000; // I don't know enough about it at this stage
        }
    }

    public ExtendedSPTable(int jobID) // brand new one
            {
        SPTable = new HashMap<>();
        for (int i = 1; i < 11; i++) {
            SPTable.put(i, 0);
        }

        this.SPType = ExtendedSPType.getFromJobID(jobID);

        if (SPType == ExtendedSPType.EVAN) {
            baseJob = 2210;
        } else {
            baseJob = 3000; // I don't know enough about it at this stage
        }
    }

    public int getSPFromJobID(int jobID) {
        if (jobID == 2200) {
            return SPTable.get(1);
        } else if (jobID >= 2210 && jobID <= 2218) // evan
        {
            return SPTable.get((jobID - baseJob) + 2);
        }
        return -1; // unsupported atm
    }

    public int getSPFromSlotID(int slotID) {
        return SPTable.get(slotID);
    }

    public void updateSPFromSlotID(int slotID, int newSP) {
        SPTable.remove(slotID);
        SPTable.put(slotID, newSP);
    }

    public void updateSPFromJobID(int jobID, int newSP) {
        if (jobID == 2200) {
            SPTable.remove(1);
            SPTable.put(1, newSP);
        } else if (jobID >= 2210 && jobID <= 2218) // evan
        {
            SPTable.remove((jobID - baseJob) + 2);
            SPTable.put((jobID - baseJob) + 2, newSP);
        }
    }

    public void addSPFromJobID(int jobID, int delta) {
        updateSPFromJobID(jobID, getSPFromJobID(jobID) + delta);
    }

    public void addSPFromSlotID(int slot, int delta) {
        updateSPFromSlotID(slot, getSPFromSlotID(slot) + delta);
    }

    private int getNonZeroSize() {
        int res = 0;
        for (Integer i : SPTable.values()) {
            if (i > 0) {
                res++;
            }
        }
        return res;
    }

    public void addSPData(OutPacket packet) {
        packet.write(getNonZeroSize());
        for (int i = 1; i < SPTable.size() + 1; i++) {
            if (SPTable.get(i) > 0) {
                packet.write(i);
                packet.write(SPTable.get(i));
            }
        }
    }
}
