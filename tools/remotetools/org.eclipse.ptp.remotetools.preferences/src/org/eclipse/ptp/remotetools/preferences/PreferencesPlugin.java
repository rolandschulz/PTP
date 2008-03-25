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
package org.eclipse.ptp.remotetools.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

import org.eclipse.ptp.remotetools.preferences.events.PreferencesChangeEvent;
import org.eclipse.ptp.remotetools.preferences.events.IPreferencesChangeListener;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class PreferencesPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static PreferencesPlugin plugin;
	
	private List propertiesListeners;
	
	private Preferences.IPropertyChangeListener propertyListener;
	
	/**
	 * The constructor.
	 */
	public PreferencesPlugin() {
		plugin = this;
		propertiesListeners = new ArrayList();
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		propertyListener = new Preferences.IPropertyChangeListener() {

			public void propertyChange(Preferences.PropertyChangeEvent event) {
				
				fireValueChanged(event.getProperty(),event.getOldValue(),event.getNewValue());
				
			}
			
		};
		this.getPluginPreferences().addPropertyChangeListener(propertyListener);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		this.getPluginPreferences().removePropertyChangeListener(propertyListener);
		this.savePluginPreferences();
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PreferencesPlugin getDefault() {
		return plugin;
	}
	
	public void addListener(IPreferencesChangeListener listener) {
		
		propertiesListeners.add(listener);
	}
	
	public void removeListener(IPreferencesChangeListener listener) {
		
		propertiesListeners.remove(listener);
	}
	
	public void fireValueChanged(String property, Object oldValue,
            Object newValue) {
        if (propertiesListeners.size() == 0)
            return;
        Iterator i = propertiesListeners.iterator();
        
        while (i.hasNext()) { 
        	IPreferencesChangeListener listener = (IPreferencesChangeListener) i.next();
        	listener.propertyChange(new PreferencesChangeEvent(this,
                property, oldValue, newValue));
        }
    }
}
