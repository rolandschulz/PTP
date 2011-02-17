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
 * 
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.rm.ibm.pe.core.rtsystem;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteProxyRuntimeSystem;

public class PERuntimeSystem extends AbstractRemoteProxyRuntimeSystem {
	public PERuntimeSystem(PEProxyRuntimeClient proxy) {
		super(proxy);
	}

	/**
	 * Get the set of attributes to be used as launch attributes
	 * 
	 * @param configuration
	 *            The current launch configuration
	 */
	@Override
	public List<IAttribute<?, ?, ?>> getAttributes(ILaunchConfiguration configuration, String mode) throws CoreException {
		List<IAttribute<?, ?, ?>> attrs = super.getAttributes(configuration, mode);

		if (configuration.getAttribute("PE_ADVANCED_MODE", "").equals("yes")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			addAttribute(configuration, attrs, "PE_ENV_SCRIPT"); //$NON-NLS-1$
		} else {
			for (Object key : configuration.getAttributes().keySet()) {
				if (key instanceof String) {
					String name = (String) key;
					if ((name.startsWith("MP_")) //$NON-NLS-1$
							|| ((name.startsWith("PE_")) && (!name //$NON-NLS-1$
									.equals("PE_ENV_SCRIPT")))) { //$NON-NLS-1$
						addAttribute(configuration, attrs, name);
					}
				}
			}
		}

		return attrs;
	}

	/**
	 * Add an attribute to the set of launch attributes if not same as default
	 * sent from proxy
	 * 
	 * @param config
	 *            The current launch configuration
	 * @param attrs
	 *            The attributes vector containing the set of launch attributes
	 * @param attrName
	 *            The name of the attribute to be added to launch attributes
	 */
	private void addAttribute(ILaunchConfiguration config, List<IAttribute<?, ?, ?>> attrs, String attrName) {
		String attrValue;
		String defaultValue;
		StringAttribute attr;
		StringAttributeDefinition attrDef;

		if (getAttributeDefinitionManager().getAttributeDefinition(attrName) != null) {
			try {
				attrValue = config.getAttribute(attrName, ""); //$NON-NLS-1$
			} catch (CoreException e) {
				attrValue = ""; //$NON-NLS-1$
			}
			defaultValue = getAttrDefaultValue(attrName);
			// Don't add attribute if it has default value or if it is blank.
			// This reduces number of attributes sent in run command.
			if ((attrValue.length() > 0) && (!attrValue.equals(defaultValue))) {
				attrDef = new StringAttributeDefinition(attrName, "", "", //$NON-NLS-1$ //$NON-NLS-2$
						false, ""); //$NON-NLS-1$
				attr = new StringAttribute(attrDef, attrValue);
				attrs.add(attr);
			}
		}
	}

	/**
	 * Get the default value for an attribute from the resource manager
	 * 
	 * @param attrName
	 *            The name of the attribute
	 * @return The value of the attribute
	 */
	private String getAttrDefaultValue(String attrName) {
		IAttributeDefinition<?, ?, ?> attrDef;

		attrDef = getAttributeDefinitionManager().getAttributeDefinition(attrName);
		if (attrDef != null) {
			try {
				return attrDef.create().getValueAsString();
			} catch (IllegalValueException e) {
				return ""; //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
	}
}
