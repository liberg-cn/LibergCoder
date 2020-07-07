package cn.liberg.coder.tool.markdown;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MdQuoteBlock implements IMdMeta {
    private static final String PREFIX = "> ";
    private String startLine;
    private List<String> otherLines;

    public static MdQuoteBlock parse(String line, ILineReader reader) throws IOException {
        if (line.startsWith(PREFIX)) {
            MdQuoteBlock meta = new MdQuoteBlock();
            meta.otherLines = new ArrayList<>();
            meta.startLine = line;
            for (; ; ) {
                line = reader.next(true);
                if (line == null || reader.skipEmptyLine()) {
                    break;
                }
                meta.otherLines.add(line);
            }
            return meta;
        } else {
            return null;
        }
    }

    @Override
    public void writeTo(BufferedWriter bw) throws IOException {
        bw.write(startLine + Formats.NL);
        for (String line : otherLines) {
            bw.write(line + Formats.NL);
        }
        bw.write(Formats.NL);
    }

    @Override
    public int getType() {
        return TYPE_QUOTE_BLOCK;
    }

    @Override
    public String toHtml() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("<div class=\"lib-quote\">");
        buildHtmlTo(sb, startLine.substring(2).trim());
        for(String line : otherLines) {
            buildHtmlTo(sb, line.substring(1).trim());
        }
        sb.append("</div>");
        return sb.toString();
    }

    private void buildHtmlTo(StringBuilder sb, String line) {
        if(line.length()==0) {
            sb.append("<br>");
        } else {
            sb.append("<p>");
            new MdTextHandler(line).buildTo(sb);
            sb.append("</p>");
        }
    }
}
