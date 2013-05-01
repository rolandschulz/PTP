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
package org.eclipse.ptp.internal.debug.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.core.model.IPWatchpoint;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * @author Clement chu
 * 
 */
public class PBreakpointWorkbenchAdapterFactory implements IAdapterFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType != IWorkbenchAdapter.class || !(adaptableObject instanceof IPBreakpoint)) {
			return null;
		}
		return new WorkbenchAdapter() {
			@Override
			public String getLabel(Object o) {
				// for now
				if (o instanceof IPLineBreakpoint) {
					return Messages.PBreakpointWorkbenchAdapterFactory_0;
				}
				if (o instanceof IPWatchpoint) {
					return Messages.PBreakpointWorkbenchAdapterFactory_1;
				}
				return super.getLabel(o);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}
}
