/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreAttributes;
import org.eclipse.ptp.rdt.core.miners.IURICreator;

/**
 * @author crecoskie
 *
 */
public class URICreatorManager {
private static URICreatorManager fInstance;
	
	private Map<String, IURICreator> fCreatorsMap;
	private DataStore fDataStore;

		
	private URICreatorManager(DataStore datastore) {
		fCreatorsMap = new HashMap<String, IURICreator>();
		fDataStore = datastore;
		loadExtensions();
	}
	
	private void loadExtensions() {
		// load all extensions from URICreator.dat, which consists of a series of one-line
		// entries that contain comma separated mappings between URI schemes and Java classes
		// e.g.
		// examplescheme,org.eclipse.example.someclass
		String pluginDir = fDataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);

		// default location
		String dataFile = pluginDir + File.separator + "URICreator.dat"; //$NON-NLS-1$
		
		// try loading in UTF-8
		if(loadCreators(DE.ENCODING_UTF_8, dataFile) == false) {
			// didn't work... try loading with native encoding
			loadCreators(null, dataFile);
		}
	}

	private boolean loadCreators(String encoding, String filename) {
		FileInputStream datafile;
		try {
			datafile = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		BufferedReader reader = null;
		if (encoding == null)
		{
			reader = new BufferedReader(new InputStreamReader(datafile));
		}
		else
		{
			try {
				reader = new BufferedReader(new InputStreamReader(datafile, encoding));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		String line;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		String scheme = null;
		while (line != null)
		{
			// check name
			line = line.trim();

			// allow comments
			if (!line.startsWith("#")) //$NON-NLS-1$
			{
				
				String[] tokens = line.split(","); //$NON-NLS-1$
				if(tokens.length < 2)
					return false;
				
				scheme = tokens[0];
				IURICreator creator = loadCreator(tokens[1]);
				if (creator != null)
				{
					fCreatorsMap.put(scheme, creator);
				}				
			}
			
			try {
				line = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private IURICreator loadCreator(String string) {
		Class theClass;
		try {
			theClass = Class.forName(string);
		} catch (ClassNotFoundException e) {
			// TODO proper logging
			e.printStackTrace();
			return null;
		}
		// call the zero argument constructor
		try {
			Constructor constructor = theClass.getConstructor(new Class[0]);
			try {
				IURICreator creator = (IURICreator) constructor.newInstance();
				
				if(creator != null) {
					return creator;
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return null;
	}

	public static synchronized URICreatorManager getDefault(DataStore datastore) {
		if(fInstance == null) {
			fInstance = new URICreatorManager(datastore);
		}
		
		return fInstance;
	}
	
	public URI createURI(String scheme, String host, String path) throws URISyntaxException {
		ScopeManager scopeManager = ScopeManager.getInstance();
		
		String scopeName = scopeManager.getScopeForFile(path);
		if(scopeName == null) {
			return new URI("rse", host, path, null, null); //$NON-NLS-1$
		}
		
		String mappedPath = scopeManager.getMappedPathForFile(path);
		String fileHost = scopeManager.getHostForFile(path, host);
		
		IURICreator creator = fCreatorsMap.get(scheme);
		
		if(creator != null) {
			return creator.createURIForScheme(scheme, fileHost, path, mappedPath);
		}
		
		else {
			return new URI(scheme, host, path, null, null);
		}
			
	}
	
}
