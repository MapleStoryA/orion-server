package constants;

import client.MapleJob;
import client.inventory.IItem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobUtils {

    public static final int MASTERY_BOOK_DB_START = 5620000;
    public static final int MASTERY_BOOK_DB_END = 5620005;

    public static boolean isDualbladeCashMasteryItem(int item) {
        return item >= MASTERY_BOOK_DB_START && item <= MASTERY_BOOK_DB_END;
    }

    public static boolean isDualbladeCashMasteryItem(IItem item) {
        return isDualbladeCashMasteryItem(item.getItemId());
    }

    public static MapleJob mapTypeToJob(int type) {
        short jobId;
        switch (type) {
            case 0:
                jobId = 1000;
                break;
            case 1:
                jobId = 0;
                break;
            case 3:
                jobId = 2001;
                break;
            case 4:
                jobId = 3000;
                break;
            default:
                jobId = 2000;
                break;
        }
        return MapleJob.getById(jobId);
    }
}
