package cn.liberg.coder.tool.core;

import java.io.IOException;

public interface ILineReader {
    String next() throws IOException;
    String next(boolean trim) throws IOException;
    default boolean skipEmptyLine() {
        return false;
    }
}
