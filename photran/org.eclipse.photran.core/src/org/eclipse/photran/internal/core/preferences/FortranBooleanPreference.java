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

/**
 * A workspace preference which holds a Boolean value.
 * 
 * @author Jeff Overbey
 * 
 * @see FortranPreference
 */
@SuppressWarnings("deprecation")
public final class FortranBooleanPreference extends FortranPreference
{
    private boolean defaultValue;

    public FortranBooleanPreference(String name, boolean defaultValue)
    {
        super("boolean", name); //$NON-NLS-1$
        this.defaultValue = defaultValue;
    }

    @Override public void setDefault()
    {
        getPreferenceStore().setDefault(getName(), defaultValue);
    }

    /**
     * Sets the value of this preference
     * @param preferences
     */
    public void setValue(boolean value)
    {
        getPreferenceStore().setValue(getName(), value);
        savePreferenceStore();
    }

    /**
     * @return the value of this preference
     */
    public boolean getValue()
    {
        if (!getPreferenceStore().contains(getName()))
            return defaultValue;
        else
            return getPreferenceStore().getBoolean(getName());
    }
}