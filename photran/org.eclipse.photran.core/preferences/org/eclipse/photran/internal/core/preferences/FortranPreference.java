/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.photran.internal.core.FortranCorePlugin;

/**
 * Superclass for a workspace preference that will be set in Photran's Core plug-in.
 * 
 * @author Jeff Overbey
 * 
 * @see FortranStringPreference
 * @see FortranBooleanPreference
 * @see FortranRGBPreference
 */
/*
 * JAVA5: This should be parameterized by type, and type-based subclasses like
 * <code>FortranStringPreference</code> and <code>FortranBooleanPreference</code> should be
 * eliminated once we start using Java 5.
 */
abstract class FortranPreference
{
    private final String prefix, suffix;
    private final Preferences preferences;
    
    public FortranPreference(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
        this.preferences = FortranCorePlugin.getDefault().getPluginPreferences();
    }

    /**
     * @return the name of this preference, e.g., <code>org.eclipse.photran.core.myPreference</code>
     */
    public final String getName()
    {
        return prefix + "." + suffix;
    }

    /**
     * Sets the default value of this preference
     */
    public abstract void setDefault();
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods for Subclasses
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Preferences getPreferenceStore()
    {
        return preferences;
    }

    protected void savePreferenceStore()
    {
        FortranCorePlugin.getDefault().savePluginPreferences();
    }
}
