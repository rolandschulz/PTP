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
package org.eclipse.ptp.debug.internal.core; 

import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
 
/**
 * Default preference value initializer for <code>CDebugCorePlugin</code>.
 */
public class PTPDebugCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/** 
	 * Constructor for CDebugCorePreferenceInitializer. 
	 */
	public PTPDebugCorePreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		PTPDebugCorePlugin.getDefault().getPluginPreferences().setDefault( IPDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, ICDIFormat.NATURAL );
		PTPDebugCorePlugin.getDefault().getPluginPreferences().setDefault( IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, ICDIFormat.NATURAL );

	}
}
