package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.java.*;

import java.util.HashMap;
import java.util.Map;

public class TempDao {
	LibergToolContext context;
	String entityName;
	String selfName;
	String tableName;
	String selfPath;
	JClass parser;
	JClassEntity entity;

	public static final Map<String, String> TYPE_MAP = new HashMap<>();
	static {
		TYPE_MAP.put("String", "StringColumn");
		TYPE_MAP.put("int", "IntegerColumn");
		TYPE_MAP.put("Integer", "IntegerColumn");
		TYPE_MAP.put("byte", "ByteColumn");
		TYPE_MAP.put("Byte", "ByteColumn");
		TYPE_MAP.put("long", "LongColumn");
		TYPE_MAP.put("Long", "LongColumn");
	}
	public static final Map<String, String> UPPER_TYPE_MAP = new HashMap<>();
	static {
		UPPER_TYPE_MAP.put("String", "String");
		UPPER_TYPE_MAP.put("int", "Integer");
		UPPER_TYPE_MAP.put("Integer", "Integer");
		UPPER_TYPE_MAP.put("byte", "Byte");
		UPPER_TYPE_MAP.put("Byte", "Byte");
		UPPER_TYPE_MAP.put("long", "Long");
		UPPER_TYPE_MAP.put("Long", "Long");
	}

	public TempDao(LibergToolContext ctx, JClassEntity entity) throws LibergToolException {
		context = ctx;
		this.entity = entity;
		entityName = entity.name;
		tableName = Formats.toTableName(entityName);
		selfName = entityName + "Dao";
		selfPath = context.getDaoPath() + selfName + ".java";

		parser = new JClass(selfPath);
		if(!parser.loadedFromFile) {
			initTemplate();
		} else {
			updateTemplate();
		}
	}

	public void save() throws LibergToolException {
		parser.writeToFile(selfPath);
		String tip = "  created.";
		if(parser.loadedFromFile) {
			tip = "  updated.";
		}
		System.out.println("> " + context.getDaoPackage() + "." + selfName + tip);
	}

	private void initTemplate() {
		parser.mPackage = context.getDaoPackage();
		parser.addImport(context.getEntityPackage() + "." + entityName);
		parser.addImport("cn.liberg.core.Column");
		parser.addImport("cn.liberg.core.ByteColumn");
		parser.addImport("cn.liberg.core.IntegerColumn");
		parser.addImport("cn.liberg.core.LongColumn");
		parser.addImport("cn.liberg.core.StringColumn");
		parser.addImport("cn.liberg.database.BaseDao");
		parser.addImport("java.sql.PreparedStatement");
		parser.addImport("java.sql.ResultSet");
		parser.addImport("java.sql.SQLException");
		parser.addImport("java.util.ArrayList");
		parser.addImport("java.util.List");

		parser.name = selfName;
		parser.defLine = "public class "+selfName+" extends BaseDao<"+entityName+"> {";

		update();
	}

	private void updateTemplate() {
		parser.removeFieldsStartsWith("column");
		update();
	}

	private int promoteSize(int size) {
		int curr = 4;
		while(curr < size) {
			curr <<= 1;
		}
		return curr;
	}

