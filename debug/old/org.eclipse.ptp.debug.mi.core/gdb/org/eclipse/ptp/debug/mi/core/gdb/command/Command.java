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

package org.eclipse.ptp.debug.mi.core.gdb.command;



import org.eclipse.ptp.debug.mi.core.gdb.MIException;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIInfo;
import org.eclipse.ptp.debug.mi.core.gdb.output.MILogStreamOutput;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIOOBRecord;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIOutput;
import org.eclipse.ptp.debug.mi.core.gdb.output.MIStreamRecord;



/**
 * A base class for all mi requests.
 */
public abstract class Command
{
	private static int globalCounter;

	int token = 0;
	MIOutput output;

	/**
	 * A global counter for all command, the token
	 * will be use to identify uniquely a command.
	 * Unless the value wraps around which is unlikely.
	 */
	private static synchronized int getUniqToken() {
		int count = ++globalCounter;
		// If we ever wrap around.
		if (count <= 0) {
			count = globalCounter = 1;
		}
		return count;
	}

	/**
	 * Returns the identifier of this request.
	 * 
	 * @return the identifier of this request
	 */
	public int getToken() {
		if (token == 0) {
			token = getUniqToken();
		}
		return token;
	}
	
//	public void setToken(int token) {
//		this.token = token;
//	}

	public MIOutput getMIOutput() {
		return output;
	}

	public void setMIOutput(MIOutput mi) {
		output = mi;
	}

	/**
	 * Parse the MIOutput generate after posting the command.
	 */
	public MIInfo getMIInfo () throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

	/**
	 * throw an MIException.
	 */
	protected void throwMIException (MIInfo info, MIOutput out) throws MIException {
		String mesg = info.getErrorMsg().trim();
		StringBuffer sb = new StringBuffer();
		MIOOBRecord[] oobs = out.getMIOOBRecords();
		for (int i = 0; i < oobs.length; i++) {
			if (oobs[i] instanceof MILogStreamOutput) {
				MIStreamRecord o = (MIStreamRecord) oobs[i];
				String s = o.getString();
				if (!s.trim().equalsIgnoreCase(mesg)) {
					sb.append(s);
				}
			}
		}
		String details = sb.toString();
		if (details.trim().length() == 0) {
			details = mesg;
		}
		throw new MIException(mesg, details);
	}

}