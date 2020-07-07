package cn.liberg.coder.tool.template;

import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.java.*;

/**
 * 待实现
 * TODO 调用dao层，实现数据缓存层
 */
public class TempAccess {
	LibergToolContext context;
	String entityName;
	String selfName;
	String tableName;
	String selfPath;
	JClass parser;
	JClassEntity entity;

	public TempAccess(LibergToolContext ctx, JClassEntity entity) throws LibergToolException {
		context = ctx;
		this.entity = entity;
		entityName = entity.name;
		tableName = Formats.toTableName(entityName);
		selfName = entityName + "Access";
		selfPath = context.getAccessPath() + selfName + ".java";

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
		System.out.println("> " + context.getAccessPackage() + "." + selfName + tip);
	}

	private void initTemplate() {
		parser.fileDescs.add(JDesc.fileDesc());
		parser.mPackage = context.getAccessPackage();
		parser.mImports.add(context.getEntityPackage() + "." + entityName);
		parser.mImports.add("cn.liberg.database.BaseAccess");
		parser.name = selfName;
		parser.defLine = "public class "+selfName+" extends BaseAccess<"+entityName+"> {";

		initFieldsAndContructor();
		parser.addOrUpdateMethod(createOtherInitMethod());
	}

	private void updateTemplate() {
		parser.removeFieldsStartsWith("col_");
		initFieldsAndContructor();
	}

	private void initFieldsAndContructor() {
		JMethod contructor = new JMethod("private "+selfName+"() {");
		contructor.appendBodyLine("        entityName = \""+entityName+"\";");
		contructor.appendBodyLine("        tableName = \""+tableName+"\";");
		contructor.appendBodyLine("        entityClazz = "+entityName+".class;");

		parser.addOrUpdateField(new JField("private static volatile "+selfName+" selfInstance;"));
		String columnName;
		String keyName;
		MetaAnno anno;
		for(JField jf : entity.fields) {
			anno = jf.getAnno();
			if(anno.hasFalse("dbmap", "isMap")) {
				continue;
			}
			columnName = Formats.toTableFieldName(jf.name);
			if(columnName.startsWith("_")) {
				keyName = "col" + columnName;
			} else {
				keyName = "col_" + columnName;
			}
			parser.addField(new JField("public static final String "+keyName+" = \"" +columnName+ "\";"));
			if(!Formats.TABLE_ID.equals(columnName)) {
				contructor.appendBodyLine("        fieldsMap.put("+keyName+", \""+jf.name+"\");");
			}
		}
		contructor.appendBodyLine("        init();");

		parser.addOrUpdateMethod(contructor);
		parser.addOrUpdateMethod(createSelfMethod());
	}

	private JMethod createOtherInitMethod() {
		JMethod jm = new JMethod("private void init() {");
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
