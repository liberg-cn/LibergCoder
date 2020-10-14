package cn.liberg.coder.tool.markdown;

import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Markdown implements ILineReader {
    public MdHeading title;
    public BufferedReader bufferedReader;
    private String path;
    private String fileName;
    public List<IMdMeta> list;

    private boolean passEmptyLine = false;
    private boolean currentEmpty = false;
    private String lastLine = "";
    private int hIndex = 0;

    public Markdown(File file) throws LibergToolException {
        path = file.getAbsolutePath();
        fileName = file.getName();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            bufferedReader = br;
            parse();
        } catch (FileNotFoundException e) {
            throw new LibergToolException("File not found: " + path);
        } catch (IOException e) {
            throw new LibergToolException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        File f = new File("D:/dev/java/github/Liberg/README.md");
        try {
            Markdown mp = new Markdown(f);
            System.out.println("end");
            mp.writeTo("D:/test/LibertTool/test_out.md");
            System.out.println(mp.toHtml());
            System.out.println(mp.buildOutline());
        } catch (LibergToolException e) {
            e.printStackTrace();
        }
    }

    public void writeTo(String path) throws IOException {
        File f = new File(path);
        if(!f.exists()) {
            f.createNewFile();
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            if(title!=null) {
                title.writeTo(bw);
            }
            for(IMdMeta meta : list) {
                meta.writeTo(bw);
            }
        }
    }

    public String toHtml() {
        StringBuilder sb = new StringBuilder(1024);
        if(title!=null) {
            sb.append(title.toHtml());
        }
        for(IMdMeta meta : list) {
            sb.append(meta.toHtml());
        }
        return sb.toString();
    }

    public String buildOutline() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"lbt-title\">"+fileName+"</div>");
        sb.append("<div class=\"lbt-subs\" style=\"display: none;\">");
        for(IMdMeta meta : list) {
            if(meta.getType() == IMdMeta.TYPE_HEADING) {
                MdHeading heading = (MdHeading)meta;
                sb.append(heading.toOutline());
            }
        }
        sb.append("</div></div>");
        return sb.toString();
    }

    private void parse() throws IOException {
        title = MdHeading.parse(next(), hIndex);
        hIndex++;
        list = new ArrayList<>();
        IMdMeta meta = null;
        String line = next();
        while (line != null) {
            if(line.startsWith("> ")) {
                passEmptyLine = false;
                meta = MdQuoteBlock.parse(line, this);
                line = lastLine;
                if(meta!=null) {
                    list.add(meta);
                }
                continue;
            } else if(line.startsWith("| ")) {
                meta = MdTable.parse(line, this);
                line = lastLine;
                if(meta!=null) {
                    list.add(meta);
                }
                continue;
            } else if(line.startsWith("```")) {
                passEmptyLine = false;
                meta = MdCodeBlock.parse(line, this);
            } else if((meta = MdHeading.parse(line, hIndex)) != null) {
                hIndex++;
            } else {
                meta = new MdParagraph(line);
            }
            if(meta!=null) {
                list.add(meta);
            }
            line = next();
        }
    }

    @Override
    public String next() throws IOException {
        String line = null;
        do {
            line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if(line.length()==0) {
                passEmptyLine = true;
                currentEmpty = true;
            } else {
                passEmptyLine = currentEmpty;
                currentEmpty = false;
            }
        } while (line.length() == 0);
        lastLine = line;
        return line;
    }

    public String next(boolean trim) throws IOException {
        String line = null;
        String trimed = null;
        do {
            line = bufferedReader.readLine();
            if(line == null) {
                break;
            }
            trimed = line.trim();
            if(line.length()==0) {
                passEmptyLine = true;
                currentEmpty = true;
            } else {
                passEmptyLine = currentEmpty;
                currentEmpty = false;
            }
        } while(trimed.length() == 0);
        lastLine = trim ? trimed : line;
        return lastLine;
    }

    @Override
    public boolean skipEmptyLine() {
        return passEmptyLine;
    }

}
