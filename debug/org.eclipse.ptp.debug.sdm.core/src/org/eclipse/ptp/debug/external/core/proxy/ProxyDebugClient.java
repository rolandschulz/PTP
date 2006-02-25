/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.debug.external.core.proxy;

import java.io.IOException;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugClient extends AbstractProxyDebugClient {
	public static final int STEP_INTO = 0;
	public static final int STEP_OVER = 1;
	public static final int STEP_FINISH = 2;
	
	public ProxyDebugClient() {
		super();
	}

	public void debugStartSession(String prog, String path, String dir, String[] args) throws IOException {
		sendCommand("INI", prog, path, dir, args);
	}
	
	public void debugSetLineBreakpoint(BitList procs, int bpid, boolean isTemporary, boolean isHardware, String file, int line, String expression, int ignoreCount, int tid) throws IOException {
		String[] args = new String[] {
			Integer.toString(bpid),
			Integer.toString(isTemporary?1:0),
			Integer.toString(isHardware?1:0),
			file,
			Integer.toString(line),
			expression,
			Integer.toString(ignoreCount),
			Integer.toString(tid)
		};
		sendCommand("SLB", procs, args);
	}
	
	public void debugSetFuncBreakpoint(BitList procs, int bpid, boolean isTemporary, boolean isHardware, String file, String func, String expression, int ignoreCount, int tid) throws IOException {
		String[] args = new String[] {
			Integer.toString(bpid),
			Integer.toString(isTemporary?1:0),
			Integer.toString(isHardware?1:0),
			file,
			func,
			expression,
			Integer.toString(ignoreCount),
			Integer.toString(tid)
		};
		sendCommand("SFB", procs, args);
	}
	
	public void debugDeleteBreakpoint(BitList procs, int bpid) throws IOException {
		sendCommand("DBP", procs, Integer.toString(bpid));
	}

	public void debugEnableBreakpoint(BitList procs, int bpid) throws IOException {
		sendCommand("EAB", procs, Integer.toString(bpid));
	}

	public void debugDisableBreakpoint(BitList procs, int bpid) throws IOException {
		sendCommand("DAB", procs, Integer.toString(bpid));
	}

	public void debugConditionBreakpoint(BitList procs, int bpid, String expr) throws IOException {
		sendCommand("CBP", procs, Integer.toString(bpid), expr);
	}

	public void debugGo(BitList procs) throws IOException {
		sendCommand("GOP", procs);
	}
	
	public void debugStep(BitList procs, int count, int type) throws IOException {
		sendCommand("STP", procs, Integer.toString(count), Integer.toString(type));
	}
	
	public void debugTerminate(BitList procs) throws IOException {
		sendCommand("TRM", procs);
	}
	
	public void debugInterrupt(BitList procs) throws IOException {
		sendCommand("HLT", procs);
	}

	public void debugListStackframes(BitList procs, int current) throws IOException {
		sendCommand("LSF", procs, Integer.toString(current));
	}

	public void debugSetCurrentStackframe(BitList procs, int level) throws IOException {
		sendCommand("SCS", procs, Integer.toString(level));
	}

	public void debugEvaluateExpression(BitList procs, String expr) throws IOException {
		sendCommand("EEX", procs, expr);
	}

	public void debugGetType(BitList procs, String expr) throws IOException {
		sendCommand("TYP", procs, expr);
	}

	public void debugListLocalVariables(BitList procs) throws IOException {
		sendCommand("LLV", procs);
	}

	public void debugListArguments(BitList procs, int level) throws IOException {
		sendCommand("LAR", procs, Integer.toString(level));
	}

	public void debugListGlobalVariables(BitList procs) throws IOException {
		sendCommand("LGV", procs);
	}
}
