/*******************************************************************************
 * Copyright (c) 2010 University of Illinois 
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *                  - modified; eliminated unused methods 05/11/2010
 *                  - removed all static final strings into the non-nls
 *                    interface (09/14/2010)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.utils;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.ui.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.ui.data.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;

/**
 * Various utilities for configuring attributes.
 * 
 * @author arossi
 */
public class ConfigUtils implements IPBSNonNLSConstants {
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
		if (!ZEROSTR.equals(toolTip))
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
				if (!ZEROSTR.equals(value) || (attr instanceof StringAttribute && !ZEROSTR.equals(defaultValue)))
					attr.setValueAsString(value);
		}
		return ap;
	}

	/**
	 * For refreshing queue (destination) info from the RM Model definition
	 */
	public static String[] getCurrentQueues(PBSResourceManager rmc) {
		IPResourceManager rm = (IPResourceManager) rmc.getAdapter(IPResourceManager.class);
		String[] items = new String[0];
		if (rm != null) {
			IPQueue[] queues = rm.getQueues();
			if (queues != null && queues.length > 0) {
				List<String> queueNames = new ArrayList<String>();
				for (IPQueue q : queues) {
					String qname = q.getName();
					if (qname.length() > 0)
						queueNames.add(qname);
				}
				items = queueNames.toArray(new String[0]);
			}
		}
		return items;
	}

	public static String readFull(final File file, int bffr_sz) throws Throwable {
		int read = 0;
		byte[] bytes = new byte[bffr_sz];
		FileInputStream stream = null;
		StringBuffer sb = new StringBuffer();
		try {
			stream = new FileInputStream(file);
			while (true) {
				try {
					read = stream.read(bytes, 0, bytes.length);
				} catch (EOFException eof) {
					break;
				}
				if (read == -1)
					break;
				if (read > 0)
					sb.append(new String(bytes, 0, read));
			}
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException t) {
				}
		}
		return sb.toString();
	}
}
