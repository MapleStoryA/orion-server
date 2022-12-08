package database.state;

import lombok.Getter;

import java.util.List;

@Getter
public class CharacterListResult {

    private List<CharacterData> characters;

    public CharacterListResult(List<CharacterData> characters) {
        this.characters = characters;
    }
}
