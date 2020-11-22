package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergTool;
import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.core.ILineReader;
import cn.liberg.coder.tool.util.FileUtils;
import org.apache.http.util.TextUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 解析pom.xml中的依赖，完成动态追加
 */
public class TempApplicationPOM implements ILineReader {
    public static final String selfName = "pom.xml";
    private String projectRootPath;
    private List<String> linesBeforeDependencies;
    private List<DependencyItem> dependencyItemList;
    private Map<String, DependencyItem> dependencyItemMap;
    private List<String> linesAfterDependencies;
    private BufferedReader br;
    private static final HtmlTag tagDependencies = new HtmlTag("dependencies");

    public void addLibergIfAbsent() {
        addDependency("cn.liberg", "liberg", LibergTool.VERSION);
    }

    public void addFastJsonIfAbsent() {
        String version = "1.2.62";
        addDependency("com.alibaba", "fastjson", version);
    }

    public void addFastJsonIfAbsent(String version) {
        addDependency("com.alibaba", "fastjson", version);
    }

    public void addMysqlIfAbsent() {
        addDependency("mysql", "mysql-connector-java", "", "runtime");
    }

    public void addDependency(String groupId, String artifactId, String version, String scope) {
        DependencyItem item = new DependencyItem(groupId, artifactId, version);
        item.restLines.add("            <scope>"+scope+"</scope>");
        add(item);
    }
    public void addDependency(String groupId, String artifactId, String version) {
        DependencyItem item = new DependencyItem(groupId, artifactId, version);
        add(item);
    }

    private void add(DependencyItem item) {
        if(!dependencyItemMap.containsKey(item.getKey())) {
            dependencyItemMap.put(item.getKey(), item);
            dependencyItemList.add(item);
        }
    }


    public TempApplicationPOM(LibergToolContext ctx) throws Exception {
        this.projectRootPath = ctx.getProjectRootPath();
        linesBeforeDependencies = new ArrayList<>();
        dependencyItemMap = new HashMap<>();
        dependencyItemList = new ArrayList<>();
        linesAfterDependencies = new ArrayList<>();
        load();
    }

    private void load() throws Exception {
        br = FileUtils.bufferedReader(projectRootPath + selfName);
        String line;
        boolean isBefore = true;
        boolean isInDependenciesTag = false;
        boolean isAfter = false;
        while((line = next(false)) != null) {
            if(tagDependencies.start.equals(line.trim())) {
                isInDependenciesTag = true;
                isBefore = false;
                continue;
            }
            if(isInDependenciesTag) {
                parseDependencies(line.trim(), this);
                isInDependenciesTag = false;
                isAfter = true;
            } else if(isBefore) {
                linesBeforeDependencies.add(line);
            } else if(isAfter) {
                linesAfterDependencies.add(line);
            }
        }
        br.close();
    }

    public void save() throws IOException {
        BufferedWriter bw = FileUtils.bufferedWriter(projectRootPath + selfName);
        for(String line : linesBeforeDependencies) {
            bw.write(line);
            bw.write("\r\n");
        }

        bw.write("    " + tagDependencies.start + "\r\n");
        for(DependencyItem item : dependencyItemList) {
            item.writeTo(bw);
        }
        bw.write("    "+tagDependencies.end + "\r\n");

        for(String line : linesAfterDependencies) {
            bw.write(line);
            bw.write("\r\n");
        }
        bw.flush();
        bw.close();
    }

    private void parseDependencies(String line, ILineReader reader) throws IOException {
        do {
            if(line.trim().equals(tagDependencies.end)) {
                break;
            }
            DependencyItem item = DependencyItem.parse(line, this);
            if(item != null) {
                dependencyItemMap.put(item.getKey(), item);
                dependencyItemList.add(item);
            }
        } while((line=reader.next(false)) != null);
    }

    @Override
    public String next() throws IOException {
        String line = null;
        do {
            line = br.readLine();
            if(line == null) {
                break;
            }
            line = line.trim();
        } while(line.length() == 0);
        return line;
    }

    @Override
    public String next(boolean trim) throws IOException {
        String line = null;
        do {
            line = br.readLine();
            if(line == null) {
                break;
            }
            if(trim) {
                line = line.trim();
            }
        } while(line.length() == 0);
        return line;
    }

