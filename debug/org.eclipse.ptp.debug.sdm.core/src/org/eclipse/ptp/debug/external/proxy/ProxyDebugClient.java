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
		SendCommand("INI " + "\""+ prog + "\" \"" + args + "\"");
	}
	
	public void debugSetLineBreakpoint(FastBitSet procs, String file, int line) throws IOException {
		SendCommand("SLB " + procs.toString() + " \"" + file + "\" " + line);
	}
	
	public void debugSetFuncBreakpoint(FastBitSet procs, String file, String func) throws IOException {
		SendCommand("SFB " + procs.toString() + " \"" + file + "\" \"" + func + "\"");
	}
	
	public void debugDeleteBreakpoint(FastBitSet procs, int bpid) throws IOException {
		SendCommand("DBP " + procs.toString() + " " + bpid);
	}
	
	public void debugGo(FastBitSet procs) throws IOException {
		SendCommand("GOP " + procs.toString());
	}
	
	public void debugStep(FastBitSet procs, int count, int type) throws IOException {
		SendCommand("STP " + procs.toString() + " " + count + " " + type);
	}
	
	public void debugTerminate(FastBitSet procs) throws IOException {
		SendCommand("HLT " + procs.toString());
	}
	
	public void debugListStackframes(FastBitSet procs, int current) throws IOException {
		SendCommand("LSF " + procs.toString() + " " + current);
	}

	public void debugSetCurrentStackframe(FastBitSet procs, int level) throws IOException {
		SendCommand("SCS " + procs.toString() + " " + level);
	}

	public void debugEvaluateExpression(FastBitSet procs, String expr) throws IOException {
		SendCommand("EEX " + procs.toString() + " \""+ expr + "\"");
	}

	public void debugGetType(FastBitSet procs, String expr) throws IOException {
		SendCommand("TYP " + procs.toString() + " \""+ expr + "\"");
	}

	public void debugListLocalVariables(FastBitSet procs) throws IOException {
		SendCommand("LLV " + procs.toString());
	}

	public void debugListArguments(FastBitSet procs) throws IOException {
		SendCommand("LAR " + procs.toString());
	}

	public void debugListGlobalVariables(FastBitSet procs) throws IOException {
		SendCommand("LGV " + procs.toString());
	}

	public IProxyEvent convertEvent(String str) {
		return ProxyDebugEvent.toEvent(str);
	}
}
