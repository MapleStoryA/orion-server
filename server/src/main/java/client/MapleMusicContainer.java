package client;

import java.util.HashMap;

/**
 * @author n. martel
 */
public class MapleMusicContainer {

  private HashMap<Integer, Byte> notecontainer = new HashMap<>();

  public void addNote(int songid, byte note) {
    notecontainer.put(songid, note);
  }

  public void removeNote(int songid, byte note) {
    notecontainer.remove(note);
  }

  public int iterateNote(int songid) {
    for (Byte i : notecontainer.values()) {
      if (i.equals(songid)) {
        return notecontainer.get(i);
      }
    }
    return 0;
  }


}
