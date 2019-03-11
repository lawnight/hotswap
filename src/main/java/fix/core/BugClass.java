package fix.core;

import java.io.File;

public class BugClass {

	private File file;

	private String packageName;

	private String name;

	private Class<?> clazz;

	private boolean internal = false;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public String getJarName() {
		if (internal) {
			return (clazz.getPackage().getName() + ".").replaceAll("\\.", "/") + file.getName();
		} else {
			return clazz.getName().replaceAll("\\.", "/") + ".class";
		}
	}

	public static void main(String[] args) {
	}

}
