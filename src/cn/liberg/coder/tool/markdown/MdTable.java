package cn.liberg.coder.tool.markdown;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MdTable implements IMdMeta {
    List<String> head;
    List<List<String>> rows;
    boolean headParsed = false;


    public static MdTable parse(String line, ILineReader reader) throws IOException {
        if(line.startsWith("| ")) {
            MdTable table = new MdTable();
            table.parseHead(line);
            table.rows = new ArrayList<>();
            for(;;) {
                line = reader.next();
                if(line==null || !line.startsWith("|")) {
                    break;
                }
                table.parseLine(line);
            }
            return table;
        } else {
            return null;
        }
    }


    private void parseLine(String line) {
        String[] arr = line.split("\\|");
        if(!headParsed && "----".equals(arr[1].trim())) {
            headParsed = true;
        } else {
            int len = Math.min(head.size()+1, arr.length);
            List<String> row = new ArrayList<>();
            for(int i=1;i<len;i++) {
                row.add(arr[i].trim());
            }
            for(int i=len;i<=head.size();i++) {
                row.add("");
            }
            rows.add(row);
        }
    }

    private void parseHead(String line) {
        head = new ArrayList<>();
        String[] arr = line.split("\\|");
        for(int i=1;i<arr.length;i++) {
            head.add(arr[i].trim());
        }
    }

    @Override
    public void writeTo(BufferedWriter bw) throws IOException {
        bw.write("| ");
        for(String item : head) {
            bw.write(item + " | ");
        }
        bw.write(Formats.NL);
        bw.write("| ");
        for(String item : head) {
            bw.write("---- | ");
        }
        bw.write(Formats.NL);
        for(List<String> row : rows) {
            bw.write("| ");
            for(String cell : row) {
                bw.write(cell + " | ");
            }
            bw.write(Formats.NL);
        }
        bw.write(Formats.NL);
    }

    @Override
    public int getType() {
        return TYPE_TABLE;
    }

    @Override
    public String toHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"table table-bordered table-hover\">");
        sb.append("<thead><tr>");
        for(String cell : head) {
            sb.append("<th>");
            sb.append(cell);
            sb.append("</th>");
        }
        sb.append("</tr></thead>");

        sb.append("<tbody>");
        for(List<String> row : rows) {
            buildHtmlTo(sb, row);
        }
        sb.append("</tbody>");

        sb.append("</table>");
        return sb.toString();
    }

    private void buildHtmlTo(StringBuilder sb, List<String> row) {
        sb.append("<tr>");
        for(String cell : row) {
            sb.append("<td>");
            sb.append(cell);
            sb.append("</td>");
        }
        sb.append("</tr>");
    }
}
