package networking.data.input;

/**
 * Represents an abstract stream of bytes.
 */
public interface ByteInputStream {

    /**
     * Reads the next byte off the stream.
     *
     * @return The next byte as an integer.
     */
    int readByte();

    /**
     * Gets the number of bytes read from the stream.
     *
     * @return The number of bytes as a long integer.
     */
    long getBytesRead();

    /**
     * Gets the number of bytes still left for reading.
     *
     * @return The number of bytes as a long integer.
     */
    long available();

    String toString(final boolean b);
}
