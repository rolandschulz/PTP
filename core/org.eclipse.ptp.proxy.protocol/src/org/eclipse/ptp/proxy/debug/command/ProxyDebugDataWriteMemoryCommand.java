package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDataWriteMemoryCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDataWriteMemoryCommand(String bits, 
			long offset, String address, String format, int wordSize, String value) {
		super(bits);
		addArgument(offset);
		addArgument(address);
		addArgument(format);
		addArgument(wordSize);
		addArgument(value);
	}

	public int getCommandID() {
		return DATAWRITEMEMORY;
	}
}
