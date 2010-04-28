/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui;

import org.eclipse.ptp.core.attributes.IAttribute;

/*
 *  Bean wrapper around IAttribute.  Allows for associating of addition
 *  toolTip and for recording whether the attribute has been selected
 *  for inclusion by the user.
 */
public class AttributePlaceholder {
	private IAttribute<?, ?, ?> attribute;
	private boolean checked = true;
	private String name;
	private String toolTip;

	public IAttribute<?, ?, ?> getAttribute() {
		return attribute;
	}

	public boolean getChecked() {
		return checked;
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

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}
}
