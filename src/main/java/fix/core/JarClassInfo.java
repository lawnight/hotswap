package fix.core;

public class JarClassInfo {
	/**
	 * 
	 */
	private String className;
	/**
	 * 
	 */
	private byte[] bytecode;

	/**
	 * @param className
	 * @param bytecode
	 */
	public JarClassInfo(String className, byte[] bytecode) {
		this.className = className;
		this.bytecode = bytecode;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the bytecode
	 */
	public byte[] getBytecode() {
		return bytecode;
	}
}
