package database;

import java.util.List;
import lombok.Getter;

@Getter
public class CharacterListResult {

    private final List<CharacterData> characters;

    public CharacterListResult(List<CharacterData> characters) {
        this.characters = characters;
    }
}
