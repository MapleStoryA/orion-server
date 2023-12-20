package networking.data.output;

import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Provides a generic writer of a little-endian sequence of bytes.
 *
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
@lombok.extern.slf4j.Slf4j
public class GenericLittleEndianWriter implements LittleEndianWriter {

    // See http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html
    private static final Charset ASCII = StandardCharsets.US_ASCII; // ISO-8859-1, UTF-8
    private ByteOutputStream bos;

    /** Class constructor - Protected to prevent instantiation with no arguments. */
    protected GenericLittleEndianWriter() {
        // Blah!
    }

    /**
     * Class constructor - only this one can be used.
     *
     * @param bos The stream to wrap this objecr around.
     */
    public GenericLittleEndianWriter(final ByteOutputStream bos) {
        this.bos = bos;
    }

    /**
     * Sets the byte-output stream for this instance of the object.
     *
     * @param bos The new output stream to set.
     */
    protected void setByteOutputStream(final ByteOutputStream bos) {
        this.bos = bos;
    }

    /**
     * Write the number of zero bytes
     *
     * @param b The bytes to write.
     */
    @Override
    public final void writeZeroBytes(final int i) {
        for (int x = 0; x < i; x++) {
            bos.writeByte((byte) 0);
        }
    }

    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
    @Override
    public final void write(final byte[] b) {
        for (int x = 0; x < b.length; x++) {
            bos.writeByte(b[x]);
        }
    }

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
    @Override
    public final void write(final byte b) {
        bos.writeByte(b);
    }

    @Override
    public final void write(final int b) {
        bos.writeByte((byte) b);
    }

    /**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     */
    @Override
    public final void writeShort(final short i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
    }

    @Override
    public final void writeShort(final int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
    }

    /**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     */
    @Override
    public final void writeInt(final int i) {
        bos.writeByte((byte) (i & 0xFF));
        bos.writeByte((byte) ((i >>> 8) & 0xFF));
        bos.writeByte((byte) ((i >>> 16) & 0xFF));
        bos.writeByte((byte) ((i >>> 24) & 0xFF));
    }

    /**
     * Writes an ASCII string the the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public final void writeAsciiString(final String s) {
        write(s.getBytes(ASCII));
    }

    @Override
    public final void writeAsciiString(String s, final int max) {
        if (s.length() > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        for (int i = s.length(); i < max; i++) {
            write(0);
        }
    }

    /**
     * Writes a maple-convention ASCII string to the stream.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
    @Override
    public final void writeMapleAsciiString(final String s) {
        writeShort((short) s.length());
        writeAsciiString(s);
    }

    /**
     * Writes a 2D 4 byte position information
     *
     * @param s The Point position to write.
     */
    @Override
    public final void writePos(final Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    /**
     * Write a long integer to the stream.
     *
     * @param l The long integer to write.
     */
    @Override
    public final void writeLong(final long l) {
        bos.writeByte((byte) (l & 0xFF));
        bos.writeByte((byte) ((l >>> 8) & 0xFF));
        bos.writeByte((byte) ((l >>> 16) & 0xFF));
        bos.writeByte((byte) ((l >>> 24) & 0xFF));
        bos.writeByte((byte) ((l >>> 32) & 0xFF));
        bos.writeByte((byte) ((l >>> 40) & 0xFF));
        bos.writeByte((byte) ((l >>> 48) & 0xFF));
        bos.writeByte((byte) ((l >>> 56) & 0xFF));
    }

    @Override
    public void writeBool(boolean bNext) {
        write(bNext ? 1 : 0);
    }
}