    public static class DependencyItem {
        private static final HtmlTag tagDependencyItem = new HtmlTag("dependency");
        private static final HtmlTag tagGroupId = new HtmlTag("groupId");
        private static final HtmlTag tagArtifactId = new HtmlTag("artifactId");
        private static final HtmlTag tagVersion = new HtmlTag("version");
        private static final String inlineTagPattern = "<\\w+>.*</\\w+>";
        private static final String tagBeginningPattern = "<\\w+>";
        String remark = "";
        String groupId = "";
        String artifactId = "";
        String version = "";
        List<String> restLines;

        public DependencyItem(String groupId, String artifactId, String version) {
            this();
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        private DependencyItem() {
            restLines = new ArrayList<>();
        }

        public static DependencyItem parse(String line, ILineReader reader) throws IOException {
            DependencyItem item = null;
            String remark = "";
            do {
                String org = line;
                line = line.trim();
                if(line.startsWith("<!--")) {
                    remark = line;
                    continue;
                }
                if(tagDependencyItem.start.equals(line)) {
                    item = new DependencyItem();
                    item.remark = remark;
                } else if(tagDependencyItem.end.equals(line)) {
                    break;
                } else {
                    if(isInlineTag(line)) {
                        String text = tagGroupId.parseContentOfLine(line);
                        if(text!=null) {
                            item.groupId = text;
                            continue;
                        }
                        text = tagArtifactId.parseContentOfLine(line);
                        if(text!=null) {
                            item.artifactId = text;
                            continue;
                        }
                        text = tagVersion.parseContentOfLine(line);
                        if(text!=null) {
                            item.version = text;
                            continue;
                        }
                        item.restLines.add(org);
                    } else {
                        handleMultiLines(item, org, reader);
                    }
                }
            } while((line=reader.next(false)) != null);
            return item;
        }

        private static void handleMultiLines(DependencyItem item, String line, ILineReader reader) throws IOException {
            item.restLines.add(line);
            line = line.trim();
            if(isTagBeginning(line)) {
                String tagEnding = "</" + line.substring(1,line.length()-2) + ">";
                while((line=reader.next(false)) != null) {
                    item.restLines.add(line);
                    if(line.trim().equals(tagEnding)) {
                        break;
                    }
                }
            }
        }

        private static boolean isInlineTag(String line) {
            return Pattern.matches(inlineTagPattern, line);
        }
        private static boolean isTagBeginning(String line) {
            return Pattern.matches(tagBeginningPattern, line);
        }

        public String getKey() {
            return groupId+"_"+artifactId;
        }

        public void writeTo(BufferedWriter bw) throws IOException {
            bw.write("        " + tagDependencyItem.start + "\r\n");
            tagGroupId.writeContentToLine(bw, groupId);
            tagArtifactId.writeContentToLine(bw, artifactId);
            if(!TextUtils.isEmpty(version)) {
                tagVersion.writeContentToLine(bw, version);
            }
            for(String line : restLines) {
                bw.write(line);
                bw.write("\r\n");
            }
            bw.write("        " + tagDependencyItem.end + "\r\n");
        }
    }

    public static class HtmlTag {
        private final String name;
        private final String start;
        private final String end;


        public HtmlTag(String tagName) {
            name = tagName;
            start = "<"+tagName+">";
            end = "</"+tagName+">";
        }

        public String head() {
            return start;
        }
        public String tail() {
            return end;
        }
        public String parseContentOfLine(String line) {
            if(line.startsWith(start) && line.endsWith(end)) {
                int start = this.start.length();
                int len = line.length() - end.length();
                return line.substring(start, len).trim();
            }
            return null;
        }

        public void writeContentToLine(BufferedWriter bw, String content) throws IOException {
            bw.write("            ");
            bw.write(start);
            bw.write(content);
            bw.write(end);
            bw.write("\r\n");
        }
    }

//    public static void main(String[] args) throws Exception {
//        TempApplicationPOM pom = new TempApplicationPOM("D:\\dev\\java\\github\\demo01\\");
//        pom.addDependency("testGroup", "testXXX", "1.0");
//        pom.addMysqlIfAbsent();
//        pom.addFastJsonIfAbsent();
//        pom.save();
//    }

}
