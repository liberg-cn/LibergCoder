package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.util.Copier;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * 类、成员变量、成员方法、方法参数的注解信息
 */
public class MetaAnno {
    private Map<String, MetaAnnoItem> map = new HashMap<>();

    public MetaAnno() {

    }

    public MetaAnno(List<String> annoLines) {
        if (annoLines != null) {
            for (String line : annoLines) {
                parse(line);
            }
        }
    }

    public MetaAnno(String annoLine) {
        parse(annoLine);
    }

    public MetaAnno copy() {
        MetaAnno an = new MetaAnno();
        MetaAnnoItem item;
        for (Map.Entry<String, MetaAnnoItem> entry : map.entrySet()) {
            item = entry.getValue();
            an.map.put(entry.getKey(), item.copy());
        }
        return an;
    }


    public void addLine(String line) {
        parse(line);
    }

    public void writeTo(BufferedWriter bw) throws IOException {
        MetaAnnoItem item = null;
        for (Map.Entry<String, MetaAnnoItem> entry : map.entrySet()) {
            item = entry.getValue();
            item.writeTo(bw);
            bw.write(" ");
        }
    }

    public void writeOrderedTo(BufferedWriter bw) throws IOException {
        ArrayList<MetaAnnoItem> list = new ArrayList<>();
        for (MetaAnnoItem item : map.values()) {
            list.add(item);
        }
        Collections.sort(list, new Comparator<MetaAnnoItem>() {
            @Override
            public int compare(MetaAnnoItem o1, MetaAnnoItem o2) {
                return o2.order - o1.order;
            }
        });
        for (MetaAnnoItem item : list) {
            item.writeTo(bw);
            bw.write(" ");
        }
    }

    public boolean has(String name) {
        return map.containsKey(name);
    }

    public boolean hasTrue(String name, String attrName) {
        return hasValue(name, attrName, "true");
    }

    public boolean hasFalse(String name, String attrName) {
        return hasValue(name, attrName, "false");
    }

    public boolean hasValue(String name, String attrName, String value) {
        boolean rt = false;
        MetaAnnoItem item = map.get(name);
        if (item != null) {
            rt = value.equals(item.get(attrName));
        }
        return rt;
    }

    public void setOrder(String name, int order) {
        MetaAnnoItem item = map.get(name);
        if (item != null) {
            item.setOrder(order);
        }
    }

    public void setValue(String name, String attrName, String value) {
        MetaAnnoItem item = map.get(name);
        if (item == null) {
            item = new MetaAnnoItem(name, null, 0);
            map.put(name, item);
        }
        item.set(attrName, value);
    }

    public void setValue(String name, String attrName, String value, int order) {
        MetaAnnoItem item = map.get(name);
        if (item == null) {
            item = new MetaAnnoItem(name, null, 0);
            map.put(name, item);
        }
        item.set(attrName, value);
        item.setOrder(order);
    }

    public String getValue(String name, String attrName, String defaultVal) {
        String rt = defaultVal;
        MetaAnnoItem item = map.get(name);
        if (item != null) {
            String val = item.get(attrName);
            if (val != null) {
                rt = val;
            }
        }
        return rt;
    }

    private void parse(String annoLine) {
        String anno;
        String[] arr = annoLine.split("@");
        for (int j = 0; j < arr.length; j++) {
            anno = arr[j].trim();
            if (anno.length() > 0) {
                MetaAnnoItem item = new MetaAnnoItem(anno);
                map.put(item.name, item);
            }
        }
    }


    private static class MetaAnnoItem {
        public String name;//名称
        public Map<String, String> values;//属性,可为null
        public int order;//排序顺序，越大越靠前

        public String get(String key) {
            String val = null;
            if (values != null) {
                val = values.get(key);
            }
            return val;
        }

        public void set(String key, String value) {
            if (values == null) {
                values = new HashMap<>();
            }
            values.put(key, value);
        }

        public void setOrder(int newOrder) {
            order = newOrder;
        }

        public MetaAnnoItem(String name, Map<String, String> values, int order) {
            this.name = name;
            this.values = values;
            this.order = order;
        }

        public MetaAnnoItem copy() {
            return new MetaAnnoItem(name, Copier.clone(values), order);
        }

        public MetaAnnoItem(String anno) {
            int idx1 = anno.indexOf('(');
            if (idx1 >= 0) {
                int idx2 = anno.indexOf(')', idx1 + 1);
                if (idx2 >= 0) {
                    name = anno.substring(0, idx1).trim();
                    String args = anno.substring(idx1 + 1, idx2).trim();
                    values = new HashMap<>();
                    if (args.indexOf('=') >= 0) {
                        String[] list = args.split(",");
                        String[] pair;
                        for (int i = 0; i < list.length; i++) {
                            pair = list[i].split("=");
                            values.put(pair[0].trim(), pair[1].trim());
                        }
                    } else {
                        values.put("value", args);
                    }
                }
            } else {
                name = anno;
            }
        }

        private void writeTo(BufferedWriter bw) throws IOException {
            bw.write("@");
            bw.write(name);
            if (values != null) {
                int size = values.size();
                bw.write("(");
                String key, val;
                boolean in = false;
                for (Map.Entry<String, String> it : values.entrySet()) {
                    key = it.getKey();
                    val = it.getValue();
                    if (size == 1 && "value".equals(key)) {
                        bw.write(val);
                        break;
                    } else {
                        if (in) {
                            bw.write(", ");
                        }
                        bw.write(key);
                        bw.write("=");
                        bw.write(
                                val);
                        in = true;
                    }
                }
                bw.write(")");
            }
        }
    }
}
