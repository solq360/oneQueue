package com.eyu.onequeue.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 统计项目
 * 
 * @author solq
 */
public class CodeTotal {

    public static void calculate(String path, Set<String> include) {
	int row = 0;
	List<File> list = total(path, include);
	for (File file : list) {
	    System.out.println(file.getName());
	    try (BufferedReader in = new BufferedReader(new FileReader(file));) {
		while ((in.readLine()) != null) {
		    row++;
		}
	    } catch (Exception e) {
	    }

	}
	System.out.println("文件数量：" + list.size());
	System.out.println("代码行数：" + row);
    }

    static List<File> total(String path, Set<String> include) {
	List<File> files = new ArrayList<File>();
	File file = new File(path);
	File[] files2 = file.listFiles();
	if(files2==null){
	    return files;
	}
	for (File file3 : files2) {
	    if (file3.isFile()) {
		if (include == null) {
		    files.add(file3);
		} else {
		    for (String p : include) {
  			if (file3.getPath().endsWith(p)) {
			    files.add(file3);
			}
		    }
		}

	    } else {
		files.addAll(files.size(), total(file3.getPath(), include));
	    }
	}
	return files;
    }

}