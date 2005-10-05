package org.eclipse.ptp.debug.external.proxy;

import java.io.IOException;

import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugClient extends AbstractProxyClient {

	public ProxyDebugClient(String host, int port) {
		super(host, port);
	}

	public void debugStartSession(String prog, String args) throws IOException {
		sendCommand("INI", "\""+ prog + "\" \"" + args + "\"");
	}
	
	public void debugSetLineBreakpoint(BitList procs, String file, int line) throws IOException {
		sendCommand("SLB", procs, "\"" + file + "\" " + line);
	}
	
	public void debugSetFuncBreakpoint(BitList procs, String file, String func) throws IOException {
		sendCommand("SFB", procs, "\"" + file + "\" \"" + func + "\"");
	}
	
	public void debugDeleteBreakpoint(BitList procs, int bpid) throws IOException {
		sendCommand("DBP", procs, Integer.toString(bpid));
	}
	
	public void debugGo(BitList procs) throws IOException {
		sendCommand("GOP", procs);
	}
	
	public void debugStep(BitList procs, int count, int type) throws IOException {
		sendCommand("STP", procs, count + " " + type);
	}
	
	public void debugTerminate(BitList procs) throws IOException {
		sendCommand("HLT", procs);
	}
	
	public void debugListStackframes(BitList procs, int current) throws IOException {
		sendCommand("LSF", procs, Integer.toString(current));
	}

	public void debugSetCurrentStackframe(BitList procs, int level) throws IOException {
		sendCommand("SCS", procs, Integer.toString(level));
	}

	public void debugEvaluateExpression(BitList procs, String expr) throws IOException {
		sendCommand("EEX", procs, "\""+ expr + "\"");
	}

	public void debugGetType(BitList procs, String expr) throws IOException {
		sendCommand("TYP", procs, "\""+ expr + "\"");
	}

	public void debugListLocalVariables(BitList procs) throws IOException {
		sendCommand("LLV", procs);
	}

	public void debugListArguments(BitList procs) throws IOException {
		sendCommand("LAR", procs);
	}

	public void debugListGlobalVariables(BitList procs) throws IOException {
		sendCommand("LGV", procs);
	}

	public IProxyEvent convertEvent(String str) {
		return ProxyDebugEvent.toEvent(str);
	}
}
