/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.ptp.debug.external.cdi.event.EndSteppingRangeEvent;

/**
 */
public class EndSteppingRangeInfo extends SessionObject implements ICDIEndSteppingRange  {

	EndSteppingRangeEvent event;
	ICDILineLocation lineLoc;

	public EndSteppingRangeInfo(Session session, EndSteppingRangeEvent e, ICDILineLocation loc) {
		super(session);
		event = e;
		lineLoc = loc;
	}
	
	public ICDILineLocation getLineLocation() {
		return lineLoc;
	}

}
