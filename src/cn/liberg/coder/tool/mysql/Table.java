package cn.liberg.coder.tool.mysql;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.java.JClassEntity;
import cn.liberg.coder.tool.java.JField;
import cn.liberg.coder.tool.java.JMethod;
import cn.liberg.coder.tool.java.MetaAnno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Table {
    public String name;
    public String entityName;
    //不含ID字段
    public ArrayList<TableField> fields = new ArrayList<>();
    public Map<String, TableField> fieldsMap = new HashMap<>();
    public static final String createMethodPrefix = "createTable";
    public static final String fieldLinePrefix = "        tb.add(";

    private Table() {

    }

    public TableField getField(String name) {
        return fieldsMap.get(name);
    }

    public Table(JClassEntity entity) {
        entityName = entity.name;
        name = Formats.toTableName(entityName);
        //去掉排第一位的ID字段
        JField jf;
        TableField tf;
        for (int i = 1; i < entity.fields.size(); i++) {
            jf = entity.fields.get(i);

            MetaAnno anno = jf.getAnno();
            if (anno.hasFalse("dbmap", "isMap")) {
                continue;//跳过不用映射到数据库的字段
            }
            tf = new TableField(jf);
            fieldsMap.put(tf.name, tf);
            fields.add(tf);
        }
    }

    public static Table fromJMethod(JMethod jm) {
        int prefixLen = createMethodPrefix.length();
        Table table = null;
        if (jm.name.length() > prefixLen) {
            table = new Table();
            table.entityName = jm.name.substring(prefixLen);
            table.name = Formats.toTableName(table.entityName);
            String line;
            char c;
            final int len = fieldLinePrefix.length();
            String fn;
            TableField tf;
            ArrayList<String> jmBody = jm.getBody();
            for (int i = 0; i < jmBody.size(); i++) {
                line = jmBody.get(i).trim();
                if (line.startsWith("tb.add")) {
                    tf = TableField.fromDefineLine(line);
                    table.fields.add(tf);
                    table.fieldsMap.put(tf.name, tf);
                }
            }
        }
        return table;
    }

    public JMethod toJMethod() {
        String line = "protected void " + createMethodPrefix + entityName + "(Statement stat) throws SQLException {";
        JMethod jm = new JMethod(line);
        ArrayList<String> body = jm.getBody();
        body.add("        TableBuilder tb = new TableBuilder(" + entityName + "Dao.self().getTableName());");
        for (TableField tf : fields) {
            body.add(fieldLinePrefix + entityName + tf.toDefineLine() + ");");
        }
        body.add("        stat.executeUpdate(tb.build());");
        return jm;
    }

    public TableUpgrader diffWith(Table t) {
        if (!name.equals(t.name)) return null;

        TableUpgrader upgrader = new TableUpgrader(name);
        TableField org;
        String afterOf = Formats.ID;
        for (TableField tf : fields) {
            org = t.getField(tf.name);
            if(org!=null) {//检查是否需要修改
                tf.diffWith(org, upgrader);
            } else {//新增
                upgrader.addColumn(tf.name, tf.typeDefine, afterOf);
            }
            afterOf = tf.name;
        }
        return upgrader.opSize()>0? upgrader : null;
    }

}
