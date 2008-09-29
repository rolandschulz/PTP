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
package org.eclipse.ptp.cell.debug.be.cdi.command.factories;

import org.eclipse.cdt.debug.mi.core.command.CLIInfoThreads;
import org.eclipse.cdt.debug.mi.core.command.MIThreadListIds;
import org.eclipse.cdt.debug.mi.core.command.factories.StandardCommandFactory;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellCLIInfoThreads;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUDMAInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUEventInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUMailboxInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUProxyDMAInfo;
import org.eclipse.ptp.cell.debug.be.cdi.command.CellMISPUSignalInfo;


/**
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class StandardCellCommandFactory extends StandardCommandFactory {

	/**
	 * 
	 */
	public StandardCellCommandFactory() {
		
	}

	/**
	 * @param miVersion
	 */
	public StandardCellCommandFactory(String miVersion) {
		super(miVersion);
		
	}
	
	public MIThreadListIds createMIThreadListIds() {
		return new MIThreadListIds(getMIVersion());
	}

	public CLIInfoThreads createCLIInfoThreads() {
		return new CellCLIInfoThreads();
	}
	
	public CellMISPUDMAInfo createMISPUDMAInfo() {
		return new CellMISPUDMAInfo(getMIVersion());
	}
	
	public CellMISPUEventInfo createMISPUEventInfo() {
		return new CellMISPUEventInfo(getMIVersion());
	}

	public CellMISPUMailboxInfo createMISPUMailboxInfo() {
		return new CellMISPUMailboxInfo(getMIVersion());
	}
	
	public CellMISPUProxyDMAInfo createMISPUProxyDMAInfo() {
		return new CellMISPUProxyDMAInfo(getMIVersion());
	}
	
	public CellMISPUSignalInfo createMISPUSignalInfo() {
		return new CellMISPUSignalInfo(getMIVersion());
	}
}
