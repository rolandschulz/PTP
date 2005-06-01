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

package org.eclipse.ptp.debug.mi.core.gdb.output;



/**
 * GDB/MI stack list locals parsing.
 * -stack-list-locals 1
 * ^done,locals=[{name="p",value="0x8048600 \"ghislaine\""},{name="buf",value="\"'\", 'x' <repeats 24 times>, \"i,xxxxxxxxx\", 'a' <repeats 24 times>"},{name="buf2",value="\"\\\"?'\\\\()~\""},{name="buf3",value="\"alain\""},{name="buf4",value="\"\\t\\t\\n\\f\\r\""},{name="i",value="0"}]
 */
public class MIStackListLocalsInfo extends MIInfo {

	MIArg[] locals;

	public MIStackListLocalsInfo(MIOutput out) {
		super(out);
		parse();
	}

	public MIArg[] getLocals() {
		if (locals == null) {
			parse();
		}
		return locals;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("locals")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							locals = MIArg.getMIArgs((MIList)value);
						}
					}
				}
			}
		}
		if (locals == null) {
			locals = new MIArg[0];
		}
	}
}
