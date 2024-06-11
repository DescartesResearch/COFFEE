package tools.descartes.coffee.shared;

public class StorageData {

    /** number of written bytes */
    private long[] writtenBytes;

    /** total write time in milliseconds */
    private long[] writeTimeMillis;

    /** number of read bytes */
    private long[] readBytes;

    /** total read time in milliseconds */
    private long[] readTimeMillis;


    public StorageData(long[] writtenBytes, long[] writeTimeMillis, long[] readBytes, long[] readTimeMillis) {
        this.writtenBytes = writtenBytes;
        this.writeTimeMillis = writeTimeMillis;
        this.readBytes = readBytes;
        this.readTimeMillis = readTimeMillis;
    }

    // Needed for JSON deserialization
    public StorageData() {

    }

    public long[] getWrittenBytes() {
        return writtenBytes;
    }

    public void setWrittenBytes(long[] writtenBytes) {
        this.writtenBytes = writtenBytes;
    }

    public long[] getWriteTimeMillis() {
        return writeTimeMillis;
    }

    public void setWriteTimeMillis(long[] writeTimeMillis) {
        this.writeTimeMillis = writeTimeMillis;
    }

    public long[] getReadBytes() {
        return readBytes;
    }

    public void setReadBytes(long[] readBytes) {
        this.readBytes = readBytes;
    }

    public long[] getReadTimeMillis() {
        return readTimeMillis;
    }

    public void setReadTimeMillis(long[] readTimeMillis) {
        this.readTimeMillis = readTimeMillis;
    }
}
