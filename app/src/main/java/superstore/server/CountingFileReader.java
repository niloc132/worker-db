package superstore.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * Created by colin on 3/25/17.
 */
public class CountingFileReader extends FileReader {
    private long position;
    public CountingFileReader(File file) throws FileNotFoundException {
        super(file);
    }


    @Override
    public int read() throws IOException {
        int read = super.read();
        if (read != -1) {
            position++;
        }
        return read;
    }

    @Override
    public int read(char[] cbuf, int offset, int length) throws IOException {
        int read = super.read(cbuf, offset, length);
        if (read != -1) {
            position += read;
        }
        return read;
    }

    public long getPosition() {
        return position;
    }
}
