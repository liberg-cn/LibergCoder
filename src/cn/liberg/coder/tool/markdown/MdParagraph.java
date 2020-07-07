package cn.liberg.coder.tool.markdown;

import cn.liberg.coder.tool.core.Formats;

import java.io.BufferedWriter;
import java.io.IOException;

public class MdParagraph implements IMdMeta {
    private String line;

    public MdParagraph(String line) {
        this.line = line;
    }
    
    @Override
    public void writeTo(BufferedWriter bw) throws IOException {
        bw.write(Formats.NL);
        bw.write(line);
        bw.write(Formats.NL2);
    }

    @Override
    public int getType() {
        return TYPE_PARAGRAPH;
    }

    @Override
    public String toHtml() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("<p>");
        new MdTextHandler(line).buildTo(sb);
        sb.append("</p>");
        return sb.toString();
    }
}
