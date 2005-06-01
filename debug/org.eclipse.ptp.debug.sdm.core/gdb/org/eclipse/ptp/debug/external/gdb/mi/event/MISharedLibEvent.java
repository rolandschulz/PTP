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
import org.eclipse.ptp.debug.external.gdb.mi.output.MIExecAsyncOutput;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIResultRecord;






/**
 *
 */
public class MISharedLibEvent extends MIStoppedEvent {

	public MISharedLibEvent(MISession source, MIExecAsyncOutput async) {
		super(source, async);
		parse();
	}
 
	public MISharedLibEvent(MISession source, MIResultRecord record) {
		super(source, record);
		parse();
	}

}
