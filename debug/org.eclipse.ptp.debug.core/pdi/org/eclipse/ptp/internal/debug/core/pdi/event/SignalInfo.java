/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.event.IPDISignalInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class SignalInfo extends SessionObject implements IPDISignalInfo {
	private String name;
	private String desc;
	private IPDISignal signal;
	private IPDILocator locator;
	
	public SignalInfo(IPDISession session, TaskSet tasks, String name, String desc, IPDISignal signal, IPDILocator locator) {
		super(session, tasks);
		this.name = name;
		this.signal = signal;
		this.desc = desc;
		this.locator = locator;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISignalInfo#getDescription()
	 */
	public String getDescription() {
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISignalInfo#getLocator()
	 */
	public IPDILocator getLocator() {
		return locator;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISignalInfo#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISignalInfo#getSignal()
	 */
	public IPDISignal getSignal() {
		return signal;
	}	
}
