/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.indexer.ILanguageMapper;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.rdt.core.IConfigurableLanguage;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

/**
 *
 */
public class RemoteLanguageMapper implements ILanguageMapper {
	
	private static final String LOG_TAG = "CDTMiner-RemoteLanguageMapper"; //$NON-NLS-1$
	private static final String LANGUAGE_CLASS_FILE_NAME = "languages.properties"; //$NON-NLS-1$
	private static final String SERVER_DIRECTORY_PROP_KEY="A_PLUGIN_PATH"; //$NON-NLS-1$
	private static final ILanguage DEFAULT_LANGUAGE = GPPLanguage.getDefault();
	
	private static Properties languageIdToClassName = null;
	
	private final Map<String,ILanguage> languages = new HashMap<String,ILanguage>();
	private final IRemoteIndexerInfoProvider provider;
	private final DataStore dataStore;
	
	
	public RemoteLanguageMapper(IRemoteIndexerInfoProvider provider, DataStore dataStore) {
		if(provider == null ){
			throw new NullPointerException();
		}
		
		this.provider = provider;
		this.dataStore = dataStore;
	}
	
	
	
	public ILanguage getLanguage(String file) {
		String languageId = provider.getLanguageID(file);
		if(languageId == null) {
			if(dataStore!=null){
				UniversalServerUtilities.logWarning(LOG_TAG, "No language id for '" + file + "'", dataStore); //$NON-NLS-1$ //$NON-NLS-2$
			}else{
				StandaloneLogService.getInstance().errorLog(LOG_TAG + ":" + "No language id for '" + file + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return DEFAULT_LANGUAGE;
		}
		
		ILanguage language = languages.get(languageId);
		if(language == null) {
			language = getLanguageById(languageId, provider.getLanguageProperties(languageId), dataStore);			
			languages.put(languageId, language);
		}
		
		return language;
	}
	
	
	
	public static ILanguage getLanguageById(String languageId, Map<String,String> languageProperties, DataStore dataStore) {
		ILanguage language = instantiateAndConfigureLanguage(languageId, languageProperties, dataStore);
		return language == null ? DEFAULT_LANGUAGE : language;
	}
	
	
	
	private static ILanguage instantiateAndConfigureLanguage(String languageId, Map<String,String> languageProperties, DataStore dataStore) {
		String className = getClassName(languageId, dataStore);
		if(className == null) {
			if(dataStore != null){
				UniversalServerUtilities.logWarning(LOG_TAG, "No class for " + languageId, dataStore); //$NON-NLS-1$
			}else{
				StandaloneLogService.getInstance().errorLog(LOG_TAG +":" +  "No class for " + languageId); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}
		
		ILanguage language = null;
		try {
			Class<?> clazz = Class.forName(className);
			language = (ILanguage) clazz.newInstance();
			if(dataStore != null){ 
				UniversalServerUtilities.logInfo(LOG_TAG, "Instantiated language: " + className, dataStore); //$NON-NLS-1$
			}else{
				StandaloneLogService.getInstance().traceLog(LOG_TAG + ":" + "Instantiated language: " + className);  //$NON-NLS-1$//$NON-NLS-2$
			}
		} catch(Exception e) {
			if(dataStore != null){ 
				UniversalServerUtilities.logError(LOG_TAG, e.toString(), e, dataStore);
			}else{
				StandaloneLogService.getInstance().errorLog(LOG_TAG + ":" + e.toString(), e); //$NON-NLS-1$
			}
			return null;
		}
		
		if(languageProperties != null && language instanceof IConfigurableLanguage) {
			((IConfigurableLanguage) language).setProperties(languageProperties);
			if(dataStore != null){ 
				UniversalServerUtilities.logDebugMessage(LOG_TAG, "Configured language: " + className, dataStore); //$NON-NLS-1$
			}else{
				StandaloneLogService.getInstance().traceLog(LOG_TAG +":"+ "Configured language: " + className); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		return language;
	}
	
	
	
	private static synchronized String getClassName(String languageId, DataStore dataStore) {
		if(languageIdToClassName == null) {
			try {
				languageIdToClassName = new Properties();
				
				// set up the defaults
				languageIdToClassName.setProperty(GPPLanguage.ID, GPPLanguage.class.getCanonicalName());
				languageIdToClassName.setProperty(GCCLanguage.ID, GCCLanguage.class.getCanonicalName());
				
				String pluginDir = null;
				// defaults may be overridden when the file is loaded
				if(dataStore!=null){
					pluginDir = dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);
				}else{
					pluginDir = System.getProperties().getProperty(SERVER_DIRECTORY_PROP_KEY, null); 
				}
				String dataFile = pluginDir + File.separator + LANGUAGE_CLASS_FILE_NAME;
				File file = new File(dataFile);
				if(file.canRead()) {
					InputStream in = new FileInputStream(file);
					languageIdToClassName.load(in);
					in.close();
					if(dataStore != null){
						UniversalServerUtilities.logInfo(LOG_TAG, "Loaded language mappings from " + file.getAbsolutePath(), dataStore); //$NON-NLS-1$
					}else{
						StandaloneLogService.getInstance().traceLog(LOG_TAG +":"+ "Loaded language mappings from " + file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				else if(dataStore != null) {
					UniversalServerUtilities.logWarning(LOG_TAG, "Cannot load language mappings file: " + file.getAbsolutePath(), dataStore); //$NON-NLS-1$
				}else{
					StandaloneLogService.getInstance().errorLog(LOG_TAG +":"+"Cannot load language mappings file: " + file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
				}

			} catch (FileNotFoundException e) {
				if(dataStore != null){
					UniversalServerUtilities.logError(LOG_TAG, e.toString(), e, dataStore);
				}else{
					StandaloneLogService.getInstance().errorLog(LOG_TAG +":"+ e.toString(), e); //$NON-NLS-1$
				}
			} catch (IOException e) {
				if(dataStore != null){
					UniversalServerUtilities.logError(LOG_TAG, e.toString(), e, dataStore);
				}else{
					StandaloneLogService.getInstance().errorLog(LOG_TAG +":" + e.toString(), e); //$NON-NLS-1$
				}
			}
			
			if(dataStore != null){
				UniversalServerUtilities.logInfo(LOG_TAG, "Language ID mappings: " + languageIdToClassName, dataStore); //$NON-NLS-1$
			}else{
				StandaloneLogService.getInstance().traceLog(LOG_TAG+":"+"Language ID mappings: " + languageIdToClassName); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		return languageIdToClassName.getProperty(languageId);
	}
	
	
}
