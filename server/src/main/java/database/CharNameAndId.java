package database;

import lombok.Getter;

@Getter
public final class CharNameAndId {

    public final String name;
    public final int id;

    public CharNameAndId(final String name, final int id) {
        super();
        this.name = name;
        this.id = id;
    }
}
