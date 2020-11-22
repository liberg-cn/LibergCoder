package cn.liberg.coder.tool.java;


import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.core.Formats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JClassEntity extends JClass {
	LibergToolContext context;

	public JClassEntity(LibergToolContext context, String path) throws LibergToolException {
		super(path);
		this.context = context;
		initFields();
	}

	/**
	 * 补齐@JSONField(name={shortName})注解
	 */
	private void initFields() {
		Set<String> shortNames = new HashSet<>();

		// 第一次遍历，保留已存在的短名称
		for(JField f : fields) {
			MetaAnno ma = f.getAnno();
			String shortName = ma.getValue("JSONField", "name");
			if(shortName!=null && shortName.length()>0) {
				shortNames.add(shortName);
			}
		}

		for(JField f : fields) {
			MetaAnno ma = f.getAnno();
			String shortN = ma.getValue("JSONField", "name");
			if (shortN != null && shortN.length() > 0) {
				ma.setOrder("JSONField", 100);
			} else {
				String theShort = Formats.forShort(f.name);
				int increIndex = 0;
				do {
					if (shortN == null) {
						shortN = theShort;
					} else {
						increIndex++;
						shortN = theShort + increIndex;
					}
				} while (shortNames.contains(shortN));

				shortNames.add(shortN);
				//将注解的顺序提前
				ma.setValue("JSONField", "name", "\"" + shortN + "\"", 100);
			}
			// 将@dbmap注解放在@JSONField后面
			ma.setOrder("dbmap", 99);
		}
	}

	@Override
	public void writeFields(BufferedWriter bw) throws IOException {
		int count = 0;

		for(JField f : fields) {
			if(f.descTop != null) {
				f.descTop.wirteTo(bw);
			}
			if(count>0 && count%5 == 0) {
				bw.write(Formats.NL);
			}
			MetaAnno ma = f.getAnno();
			bw.write(Formats.IN);
			// 按order顺序写入各个注解
			ma.writeOrderedTo(bw);

			bw.write(Formats.NL);
			bw.write(Formats.IN);
			bw.write(f.defLine);
			bw.write(Formats.NL);
			count++;
		}
	}

	public void save() throws LibergToolException {
		boolean hasJSONFieldAnno = false;
		for(String im : getImports()) {
			if(im.endsWith(".JSONField")) {
				hasJSONFieldAnno = true;
				break;
			}
		}
		if(!hasJSONFieldAnno) {
			addImport("com.alibaba.fastjson.annotation.JSONField");
		}
		writeToFile(mCodeFilePath);
		System.out.println("> " + context.getAccessPackage() + "." + name + "    updated.");
	}

}
