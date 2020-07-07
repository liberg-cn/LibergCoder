package cn.liberg.coder.tool.markdown;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MdCodeBlock implements IMdMeta {
    public static final String PREFIX = "```";
    private String codeType = "";
    private List<String> list;

    public static MdCodeBlock parse(String line, ILineReader reader) throws IOException {
        if (line.startsWith(PREFIX)) {
            MdCodeBlock meta = new MdCodeBlock();
            meta.codeType = line.substring(PREFIX.length()).trim();
            meta.list = new ArrayList<>();
            for (; ; ) {
                line = reader.next(false);
                if (line == null || line.trim().equals("```")) {
                    break;
                }
                if (reader.skipEmptyLine()) {
                    //合并多个空行为1行
                    meta.list.add("");
                }
                meta.list.add(line);
            }
            return meta;
        } else {
            return null;
        }
    }

    @Override
    public void writeTo(BufferedWriter bw) throws IOException {
        bw.write(PREFIX + codeType);
        bw.write(Formats.NL);
        for (String line : list) {
            bw.write(line);
            bw.write(Formats.NL);
        }
        bw.write(PREFIX);
        bw.write(Formats.NL2);
    }

    @Override
    public int getType() {
        return TYPE_CODE_BLOCK;
    }

    @Override
    public String toHtml() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("<pre class=\"line-numbers\"><code class=\"language-" + codeType + "\">");
        for(String line : list) {
            sb.append(line);
            sb.append("\r\n");
        }
        sb.append("</code></pre>");
        return sb.toString();
    }
}
