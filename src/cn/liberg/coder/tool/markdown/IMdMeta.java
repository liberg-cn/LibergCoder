package cn.liberg.coder.tool.markdown;

import java.io.BufferedWriter;
import java.io.IOException;

public interface IMdMeta {

    public static final int TYPE_PARAGRAPH = 0;//普通段落
    public static final int TYPE_HEADING = 1;
    public static final int TYPE_CODE_BLOCK = 2;
    public static final int TYPE_TABLE = 3;
    public static final int TYPE_QUOTE_BLOCK = 7;
    public void writeTo(BufferedWriter bw) throws IOException;
    public int getType();
    public String toHtml();
}
