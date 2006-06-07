/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.Assert;

/**
 * A collection of resource managers.
 */
public class ResourceManagerStore {
	/** The stored resource managers. */
	private final List fResourceManagers= new ArrayList();
	/** The preference store. */
	private IPreferenceStore fPreferenceStore;
	/**
	 * The key into <code>fPreferenceStore</code> the value of which holds custom resource managers
	 * encoded as XML.
	 */
	private String fKey;

	/**
	 * Set to <code>true</code> if property change events should be ignored (e.g. during writing
	 * to the preference store).
	 * 
	 */
	private boolean fIgnorePreferenceStoreChanges= false;
	
	/**
	 * The property listener, if any is registered, <code>null</code> otherwise.
	 */
	private IPropertyChangeListener fPropertyListener;


	/**
	 * Creates a new resource manager store.
	 *
	 * @param store the preference store in which to store custom resource managers
	 *        under <code>key</code>
	 * @param key the key into <code>store</code> where to store custom
	 *        resource managers
	 */
	public ResourceManagerStore(IPreferenceStore store, String key) {
		Assert.isNotNull(store);
		Assert.isNotNull(key);
		fPreferenceStore= store;
		fKey= key;
	}

	/**
	 * Loads the resource managers from contributions and preferences.
	 *
	 * @throws IOException if loading fails.
	 */
	public void load() throws IOException {
		fResourceManagers.clear();
		loadResourceManagers();
	}
	
	/**
	 * Starts listening for property changes on the preference store. If the configured preference
	 * key changes, the resource manager store is {@link #load() reloaded}. Call
	 * {@link #stopListeningForPreferenceChanges()} to remove any listener and stop the
	 * auto-updating behavior.
	 * 
	 */
	public final void startListeningForPreferenceChanges() {
		if (fPropertyListener == null) {
			fPropertyListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					/*
					 * Don't load if we are in the process of saving ourselves. We are in sync anyway after the
					 * save operation, and clients may trigger reloading by listening to preference store
					 * updates.
					 */
					if (!fIgnorePreferenceStoreChanges && fKey.equals(event.getProperty()))
						try {
							load();
						} catch (IOException x) {
							handleException(x);
						}
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPropertyListener);
		}
		
	}
	
	/**
	 * Stops the auto-updating behavior started by calling
	 * {@link #startListeningForPreferenceChanges()}.
	 * 
	 */
	public final void stopListeningForPreferenceChanges() {
		if (fPropertyListener != null) {
			fPreferenceStore.removePropertyChangeListener(fPropertyListener);
			fPropertyListener= null;
		}
	}
	
	/**
	 * Handles an {@link IOException} thrown during reloading the preferences due to a preference
	 * store update. The default is to write to stderr.
	 * 
	 * @param x the exception
	 * @since 3.2
	 */
	protected void handleException(IOException x) {
		x.printStackTrace();
	}

	/**
	 * Saves the resource managers to the preferences.
	 *
	 * @throws IOException if the resource managers cannot be written
	 */
	public void save() throws IOException {

		StringWriter output= new StringWriter();
		ResourceManagerReaderWriter writer= new ResourceManagerReaderWriter();
		writer.save((IResourceManager[]) fResourceManagers.toArray(new IResourceManager[fResourceManagers.size()]), output);

		fIgnorePreferenceStoreChanges= true;
		try {
			fPreferenceStore.setValue(fKey, output.toString());
			if (fPreferenceStore instanceof IPersistentPreferenceStore)
				((IPersistentPreferenceStore)fPreferenceStore).save();
		} finally {
			fIgnorePreferenceStoreChanges= false;
		}
	}

	/**
	 * Adds a resource manager encapsulated in its persistent form.
	 *
	 * @param data the resource manager to add
	 */
	public void add(IResourceManager rm) {

		if (!validateResourceManager(rm))
			return;

		fResourceManagers.add(rm);
	}

	/**
	 * Removes a resource manager from the store.
	 *
	 * @param data the resource manager to remove
	 */
	public void delete(IResourceManager rm) {
			fResourceManagers.remove(rm);
	}

	/**
	 * Returns all resource managers.
	 *
	 * @return all resource managers
	 */
	public IResourceManager[] getResourceManagers() {
		return getResourceManagers(null);
	}

	/**
	 * Returns all resource managers for the given resource manager type.
	 *
	 * @param resourceManagerId the id of the resource manager type, or <code>null</code> if all resource managers should be returned
	 * @return all resource managers for the given type
	 */
	public IResourceManager[] getResourceManagers(String resourceManagerId) {
		List rms= new ArrayList();
		for (Iterator it= fResourceManagers.iterator(); it.hasNext();) {
			IResourceManager rm= (IResourceManager) it.next();
			if (resourceManagerId == null || resourceManagerId.equals(rm.getConfiguration().getResourceManagerId()))
				rms.add(rm);
		}

		return (IResourceManager[]) rms.toArray(new IResourceManager[rms.size()]);
	}

	/**
	 * Returns the first resource manager that matches the name.
	 *
	 * @param name the name of the resource manager searched for
	 * @return the first enabled resource manager that matches both name and type, or <code>null</code> if none is found
	 */
	public IResourceManager findResourceManager(String name) {
		return findResourceManager(name, null);
	}

	/**
	 * Returns the first resource manger that matches both name and type id.
	 *
	 * @param name the name of the resource manager searched for
	 * @param typeId the resource manager type, or <code>null</code> if any type is OK
	 * @return the first enabled resource manager that matches both name and type id, or <code>null</code> if none is found
	 */
	public IResourceManager findResourceManager(String name, String typeId) {
		Assert.isNotNull(name);

		for (Iterator it= fResourceManagers.iterator(); it.hasNext();) {
			IResourceManager rm = (IResourceManager) it.next();
			if (typeId == null || typeId.equals(rm.getConfiguration().getResourceManagerId())
					&& name.equals(rm.getConfiguration().getName()))
				return rm;
		}

		return null;
	}

	private void loadResourceManagers() throws IOException {
		String pref= fPreferenceStore.getString(fKey);
		if (pref != null && pref.trim().length() > 0) {
			Reader input= new StringReader(pref);
			ResourceManagerReaderWriter reader= new ResourceManagerReaderWriter();
			IResourceManager[] rms= reader.read(input);
			for (int i= 0; i < rms.length; i++) {
				add(rms[i]);
			}
		}
	}

	/**
	 * Validates a resource manager.
	 *
	 * @param resource manager the resource manager to validate
	 * @return <code>true</code> if validation is successful,
	 * <code>false</code> if validation fails
	 */
	private boolean validateResourceManager(IResourceManager rm) {
		return true;
	}

}

