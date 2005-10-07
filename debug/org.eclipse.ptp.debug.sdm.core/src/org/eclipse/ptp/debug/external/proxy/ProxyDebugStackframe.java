package org.eclipse.ptp.debug.external.proxy;

public class ProxyDebugStackframe {
	private int		level;
	private String	file;
	private String	func;
	private int		line;
	private String	addr;
	
	public ProxyDebugStackframe(int level, String file, String func, int line, String addr) {
		this.level = level;
		this.file = file;
		this.func = func;
		this.line = line;
		this.addr = addr;
	}
	
	public int getLevel() {
		return this.level;
	}

	public String getFile() {
		return this.file;
	}
	
	public String getFunc() {
		return this.func;
	}
	
	public int getLine() {
		return this.line;
	}
	
	public String getAddr() {
		return this.addr;
	}
}
