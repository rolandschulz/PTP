package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDataReadMemoryCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDataReadMemoryCommand(String bits, long offset, String address, 
			String format, int wordSize, int rows, int cols, Character asChar) {
		super(DATAREADMEMORY, bits);
		addArgument(offset);
		addArgument(address);
		addArgument(format);
		addArgument(wordSize);
		addArgument(rows);
		addArgument(cols);
		addArgument(asChar);
	}
	
	public ProxyDebugDataReadMemoryCommand(int transID, String[] args) {
		super(DATAREADMEMORY, transID, args);
	}
}
