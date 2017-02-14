package com.eyu.onequeue.util;

import java.io.File;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
/**
 * @author solq
 **/
public abstract class FileUtil {

    public static void createDirs(String fileName) {
	File file = new File(fileName);
	if (!file.exists()) {
	    if (!file.getParentFile().exists()) {
		file.getParentFile().mkdirs();
	    }
	}
    }

    public static void close(AutoCloseable r) {
	if (r == null) {
	    return;
	}
	try {
	    r.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void clean(final Object buffer) throws Exception {
	AccessController.doPrivileged(new PrivilegedAction() {
	    @SuppressWarnings("restriction")
	    public Object run() {
		try {
		    Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
		    getCleanerMethod.setAccessible(true);
		    sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
		    cleaner.clean();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return null;
	    }
	});

    }

}
