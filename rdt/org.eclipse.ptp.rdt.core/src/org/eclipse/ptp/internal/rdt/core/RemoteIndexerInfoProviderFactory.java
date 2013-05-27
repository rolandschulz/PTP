/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.upc.UPCLanguage;
import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.core.indexer.FileEncodingRegistry;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ptp.rdt.core.ILanguagePropertyProvider;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.activator.Activator;

/**
 * Used to get instances of RemoteIndexerInfoProvider.
 * 
 * The factory is not part of the remote JAR because it depends on
 * eclipse and CDT APIs.
 */
public class RemoteIndexerInfoProviderFactory {
	
	private static final String LANGUAGE_PROPERTIES_EXTENSION_ID = "languageProperties"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	
	// These are the linkages supported by RDT
	private static final int[] LINKAGE_IDS = new int[] { ILinkage.C_LINKAGE_ID, ILinkage.CPP_LINKAGE_ID };
	
	// indexer preference keys that the remote indexer cares about
	private static final String[] INDEXER_PREFERENCE_KEYS = {
		//IRemoteIndexerInfoProvider.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG,
		//IRemoteIndexerInfoProvider.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG,
		IRemoteIndexerInfoProvider.KEY_INDEX_ALL_FILES,
		//IRemoteIndexerInfoProvider.KEY_INCLUDE_HEURISTICS,
		IRemoteIndexerInfoProvider.KEY_SKIP_ALL_REFERENCES,
		//IRemoteIndexerInfoProvider.KEY_SKIP_IMPLICIT_REFERENCES,
		IRemoteIndexerInfoProvider.KEY_SKIP_TYPE_REFERENCES,
		IRemoteIndexerInfoProvider.KEY_SKIP_MACRO_REFERENCES
	};
	
	private static Map<String, List<ILanguagePropertyProvider>> languagePropertyProviderMap = 
		new HashMap<String, List<ILanguagePropertyProvider>>();
	
	

	
	/**
	 * Creates instances of ILanguagePropertyProvider from the languageProperties extension point.
	 * Will not activate a contributing plug-in if the language it provides properties for is never used.
	 */
	private static List<ILanguagePropertyProvider> getLanguagePropertyProviders(String languageId) {
		List<ILanguagePropertyProvider> providers = languagePropertyProviderMap.get(languageId);
		if(providers == null) {
			providers = new ArrayList<ILanguagePropertyProvider>();
			languagePropertyProviderMap.put(languageId, providers);
			
			IExtensionPoint extensionPoint = 
				Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, LANGUAGE_PROPERTIES_EXTENSION_ID);
			if(extensionPoint == null)
				return providers;
			
			for(IExtension extension : extensionPoint.getExtensions()) {
				for(IConfigurationElement providerElement : extension.getConfigurationElements()) {
					for(IConfigurationElement languageElement : providerElement.getChildren()) {
						String languageIdAttr = languageElement.getAttribute(ATTR_ID);
						if(languageId.equals(languageIdAttr)) {
							try {
								providers.add((ILanguagePropertyProvider)providerElement.createExecutableExtension(ATTR_CLASS));
							} catch (CoreException e) {
								RDTLog.logError(e);
							}
							break;
						}
					}
				}
			}
		}
		