	private void update() {
		JMethod oldConstructor = parser.getMethod(selfName);
		JMethod constructor = new JMethod("private "+selfName+"() {");
		constructor.appendBodyLine("        super(\""+tableName+"\");");
		if(oldConstructor != null) {
			String trimed;
			for(String line : oldConstructor.getBody()) {
				trimed = line.trim();
				if(!(trimed.startsWith("super(") || trimed.startsWith("init("))) {
					constructor.appendBodyLine(line);
				}
			}
		}
		constructor.appendBodyLine("        init();");

		JMethod jm_buildEntity = new JMethod("public "+entityName+" buildEntity(ResultSet rs) throws SQLException {");
		jm_buildEntity.addAnnoLine("@Override");
		jm_buildEntity.appendBodyLine("        "+entityName+" entity = new "+entityName+"();");
		jm_buildEntity.appendBodyLine("        entity.id = rs.getLong(1);");

		JMethod jm_fillPreparedStatement = new JMethod("protected void fillPreparedStatement("+entityName+" entity, PreparedStatement ps) throws SQLException {");
		jm_fillPreparedStatement.addAnnoLine("@Override");

		JMethod jm_getColumns = new JMethod("public List<Column> getColumns() {");
		jm_getColumns.addAnnoLine("@Override");
		jm_getColumns.appendBodyLine("        if(columns == null) {");
		jm_getColumns.appendBodyLine("            columns = new ArrayList<>("+promoteSize(entity.fields.size()-1)+");");


		parser.addOrUpdateField(new JField("private static volatile "+selfName+" selfInstance;"));
		String varName;
		String typeName;
		String upperType;
		String upperFirstLetterType;
		String shortName;
		MetaAnno anno;
		int index = 0;
		for(JField jf : entity.fields) {
			anno = jf.getAnno();
			// @dbmap(isMap=false)的成员不映射到数据表的列
			if(anno.hasFalse("dbmap", "isMap")) {
				continue;
			}
			// 被"号包裹的短名称
			shortName = anno.getValue("JSONField", "name");
			upperType = UPPER_TYPE_MAP.get(jf.type);
			upperFirstLetterType = Formats.upperEntityField(jf.type);
			varName = Formats.toColumnFieldName(jf.name);
			typeName = TYPE_MAP.get(jf.type);
			if(!Formats.ID.equals(jf.name)) {
				parser.addField(new JField("public static final Column<"+upperType + "> " + varName+" = new "+typeName+"(\"" +jf.name+ "\", "+shortName+");"));
				index++;
				jm_buildEntity.appendBodyLine("        entity."+jf.name+" = rs.get"+upperFirstLetterType+"("+(index+1)+");");
				jm_fillPreparedStatement.appendBodyLine("        ps.set"+upperFirstLetterType+"("+index+", entity."+jf.name+");");
				jm_getColumns.appendBodyLine("            columns.add("+varName+");");
			}
		}
		jm_buildEntity.appendBodyLine("        return entity;");
		jm_getColumns.appendBodyLine("        }");
		jm_getColumns.appendBodyLine("        return columns;");

		parser.addOrUpdateMethod(constructor);
		if(!parser.hasMethod("init")) {
			parser.addMethod(createInitMethod());
		}
		addMethodIfAbsent_setEntityId();
		addMethodIfAbsent_getEntityId();
		parser.addOrUpdateMethod(jm_buildEntity);
		parser.addOrUpdateMethod(jm_fillPreparedStatement);
		parser.addOrUpdateMethod(jm_getColumns);
		parser.addOrUpdateMethod(createSelfMethod());
	}

	private JMethod createInitMethod() {
		JMethod jm = new JMethod("private void init() {");
		return jm;
	}

	private JMethod addMethodIfAbsent_setEntityId() {
		JMethod jm = new JMethod("public void setEntityId("+entityName+" entity, long id) {");
		jm.addAnnoLine("@Override");
		jm.appendBodyLine("        entity.id = id;");
		parser.addOrUpdateMethod(jm);
		return jm;
	}
	private JMethod addMethodIfAbsent_getEntityId() {
		JMethod jm = new JMethod("public long getEntityId("+entityName+" entity) {");
		jm.addAnnoLine("@Override");
		jm.appendBodyLine("        return entity.id;");
		parser.addOrUpdateMethod(jm);
		return jm;
	}

	private JMethod createSelfMethod() {
		JMethod jm = new JMethod("public static "+selfName+" self() {");
		jm.appendBodyLine("		if (selfInstance == null) {");
		jm.appendBodyLine("		    synchronized ("+selfName+".class) {");
		jm.appendBodyLine("                if (selfInstance == null) {");
		jm.appendBodyLine("                    selfInstance = new "+selfName+"();");
		jm.appendBodyLine("                }");
		jm.appendBodyLine("            }");
		jm.appendBodyLine("		}");
		jm.appendBodyLine("		return selfInstance;");
		return jm;
	}
}

