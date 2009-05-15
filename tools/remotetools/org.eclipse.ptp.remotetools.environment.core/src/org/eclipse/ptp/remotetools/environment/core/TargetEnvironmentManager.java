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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
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

	private static String ENVIRONMENTS = "Environments"; //$NON-NLS-1$
	private static String ENVIRONMENTS_TYPE = "Type"; //$NON-NLS-1$
	private static String ENVIRONMENTS_TYPE_NAME = "Name"; //$NON-NLS-1$
	private static String ENVIRONMENTS_TYPE_CONFIG = "Configuration"; //$NON-NLS-1$
	private static String ENVIRONMENTS_TYPE_CONFIG_NAME = "CfgName"; //$NON-NLS-1$
	
	private List<TargetTypeElement> targetTypeElements = new ArrayList<TargetTypeElement>();
	private ListenerList eventListeners = new ListenerList();
	private ListenerList modelChangedListeners = new ListenerList();
	
	private Map<String, Set<String>> storedCypherEnvToKeyMap;
	
	public TargetEnvironmentManager() {
		
		super();
		Map<String, ITargetTypeExtension> targets = EnvironmentPlugin.getDefault().getControls();
		ISecurePreferences cypherEnvTypeMap = SecurePreferencesFactory.getDefault();
		
		// Create a map that contains (environment type name, cipher key set) tuples
		storedCypherEnvToKeyMap = new HashMap<String, Set<String>>();
			
		File file = EnvironmentPlugin.getDefault().getStateLocation().append(EnvironmentPlugin.FILENAME).toFile();
		XMLMemento memento;
		try {		
			if (!file.exists()) file.createNewFile();
			FileReader reader = new FileReader(file);
			try {
				memento = XMLMemento.createReadRoot(reader);
				targets = initContentFromFile(memento, targets, cypherEnvTypeMap);
			}
			catch (WorkbenchException exc) {
				memento = XMLMemento.createWriteRoot(ENVIRONMENTS);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		Set<String> cipherKeySet = new HashSet<String>();
		for (String name : targets.keySet()) {
			ITargetTypeExtension env = targets.get(name);
			targetTypeElements.add(new TargetTypeElement(name,env,this));
			
			// Also save the ciphered key names
			// Get cryptographed keys
			String [] controlKeysCypher = env.getControlAttributeNamesForCipheredKeys();
			
			// Add all key names to the set
			if(controlKeysCypher != null) {
				List<String> keyList = Arrays.asList(controlKeysCypher);
				cipherKeySet.addAll(keyList);
			}
			
//			 Add set to the map (save it)
			storedCypherEnvToKeyMap.put(name, cipherKeySet);
		}
		
	}
	
	public Map<String, ITargetTypeExtension> initContentFromFile(XMLMemento memento, 
			Map<String, ITargetTypeExtension> targets, ISecurePreferences cypherEnvTypeMap) {
		
		IMemento[] children = memento.getChildren(ENVIRONMENTS_TYPE);
		for (int i = 0; i < children.length; i++) {
			String name = children[i].getString(ENVIRONMENTS_TYPE_NAME);
			ITargetTypeExtension env = targets.get(name);
			// Get config name map from the given type name.
			ISecurePreferences cypherConfigNameMap = null;
			// Set null if the parent map is null.
			if(cypherEnvTypeMap != null) cypherConfigNameMap = cypherEnvTypeMap.node(name);
			if (env != null) {
//				 Create cipher key set that will contain the name of the keys that are ciphered
				Set<String> cipherKeySet = new HashSet<String>();
				
//				 Get cryptographed keys
				String [] controlKeysCypher = env.getControlAttributeNamesForCipheredKeys();
				
				// Add all key names to the set
				if(controlKeysCypher != null) {
					List<String> keyList = Arrays.asList(controlKeysCypher);
					cipherKeySet.addAll(keyList);
				}
				
//				 Add set to the map
				storedCypherEnvToKeyMap.put(name, cipherKeySet);
				
				TargetTypeElement typeElement = new TargetTypeElement(name,env,this);
				IMemento[] childrenElements = children[i].getChildren(ENVIRONMENTS_TYPE_CONFIG);
				for (int j = 0; j < childrenElements.length; j++) {
					String nameElement = childrenElements[j].getString(ENVIRONMENTS_TYPE_CONFIG_NAME);
					
					Map<String,String> attrsElement = new HashMap<String,String>();
					String[] controlKeys = env.getControlAttributeNames();
					for (int k = 0; k < controlKeys.length; k++) {
						attrsElement.put(controlKeys[k],childrenElements[j].getString(controlKeys[k]));
					}
					
					// Get password key map from the given configuration name
					ISecurePreferences cypherPasswdKeyMap = null;
					// Set null if the parent map is null
					if(cypherConfigNameMap != null) cypherPasswdKeyMap = cypherConfigNameMap.node(nameElement);
					
					if(controlKeysCypher != null) {
						for(int k=0; k < controlKeysCypher.length; k++) {
							// Insert into key into the hash set to f
							// Get the passwords or set them to empty string, if map not available.
							if(cypherPasswdKeyMap == null) {
								attrsElement.put(controlKeysCypher[k], ""); //$NON-NLS-1$
							} else {
								try {
									attrsElement.put(controlKeysCypher[k], cypherPasswdKeyMap.get(controlKeysCypher[k], "")); //$NON-NLS-1$
								} catch (StorageException e) {
									attrsElement.put(controlKeysCypher[k], ""); //$NON-NLS-1$
								}
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
	
	public void writeToFile() throws StorageException {
		
		// Create new ciphered Environment type map
		ISecurePreferences cypherEnvTypeMap = SecurePreferencesFactory.getDefault();
		
		File file = EnvironmentPlugin.getDefault().getStateLocation().append(EnvironmentPlugin.FILENAME).toFile();
		XMLMemento memento = XMLMemento.createWriteRoot(ENVIRONMENTS);		
		for (TargetTypeElement type : targetTypeElements) {
		    IMemento typeMemento = memento.createChild(ENVIRONMENTS_TYPE);
		    typeMemento.putString(ENVIRONMENTS_TYPE_NAME, type.getName());
			
		    // Create new ciphered configuration map
		    ISecurePreferences cypherConfigNameMap = cypherEnvTypeMap.node(type.getName());
		    
		    // Get set of ciphered keys for this environment type name
		    Set<String> cypherKeySet = storedCypherEnvToKeyMap.get(type.getName());
		    
		    for (ITargetElement element : type.getElements()) {
				IMemento elementMemento = typeMemento.createChild(ENVIRONMENTS_TYPE_CONFIG);
				elementMemento.putString(ENVIRONMENTS_TYPE_CONFIG_NAME,element.getName());
				
				Map<String, String> elementAttr = element.getAttributes();
				
				// Create new ciphered password key map
				ISecurePreferences cypherPasswdKeyMap = cypherConfigNameMap.node(element.getName());
				
				for (String key : elementAttr.keySet()) {
					// If is ciphered put it as a value to the ciphered password key map
					if(cypherKeySet.contains(key)) {
						cypherPasswdKeyMap.put(key, (String) elementAttr.get(key), true);
					} else {
						elementMemento.putString(key,(String) elementAttr.get(key));
					}
				}
				
				// Save the Target id from the Target Element.
				elementMemento.putString(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID,
						element.getId());
			
			}
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

	public List<TargetTypeElement> getTypeElements() {
		return targetTypeElements;
	}
	
	public synchronized ITargetElement[] getConfigElements() {
		List<ITargetElement> rsp = new ArrayList<ITargetElement>();
		for (TargetTypeElement element : targetTypeElements) {
			rsp.addAll( element.getElements() );
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
		if (listener != null)
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
		if (listener != null)
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
		if (listener != null)
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
		if (listener != null)
		{
			modelChangedListeners.remove (listener);
		}
	}

	public void fireModelChanged(int action,ITargetElement oldElement,TargetElement newElement) {
		
		if (oldElement == null && newElement != null) {
			for (Object listener : modelChangedListeners.getListeners()) {
				((ITargetEnvironmentEventListener)listener).elementAdded(newElement);
			}
		} else if (oldElement != null && newElement == null) {
			for (Object listener : modelChangedListeners.getListeners()) {
				((ITargetEnvironmentEventListener)listener).elementRemoved(oldElement);
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
		
		for (Object listener : eventListeners.getListeners()) {
			((ITargetEventListener)listener).handleStateChangeEvent(status, element);
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
		List<String> names = new ArrayList<String>();
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
