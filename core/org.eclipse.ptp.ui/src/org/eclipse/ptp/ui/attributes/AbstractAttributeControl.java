/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.ui.attributes;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractAttributeControl implements IAttributeControl {
	
	public static final String IS_VALID_PROPERTY = "IS_VALID";
	public static final String VALUE_CHANGED_PROPERTY = "VALUE_CHANGED";
	private final Control control;
	private boolean isValid = true;
	private final ListenerList listenerList = new ListenerList();
	private String errorMessage = "";
	
	public AbstractAttributeControl(Composite parent, int style) {
		this.control = doCreateControl(parent, style);
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.add(listener);
	}
	
	public void dispose() {
		control.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#getAttribute()
	 */
	public abstract IAttribute getAttribute();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#getControl()
	 */
	public Control getControl() {
		return control;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#isValid()
	 */
	public boolean isValid() {
		return isValid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#resetToInitialValue()
	 */
	public abstract void resetToInitialValue();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.attributes.IAttributeControl#setCurrentToInitialValue()
	 */
	public abstract void setCurrentToInitialValue();

	/**
	 * @param parent
	 * @param style
	 * @return
	 */
	protected abstract Control doCreateControl(Composite parent, int style);

	protected void firePropertyChanged(String property, Object oldValue, Object newValue) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue,
				newValue);
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}

	protected void fireValueChanged(Object oldValue, Object newValue) {
		firePropertyChanged(VALUE_CHANGED_PROPERTY,	oldValue, newValue);
	}

	protected void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @param isValid the isValid to set
	 */
	protected void setValid(boolean isValid) {
		boolean oldIsValid = this.isValid;
		this.isValid = isValid;
		if (oldIsValid != this.isValid) {
			if (isValid) {
				setErrorMessage("");
			}
			firePropertyChanged(IS_VALID_PROPERTY, Boolean.valueOf(oldIsValid),
					Boolean.valueOf(this.isValid));
		}
	}
}
