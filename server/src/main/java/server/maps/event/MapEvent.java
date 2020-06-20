package server.maps.event;

import client.MapleCharacter;

public interface MapEvent {

  MapEvent noOperationUserEnter = new MapEvent() {

    @Override
    public void onUserExit(MapleCharacter c) {


    }

    @Override
    public void onUserEnter(MapleCharacter c) {

    }
  };

  void onUserEnter(MapleCharacter c);


  void onUserExit(MapleCharacter c);


}
