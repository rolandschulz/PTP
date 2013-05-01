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
package org.eclipse.ptp.internal.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.osgi.service.prefs.Preferences;

public class CommonSourceLookupDirector extends PSourceLookupDirector {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector#
	 * setSourceContainers
	 * (org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	@Override
	public void setSourceContainers(ISourceContainer[] containers) {
		try {
			super.setSourceContainers(containers);
			Preferences preferences = new InstanceScope().getNode(PTPDebugCorePlugin.getUniqueIdentifier());
			preferences.put(IPDebugConstants.PREF_COMMON_SOURCE_CONTAINERS, getMemento());
		} catch (CoreException e) {
			PTPDebugCorePlugin.log(e.getStatus());
		}
	}
}
