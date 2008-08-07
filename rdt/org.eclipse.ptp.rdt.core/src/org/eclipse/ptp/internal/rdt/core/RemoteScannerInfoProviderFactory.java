/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rdt.core.RDTLog;

/**
 * Used to get instances of RemoteScannerInfoProvider.
 * 
 * The factory is not part of the remote JAR because it depends on
 * eclipse and CDT APIs.
 * 
 * @author Mike Kucera
 *
 */
public class RemoteScannerInfoProviderFactory {
	
	
	/**
	 * Returns a RemoteScannerInfoProvider that contains all the IScannerInfos
	 * for all the translation units in the given scope (project name).
	 * @param scopeName the name of a project
	 */
	public static RemoteScannerInfoProvider getProvider(String scopeName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(scopeName);
		if(project == null)
			return new RemoteScannerInfoProvider();
		return getProvider(project);
	}
	
	
	/**
	 * Returns a RemoteScannerInfoProvider that contains all the IScannerInfos
	 * for all the translation units in the given project.
	 */
	public static RemoteScannerInfoProvider getProvider(IProject project) {
		final List<ICElement> elements = new ArrayList<ICElement>();
		IResourceVisitor resourceVisitor = new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (!(resource instanceof IFile))
					return true;
				ITranslationUnit tu = CoreModelUtil.findTranslationUnit((IFile) resource);
				if(tu != null)
					elements.add(tu);
				return true;
			}
		};
		
		try {
			project.accept(resourceVisitor);
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
		
		return getProvider(elements);
	}
	
	
	/**
	 * This class is used to guarantee a consistent implementation
	 * of equals() and hashCode() for using IScannerInfo objects as
	 * keys in a Map.
	 */
	private static class ScannerInfoKey {
		private IScannerInfo scannerInfo;
		private int hashCode = 0;

		public ScannerInfoKey(IScannerInfo scannerInfo) {
			this.scannerInfo = scannerInfo;
		}

		public boolean equals(Object obj) {
			// no need for instanceof check
			ScannerInfoKey other = (ScannerInfoKey)obj; 
			return scannerInfo.getDefinedSymbols().equals(other.scannerInfo.getDefinedSymbols())
			    && Arrays.equals(scannerInfo.getIncludePaths(), other.scannerInfo.getIncludePaths());
		}

		public int hashCode() {
			if(hashCode == 0) {
				hashCode = scannerInfo.getDefinedSymbols().hashCode() 
			             + Arrays.hashCode(scannerInfo.getIncludePaths());
			}
			return hashCode;
		}
	}
	
	

	/**
	 * Returns a RemoteScannerInfoProvider that contains IScannerInfos for every
	 * translation unit in the given list of ICElements. The returned RemoteScannerInfoProvider
	 * is optimized to not contain any duplicate scanner infos.
	 */
	public static RemoteScannerInfoProvider getProvider(List<ICElement> elements) {
		// this map is used to build the RemoteScannerInfo
		Map<String,RemoteScannerInfo> scannerInfoMap = new HashMap<String,RemoteScannerInfo>();
		
		// the cache of shared instances of RemoteScannerInfo
		Map<ScannerInfoKey,RemoteScannerInfo> cache = new HashMap<ScannerInfoKey,RemoteScannerInfo>();
		
		for(ICElement element : elements) {
			if(element instanceof ITranslationUnit) {
				IProject project = element.getCProject().getProject();
				IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
				
				IScannerInfo localScannerInfo = provider.getScannerInformation(element.getResource());
				ScannerInfoKey key = new ScannerInfoKey(localScannerInfo);
				
				// If we have already seen a IScannerInfo that is identical then just reuse the
				// existing remote version of it.
				RemoteScannerInfo remoteScannerInfo = cache.get(key);

				if(remoteScannerInfo == null) {
					remoteScannerInfo = new RemoteScannerInfo(localScannerInfo);
					cache.put(key, remoteScannerInfo);
				}
				
				String path = element.getLocationURI().getPath();
				scannerInfoMap.put(path, remoteScannerInfo);
			}
		}
		
		return new RemoteScannerInfoProvider(scannerInfoMap);
	}
	

	/**
	 * Convenience method for getting a RemoteScannerInfo for a resource.
	 */
	public static RemoteScannerInfo getScannerInfo(IResource resource) {
		final IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(resource.getProject());
		IScannerInfo scannerInfo = provider.getScannerInformation(resource);
		return new RemoteScannerInfo(scannerInfo);
	}
}
