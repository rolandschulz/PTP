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
package org.eclipse.ptp.internal.debug.ui.propertypages;

import java.util.HashMap;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author clement chu
 *
 */
public class PBreakpointPreferenceStore implements IPreferenceStore {
	protected final static String ENABLED = "ENABLED"; //$NON-NLS-1$
	protected final static String CONDITION = "CONDITION"; //$NON-NLS-1$
	protected final static String IGNORE_COUNT = "IGNORE_COUNT"; //$NON-NLS-1$

	protected HashMap<String, Object> properties;
	private boolean isDirty = false;
	private ListenerList listeners;

	/** Constructor
	 * 
	 */
	public PBreakpointPreferenceStore() {
		properties = new HashMap<String, Object>(3);
		listeners = new ListenerList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		return properties.containsKey(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		Object[] listenerArray = listeners.getListeners();
		if (listenerArray.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
			PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
			for(int i = 0; i < listenerArray.length; ++i) {
				IPropertyChangeListener l = (IPropertyChangeListener)listenerArray[i];
				l.propertyChange(pe);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		Object b = properties.get(name);
		if (b instanceof Boolean) {
			return ((Boolean)b).booleanValue();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
	 */
	public boolean getDefaultBoolean(String name) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
	 */
	public double getDefaultDouble(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
	 */
	public float getDefaultFloat(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
	 */
	public int getDefaultInt(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
	 */
	public long getDefaultLong(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
	 */
	public String getDefaultString(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
	 */
	public float getFloat(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		Object i = properties.get(name);
		if (i instanceof Integer) {
			return ((Integer)i).intValue();
		}
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
	 */
	public long getLong(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
	 */
	public String getString(String name) {
		Object str = properties.get(name);
		if (str instanceof String) {
			return (String)str;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
	 */
	public boolean isDefault(String name) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
	 */
	public void putValue(String name, String newValue) {
		Object oldValue = properties.get(name);
		if (oldValue == null || !oldValue.equals(newValue)) {
			properties.put(name, newValue);
			setDirty(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
	 */
	public void setDefault(String name, double value) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
	 */
	public void setDefault(String name, float value) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
	 */
	public void setDefault(String name, int value) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
	 */
	public void setDefault(String name, long value) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String name, String defaultObject) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
	 */
	public void setDefault(String name, boolean value) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
	 */
	public void setToDefault(String name) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
	 */
	public void setValue(String name, double value) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
	 */
	public void setValue(String name, float value) {}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
	 */
	public void setValue(String name, int newValue) {
		int oldValue = getInt(name);
		if (oldValue != newValue) {
			properties.put(name, new Integer(newValue) );
			setDirty(true);
			firePropertyChangeEvent(name, new Integer(oldValue), new Integer(newValue));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
	 */
	public void setValue(String name, long value) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
	 */
	public void setValue(String name, String newValue) {
		Object oldValue = properties.get(name);
		if (oldValue == null || !oldValue.equals(newValue)) {
			properties.put(name, newValue);
			setDirty(true);
			firePropertyChangeEvent(name, oldValue, newValue);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
	 */
	public void setValue(String name, boolean newValue) {
		boolean oldValue = getBoolean(name);
		if (oldValue != newValue) {
			properties.put(name, new Boolean(newValue));
			setDirty(true);
			firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(newValue));
		}
	}

	/** Set Dirty
	 * @param isDirty
	 */
	protected void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
}
