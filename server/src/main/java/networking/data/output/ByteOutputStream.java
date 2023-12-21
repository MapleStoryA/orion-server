package networking.data.output;

/**
 * Provides an interface to an output stream of bytes.
 *
 */
public interface ByteOutputStream {

    /**
     * Writes a byte to the stream.
     *
     * @param b The byte to write.
     */
    void writeByte(final byte b);
}
