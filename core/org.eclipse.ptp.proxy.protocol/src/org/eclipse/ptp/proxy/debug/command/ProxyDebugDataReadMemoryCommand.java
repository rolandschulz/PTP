package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDataReadMemoryCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDataReadMemoryCommand(String bits, long offset, String address, 
			String format, int wordSize, int rows, int cols, Character asChar) {
		super(bits);
		addArgument(offset);
		addArgument(address);
		addArgument(format);
		addArgument(wordSize);
		addArgument(rows);
		addArgument(cols);
		addArgument(asChar);
	}

	public int getCommandID() {
		return DATAREADMEMORY;
	}
}
