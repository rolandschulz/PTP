/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * Class responsible for managing all Targets available. Its managing tasks consist on:
 * <ul>
 * <li> Read/write targets information from/to disk (sensitive data is written to a secure container)
 * <li> Keep a list of all target types elements based on extension point information. Each target type can reference a list of target elements
 * <li> Insert/remove listeners for targets events.
 * <li> Fire events for the listeners
 * <li> Provides getters for configs
 * 
 * @author Ricardo M. Matinata, Richard Maciel
 * @since 1.1
 */
public class TargetEnvironmentManager {

	private static String ENVIRONMENTS = "Environments";
	private static String ENVIRONMENTS_TYPE = "Type";
	private static String ENVIRONMENTS_TYPE_NAME = "Name";
	private static String ENVIRONMENTS_TYPE_CONFIG = "Configuration";
	private static String ENVIRONMENTS_TYPE_CONFIG_NAME = "CfgName";
	
	private List targetTypeElements;
	private List eventListeners = new ArrayList();
	private List modelChangedListeners = new ArrayList();
	
	private TargetEnvironmentEventManager manager;
	
	private Map storedCypherEnvToKeyMap;
	
	public TargetEnvironmentManager() {
		
		super();
		Map targets = EnvironmentPlugin.getDefault().getControls();
		targetTypeElements = new ArrayList();
		manager = new TargetEnvironmentEventManager(this);
		
		URL pluginURL = EnvironmentPlugin.getDefault().getBundle().getEntry("/");
		Map cypherEnvTypeMap = Platform.getAuthorizationInfo(pluginURL, "", "");
		
		// Create a map that contains (environment type name, cipher key set) tuples
		 storedCypherEnvToKeyMap = new HashMap();
			
		File file = EnvironmentPlugin.getDefault().getStateLocation().append(EnvironmentPlugin.FILENAME).toFile();
		XMLMemento memento;
		try {		
			if (!file.exists()) file.createNewFile();
			FileReader reader = new FileReader(file);
			try {
				memento = XMLMemento.createReadRoot(reader);
				targets = initContentFromFile(memento,targets,cypherEnvTypeMap);
			}
			catch (WorkbenchException exc) {
				memento = XMLMemento.createWriteRoot(ENVIRONMENTS);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Iterator keys = targets.keySet().iterator();
		Set cipherKeySet = new HashSet();
		while (keys.hasNext()) {
			String name = (String)keys.next();
			ITargetTypeExtension env = (ITargetTypeExtension) targets.get(name);
			targetTypeElements.add(new TargetTypeElement(name,env,this));
			
			// Also save the ciphered key names
			// Get cryptographed keys
			String [] controlKeysCypher = env.getControlAttributeNamesForCipheredKeys();
			
			// Add all key names to the set
			if(controlKeysCypher != null) {
				List keyList = Arrays.asList(controlKeysCypher);
				cipherKeySet.addAll(keyList);
			}
			
//			 Add set to the map (save it)
			storedCypherEnvToKeyMap.put(name, cipherKeySet);
		}
		
	}
	
	public Map initContentFromFile(XMLMemento memento,Map targets, Map cypherEnvTypeMap) {
		
		IMemento[] children = memento.getChildren(ENVIRONMENTS_TYPE);
		for (int i = 0; i < children.length; i++) {
			String name = children[i].getString(ENVIRONMENTS_TYPE_NAME);
			ITargetTypeExtension env = (ITargetTypeExtension) targets.get(name);
			// Get config name map from the given type name.
			Map cypherConfigNameMap = null;
			// Set null if the parent map is null.
			if(cypherEnvTypeMap != null) cypherConfigNameMap = (Map)cypherEnvTypeMap.get(name);
			if (env != null) {
//				 Create cipher key set that will contain the name of the keys that are ciphered
				Set cipherKeySet = new HashSet();
				
//				 Get cryptographed keys
				String [] controlKeysCypher = env.getControlAttributeNamesForCipheredKeys();
				
				// Add all key names to the set
				if(controlKeysCypher != null) {
					List keyList = Arrays.asList(controlKeysCypher);
					cipherKeySet.addAll(keyList);
				}
				
//				 Add set to the map
				storedCypherEnvToKeyMap.put(name, cipherKeySet);
				
				TargetTypeElement typeElement = new TargetTypeElement(name,env,this);
				IMemento[] childrenElements = children[i].getChildren(ENVIRONMENTS_TYPE_CONFIG);
				for (int j = 0; j < childrenElements.length; j++) {
					String nameElement = childrenElements[j].getString(ENVIRONMENTS_TYPE_CONFIG_NAME);
					
					Map attrsElement = new HashMap();
					String[] controlKeys = env.getControlAttributeNames();
					for (int k = 0; k < controlKeys.length; k++) {
						attrsElement.put(controlKeys[k],childrenElements[j].getString(controlKeys[k]));
					}
					
					// Get password key map from the given configuration name
					Map cypherPasswdKeyMap = null;
					// Set null if the parent map is null
					if(cypherConfigNameMap != null) cypherPasswdKeyMap = (Map)cypherConfigNameMap.get(nameElement);
					
					if(controlKeysCypher != null) {
						for(int k=0; k < controlKeysCypher.length; k++) {
							// Insert into key into the hash set to f
	//						// Get the passwords or set them to empty string, if map not available.
							if(cypherPasswdKeyMap == null) {
								attrsElement.put(controlKeysCypher[k], "");
							} else {
								attrsElement.put(controlKeysCypher[k], cypherPasswdKeyMap.get(controlKeysCypher[k]));
							}
							// Include key name in the set
							cipherKeySet.add(controlKeysCypher[k]);
						}
					}
					
					// Finally insert the identifier of the environment
					// Create it, if necessary.
					String id;
					if(childrenElements[j].getString(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID) == null) {
						id = EnvironmentPlugin.getDefault().getEnvironmentUniqueID();
						/*attrsElement.put(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID, 
								EnvironmentPlugin.getDefault().getEnvironmentUniqueID());*/
					} else {
						id = childrenElements[j].getString(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID);
						/*attrsElement.put(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID, 
							childrenElements[j].getString(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID));*/
					}
					
					typeElement.addElement(new TargetElement(typeElement,nameElement,attrsElement, id));
				}
				
				targetTypeElements.add(typeElement);
				targets.remove(name);
			}
		}
		
		return targets;
		
	}
	
	public void writeToFile() {
		
		// Create new ciphered Environment type map
		Map cypherEnvTypeMap = new HashMap();
		
		File file = EnvironmentPlugin.getDefault().getStateLocation().append(EnvironmentPlugin.FILENAME).toFile();
		Iterator iterator = targetTypeElements.iterator();
		XMLMemento memento = XMLMemento.createWriteRoot(ENVIRONMENTS);		
		while (iterator.hasNext()) {
			TargetTypeElement type = ((TargetTypeElement)iterator.next());
		    IMemento typeMemento = memento.createChild(ENVIRONMENTS_TYPE);
		    typeMemento.putString(ENVIRONMENTS_TYPE_NAME, type.getName());
			
		    // Create new ciphered configuration map
		    Map cypherConfigNameMap = new HashMap();
		    
		    // Get set of ciphered keys for this environment type name
		    Set cypherKeySet = (Set)storedCypherEnvToKeyMap.get(type.getName());
		    
			Iterator elements = type.getElements().iterator();
			while (elements.hasNext()) {
				ITargetElement element = (ITargetElement) elements.next();
				IMemento elementMemento = typeMemento.createChild(ENVIRONMENTS_TYPE_CONFIG);
				elementMemento.putString(ENVIRONMENTS_TYPE_CONFIG_NAME,element.getName());
				
				Map elementAttr = element.getAttributes();
				
				// Create new ciphered password key map
				Map cypherPasswdKeyMap = new HashMap();
				
				Iterator attrKeys = elementAttr.keySet().iterator();
				while (attrKeys.hasNext()) {
					String key = (String) attrKeys.next();
					
					// If is ciphered put it as a value to the ciphered password key map
					if(cypherKeySet.contains(key)) {
						cypherPasswdKeyMap.put(key, (String) elementAttr.get(key));
					} else {
						elementMemento.putString(key,(String) elementAttr.get(key));
					}
				}
				
				// Save the Target id from the Target Element.
				elementMemento.putString(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID,
						element.getId());
			
				// Fill ciphered configuration map with (configuration name, ciph passwd key map) tuples
				cypherConfigNameMap.put(element.getName(), cypherPasswdKeyMap);
			}
			// Fill ciphered environment type map with (environment type name, ciph config map) tuples
			cypherEnvTypeMap.put(type.getName(), cypherConfigNameMap);
		}
		
		URL pluginURL = EnvironmentPlugin.getDefault().getBundle().getEntry("/");
		try {
			Platform.addAuthorizationInfo(pluginURL, "", "", cypherEnvTypeMap);
		} catch (CoreException e1) {
			// Generates a runtime exception containing the CoreException
			throw new RuntimeException(e1);
		}
		
		Writer writer;
		try {
			writer = new FileWriter(file);
			memento.save(writer);
		} catch (IOException e) {
			// This should not happen. Throw a runtime exception
			throw new RuntimeException(e);
		}
		
	}

	public List getTypeElements() {
		return targetTypeElements;
	}
	
	public synchronized ITargetElement[] getConfigElements() {
		List rsp = new ArrayList();
		Iterator i = targetTypeElements.iterator();
		while (i.hasNext()) {
			rsp.addAll( ((TargetTypeElement)i.next()).getElements() );
		}
		
		ITargetElement[] rspObj = null;
		
		if (rsp.size() > 0) {
			rspObj = (ITargetElement[])rsp.toArray(new ITargetElement[]{});
		}
		return rspObj;
		
	}
	
	/**
	 * Registers a model event listener. If the listener argument is null or already registered, this 
	 * method has no effect.
	 */
	
	public synchronized void addModelEventListener (ITargetEventListener listener)
	{
		if (listener != null && !eventListeners.contains (listener))
		{
			eventListeners.add (listener);
		}
	}
	
	/**
	 * De-registers a model event listener. If the listener argument is null or not already registered, this 
	 * method has no effect.
	 */

	public synchronized void removeModelEventListener (ITargetEventListener listener)
	{
		if (listener != null && eventListeners.contains(listener))
		{
			eventListeners.remove (listener);
		}
	}
	
	/**
	 * Registers a model changed listener. If the listener argument is null or already registered, this 
	 * method has no effect.
	 */
	
	public synchronized void addModelChangedListener (ITargetEnvironmentEventListener listener)
	{
		if (listener != null && !modelChangedListeners.contains (listener))
		{
			modelChangedListeners.add (listener);
		}
	}
	
	/**
	 * De-registers a model changed listener. If the listener argument is null or not already registered, this 
	 * method has no effect.
	 */

	public synchronized void removeModelChangedListener (ITargetEnvironmentEventListener listener)
	{
		if (listener != null && modelChangedListeners.contains(listener))
		{
			modelChangedListeners.remove (listener);
		}
	}

	public void fireModelChanged(int action,ITargetElement oldElement,TargetElement newElement) {
		
		Iterator i = modelChangedListeners.iterator();
		
		if (oldElement == null && newElement != null) {
			while (i.hasNext()) {
				ITargetEnvironmentEventListener listener = (ITargetEnvironmentEventListener) i.next();
				listener.elementAdded(newElement);
			}
		} else if (oldElement != null && newElement == null) {
			while (i.hasNext()) {
				ITargetEnvironmentEventListener listener = (ITargetEnvironmentEventListener) i.next();
				listener.elementRemoved(oldElement);
			}
		}
		
	}
	
	public synchronized void fireElementEvent(int event,TargetElement element) {

		int status = ITargetElementStatus.STOPPED;
		switch (event) {
			case ITargetStatus.STARTED:
				status = ITargetElementStatus.STARTED;
				break;
			case ITargetStatus.STOPPED:
				status = ITargetElementStatus.STOPPED;
				break;
			case ITargetStatus.RESUMED:
				status = ITargetElementStatus.RESUMED;
				break;
			case ITargetStatus.PAUSED:
				status = ITargetElementStatus.PAUSED;
				break;
		}
		element.setStatus(status);
		
		Iterator i = eventListeners.iterator();
		while (i.hasNext()) {
			ITargetEventListener listener = (ITargetEventListener) i.next();
			listener.handleStateChangeEvent(status, element);
		}
		
	}
	
	// Implementation of methods from the ITargetProvider
	
	public ITargetControl selectControl(String id) {
		ITargetElement[] elements = getConfigElements();
		if (elements != null) {
			for (int i = 0; i < elements.length; i++)
			{
				ITargetElement element = elements [i];
				if (element.getName().equals(id)) {
					try {
						return element.getControl();
					} catch (CoreException e) {
						
					}
				}
			}
		}
		return null;
	}

	public String[] getAllConfigNames() {
		ITargetElement[] elements = getConfigElements();
		List names = new ArrayList();
		if (elements != null) {
			for (int i = 0; i < elements.length; i++)
			{
				ITargetElement element = elements [i];
				names.add(element.getName());
			}
		}
		
		return (String[]) names.toArray(new String[]{});
	}
	
}
