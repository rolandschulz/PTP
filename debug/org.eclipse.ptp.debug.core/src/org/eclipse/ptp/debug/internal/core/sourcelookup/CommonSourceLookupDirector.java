/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.ptp.debug.internal.core.sourcelookup; 

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.internal.core.IPDebugInternalConstants;
 
/**
 * Director of the common source containers.
 */
public class CommonSourceLookupDirector extends CSourceLookupDirector {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector#setSourceContainers(org.eclipse.debug.core.sourcelookup.ISourceContainer[])
	 */
	public void setSourceContainers( ISourceContainer[] containers ) {
		try {
			super.setSourceContainers( containers );
			PTPDebugCorePlugin.getDefault().getPluginPreferences().setValue( IPDebugInternalConstants.PREF_COMMON_SOURCE_CONTAINERS, getMemento() );
			PTPDebugCorePlugin.getDefault().savePluginPreferences();
		}
		catch( CoreException e ) {
			PTPDebugCorePlugin.log( e.getStatus() );
		}
	}
}
