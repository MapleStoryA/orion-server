package tools;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class InsertBuilder {

  private final Set<Entry> values = new LinkedHashSet<>();
  private final Set<String> names = new LinkedHashSet<>();
  private String table;

  enum FieldType {
    STRING,
    DOUBLE,
    INTEGER
  }

  final class Entry {
    private final String value;
    private final FieldType type;


    Entry(Object o) {
      type = getTypeFromObject(o);
      value = o.toString();

    }

    Entry(String value, FieldType type) {
      this.value = value;
      this.type = type;
    }

    private FieldType getTypeFromObject(Object o) {
      if (o instanceof Integer) {
        return FieldType.INTEGER;
      } else if (o instanceof Double) {
        return FieldType.DOUBLE;
      } else {
        return FieldType.STRING;
      }
    }

    public String toString() {
      switch (type) {
        case STRING:
          return "'" + value + "'";
        case DOUBLE:
        case INTEGER:
        default:
          return value;
      }
    }
  }


  private InsertBuilder() {

  }

  public static InsertBuilder create() {
    return new InsertBuilder();
  }

  public InsertBuilder forTable(String table) {
    this.table = table;
    return this;
  }

  public InsertBuilder newIntValue(String name, Integer value) {
    names.add(name);
    values.add(new Entry(value));
    return this;
  }

  public InsertBuilder newStringValue(String name, String value) {
    names.add(name);
    values.add(new Entry(value));
    return this;
  }

  public InsertBuilder newDoubleValue(String name, Double value) {
    names.add(name);
    values.add(new Entry(value));
    return this;
  }

  private <T> void writeIterator(Iterator<T> it, StringBuilder builder, Class<T> capture) {
    for (; ; ) {
      T current = it.next();
      builder.append(current);
      if (!it.hasNext()) {
        break;
      }
      builder.append(", ");
    }
  }

  public String toString() {
    StringBuilder builder = new StringBuilder(String.format("INSERT INTO %s (", table));
    writeIterator(names.iterator(), builder, String.class);
    builder.append(") VALUES (");
    writeIterator(values.iterator(), builder, Entry.class);
    builder.append(");");
    return builder.toString();


  }


}
