package server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@lombok.extern.slf4j.Slf4j
public class StructSetItem {

    private byte completeCount;
    private byte setItemID;
    private Map<Integer, SetItem> items = new LinkedHashMap<Integer, SetItem>();
    private List<Integer> itemIDs = new ArrayList<Integer>();

    public Map<Integer, SetItem> getItems() {
        return new LinkedHashMap<Integer, SetItem>(items);
    }

    public byte getCompleteCount() {
        return completeCount;
    }

    public void setCompleteCount(byte completeCount) {
        this.completeCount = completeCount;
    }

    public byte getSetItemID() {
        return setItemID;
    }

    public void setSetItemID(byte setItemID) {
        this.setItemID = setItemID;
    }

    public void setItems(Map<Integer, SetItem> items) {
        this.items = items;
    }

    public List<Integer> getItemIDs() {
        return itemIDs;
    }

    public void setItemIDs(List<Integer> itemIDs) {
        this.itemIDs = itemIDs;
    }

    public static class SetItem {

        public int incPDD,
                incMDD,
                incSTR,
                incDEX,
                incINT,
                incLUK,
                incACC,
                incPAD,
                incMAD,
                incSpeed,
                incMHP,
                incMMP;
    }
}
