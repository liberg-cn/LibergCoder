package cn.liberg.coder.tool.mysql;

import java.util.ArrayList;
import java.util.List;

public class TableUpgrader {
    private String tableName;
    private List<String> ops;

    public TableUpgrader(String tableName) {
        this.tableName = tableName;
        ops = new ArrayList<>();
    }

    public int opSize() {
        return ops.size();
    }

    public void addIndex(String column) {
        ops.add("addIndex(\"" + column + "\")");
    }

    public void dropIndex(String column) {
        ops.add("dropIndex(\"" + column + "\")");
    }

    public void addColumn(String column, String type, String afterOf) {
        ops.add("addColumn(\"" + column + "\", " + type + ", \"" + afterOf + "\")");
    }

    public void modifyColumn(String column, String newType) {
        ops.add("modifyColumn(\"" + column + "\", " + newType + ")");
    }


    public List<String> build() {
        List<String> list = new ArrayList<>();
        if (ops.size() > 0) {
            list.add("alter(\"" + tableName + "\")");
            for (String item : ops) {
                list.add("        ."+item);
            }
            list.add("        .exec(stat);");
        }
        return list;
    }
}
