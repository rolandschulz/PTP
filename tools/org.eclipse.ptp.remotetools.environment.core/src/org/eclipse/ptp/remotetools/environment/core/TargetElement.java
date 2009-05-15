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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;


/**
 * 
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class TargetElement implements ITargetElement {

	private TargetTypeElement type;
	private Map<String, String> attributes;
	private String name;
	private String id;
	private ITargetControl control;
	private int status = ITargetStatus.STOPPED;
	private boolean dirty = false;
	
	public TargetElement(TargetTypeElement type, String id) {
		super();
		this.type = type;
		this.id = id;
	}
	
	public TargetElement(TargetTypeElement type, String name, Map<String, String> attrs, String id) {
		this(type, id);
		this.attributes = attrs;
		this.name = name;
		this.type = type;
	}
	
	public void update(Map<String, String> attr) {
		try {
			ITargetControl ctrl = getControl();
			if (attr == null) {
				ctrl.updateConfiguration();
			} else {
				ctrl.updateConfiguration();
			}
			dirty = false;
		} catch (CoreException e) {
			dirty = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
		if (getStatus() == ITargetStatus.STOPPED) {
			update(attributes);
		} else 
			dirty = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#getControl()
	 */
	public ITargetControl getControl() throws CoreException {
		if (control == null) {
			control = type.getExtension().controlFactory(this);
		}
		return control;
	}

	public void setControl(ITargetControl control) {
		this.control = control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#getType()
	 */
	public TargetTypeElement getType() {
		return type;
	}

	public void setType(TargetTypeElement type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#getStatus()
	 */
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
		if (status == ITargetStatus.STOPPED && dirty) {
			update(getAttributes());
			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#toString()
	 */
	public String toString() {
		
		return this.getName() != null ? this.getName() : super.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetElement#getId()
	 */
	public String getId() {
		return id;
	}
	
	
}
