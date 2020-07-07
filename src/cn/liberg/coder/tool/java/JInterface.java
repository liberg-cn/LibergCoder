package cn.liberg.coder.tool.java;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.util.RegExpr;
import cn.liberg.coder.tool.core.ILineReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Java文件(.java)解析器
 * 暂不支持静态内部类
 */
public class JInterface implements ILineReader {
	public String mCodeFilePath;
	public boolean loadedFromFile;
	public ArrayList<JDesc> fileDescs;//文件最开始的注释
	public String mPackage;
	public ArrayList<String> mImports;
	public String name;
	public ArrayList<JDesc> classDescs;//接口定义开始的注释
	public ArrayList<String> mAnnos;//接口上面的注解
	public String defLine;//interface定义的一行，多行合并为一行
	public String visitor = "public";//接口只能是public

	public static final RegExpr RE = new RegExpr("^(public +)?interface +(\\w+)");

	//TODO 暂未支持解析接口中的成员变量
	//private members 不支持方法重载
	private ArrayList<JInterfaceMethod> methods;
	private Map<String, JInterfaceMethod> methodsMap = null;
	private BufferedReader mBr = null;

	public JInterface(String path) throws LibergToolException {
		mCodeFilePath = path;
		File file = new File(path);
		fileDescs = new ArrayList<JDesc>();
		mImports = new ArrayList<String>();
		classDescs = new ArrayList<JDesc>();
		mAnnos = new ArrayList<String>();
		methods = new ArrayList<JInterfaceMethod>();
		methodsMap = new HashMap<String, JInterfaceMethod>();
		if(file.exists()) {
			loadedFromFile = true;
			try {
				mBr = new BufferedReader(new FileReader(file));
				tryParse();
			} catch (Exception e) {
				e.printStackTrace();
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


	public ArrayList<JInterfaceMethod> getMethods() {
		return methods;
	}

	public void addMethod(JInterfaceMethod method) {
		methods.add(method);
		methodsMap.put(method.name, method);
	}

	public boolean addOrUpdateMethod(JInterfaceMethod method) {
		String name = method.name;
		JInterfaceMethod jm = methodsMap.get(method.name);
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

	public void appendLineInMethod(String methodName, String line) {
		JInterfaceMethod jm = methodsMap.get(methodName);
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
			for(String it : mAnnos) {
				bw.write(it);
				bw.write(Formats.NL);
			}
			bw.write(defLine);
			bw.write(Formats.NL);
			
			for(JInterfaceMethod m : methods) {
				m.writeTo(bw);
			}
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
				mAnnos.add(line);
			} else if(line.indexOf(" interface ")>=0) {
				defLine = line;
				parseDefLine(line);
				break;
			}
		}
		//parse body
		JDesc topDesc = null;
		boolean isInMethod = false;
		ArrayList<String> annos = null;
		JInterfaceMethod jm = null;
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
				
			} else if(line.startsWith("public ")) {
				if(line.indexOf("=")>=0 && line.indexOf("@")<0) {//接口中成员变量必须赋初值
					isInMethod = false;
					//TODO now skip the fields
				} else {
					appendRawMethod(jm, false);
					isInMethod = true;
					jm = new JInterfaceMethod(this, line);
					jm.annoLines = annos;
					jm.descTop = topDesc;
				}
				annos = null;
				topDesc = null;
			} else {
				if(jm != null) {
					jm.appendBodyLine(lineRaw);
				}
			}
		}
		appendRawMethod(jm, true);
	}

	private void parseDefLine(String line) {
		Matcher matcher = RE.findMatcher(line);
		if(matcher != null) {
			name = matcher.group(2);
		}
	}
	
	private void appendRawMethod(JInterfaceMethod jm, boolean last) {
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
