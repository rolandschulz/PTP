/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Mike Kucera (IBM)
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ptp.rdt.core.RDTLog;

/**
 * Used to get instances of RemoteIndexerInfoProvider.
 * 
 * The factory is not part of the remote JAR because it depends on
 * eclipse and CDT APIs.
 */
public class RemoteIndexerInfoProviderFactory {
	
	// These are the linkages supported by RDT
	private static final int[] LINKAGE_IDS = new int[] { ILinkage.C_LINKAGE_ID, ILinkage.CPP_LINKAGE_ID };
	
	
	/**
	 * Returns a RemoteIndexerInfoProvider that contains all the IScannerInfos and language mapping info
	 * for all the translation units in the given scope (project name).
	 * @param scopeName the name of a project
	 */
	public static RemoteIndexerInfoProvider getProvider(String scopeName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(scopeName);
		return getProvider(project);
	}
	
	
	/**
	 * Returns a RemoteIndexerInfoProvider that contains all the IScannerInfos and language mapping info
	 * for all the translation units in the given project.
	 * Returns an empty provider if the project is null.
	 */
	public static RemoteIndexerInfoProvider getProvider(IProject project) {
		if(project == null)
			return new RemoteIndexerInfoProvider();
		
		final List<ICElement> elements = new ArrayList<ICElement>();
		
		// TODO replace with ICElementVisitor
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

		ScannerInfoKey(IScannerInfo scannerInfo) {
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
	 * Maintains a cache of shared instances of RemoteScannerInfo.
	 */
	private static class RemoteScannerInfoCache {
		Map<ScannerInfoKey,RemoteScannerInfo> cache = new HashMap<ScannerInfoKey,RemoteScannerInfo>();
		
		RemoteScannerInfo get(IScannerInfo localScannerInfo) {
			// If we have already seen a IScannerInfo that is identical then just reuse the
			// existing remote version of it.
			ScannerInfoKey key = new ScannerInfoKey(localScannerInfo);
			RemoteScannerInfo remoteScannerInfo = cache.get(key);
			if(remoteScannerInfo == null) {
				remoteScannerInfo = new RemoteScannerInfo(localScannerInfo);
				cache.put(key, remoteScannerInfo);
			}
			return remoteScannerInfo;
		}
	}
	
	

	/**
	 * Returns a RemoteIndexerInfoProvider that contains IScannerInfos for every
	 * translation unit in the given list of ICElements. The returned RemoteIndexerInfoProvider
	 * is optimized to not contain any duplicate scanner infos.
	 * 
	 * It is assumed that all the elements are from the same project.
	 * 
	 * @throws NullPointerException if the given list is null or contains a null element
	 */
	public static RemoteIndexerInfoProvider getProvider(List<ICElement> elements) {
		if(elements.isEmpty())
			return new RemoteIndexerInfoProvider();
		
		Map<String,RemoteScannerInfo> scannerInfoMap = new HashMap<String,RemoteScannerInfo>();
		Map<Integer,RemoteScannerInfo> linkageMap = new HashMap<Integer,RemoteScannerInfo>();
		Map<String,String> languageMap = new HashMap<String,String>();
		Map<String,Boolean> isHeaderMap = new HashMap<String,Boolean>();
		
		// we assume all the elements are from the same project
		IProject project = elements.get(0).getCProject().getProject(); 
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		

		RemoteScannerInfoCache cache = new RemoteScannerInfoCache();
		
		for(ICElement element : elements) {
			if(element instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) element;
				
				// compute the scanner info, share identical scanner infos
				IScannerInfo localScannerInfo = provider.getScannerInformation(tu.getResource());
				RemoteScannerInfo remoteScannerInfo = cache.get(localScannerInfo);
				String path = element.getLocationURI().getPath();
				scannerInfoMap.put(path, remoteScannerInfo);
		
				// compute the language
				try {
					ILanguage language = tu.getLanguage();
					languageMap.put(path, language.getId());
				} catch (CoreException e) {
					RDTLog.logError(e);
				}
				
				// is it a header file?
				isHeaderMap.put(path, tu.isHeaderUnit());
			}
		}
		
		for(int id : LINKAGE_IDS) {
			IScannerInfo defaultScannerInfo = getDefaultScannerInfo(project, id);
			RemoteScannerInfo remoteScannerInfo = cache.get(defaultScannerInfo);
			linkageMap.put(id, remoteScannerInfo);
		}
		
		// compute the indexer preferences
		Properties props = IndexerPreferences.getProperties(project);
		Map<String,Boolean> preferences = computeIndexerPreferences(props);
		
		String filePref = (String) props.get(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT);
		List<String> filesToParseUpFront = Arrays.asList(filePref.split(",")); //$NON-NLS-1$
		
		// need to compute the languages of the files to parse up front
		// TODO there are two things wrong with this:
		// 1) The file may need to be parsed as more than one language, see PDOMIndexerTask.getLanguages()
		// 2) If there is a file in the project with the same name as a file to parse up front it may get the wrong scanner info
		for(String filename : filesToParseUpFront) {
			IContentType ct= CCorePlugin.getContentType(project, filename);
			if (ct != null) {
				ILanguage language = LanguageManager.getInstance().getLanguage(ct);
				languageMap.put(filename, language.getId());
			}
		}
		
		return new RemoteIndexerInfoProvider(scannerInfoMap, linkageMap, languageMap, isHeaderMap, preferences, filesToParseUpFront);
	}

	
	
	
	private static Map<String,Boolean> computeIndexerPreferences(Properties props) {
		Map<String,Boolean> prefs = new HashMap<String,Boolean>(props.size());
		//prefs.put(IRemoteIndexerInfoProvider.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG)));
		//prefs.put(IRemoteIndexerInfoProvider.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG)));
		prefs.put(IRemoteIndexerInfoProvider.KEY_INDEX_ALL_FILES, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_INDEX_ALL_FILES)));
		//prefs.put(IRemoteIndexerInfoProvider.KEY_INCLUDE_HEURISTICS, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_INCLUDE_HEURISTICS)));
		prefs.put(IRemoteIndexerInfoProvider.KEY_SKIP_ALL_REFERENCES, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_SKIP_ALL_REFERENCES)));
		//prefs.put(IRemoteIndexerInfoProvider.KEY_SKIP_IMPLICIT_REFERENCES, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES)));
		prefs.put(IRemoteIndexerInfoProvider.KEY_SKIP_TYPE_REFERENCES, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES)));
		prefs.put(IRemoteIndexerInfoProvider.KEY_SKIP_MACRO_REFERENCES, Boolean.valueOf(props.getProperty(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES)));
		return prefs;
	}
	
	
	
	
	/*
	 * This code was copied from PDOMIndexerTask.createDefaultScannerConfig(int)
	 */
	private static IScannerInfo getDefaultScannerInfo(IProject project, int linkageID) {
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if(provider == null)
			return null;
		
		String filename = linkageID == ILinkage.C_LINKAGE_ID ? "__cdt__.c" : "__cdt__.cpp"; //$NON-NLS-1$//$NON-NLS-2$
		IFile file = project.getFile(filename);
		IScannerInfo scanInfo = provider.getScannerInformation(file);
		if(scanInfo == null || scanInfo.getDefinedSymbols().isEmpty()) {
			scanInfo = provider.getScannerInformation(project);
			if(linkageID == ILinkage.C_LINKAGE_ID) {
				final Map<String, String> definedSymbols = scanInfo.getDefinedSymbols();
				definedSymbols.remove("__cplusplus__"); //$NON-NLS-1$
				definedSymbols.remove("__cplusplus"); //$NON-NLS-1$
			}
		}
		return scanInfo;
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
