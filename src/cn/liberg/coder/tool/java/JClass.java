package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.util.RegExpr;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Java文件(.java)解析器
 * 1.暂不支持静态内部类
 * 2.所有成员必须以public/private/protect访问修饰符打头
 */
public class JClass implements ILineReader {
	public String mCodeFilePath;
	public boolean loadedFromFile;
	public ArrayList<JDesc> fileDescs;//文件最开始的注释
	public String mPackage;
	public ArrayList<String> mImports;
	public String name;
	public ArrayList<JDesc> classDescs;//类定义开始的注释
	public ArrayList<String> classAnnos;//class注解


	public String defLine;//class定义的一行
	public String visitor = "public";//目前仅支持单个java文件内的顶级public类
	public boolean isStatic;
	public boolean isFinal;

	public ArrayList<JField> fields;
	public ArrayList<JMethod> methods;
	//不支持方法重载
	private Map<String, JMethod> methodsMap = null;
	private Map<String, JField> fieldsMap = null;
	private BufferedReader mBr = null;
	public static final RegExpr RE = new RegExpr("^public +(static +)?(final +)?class +(\\w+)");

	public JClass(String path) throws LibergToolException {
		mCodeFilePath = path;
		File file = new File(path);
		fileDescs = new ArrayList<JDesc>();
		mImports = new ArrayList<String>();
		classDescs = new ArrayList<JDesc>();
		classAnnos = new ArrayList<String>();
		fields = new ArrayList<JField>();
		methods = new ArrayList<JMethod>();
		methodsMap = new HashMap<String, JMethod>();
		fieldsMap = new HashMap<String, JField>();
		if(file.exists()) {
			loadedFromFile = true;
			try {
				mBr = new BufferedReader(new FileReader(file));
				tryParse();
			} catch (Exception e) {
				throw new LibergToolException("Parse error: " + e.getMessage());
			} finally {
				if(mBr != null) {
					try {
						mBr.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public void removeFieldsStartsWith(String prefix) {
		JField jf;
		Iterator<JField> it = fields.iterator();
		while(it.hasNext()) {
			jf = it.next();
			if(jf.name.startsWith(prefix)) {
				it.remove();
				fieldsMap.remove(jf.name);
			}
		}
	}

	public void removeFieldsByType(String type) {
		JField jf;
		Iterator<JField> it = fields.iterator();
		while(it.hasNext()) {
			jf = it.next();
			if(type.equals(jf.type)) {
				it.remove();
				fieldsMap.remove(jf.name);
			}
		}
	}

	public void prependMethod(JMethod method) {
		methods.add(0, method);
		methodsMap.put(method.name, method);
	}

	public void addMethod(JMethod method) {
		methods.add(method);
		methodsMap.put(method.name, method);
	}
	public void addMethod(JMethod method, int index) {
		methods.add(index, method);
		methodsMap.put(method.name, method);
	}
	public void addField(JField field) {
		fields.add(field);
		fieldsMap.put(field.name, field);
	}

	public void updateMethod(JMethod method) {
		String methodName = method.name;
		JMethod jm = methodsMap.get(methodName);
		if(jm != null) {
			for(int i = 0; i< methods.size(); i++) {
				if(methodName.equals(methods.get(i).name)) {
					methods.set(i, method);
					methodsMap.put(methodName, method);
				}
			}
		}
	}

	public boolean addOrUpdateMethod(JMethod method) {
		String name = method.name;
		JMethod jm = methodsMap.get(method.name);
		if(jm != null) {
			for(int i = 0; i< methods.size(); i++) {
				if(name.equals(methods.get(i).name)) {
					methods.set(i, method);
					methodsMap.put(name, method);
					return false;
				}
			}
		}
		addMethod(method);
		return true;
	}

	public boolean addOrUpdateMethodSignature(JMethod method) {
		String name = method.name;
		JMethod jm = methodsMap.get(method.name);
		if(jm != null) {
			for(int i = 0; i< methods.size(); i++) {
				if(name.equals(methods.get(i).name)) {
					method.setBody(jm.getBody());//reserve the body
					methods.set(i, method);
					methodsMap.put(name, method);
					return false;
				}
			}
		}
		addMethod(method);
		return true;
	}

	public JMethod getMethod(String name) {
		return methodsMap.get(name);
	}
	public boolean hasMethod(String name) {
		return methodsMap.get(name)!=null;
	}

	public boolean addMethodIfNotExists(JMethod method) {
		String name = method.name;
		JMethod jm = methodsMap.get(method.name);
		if(jm == null) {
			addMethod(method);
			return true;
		} else {
			return false;
		}
	}

	public boolean addOrUpdateField(JField field) {
		String name = field.name;
		JField jf = fieldsMap.get(field.name);
		if(jf != null) {
			for(int i=0;i<fields.size();i++) {
				if(name.equals(fields.get(i).name)) {
					fields.set(i, field);
					fieldsMap.put(name, field);
					return false;
				}
			}
		}
		addField(field);
		return true;
	}
	
	public void appendLineInMethod(String methodName, String line) {
		JMethod jm = methodsMap.get(methodName);
		if(jm != null) {
			jm.appendBodyLine(line);
		}
	}
	
	public void writeToFile(String path) throws LibergToolException {
		BufferedWriter bw = null;
		try {
			File file = new File(path);
			if(!file.exists()) {
				file.createNewFile();
			}
			bw = new BufferedWriter(new FileWriter(file));
			for(JDesc it : fileDescs) {
				it.wirteTo(bw);
			}
			bw.write("package " + mPackage + ";");
			bw.write(Formats.NL2);
			for(String it : mImports) {
				bw.write("import ");
				bw.write(it);
				bw.write(";");
				bw.write(Formats.NL);
			}
			bw.write(Formats.NL);
			for(JDesc it : classDescs) {
				it.wirteTo(bw);
			}
			for(String it : classAnnos) {
				bw.write(it);
				bw.write(Formats.NL);
			}
			bw.write(defLine);
			bw.write(Formats.NL);

			writeFields(bw);
			bw.write(Formats.NL);
			writeMethods(bw);
			bw.write("}");
		} catch (IOException e) {
			e.printStackTrace();
			throw new LibergToolException(e);
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void writeFields(BufferedWriter bw) throws IOException {
		for(JField f : fields) {
			f.writeTo(bw);
		}
	}
	protected void writeMethods(BufferedWriter bw) throws IOException {
		for(JMethod m : methods) {
			m.writeTo(bw);
		}
	}

	
	private void tryParse() throws Exception {
		String line;
		
		//parse head
		int step = 0;
		while((line = next()) != null) {
			if(line.startsWith("//") || line.startsWith("/*")) {
				if(step == 0) {
					fileDescs.add(JDesc.parse(this, line));
				} else {
					classDescs.add(JDesc.parse(this, line));
				}
			} else if(line.startsWith("package")) {
				step = 1;
				mPackage = line.substring(8, line.length()-1).trim();
			} else if(line.startsWith("import")) {
				step = 1;
				mImports.add(line.substring(7, line.length()-1).trim());
			} else if(line.startsWith("@")) {//类的注解开始
				step = 1;
				classAnnos.add(line);
			} else if(line.indexOf(" class ")>=0) {
				defLine = line;
				parseDefLine(line);
				break;
			}
		}
		//parse body
		JDesc topDesc = null;
		boolean isInMethod = false;
		ArrayList<String> annos = null;
		JMethod jm = null;
		while((line = next(false)) != null) {
			String lineRaw = line;
			line = line.trim();
			if(line.startsWith("//") || line.startsWith("/*")) {
				if(isInMethod) {//方法体内部的注释
					jm.appendBodyLine(lineRaw);
				} else {
					topDesc = JDesc.parse(this, lineRaw);//方法上面，或者成员变量上面的注释
				}
			} else if(line.startsWith("@")) {
				if(annos == null)annos = new ArrayList<String>();
				annos.add(line);
				
			} else if(line.startsWith("public ") || line.startsWith("private ") || line.startsWith("protected ")) {
				String descTrimed = trimRightDesc(line);
				if(descTrimed.endsWith("{") || descTrimed.endsWith(",")) {
					appendRawMethod(jm, false);
					isInMethod = true;
					jm = new JMethod(this, line);
					jm.annoLines = annos;
					jm.descTop = topDesc;
				} else {
					isInMethod = false;
					JField jf = new JField(line);
					jf.annoLines = annos;
					jf.descTop = topDesc;
					fields.add(jf);
					fieldsMap.put(jf.name, jf);
				}
				annos = null;
				topDesc = null;
			} else {
				if(jm != null) {
					jm.appendBodyLine(lineRaw);
				}
				//待优化
				if(lineRaw.startsWith("    }")) {
					isInMethod = false;
				}
			}
		}
		appendRawMethod(jm, true);
	}

	private String trimRightDesc(String line) {
		String rt = line;
		int idx = rt.indexOf("//");
		if(idx>0) {
			rt = rt.substring(0, idx).trim();
		}
		idx = rt.indexOf("/*");
		if(idx>0) {
			rt = rt.substring(0, idx).trim();
		}
		return rt;
	}

	private void parseDefLine(String line) {
		Matcher matcher = RE.findMatcher(line);
		if(matcher != null) {
			if(matcher.group(1) != null) {
				isStatic = true;
			}
			if(matcher.group(2) != null) {
				isFinal = true;
			}
			name = matcher.group(3);
		}
	}
	
	private void appendRawMethod(JMethod jm, boolean last) {
		if(jm != null) {
			if(last) {
				jm.popBodyLine();
			}
			jm.popBodyLine();
			methods.add(jm);
			methodsMap.put(jm.name, jm);
		}
	}
	
	public String next() throws IOException {
		String line = null;
		do {
			line = mBr.readLine();
			if(line == null) {
				break;
			}
			line = line.trim();
		} while(line.length() == 0);
		return line;
	}
	
	public String next(boolean trim) throws IOException {
		String line = null;
		String trimed = null;
		do {
			line = mBr.readLine();
			if(line == null) {
				break;
			}
			trimed = line.trim();
		} while(trimed.length() == 0);
		return trim ? trimed : line;
	}
	
}
