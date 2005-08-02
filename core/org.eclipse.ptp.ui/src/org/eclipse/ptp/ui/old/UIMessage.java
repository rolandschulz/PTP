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
package org.eclipse.ptp.ui.old;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class UIMessage {
    private static final String BUNDLE_NAME = PTPUIPlugin.PLUGIN_ID + ".UIMessages";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public static String getResourceString(String key) {
		// No point trying if bundle is null as exceptions are costly
		if (RESOURCE_BUNDLE != null) {
			try {
				return RESOURCE_BUNDLE.getString(key);
			} catch (MissingResourceException e) {
				return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
			} catch (NullPointerException e) {
				return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// If we get here, then bundle is null.
		return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getFormattedResourceString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[]{arg});
	}

	public static String getFormattedResourceString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}
}
