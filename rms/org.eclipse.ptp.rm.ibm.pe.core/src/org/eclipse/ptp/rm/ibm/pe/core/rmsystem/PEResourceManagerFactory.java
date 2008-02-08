/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 *  
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceConstants;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceManager;
import org.eclipse.ptp.rm.remote.ui.preferences.PreferenceConstants;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

public class PEResourceManagerFactory extends AbstractResourceManagerFactory {

	public PEResourceManagerFactory() {
		super("PE");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#copyConfiguration(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public IResourceManagerConfiguration copyConfiguration(IResourceManagerConfiguration configuration) {
	    return (IResourceManagerConfiguration) configuration.clone();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#create(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public IResourceManagerControl create(IResourceManagerConfiguration confIn) {
		PEResourceManagerConfiguration configuration = (PEResourceManagerConfiguration) confIn;
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		final IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new PEResourceManager(universe.getNextResourceManagerId(), universe, configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#createConfiguration()
	 */
	public IResourceManagerConfiguration createConfiguration() {
		PEResourceManagerConfiguration conf = new PEResourceManagerConfiguration(this);
		
		Preferences preferences = PEPreferenceManager.getPreferences();
		
		conf.setProxyServerPath(preferences.getString(PreferenceConstants.PROXY_PATH));
		conf.setOptions(preferences.getInt(PreferenceConstants.OPTIONS));
		conf.setDebugLevel(preferences.getString(PEPreferenceConstants.TRACE_LEVEL));
		conf.setJobPollInterval(preferences.getString(PEPreferenceConstants.JOB_POLL_INTERVAL));
		conf.setLibraryOverride(preferences.getString(PEPreferenceConstants.LIBRARY_OVERRIDE));
		conf.setLoadLevelerMode(preferences.getString(PEPreferenceConstants.LOAD_LEVELER_MODE));
		conf.setNodeMaxPollInterval(preferences.getString(PEPreferenceConstants.NODE_MAX_POLL_INTERVAL));
		conf.setNodeMinPollInterval(preferences.getString(PEPreferenceConstants.NODE_MIN_POLL_INTERVAL));
		conf.setRunMiniproxy(preferences.getString(PEPreferenceConstants.RUN_MINIPROXY));
		conf.setUseLoadLeveler(preferences.getString(PEPreferenceConstants.LOAD_LEVELER_OPTION));

		return conf;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#loadConfiguration(org.eclipse.ui.IMemento)
	 */
	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return PEResourceManagerConfiguration.load(this, memento);
	}

}
