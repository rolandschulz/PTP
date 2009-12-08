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

import java.lang.reflect.Field;

import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.swt.graphics.RGB;

/**
 * Provides access to all of the workspace-wide preferences for Photran.
 * <p>
 * The user may set these via the standard Eclipse preferences mechanism
 * (usually by clicking Window > Preferences).
 * <p>
 * <i>All Photran preferences must have a constant declared in this file
 * and should be accessed via that constant.  They should be implemented
 * as subclasses of <code>FortranPreference</code>.</i>
 *
 * @author Jeff Overbey
 *
 * TODO: Jeff: Make sure we call <code>FortranPreferences.initializeDefaults(getPluginPreferences());</code>
 */
public final class FortranPreferences
{
    public static final FortranStringPreference RELEASE_NOTES_SHOWN = new FortranStringPreference("releasenotesversionshown", "");

    public static final FortranBooleanPreference ENABLE_VPG_LOGGING = new FortranBooleanPreference("vpglogging", false);
    public static final FortranBooleanPreference SHOW_PARSE_TREE = new FortranBooleanPreference("parsetree", false);
    public static final FortranBooleanPreference ENABLE_FIXED_FORM_FOLDING = new FortranBooleanPreference("fixedfolding", false);
    public static final FortranBooleanPreference ENABLE_FREE_FORM_FOLDING = new FortranBooleanPreference("freefolding", false);

    public static final FortranRGBPreference COLOR_COMMENTS    = new FortranRGBPreference("comments",    new RGB(63,  127, 95 ));
    public static final FortranRGBPreference COLOR_IDENTIFIERS = new FortranRGBPreference("identifiers", new RGB(0,   0,   192));
    public static final FortranRGBPreference COLOR_INTRINSICS  = new FortranRGBPreference("intrinsics",  new RGB(96,  0,   192));
    public static final FortranRGBPreference COLOR_KEYWORDS    = new FortranRGBPreference("keywords",    new RGB(127, 0,   85 ));
    public static final FortranRGBPreference COLOR_STRINGS     = new FortranRGBPreference("strings",     new RGB(42,  0,   255));
    public static final FortranRGBPreference COLOR_CPP         = new FortranRGBPreference("cpp",         new RGB(128, 128, 128));

    public static final FortranStringPreference PREFERRED_MODEL_BUILDER = new FortranStringPreference("modelbuilder", "");
    public static final FortranStringPreference PREFERRED_DOM_PARSER = new FortranStringPreference("domparser", "");

    private FortranPreferences() {}

    /** Initializes/resets all Photran-specific preferences to their default values */
    public static void initializeDefaults()
    {
        try
        {
            Field[] fields = FortranPreferences.class.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                FortranPreference pref = (FortranPreference)fields[i].get(null);
                pref.setDefault();
            }
        }
        catch (IllegalAccessException e)
        {
            throw new Error(e);
        }
    }

    /** @return true iff the given preference is a Photran-specific preference */
    public static boolean respondToPreferenceChange(String property)
    {
        try
        {
            Field[] fields = FortranPreferences.class.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                FortranPreference pref = (FortranPreference)fields[i].get(null);
                if (property.equals(pref.getName()))
                    return true;
            }
            return false;
        }
        catch (IllegalAccessException e)
        {
            throw new Error(e);
        }
    }

    /** Saves the values of <i>all</i> workspace-wide Photran preferences */
    public static void savePreferenceStore()
    {
        FortranCorePlugin.getDefault().savePluginPreferences();
    }
}
