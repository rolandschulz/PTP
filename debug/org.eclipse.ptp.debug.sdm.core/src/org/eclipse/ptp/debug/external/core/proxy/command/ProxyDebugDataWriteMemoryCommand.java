package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugDataWriteMemoryCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDataWriteMemoryCommand(IProxyClient client, BitList procs, 
			long offset, String address, String format, int wordSize, String value) {
		super(client, procs);
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
