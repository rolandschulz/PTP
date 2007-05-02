package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugDataReadMemoryCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDataReadMemoryCommand(IProxyClient client, BitList procs, 
			long offset, String address, String format, int wordSize, int rows, int cols, Character asChar) {
		super(client, procs);
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
