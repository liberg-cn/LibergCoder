package cn.liberg.coder.tool.java;


import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.LibergToolException;
import cn.liberg.coder.tool.core.Formats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JClassEntity extends JClass {
	LibergToolContext context;

	public JClassEntity(LibergToolContext context, String path) throws LibergToolException {
		super(path);
		this.context = context;
	}

	@Override
	public void writeFields(BufferedWriter bw) throws IOException {
		int count = 0;
		Map<String, String> shortNames = new HashMap<>();
		for(JField f : fields) {
			if(f.descTop != null) {
				f.descTop.wirteTo(bw);
			}
			if(count>0 && count%5 == 0) {
				bw.write(Formats.NL);
			}
			MetaAnno ma = f.getAnno();

			String theShort = Formats.forShort(f.name);
			String shortN = null;
			int increIndex = 0;
			do {
				if(shortN==null) {
					shortN = theShort;
				} else {
					increIndex++;
					shortN = theShort+increIndex;
				}
			} while (shortNames.containsKey(shortN));

			shortNames.put(shortN, "");
			//将这两个注解的顺序提前
			ma.setValue("JSONField", "name", "\""+shortN+"\"", 100);
			ma.setOrder("dbmap", 99);
			bw.write(Formats.IN);
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
		for(String im : mImports) {
			if(im.endsWith(".JSONField")) {
				hasJSONFieldAnno = true;
				break;
			}
		}
		if(!hasJSONFieldAnno) {
			mImports.add("com.alibaba.fastjson.annotation.JSONField");
		}
		writeToFile(mCodeFilePath);
		System.out.println("> " + context.getAccessPackage() + "." + name + "    updated.");
	}

}
