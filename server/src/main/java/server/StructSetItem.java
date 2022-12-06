package server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@lombok.extern.slf4j.Slf4j
public class StructSetItem {

    public byte completeCount, setItemID;
    public Map<Integer, SetItem> items = new LinkedHashMap<Integer, SetItem>();
    public List<Integer> itemIDs = new ArrayList<Integer>();

    public Map<Integer, SetItem> getItems() {
        return new LinkedHashMap<Integer, SetItem>(items);
    }

    public static class SetItem {

        public int incPDD, incMDD, incSTR, incDEX, incINT, incLUK, incACC, incPAD, incMAD, incSpeed, incMHP, incMMP;
    }
}
