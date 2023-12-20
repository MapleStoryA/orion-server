package client;

import lombok.Getter;
import server.maps.SavedLocationType;

public class SavedLocations {
    private int[] savedLocations;

    @Getter
    private boolean changed;

    public SavedLocations() {
        savedLocations = new int[SavedLocationType.values().length];
        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = -1;
        }
    }

    public int getSavedLocation(int index) {
        if (index > savedLocations.length) {
            return -1;
        }
        return savedLocations[index];
    }

    public int getSavedLocation(SavedLocationType type) {
        return savedLocations[type.getValue()];
    }

    public void saveLocation(SavedLocationType type, int map) {
        savedLocations[type.getValue()] = map;
        changed = true;
    }

    public void saveLocation(SavedLocationType type, MapleCharacter character) {
        saveLocation(type, character.getMapId());
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.getValue()] = -1;
        changed = true;
    }
}
