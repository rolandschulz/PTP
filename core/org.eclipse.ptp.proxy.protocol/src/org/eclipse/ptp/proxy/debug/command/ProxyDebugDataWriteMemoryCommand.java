package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDataWriteMemoryCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDataWriteMemoryCommand(String bits, 
			long offset, String address, String format, int wordSize, String value) {
		super(DATAWRITEMEMORY, bits);
		addArgument(offset);
		addArgument(address);
		addArgument(format);
		addArgument(wordSize);
		addArgument(value);
	}
	
	public ProxyDebugDataWriteMemoryCommand(int transID, String[] args) {
		super(DATAWRITEMEMORY, transID, args);
	}
}
