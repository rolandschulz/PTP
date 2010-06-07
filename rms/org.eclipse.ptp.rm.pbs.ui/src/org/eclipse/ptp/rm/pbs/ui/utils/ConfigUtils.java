/*******************************************************************************
 * Copyright (c) 2010 University of Illinois 
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *                  - modified; eliminated unused methods 05/11/2010
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.rm.pbs.ui.data.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;

/**
 * Various utilities for configuring attributes.
 * 
 * @author arossi
 */
public class ConfigUtils {
	public static class PrefixFilter implements FilenameFilter {
		private final String prefix;

		public PrefixFilter(String prefix) {
			this.prefix = prefix;
		}

		public boolean accept(File dir, String name) {
			return name.startsWith(prefix);
		}

	}

	public static class SuffixFilter implements FilenameFilter {
		private final String suffix;

		public SuffixFilter(String suffix) {
			this.suffix = suffix;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(suffix);
		}
	}

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	public static String REMOTE_LINE_SEP = "\n"; //$NON-NLS-1$
	public static String REMOTE_PATH_SEP = "/"; //$NON-NLS-1$

	private ConfigUtils() {
	}

	/**
	 * General-purpose method for creating an attribute placeholder. Checks the
	 * key (name) against the known PBS Job Attribute definitions. If valid,
	 * constructs an attribute and sets it on the placeholder.
	 * 
	 * @param key
	 *            name of the attribute
	 * @param value
	 *            of the attribute (can be <code>null</code>)
	 * @param toolTip
	 *            from the definition file, or special internal designation
	 * @param defs
	 *            list of all static (known) attribute definitions
	 * @return the constructed placeholder
	 * @throws IllegalValueException
	 */
	public static AttributePlaceholder getAttributePlaceholder(String key, String value, String toolTip,
			Map<String, IAttributeDefinition<?, ?, ?>> defs) throws IllegalValueException {
		AttributePlaceholder ap = new AttributePlaceholder();
		ap.setName(key);
		if (!EMPTY_STRING.equals(toolTip))
			ap.setToolTip(toolTip);
		if (defs != null) {
			IAttributeDefinition<?, ?, ?> def = defs.get(key);
			if (def == null)
				throw new IllegalValueException(Messages.PBSAttributeNotFound + key);
			/*
			 * creates with default value; we save this in the placeholder
			 */
			IAttribute<?, ?, ?> attr = def.create();
			ap.setAttribute(attr);
			String defaultValue = attr.getValueAsString();
			ap.setDefaultString(defaultValue);
			if (value != null)
				if (!EMPTY_STRING.equals(value) || (attr instanceof StringAttribute && !EMPTY_STRING.equals(defaultValue)))
					attr.setValueAsString(value);
		}
		return ap;
	}

	/*
	 * TODO we need some way of determining this dynamically, though I would
	 * imagine in the vast majority of cases PBS will be running on a UNIX-type
	 * system, so it will be "\n".
	 * 
	 * @param rEMOTE_LINE_SEP
	 */
	public static void setREMOTE_LINE_SEP(String rEMOTE_LINE_SEP) {
		REMOTE_LINE_SEP = rEMOTE_LINE_SEP;
	}

	/*
	 * TODO we need some way of determining this dynamically, though I would
	 * imagine in the vast majority of cases PBS will be running on a UNIX-type
	 * system, so it will be "/".
	 * 
	 * @param rEMOTE_PATH_SEP
	 */
	public static void setREMOTE_PATH_SEP(String rEMOTE_PATH_SEP) {
		REMOTE_PATH_SEP = rEMOTE_PATH_SEP;
	}
}
