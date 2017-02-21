package com.eyu.onequeue.util;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author solq
 */
public class PackageScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageScanner.class);

    /**
     * @param packageNames
     *            过滤的包名，如果为NULL即扫描所有类
     */
    public static void scan(Consumer<Class<?>> atcion, final String... packageNames) {
	Set<String> classPath = new HashSet<>();
	Set<String> filterPackage = new HashSet<>();
	if (packageNames == null || packageNames.length == 0) {
	    String classpathProp = System.getProperty("java.class.path");
	    if (classpathProp != null) {
		String[] classpathEntries = classpathProp.split(File.pathSeparator);
		for (String cpe : classpathEntries) {
		    cpe = trimr(cpe, '/');
		    classPath.add(new File(cpe).getAbsolutePath());
		}
	    }
	    ClassLoader cl = ClassLoader.getSystemClassLoader();
	    URL[] urls = ((URLClassLoader) cl).getURLs();
	    for (URL url : urls) {
		String path = trimr(url.getPath(), '/');
		classPath.add(new File(path).getAbsolutePath());
	    }
	} else {
	    Collections.addAll(classPath, packageNames);
	    Collections.addAll(filterPackage, packageNames);
	}
	
	/***
	 * 扫描有三种策略
	 * 1.jar文件
	 * 2.class文件
	 * 3.classPath目录
	 * */
	
	for (String path : classPath) {
	    try {
		if (path.endsWith(".jar")) {
		    parseJar(path, filterPackage, atcion);
		}else if (new File(path).isDirectory()) {
		    parseFile(path, null, filterPackage, atcion);
		} else {
		    final String packageDirectory = path.replace('.', '/');
		    final Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packageDirectory);
		    while (urls.hasMoreElements()) {
			final URL url = urls.nextElement();
			if ("file".equals(url.getProtocol())) {
			    parseFile(url.getPath(), url.getPath().replace(packageDirectory, ""), filterPackage,atcion);
			} else if ("jar".equals(url.getProtocol())) {
			    parseJar(url.getPath(),filterPackage, atcion);
			}
		    }
		}
	    } catch (Exception exception) {
		throw new RuntimeException(exception);
	    }
	}
    }

    private static void parseFile(String path, String root, Set<String> filterPackage,Consumer<Class<?>> atcion) throws Exception {
	File directory = new File(path);
	File rootDir = root == null ? directory : new File(root);
	if (!directory.isDirectory() || !rootDir.isDirectory()) {
	    throw new RuntimeException("package:[" + directory.getPath() + "] is not directory");
	}
	final Stack<File> scanDirectories = new Stack<File>();
	final Collection<File> classFiles = new ArrayList<File>();
	final FileFilter fileFilter = new FileFilter() {
	    @Override
	    public boolean accept(File file) {
		if (file.isDirectory()) {
		    scanDirectories.push(file);
		    return false;
		}
		return file.getName().matches(".*\\.class$");
	    }
	};
	scanDirectories.push(directory);
	while (!scanDirectories.isEmpty()) {
	    final File scanDirectory = scanDirectories.pop();
	    Collections.addAll(classFiles, scanDirectory.listFiles(fileFilter));
	}
	for (File file : classFiles) {
	    String rootPath = trimr(rootDir.getAbsolutePath(), File.separatorChar);
	    int from = rootPath.length() + 1;
	    String relName = file.getAbsolutePath().substring(from);
	    String clsName = sub(relName, 0, -6).replace('/', '.').replace('\\', '.');
	    boolean flag = true;
	    for (String f : filterPackage) {
		if (clsName.contains(f)) {
		    flag = true;
		    break;
		}
		flag = false;
	    }
	    if (!flag) {
		continue;
	    }
	    try {
		atcion.accept(Class.forName(clsName));
	    } catch (Exception e) {
		System.err.println("to class error :[" + clsName + "] path :" + path);
	    }
	}
    }

    private static void parseJar(String path, Set<String> filterPackage, Consumer<Class<?>> atcion) throws Exception {
	ZipFile zip = new ZipFile(new File(path));
	try {
	    final Enumeration<? extends ZipEntry> jarEntries = zip.entries();
	    while (jarEntries.hasMoreElements()) {
		final ZipEntry entry = jarEntries.nextElement();
		String name = entry.getName();
		if (name.endsWith(".class")) {
		    name = sub(name, 0, -6).replace('/', '.').replace('\\', '.');
		    boolean flag = true;
		    for (String f : filterPackage) {
			if (name.contains(f)) {
			    flag = true;
			    break;
			}
			flag = false;
		    }
		    if (!flag) {
			continue;
		    }
		    try {
			atcion.accept(Class.forName(name));
		    } catch (Throwable e) {
			//有的JAR里的类读会出错,原因是没有依赖完整的包
			if(LOGGER.isWarnEnabled()){
			    LOGGER.warn("to class error :[" + name + "] path :" + path);
			}
		    }
		}
	    }
	} finally {
	    zip.close();
	}
    }

    public static String trimr(String s, char suffix) {
	return (!s.isEmpty() && s.charAt(s.length() - 1) == suffix) ? sub(s, 0, -1) : s;
    }

    public static String sub(String s, int beginIndex, int endIndex) {
	if (endIndex < 0) {
	    endIndex = s.length() + endIndex;
	}
	return s.substring(beginIndex, endIndex);
    }
}
