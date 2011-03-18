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
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

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
@SuppressWarnings({"deprecation", "restriction"})
public final class FortranPreferences
{
    public static final FortranStringPreference RELEASE_NOTES_SHOWN = new FortranStringPreference("releasenotesversionshown", ""); //$NON-NLS-1$ //$NON-NLS-2$

    public static final FortranBooleanPreference ENABLE_VPG_LOGGING = new FortranBooleanPreference("vpglogging", false); //$NON-NLS-1$
    public static final FortranBooleanPreference SHOW_PARSE_TREE = new FortranBooleanPreference("parsetree", false); //$NON-NLS-1$
    public static final FortranBooleanPreference ENABLE_FOLDING = new FortranBooleanPreference("folding", true); //$NON-NLS-1$
    public static final FortranBooleanPreference ENABLE_RULER = new FortranBooleanPreference("ruler", true); //$NON-NLS-1$

    public static final FortranRGBPreference COLOR_COMMENTS            = new FortranRGBPreference("comments",    new RGB(63,  127, 95 )); //$NON-NLS-1$
    public static final FortranRGBPreference COLOR_IDENTIFIERS         = new FortranRGBPreference("identifiers", new RGB(0,   0,   192)); //$NON-NLS-1$
    public static final FortranRGBPreference COLOR_INTRINSICS          = new FortranRGBPreference("intrinsics",  new RGB(96,  0,   192)); //$NON-NLS-1$
    public static final FortranRGBPreference COLOR_KEYWORDS            = new FortranRGBPreference("keywords",    new RGB(127, 0,   85 )); //$NON-NLS-1$
    public static final FortranRGBPreference COLOR_STRINGS             = new FortranRGBPreference("strings",     new RGB(42,  0,   255)); //$NON-NLS-1$
    public static final FortranRGBPreference COLOR_NUMBERS_PUNCTUATION = new FortranRGBPreference("nums_punc",   new RGB(0,   0,   0  )); //$NON-NLS-1$
    public static final FortranRGBPreference COLOR_CPP                 = new FortranRGBPreference("cpp",         new RGB(128, 128, 128)); //$NON-NLS-1$

    public static final FortranStringPreference PREFERRED_MODEL_BUILDER = new FortranStringPreference("modelbuilder", ""); //$NON-NLS-1$ //$NON-NLS-2$
    public static final FortranStringPreference PREFERRED_DOM_PARSER = new FortranStringPreference("domparser", ""); //$NON-NLS-1$ //$NON-NLS-2$

    public static final FortranIntegerPreference FIXED_FORM_COMMENT_COLUMN = new FortranIntegerPreference("fixedformcommentcolum", 72, 9999, 72); //$NON-NLS-1$
    public static final FortranBooleanPreference CONVERT_TABS_TO_SPACES = new FortranBooleanPreference("converttabs", true); //$NON-NLS-1$
    public static final FortranTabWidthPreference TAB_WIDTH = new FortranTabWidthPreference("tabwidth", 0, 16, 0); //$NON-NLS-1$
    
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
