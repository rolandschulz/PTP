/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.attributes;

import org.eclipse.ptp.core.attributes.IAttribute;

/**
 * Wrapper around IAttribute. Allows for associating toolTip and for recording
 * whether the attribute has been selected for inclusion by the user.
 * 
 * @author arossi
 * @since 5.0
 */
public class AttributePlaceholder {
	private IAttribute<?, ?, ?> attribute;
	private String defaultString;
	private String name;
	private String toolTip;

	public IAttribute<?, ?, ?> getAttribute() {
		return attribute;
	}

	public String getDefaultString() {
		return defaultString;
	}

	public String getName() {
		return name;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setAttribute(IAttribute<?, ?, ?> attribute) {
		this.attribute = attribute;
	}

	public void setDefaultString(String defaultString) {
		this.defaultString = defaultString;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}
}
