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
import org.eclipse.ptp.cell.debug.be.cdi.command.output.MISPUSignal;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class CellMISPUSignalInfoInfo extends MIInfo {
	
	private MISPUSignal[] signals;

	/**
	 * @param out
	 */
	public CellMISPUSignalInfoInfo(MIOutput out) {
		super(out);
	}
	
	public MISPUSignal[] getSignals() {
		if (signals == null) {
			parse();
		}
		return signals;
	}
	
	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("SPUInfoSignal")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MITuple) {
							parseSignal((MITuple)val);
						}
					}
				}
			}
		}
	}
	
	protected void parseSignal(MITuple tuple) {
		MIResult[] args = tuple.getMIResults();
		List signalsList = new ArrayList();
		if (args.length == 6) {
			signalsList.add(new MISPUSignal(args[0]));
			signalsList.add(new MISPUSignal(args[1]));
			signalsList.add(new MISPUSignal(args[2]));
			signalsList.add(new MISPUSignal(args[3]));
			signalsList.add(new MISPUSignal(args[4]));
			signalsList.add(new MISPUSignal(args[5]));
			signals = (MISPUSignal[]) signalsList.toArray(new MISPUSignal[] {});
		} else {
			signalsList.add(new MISPUSignal(MISPUSignal.SIGNAL1_PENDING,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			signalsList.add(new MISPUSignal(MISPUSignal.SIGNAL1,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			signalsList.add(new MISPUSignal(MISPUSignal.SIGNAL1_TYPE,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			signalsList.add(new MISPUSignal(MISPUSignal.SIGNAL2_PENDING,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			signalsList.add(new MISPUSignal(MISPUSignal.SIGNAL2,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			signalsList.add(new MISPUSignal(MISPUSignal.SIGNAL2_TYPE,BEMessages.getString("SPUEnhancementsProcessor.0"))); //$NON-NLS-1$
			signals = (MISPUSignal[]) signalsList.toArray(new MISPUSignal[] {});
		}
		
	}

}
