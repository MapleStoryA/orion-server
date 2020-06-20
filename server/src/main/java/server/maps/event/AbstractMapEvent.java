package server.maps.event;

import client.MapleCharacter;
import server.maps.MapleMap;

public abstract class AbstractMapEvent implements MapEvent {

  protected MapleMap map;

  private AbstractMapEvent() {

  }

  public AbstractMapEvent(MapleMap map) {
    this();
    this.map = map;
  }

  @Override
  public void onUserEnter(MapleCharacter c) {


  }

  @Override
  public void onUserExit(MapleCharacter c) {

  }

}
