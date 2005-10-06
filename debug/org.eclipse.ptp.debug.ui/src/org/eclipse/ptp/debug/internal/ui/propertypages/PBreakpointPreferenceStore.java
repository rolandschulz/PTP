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
package org.eclipse.ptp.debug.internal.ui.propertypages;

import java.util.HashMap;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author clement chu
 *
 */
public class PBreakpointPreferenceStore implements IPreferenceStore {
	protected final static String ENABLED = "ENABLED";
	protected final static String CONDITION = "CONDITION";
	protected final static String IGNORE_COUNT = "IGNORE_COUNT";

	protected HashMap properties;
	private boolean isDirty = false;
	private ListenerList listeners;

	public PBreakpointPreferenceStore() {
		properties = new HashMap(3);
		listeners = new ListenerList();
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	public boolean contains(String name) {
		return properties.containsKey(name);
	}

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

	public boolean getBoolean(String name) {
		Object b = properties.get(name);
		if (b instanceof Boolean) {
			return ((Boolean)b).booleanValue();
		}
		return false;
	}

	public boolean getDefaultBoolean(String name) {
		return false;
	}

	public double getDefaultDouble(String name) {
		return 0;
	}

	public float getDefaultFloat(String name) {
		return 0;
	}

	public int getDefaultInt(String name) {
		return 0;
	}

	public long getDefaultLong(String name) {
		return 0;
	}

	public String getDefaultString(String name) {
		return null;
	}

	public double getDouble(String name) {
		return 0;
	}

	public float getFloat(String name) {
		return 0;
	}

	public int getInt(String name) {
		Object i = properties.get(name);
		if (i instanceof Integer) {
			return ((Integer)i).intValue();
		}
		return 1;
	}

	public long getLong(String name) {
		return 0;
	}

	public String getString(String name) {
		Object str = properties.get(name);
		if (str instanceof String) {
			return (String)str;
		}
		return null;
	}

	public boolean isDefault(String name) {
		return false;
	}

	public boolean needsSaving() {
		return isDirty;
	}

	public void putValue(String name, String newValue) {
		Object oldValue = properties.get(name);
		if (oldValue == null || !oldValue.equals(newValue)) {
			properties.put(name, newValue);
			setDirty(true);
		}
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	public void setDefault(String name, double value) {
	}
	public void setDefault(String name, float value) {
	}
	public void setDefault(String name, int value) {
	}
	public void setDefault(String name, long value) {
	}
	public void setDefault(String name, String defaultObject) {
	}
	public void setDefault(String name, boolean value) {
	}
	public void setToDefault(String name) {
	}
	public void setValue(String name, double value) {
	}
	public void setValue(String name, float value) {
	}
	public void setValue(String name, int newValue) {
		int oldValue = getInt(name);
		if (oldValue != newValue) {
			properties.put(name, new Integer(newValue) );
			setDirty(true);
			firePropertyChangeEvent(name, new Integer(oldValue), new Integer(newValue));
		}
	}

	public void setValue(String name, long value) {
	}

	public void setValue(String name, String newValue) {
		Object oldValue = properties.get(name);
		if (oldValue == null || !oldValue.equals(newValue)) {
			properties.put(name, newValue);
			setDirty(true);
			firePropertyChangeEvent(name, oldValue, newValue);
		}
	}

	public void setValue(String name, boolean newValue) {
		boolean oldValue = getBoolean(name);
		if (oldValue != newValue) {
			properties.put(name, new Boolean(newValue));
			setDirty(true);
			firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(newValue));
		}
	}

	protected void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
}