		return providers;
	}
	
	
	public static Map<String, String> getLanguageProperties(String languageId, IProject project) {
		List<ILanguagePropertyProvider> providers = getLanguagePropertyProviders(languageId);
		Map<String,String> properties = new HashMap<String,String>();
		
		for(ILanguagePropertyProvider provider : providers) {
			Map<String,String> languageProperties = provider.getProperties(languageId, project);
			if(languageProperties != null)
				properties.putAll(languageProperties);
		}
		
		return properties;
	}
	
	
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
		
		final List<ICElement> elements = RemoteProjectResourcesUtil.getCElements(project);
		/*	
			new ArrayList<ICElement>();
		
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
		*/
		
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
	
	
	private static IScannerInfoProvider getLocalScannerInfoProvider(IProject project) {
		// return CCorePlugin.getDefault().getScannerInfoProvider(project); 
		// Warning: this is here to work around a defect in CDT
		return new LanguageSettingsScannerInfoProvider_Hack();
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
		Set<String> headerSet = new HashSet<String>();
		Map<String,Map<String,String>> languagePropertyMap = new HashMap<String, Map<String,String>>();
		
		
		// we assume all the elements are from the same project
		IProject project = elements.get(0).getCProject().getProject(); 
		IScannerInfoProvider provider = getLocalScannerInfoProvider(project);

		RemoteScannerInfoCache cache = new RemoteScannerInfoCache();
		
		FileEncodingRegistry fileEncodingRegistry = null;
		try {
			fileEncodingRegistry = new FileEncodingRegistry(project.getDefaultCharset());
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
		
		for(ICElement element : elements) {
			if(element instanceof ITranslationUnit) {
				ITranslationUnit tu = (ITranslationUnit) element;
				
				// compute the scanner info, share identical scanner infos
				IScannerInfo localScannerInfo = provider.getScannerInformation(tu.getResource());
				RemoteScannerInfo remoteScannerInfo = cache.get(localScannerInfo);
				String path = EFSExtensionManager.getDefault().getPathFromURI(element.getLocationURI());
				scannerInfoMap.put(path, remoteScannerInfo);
		
				// compute the language
				try {
					ILanguage language = tu.getLanguage();
					String id = language.getId();
					languageMap.put(path, id);
					IContentType ct= CCorePlugin.getContentType(project, path);
					addLanguageIDByContenttype(project, languageMap, ct);
					
					if(!languagePropertyMap.containsKey(id))
						languagePropertyMap.put(id, getLanguageProperties(id, project));
					
					if(fileEncodingRegistry != null){
						registerFileEncoding(fileEncodingRegistry, tu, path);
					}
					
				} catch (CoreException e) {
					RDTLog.logError(e);
				}
				
				if(tu.isHeaderUnit())
					headerSet.add(path);
			}
		}
		
		for(int id : LINKAGE_IDS) {
			IScannerInfo defaultScannerInfo = getDefaultScannerInfo(project, id);
			RemoteScannerInfo remoteScannerInfo = cache.get(defaultScannerInfo);
			linkageMap.put(id, remoteScannerInfo);
		}
		
		// compute the indexer preferences
		Properties props = IndexerPreferences.getProperties(project);
		Set<String> preferences = computeIndexerPreferences(props);

		return new RemoteIndexerInfoProvider(scannerInfoMap, linkageMap, languageMap, languagePropertyMap, 
				                             headerSet, preferences, fileEncodingRegistry, new HashSet<String>());
	}
	/**
	 * add the given contentTypeID's language to the language map.
	 * @param project
	 * @param linkageID
	 * @return
	 */
	private static void addLanguageIDByContenttype(IProject project, Map<String,String> languageMap, IContentType ct){
		if(ct==null){
			return;
		}
		String languageID= null;
		LanguageManager langManager = LanguageManager.getInstance();
		ProjectLanguageConfiguration projLangConfig=null;
		try {
			projLangConfig = langManager.getLanguageConfiguration(project);
		} catch (CoreException e) {
			
		}
		if(projLangConfig==null){
			languageID = langManager.getLanguage(ct).getId();
		}else{
			languageID = projLangConfig.getLanguageForContentType(null, ct.getId());
			if(languageID==null){
				ILanguage lang= langManager.getLanguage(ct);
				if(lang!=null){
					languageID = lang.getId();
				}
			}
		}
		
		
		if(languageID!=null){
		
			if(CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(ct.getId())){
				languageMap.put(IRemoteIndexerInfoProvider.ASM_LANGUAGEID_key, languageID);
			}
			if(UPCLanguage.UPC_CONTENT_TYPE_ID.equals(ct.getId())){
				languageMap.put(IRemoteIndexerInfoProvider.UPC_LANGUAGEID_key, languageID);
			}
			
			if(CCorePlugin.CONTENT_TYPE_CHEADER.equals(ct.getId())){
				languageMap.put(IRemoteIndexerInfoProvider.CHEADER_LANGUAGEID_key, languageID);
			}
			if(CCorePlugin.CONTENT_TYPE_CSOURCE.equals(ct.getId())){
				languageMap.put(IRemoteIndexerInfoProvider.CSOURCE_LANGUAGEID_key, languageID);
			}
			if(CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(ct.getId())){
				languageMap.put(IRemoteIndexerInfoProvider.CXXHEADER_LANGUAGEID_key, languageID);
			}
			if(CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(ct.getId())){
				languageMap.put(IRemoteIndexerInfoProvider.CXXSOURCE_LANGUAGEID_key, languageID);
			}
		}
	}

	
	private static void registerFileEncoding(FileEncodingRegistry fileEncodingRegistry, ITranslationUnit tu, String filePath) throws CoreException {
		IResource resource = tu.getResource();
		String specificEncoding = null;
		if (resource instanceof IFile) {
			specificEncoding = ((IFile) resource).getCharset(false);
		}
		if (filePath != null && specificEncoding != null) {
			fileEncodingRegistry.registerFileEncoding(filePath, specificEncoding);
		}
	}
	
	private static Set<String> computeIndexerPreferences(Properties props) {
		Set<String> prefs = new HashSet<String>(props.size());
		for(String key : INDEXER_PREFERENCE_KEYS) {
			if(Boolean.valueOf(props.getProperty(key))) {
				prefs.add(key);
			}
		}
		return prefs;
	}

	/*
	 * This code was copied from PDOMIndexerTask.createDefaultScannerConfig(int)
	 */
	private static IScannerInfo getDefaultScannerInfo(IProject project, int linkageID) {
		IScannerInfoProvider provider = getLocalScannerInfoProvider(project);
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
		
		String[] includePaths = scanInfo.getIncludePaths();
		for(String include : includePaths) {
			// HACK: convert backslashes to slashes to compensate for CDT bug 315632
			include = include.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return scanInfo;
	}
	

	/**
	 * Convenience method for getting a RemoteScannerInfo for a resource.
	 */
	public static RemoteScannerInfo getScannerInfo(IResource resource) {
		final IScannerInfoProvider provider = getLocalScannerInfoProvider(resource.getProject());
		IScannerInfo scannerInfo = provider.getScannerInformation(resource);
		return new RemoteScannerInfo(scannerInfo);
	}
	
	
	
	
}
