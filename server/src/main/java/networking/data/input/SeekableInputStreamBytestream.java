package networking.data.input;

import java.io.IOException;

/**
 * Provides an abstract interface to a stream of bytes. This stream can be seeked.
 */
public interface SeekableInputStreamBytestream extends ByteInputStream {

    /**
     * Seeks the stream by the specified offset.
     *
     * @param offset Number of bytes to seek.
     * @throws IOException
     */
    void seek(long offset) throws IOException;

    /**
     * Gets the current position of the stream.
     *
     * @return The stream position as a long integer.
     * @throws IOException
     */
    long getPosition() throws IOException;

    String toString(final boolean b);
}
