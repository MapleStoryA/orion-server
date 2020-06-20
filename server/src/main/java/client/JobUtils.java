package client;

import client.inventory.IItem;

public class JobUtils {

  public static final int MASTERY_BOOK_DB_START = 5620000;
  public static final int MASTERY_BOOK_DB_END = 5620005;

  public static boolean isDualbladeCashMasteryItem(int item) {
    return item >= MASTERY_BOOK_DB_START && item <= MASTERY_BOOK_DB_END;
  }

  public static boolean isDualbladeCashMasteryItem(IItem item) {
    return isDualbladeCashMasteryItem(item.getItemId());
  }

}
