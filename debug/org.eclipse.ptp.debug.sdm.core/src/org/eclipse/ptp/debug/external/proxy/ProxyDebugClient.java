package org.eclipse.ptp.debug.external.proxy;

import java.io.IOException;

import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.FastBitSet;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyDebugClient extends AbstractProxyClient {

	public ProxyDebugClient(String host, int port) {
		super(host, port);
	}

	public void debugStartSession(String prog, String args) throws IOException {
		sendCommand("INI", "\""+ prog + "\" \"" + args + "\"");
	}
	
	public void debugSetLineBreakpoint(FastBitSet procs, String file, int line) throws IOException {
		sendCommand("SLB", procs, "\"" + file + "\" " + line);
	}
	
	public void debugSetFuncBreakpoint(FastBitSet procs, String file, String func) throws IOException {
		sendCommand("SFB", procs, "\"" + file + "\" \"" + func + "\"");
	}
	
	public void debugDeleteBreakpoint(FastBitSet procs, int bpid) throws IOException {
		sendCommand("DBP", procs, Integer.toString(bpid));
	}
	
	public void debugGo(FastBitSet procs) throws IOException {
		sendCommand("GOP", procs);
	}
	
	public void debugStep(FastBitSet procs, int count, int type) throws IOException {
		sendCommand("STP", procs, count + " " + type);
	}
	
	public void debugTerminate(FastBitSet procs) throws IOException {
		sendCommand("HLT", procs);
	}
	
	public void debugListStackframes(FastBitSet procs, int current) throws IOException {
		sendCommand("LSF", procs, Integer.toString(current));
	}

	public void debugSetCurrentStackframe(FastBitSet procs, int level) throws IOException {
		sendCommand("SCS", procs, Integer.toString(level));
	}

	public void debugEvaluateExpression(FastBitSet procs, String expr) throws IOException {
		sendCommand("EEX", procs, "\""+ expr + "\"");
	}

	public void debugGetType(FastBitSet procs, String expr) throws IOException {
		sendCommand("TYP", procs, "\""+ expr + "\"");
	}

	public void debugListLocalVariables(FastBitSet procs) throws IOException {
		sendCommand("LLV", procs);
	}

	public void debugListArguments(FastBitSet procs) throws IOException {
		sendCommand("LAR", procs);
	}

	public void debugListGlobalVariables(FastBitSet procs) throws IOException {
		sendCommand("LGV", procs);
	}

	public IProxyEvent convertEvent(String str) {
		return ProxyDebugEvent.toEvent(str);
	}
}
