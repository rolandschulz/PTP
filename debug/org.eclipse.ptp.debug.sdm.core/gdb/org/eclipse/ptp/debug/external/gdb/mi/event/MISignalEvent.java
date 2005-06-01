/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.gdb.mi.event;




import org.eclipse.ptp.debug.external.gdb.mi.MISession;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIConst;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIExecAsyncOutput;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIFrame;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIResult;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIResultRecord;
import org.eclipse.ptp.debug.external.gdb.mi.output.MITuple;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIValue;




/**
 *  *stopped,reason="signal-received",signal-name="SIGINT",signal-meaning="Interrupt",thread-id="0",frame={addr="0x400e18e1",func="__libc_nanosleep",args=[],file="__libc_nanosleep",line="-1"}
 *
 */
public class MISignalEvent extends MIStoppedEvent {

	String sigName = ""; //$NON-NLS-1$
	String sigMeaning = ""; //$NON-NLS-1$

	public MISignalEvent(MISession source, MIExecAsyncOutput async) {
		super(source, async);
		parse();
	}

	public MISignalEvent(MISession source, MIResultRecord record) {
		super(source, record);
		parse();
	}

	public String getName() {
		return sigName;
	}

	public String getMeaning() {
		return sigMeaning;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("signal-name=" + sigName + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("signal-meaning=" + sigMeaning + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("thread-id=").append(getThreadId()).append('\n'); //$NON-NLS-1$
		MIFrame f = getFrame();
		if (f != null) {
			buffer.append(f.toString());
		}
		return buffer.toString();
	}

	void parse () {
		MIExecAsyncOutput exec = getMIExecAsyncOutput();
		MIResultRecord rr = getMIResultRecord();

		MIResult[] results = null;
		if (exec != null) {
			results = exec.getMIResults();
		} else if (rr != null) {
			results = rr.getMIResults();
		}
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				MIValue value = results[i].getMIValue();
				String str = ""; //$NON-NLS-1$
				if (value instanceof MIConst) {
					str = ((MIConst)value).getString();
				}

				if (var.equals("signal-name")) { //$NON-NLS-1$
					sigName = str;
				} else if (var.equals("signal-meaning")) { //$NON-NLS-1$
					sigMeaning = str;
				} else if (var.equals("thread-id")) { //$NON-NLS-1$
					try {
						int id = Integer.parseInt(str.trim());
						setThreadId(id);
					} catch (NumberFormatException e) {
					}
				} else if (var.equals("frame")) { //$NON-NLS-1$
					if (value instanceof MITuple) {
						MIFrame f = new MIFrame((MITuple)value);
						setFrame(f);
					}
				}
			}
		}
	}
}
