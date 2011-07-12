/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.managedbuilder.gnu.ui.scannerdiscovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerProjectDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class PerProjectSICollectorSkipCygTrans extends PerProjectSICollector {
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#updateScannerConfiguration(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public synchronized void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
        IDiscoveredPathInfo pathInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project, context);
        if (pathInfo instanceof IPerProjectDiscoveredPathInfo) {
            IPerProjectDiscoveredPathInfo projectPathInfo = (IPerProjectDiscoveredPathInfo) pathInfo;
            
            monitor.beginTask(MakeMessages.getString("ScannerInfoCollector.Processing"), 100); //$NON-NLS-1$
            monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
            if (scannerConfigNeedsUpdate(projectPathInfo)) {
                monitor.worked(50);
                monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Updating") + project.getName()); //$NON-NLS-1$
                try {
                    // update scanner configuration
					List<IResource> resourceDelta = new ArrayList<IResource>(1);
					resourceDelta.add(project);
                    MakeCorePlugin.getDefault().getDiscoveryManager().updateDiscoveredInfo(context, pathInfo, context.isDefaultContext(), resourceDelta);
                    monitor.worked(50);
                } catch (CoreException e) {
                    MakeCorePlugin.log(e);
                }
            }
            monitor.done();
            scPersisted = true;
        }
	}
	
	/**
	 * Compare discovered include paths and symbol definitions with the ones from scanInfo.
	 */
	private boolean scannerConfigNeedsUpdate(IPerProjectDiscoveredPathInfo discPathInfo) {
		boolean addedIncludes = includePathsNeedUpdate(discPathInfo);
		boolean addedSymbols = definedSymbolsNeedUpdate(discPathInfo);
		
		return (addedIncludes | addedSymbols);
	}
	
	/**
	 * Compare include paths with already discovered.
	 */
	private boolean includePathsNeedUpdate(IPerProjectDiscoveredPathInfo discPathInfo) {
		boolean addedIncludes = false;
        List<String> discoveredIncludes = discoveredSI.get(ScannerInfoTypes.INCLUDE_PATHS);
		if (discoveredIncludes != null) {
			// Step 1. Add discovered scanner config to the existing discovered scanner config 
			// add the includes from the latest discovery
            addedIncludes = addItemsWithOrder(sumDiscoveredIncludes, discoveredIncludes, true);
			
			// Step 2. Get project's scanner config
			LinkedHashMap<String, Boolean> persistedIncludes = discPathInfo.getIncludeMap();
	
			// Step 3. Merge scanner config from steps 1 and 2
			// order is important, use list to preserve it
			ArrayList<String> persistedKeyList = new ArrayList<String>(persistedIncludes.keySet());
			addedIncludes = addItemsWithOrder(persistedKeyList, sumDiscoveredIncludes, true);
			
			LinkedHashMap<String, Boolean> newPersistedIncludes;
			if (addedIncludes) {
				newPersistedIncludes = new LinkedHashMap<String, Boolean>(persistedKeyList.size());
				for (String include : persistedKeyList) {
					if (persistedIncludes.containsKey(include)) {
						newPersistedIncludes.put(include, persistedIncludes.get(include));
					}
					else {
						// the paths may be on EFS resources, not local
						Boolean includePathExists = true;
						URI projectLocationURI = discPathInfo.getProject().getLocationURI();
						
						// use the project's location... create a URI that uses the same provider but that points to the include path
						URI includeURI = EFSExtensionManager.getDefault().createNewURIFromPath(projectLocationURI, include);
						
						// ask EFS if the path exists
						try {
							IFileStore fileStore = EFS.getStore(includeURI);
							IFileInfo info = fileStore.fetchInfo();
							if(!info.exists()) {
								includePathExists = false;
							}
						} catch (CoreException e) {
							MakeCorePlugin.log(e);
						}
						
						// if the include path doesn't exist, then we tell the scanner config system that the folder
						// has been "removed", and thus it won't show up in the UI
						newPersistedIncludes.put(include, !includePathExists);
					}
				}
			}
			else {
				newPersistedIncludes = persistedIncludes;
			}
			
			// Step 4. Set resulting scanner config
			discPathInfo.setIncludeMap(newPersistedIncludes);
		}
		return addedIncludes;
	}
	
}
