/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core;

import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author Clement Chu
 */
public class PTPPreferenceStore implements IPreferenceStore {
	private Preferences fPreferences;
	private HashMap fListeners = new HashMap();
	public PTPPreferenceStore (Preferences pref) {
		fPreferences = pref;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(final IPropertyChangeListener listener) {
		Preferences.IPropertyChangeListener l = new Preferences.IPropertyChangeListener() {
			public void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
				listener.propertyChange(new PropertyChangeEvent(PTPPreferenceStore.this, event.getProperty(), event.getNewValue(), event.getOldValue()));
			}
		};
		fListeners.put(listener, l);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		return getPreferences().contains(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		Iterator it = fListeners.keySet().iterator();
		while (it.hasNext()) {
			((IPropertyChangeListener) it.next()).propertyChange(new PropertyChangeEvent(this, name, oldValue, newValue));
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		return fPreferences.getBoolean(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
	 */
	public boolean getDefaultBoolean(String name) {
		return fPreferences.getDefaultBoolean(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
	 */
	public double getDefaultDouble(String name) {
		return fPreferences.getDefaultDouble(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
	 */
	public float getDefaultFloat(String name) {
		return fPreferences.getDefaultFloat(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
	 */
	public int getDefaultInt(String name) {
		return fPreferences.getDefaultInt(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
	 */
	public long getDefaultLong(String name) {
		return fPreferences.getDefaultLong(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
	 */
	public String getDefaultString(String name) {
		return fPreferences.getDefaultString(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		return fPreferences.getDouble(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
	 */
	public float getFloat(String name) {
		return fPreferences.getFloat(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		return fPreferences.getInt(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
	 */
	public long getLong(String name) {
		return fPreferences.getLong(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
	 */
	public String getString(String name) {
		return fPreferences.getString(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
	 */
	public boolean isDefault(String name) {
		return fPreferences.isDefault(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return getPreferences().needsSaving();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
	 */
	public void putValue(String name, String value) {
		getPreferences().setValue(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
	 */
	public void setDefault(String name, double value) {
		getPreferences().setDefault(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
	 */
	public void setDefault(String name, float value) {
		getPreferences().setDefault(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
	 */
	public void setDefault(String name, int value) {
		getPreferences().setDefault(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
	 */
	public void setDefault(String name, long value) {
		getPreferences().setDefault(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String name, String defaultObject) {
		getPreferences().setDefault(name, defaultObject);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
	 */
	public void setDefault(String name, boolean value) {
		getPreferences().setDefault(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
	 */
	public void setToDefault(String name) {
		getPreferences().setToDefault(name);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
	 */
	public void setValue(String name, double value) {
		getPreferences().setValue(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
	 */
	public void setValue(String name, float value) {
		getPreferences().setValue(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
	 */
	public void setValue(String name, int value) {
		getPreferences().setValue(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
	 */
	public void setValue(String name, long value) {
		getPreferences().setValue(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
	 */
	public void setValue(String name, String value) {
		getPreferences().setValue(name, value);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
	 */
	public void setValue(String name, boolean value) {
		getPreferences().setValue(name, value);
	}
	protected Preferences getPreferences() {
		return fPreferences;
	}
}
