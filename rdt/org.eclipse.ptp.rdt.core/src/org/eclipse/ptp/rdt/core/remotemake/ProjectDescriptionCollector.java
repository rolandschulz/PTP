/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.remotemake;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.core.RDTLog;

public class ProjectDescriptionCollector implements IScannerInfoCollector3 {

	private IProject fProject;

	private InfoContext fInfoContext;


	@SuppressWarnings("unchecked")
	public void contributeToScannerConfig(Object resource, Map/*<ScannerInfoTypes, List<String>>*/ scannerInfo) {
		if(!(resource instanceof IResource))
			return;
		
		Map<ScannerInfoTypes, List<String>> info = (Map<ScannerInfoTypes, List<String>>)scannerInfo;
		
		// get a writable description
		IProject project = ((IResource)resource).getProject();
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project, true);
		
		for(ICConfigurationDescription config : projectDescription.getConfigurations()) {
			if (config.getRootFolderDescription() != null) {
				for (ICLanguageSetting setting : config
						.getRootFolderDescription().getLanguageSettings()) {

					List<String> paths = info.get(ScannerInfoTypes.INCLUDE_PATHS);
					addSettingEntries(setting, ICSettingEntry.INCLUDE_PATH, paths, INCLUDE_PATH_FACTORY);

					List<String> macros = info.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
					addSettingEntries(setting, ICSettingEntry.MACRO, macros,
							MACRO_FACTORY);
				}
			}
		}
		
		try {
			// save to .cproject file
			CoreModel.getDefault().setProjectDescription(project, projectDescription);
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
	}

	
	
	private void addSettingEntries(ICLanguageSetting setting, int settingType, List<String> values, SettingFactory factory) {
		if(values.isEmpty())
			return;
		
		// maintain insertion order and remote duplicates
		Set<ICLanguageSettingEntry> entries = new LinkedHashSet<ICLanguageSettingEntry>();
		
		// need to do this or else existing settings will disappear
		List<ICLanguageSettingEntry> existingEntries = setting.getSettingEntriesList(settingType);
		entries.addAll(existingEntries);
		
		for(String value : values) {
			entries.add(factory.createEntry(value));
		}
		
		setting.setSettingEntries(settingType, new ArrayList<ICLanguageSettingEntry>(entries));
	}
	
	
	// Abstract Factory pattern
	private interface SettingFactory {
		ICLanguageSettingEntry createEntry(String value);
	}
	
	private static final SettingFactory MACRO_FACTORY = new SettingFactory() {
		public ICLanguageSettingEntry createEntry(String value) {
			String[] split = value.split("="); //$NON-NLS-1$
			switch(split.length) {
				case 2 : return new CMacroEntry(split[0], split[1], 0);
				default: return new CMacroEntry(value, null, 0);
			}
		}
	};
	
	private static final SettingFactory INCLUDE_PATH_FACTORY = new SettingFactory() {
		public ICLanguageSettingEntry createEntry(String value) {
			return new CIncludePathEntry(value, 0);
		}
	};
	

	public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
		return null;
	}

	public void setInfoContext(InfoContext context) {
		fInfoContext = context;
		fProject = fInfoContext.getProject();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#createPathInfoObject()
	 */
	public IDiscoveredPathInfo createPathInfoObject() {
		return new DiscoveredPathInfo(fProject);
	}

	public void setProject(IProject project) {
		fProject = project;
	}


	public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
	}

}
