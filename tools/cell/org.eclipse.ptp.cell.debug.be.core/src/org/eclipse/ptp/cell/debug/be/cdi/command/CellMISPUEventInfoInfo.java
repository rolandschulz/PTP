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
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MITuple;
import org.eclipse.cdt.debug.mi.core.output.MIValue;
import org.eclipse.ptp.cell.debug.be.BEMessages;
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUEvent;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class CellMISPUEventInfoInfo extends MIInfo {

	MISPUEvent[] events;
	
	/**
	 * @param out
	 */
	public CellMISPUEventInfoInfo(MIOutput out) {
		super(out);
	}
	
	public MISPUEvent[] getEvents() {
		if (events == null) {
			parse();
		}
		return events;
	}
	
	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("SPUInfoEvent")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple) {
							parseEvent((MITuple)val);
						}
					}
				}
			}
		}
	}
	
	protected void parseEvent(MITuple tuple) {
		MIResult[] args = tuple.getMIResults();
		List eventsList = new ArrayList();
		if (args.length == 2) {
			eventsList.add(new MISPUEvent(args[0]));
			eventsList.add(new MISPUEvent(args[1]));
			events = (MISPUEvent[]) eventsList.toArray(new MISPUEvent[] {});
		} else {
			eventsList.add(new MISPUEvent(MISPUEvent.EVENT_MASK,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			eventsList.add(new MISPUEvent(MISPUEvent.EVENT_STATUS,BEMessages.getString("SPUEnhancementsProcessor.1"))); //$NON-NLS-1$
			events = (MISPUEvent[]) eventsList.toArray(new MISPUEvent[] {});
		}
		
	}

}
