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
package org.eclipse.fdt.debug.mi.core.output;

import java.util.StringTokenizer;


/**
 * GDB/MI info program parsing.
(gdb) 
info program
&"info program\n"
~"\tUsing the running image of child process 21301.\n"
~"Program stopped at 0x804853f.\n"
~"It stopped at breakpoint 1.\n"
~"Type \"info stack\" or \"info registers\" for more information.\n"
^done
(gdb) 

 */
public class MIInfoProgramInfo extends MIInfo {

	int pid;

	public MIInfoProgramInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getPID() {
		return pid;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIOOBRecord[] oobs = out.getMIOOBRecords();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MIConsoleStreamOutput) {
					MIStreamRecord cons = (MIStreamRecord) oobs[i];
					String str = cons.getString();
					// We are interested in the signal info
					parseLine(str);
				}
			}
		}
	}

	void parseLine(String str) {
		if (str != null && str.length() > 0) {
			str = str.replace('.', ' ');
			str = str.trim();
			if (str.startsWith("Using")) { //$NON-NLS-1$
				StringTokenizer st = new StringTokenizer(str);
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					if (Character.isDigit(s.charAt(0))) {
						try {
							pid = Integer.decode(s).intValue();
							break;
						} catch (NumberFormatException e) {
						}
					}
				}
			}
		}
	}
}
