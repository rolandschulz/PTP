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
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUMailbox;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class CellMISPUMailboxInfoInfo extends MIInfo {

	private MISPUMailbox[] mailboxes;
	/**
	 * @param out
	 */
	public CellMISPUMailboxInfoInfo(MIOutput out) {
		super(out);
	}
	
	public MISPUMailbox[] getMailboxes() {
		if (mailboxes == null) {
			parse();
		}
		return mailboxes;
	}
	
	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("SPUInfoMailbox")) { //$NON-NLS-1$
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
		MIResult[] tables = tuples.getMIResults();
		for (int j = 0; j < tables.length; j++) {
			String tableName = tables[j].getVariable();
			if (tableName.equals("mbox")) { //$NON-NLS-1$
				MIValue tableContent = tables[j].getMIValue();
				parseTable(tableContent, aList);
			}
		}
		
		if (aList.size() == 0) {
			aList.add(new MISPUMailbox(MISPUMailbox.MBOX,BEMessages.getString("SPUEnhancementsProcessor.0"),Integer.toString(0))); //$NON-NLS-1$
			aList.add(new MISPUMailbox(MISPUMailbox.IBOX,BEMessages.getString("SPUEnhancementsProcessor.0"),Integer.toString(0))); //$NON-NLS-1$
			aList.add(new MISPUMailbox(MISPUMailbox.WBOX,BEMessages.getString("SPUEnhancementsProcessor.0"),Integer.toString(0))); //$NON-NLS-1$
		}
		mailboxes = (MISPUMailbox[]) aList.toArray(new MISPUMailbox[] {});
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
				if (m.equals("mbox")) { //$NON-NLS-1$
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
		for (int i = 0; i < args.length; i++) {
			aList.add(new MISPUMailbox(args[i], Integer.toString(row)));
		}
	}
}
