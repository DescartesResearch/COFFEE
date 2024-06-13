package tools.descartes.coffee.controller.monitoring.database.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class StorageTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** number of written bytes */
    public long writtenBytes;

    /** total write time in milliseconds */
    public long writeTimeMillis;

    /** number of read bytes */
    public long readBytes;

    /** total read time in milliseconds */
    public long readTimeMillis;

    protected StorageTime() {
    }

    public StorageTime(long writtenBytes, long writeTimeMillis, long readBytes, long readTimeMillis) {
        this.writtenBytes = writtenBytes;
        this.writeTimeMillis = writeTimeMillis;
        this.readBytes = readBytes;
        this.readTimeMillis = readTimeMillis;
    }

    @Override
    public String toString() {
        return String.format(
                "StorageTime[id=%d, writtenBytes='%d', writeTimeMillis=%d, readBytes=%d, readTimeMillis='%d']",
                id, writtenBytes, writeTimeMillis, readBytes, readTimeMillis);
    }

    public long getWrittenBytes() {
        return writtenBytes;
    }

    public long getWriteTimeMillis() {
        return writeTimeMillis;
    }

    public long getReadBytes() {
        return readBytes;
    }

    public long getReadTimeMillis() {
        return readTimeMillis;
    }
}
