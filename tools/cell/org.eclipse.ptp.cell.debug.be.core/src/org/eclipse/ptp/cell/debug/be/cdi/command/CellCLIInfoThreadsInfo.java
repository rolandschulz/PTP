/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.cdi.command;

import org.eclipse.cdt.debug.mi.core.output.CLIInfoThreadsInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class CellCLIInfoThreadsInfo extends CLIInfoThreadsInfo {

	protected String[] names;
	/**
	 * @param out
	 */
	public CellCLIInfoThreadsInfo(MIOutput out) {
		super(out);
		
	}
	
	public String[] getThreadNames() {
		
		return names;
	}
	
	protected void parse() {
		super.parse();
		names = new String[threadIds.length];
		for (int i = 0; i < threadIds.length; i++) {
			names[i] = obtainThreadName(threadIds[i]);
		}
	}

	protected String obtainThreadName(int tid) {
		
		return "cell"; //$NON-NLS-1$
	}
}
