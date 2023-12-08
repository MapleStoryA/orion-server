package database;

import java.util.List;
import lombok.Getter;

@Getter
public class CharacterListResult {

    private List<CharacterData> characters;

    public CharacterListResult(List<CharacterData> characters) {
        this.characters = characters;
    }
}
