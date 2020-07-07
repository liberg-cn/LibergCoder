package cn.liberg.coder.tool.markdown;

import cn.liberg.coder.tool.core.Formats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MdHeading implements IMdMeta {
    private String prefix;
    private int level = 1;
    private String text = "";
    private int hIndex;
    public static final Pattern pattern = Pattern.compile("^(\\d+\\.?)? *(#{1,6}) +(.*)");


    public static MdHeading parse(String line, int hIndex) {
        Matcher mch = pattern.matcher(line);
        if (mch.find()) {
            MdHeading meta = new MdHeading();
            if (mch.group(1) != null) {
                meta.prefix = mch.group(1);
            }
            meta.level = mch.group(2).length();
            meta.text = mch.group(3);
            meta.hIndex = hIndex;
            return meta;
        } else {
            return null;
        }
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void writeTo(BufferedWriter bw) throws IOException {
        if (prefix != null) {
            bw.write(prefix + " ");
        }
        for (int i = 0; i < level; i++) {
            bw.write('#');
        }
        bw.write(" ");
        bw.write(text);
        bw.write(Formats.NL);
    }

    @Override
    public int getType() {
        return TYPE_HEADING;
    }

    @Override
    public String toHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h" + level + " id=\"mdh"+hIndex+"\">");
        sb.append(text);
        sb.append("</h" + level + ">");
        return sb.toString();
    }

    public String toOutline() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"lbt-h"+level+"\">");
        sb.append("<a class=\"lbt-heading\" href=\"#mdh"+hIndex+"\">"+text+"</a>");
        sb.append("</div>");
        return sb.toString();
    }

}
