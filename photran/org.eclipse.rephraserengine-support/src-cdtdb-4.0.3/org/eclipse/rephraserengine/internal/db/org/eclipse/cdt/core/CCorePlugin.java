/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *     oyvind.harboe@zylin.com - http://bugs.eclipse.org/250638
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class CCorePlugin
{
    public static final String PLUGIN_ID = "bz.over.vpg.cdtdb"; //$NON-NLS-1$

    private static ResourceBundle fgResourceBundle;

    // -------- static methods --------

    static {
        try {
            fgResourceBundle = ResourceBundle.getBundle("org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.CCorePluginResources"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            fgResourceBundle = null;
        }
    }

    public static String getResourceString(String key) {
        try {
            return fgResourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
        } catch (NullPointerException e) {
            return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void log(Throwable e)
    {
        e.printStackTrace();
    }
}
