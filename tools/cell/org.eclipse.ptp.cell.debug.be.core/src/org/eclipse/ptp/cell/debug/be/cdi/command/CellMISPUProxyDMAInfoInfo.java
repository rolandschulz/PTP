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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIList;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MITuple;
import org.eclipse.cdt.debug.mi.core.output.MIValue;
import org.eclipse.ptp.cell.debug.be.BEMessages;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUDMAListTuple;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUProxyDMAElement;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class CellMISPUProxyDMAInfoInfo extends MIInfo {

	private MISPUProxyDMAElement[] dmacmds;
	
	private MISPUDMAListTuple[] dmacmdlist;
	
	/**
	 * @param out
	 */
	public CellMISPUProxyDMAInfoInfo(MIOutput out) {
		super(out);
	}
	
	public MISPUDMAListTuple[] getProxyDMAcmdlist() {
		if (dmacmdlist == null) {
			parse();
		}
		return dmacmdlist;
	}

	public MISPUProxyDMAElement[] getProxyDMAcmds() {
		if (dmacmds == null) {
			parse();
		}
		return dmacmds;
	}
	
	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("SPUInfoProxyDMA")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple) {
							parseOutterTuple(((MITuple)val));
						}
					}
				}
			}
		}
	}

	protected void parseOutterTuple(MITuple tuples) {
		List aList = new ArrayList();
		List bList = new ArrayList();
		MIResult[] tables = tuples.getMIResults();
		for (int j = 0; j < tables.length; j++) {
			String tableName = tables[j].getVariable();
			if (tableName.equals("dma_cmd")) { //$NON-NLS-1$
				MIValue tableContent = tables[j].getMIValue();
				parseTable(tableContent, aList);
			} else if (tableName.equals("proxydma_info_type")) { //$NON-NLS-1$
				bList.add(new MISPUProxyDMAElement(tables[j]));
			} else if (tableName.equals("proxydma_info_mask")) { //$NON-NLS-1$
				bList.add(new MISPUProxyDMAElement(tables[j]));
			} else if (tableName.equals("proxydma_info_status")) { //$NON-NLS-1$
				bList.add(new MISPUProxyDMAElement(tables[j]));
			}
			
		}
		
		if (aList.size() == 0) {
			aList.add(new MISPUDMAListTuple(BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"),BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		}
		if (bList.size() == 0) {
			bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_TYPE,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_MASK,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			bList.add(new MISPUProxyDMAElement(MISPUProxyDMAElement.PROXYDMA_INFO_STATUS,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			
		}
		dmacmdlist = (MISPUDMAListTuple[]) aList.toArray(new MISPUDMAListTuple[] {});
		dmacmds = (MISPUProxyDMAElement[]) bList.toArray(new MISPUProxyDMAElement[] {});
	}		

	protected void parseTable(MIValue val, List aList) {
		if (val instanceof MITuple) {
			MIResult[] table = ((MITuple)val).getMIResults();
			for (int j = 0; j < table.length; j++) {
				String variable = table[j].getVariable();
				if (variable.equals("body")) { //$NON-NLS-1$
					parseBody(table[j].getMIValue(), aList);
				}
			}
		}
	}
	
	protected void parseBody(MIValue body, List aList) {
		if (body instanceof MIList) {
			MIResult[] mboxs = ((MIList)body).getMIResults();
			for (int i = 0; i < mboxs.length; i++) {
				String m = mboxs[i].getVariable();
				if (m.equals("cmd")) { //$NON-NLS-1$
					MIValue value = mboxs[i].getMIValue();
					if (value instanceof MITuple) {
						parseTuple( ((MITuple)value),aList, i);
					}
				}
			}
		}
	}
	
	protected void parseTuple(MITuple tuple, List aList, int row) {
		MIResult[] args = tuple.getMIResults();
		aList.add(new MISPUDMAListTuple(args));
	}
}
