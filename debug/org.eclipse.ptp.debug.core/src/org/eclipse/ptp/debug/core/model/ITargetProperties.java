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
package org.eclipse.ptp.debug.core.model;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;

/**
 * Interface for managing target properties
 * 
 * @author clement
 * 
 */
public interface ITargetProperties {
	public static final String PREF_INSTRUCTION_STEPPING_MODE = "instruction_stepping_mode"; //$NON-NLS-1$

	/**
	 * Adds a preference change listener to this target. Has no affect if the
	 * identical listener is already registered.
	 * 
	 * @param listener
	 *            a property change listener
	 * @since 5.0
	 */
	public void addPreferenceChangeListener(IPreferenceChangeListener listener);

	/**
	 * Removes the given listener from this target. Has no affect if the
	 * listener is not registered.
	 * 
	 * @param listener
	 *            a property change listener
	 * @since 5.0
	 */
	public void removePreferenceChangeListener(IPreferenceChangeListener listener);
}
