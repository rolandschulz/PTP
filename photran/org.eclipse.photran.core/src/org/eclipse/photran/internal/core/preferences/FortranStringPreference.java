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
 * A workspace preference which holds a string value.
 * 
 * @author Jeff Overbey
 * 
 * @see FortranPreference
 */
@SuppressWarnings("deprecation")
public final class FortranStringPreference extends FortranPreference
{
    private String defaultValue;

    public FortranStringPreference(String name, String defaultValue)
    {
        super("string", name); //$NON-NLS-1$
        this.defaultValue = defaultValue;
    }

    public void setDefault()
    {
        getPreferenceStore().setDefault(getName(), defaultValue);
    }

    /**
     * Sets the value of this preference
     * @param preferences
     */
    public void setValue(String value)
    {
        getPreferenceStore().setValue(getName(), value);
        savePreferenceStore();
    }

    /**
     * @return the value of this preference
     */
    public String getValue()
    {
        return getPreferenceStore().getString(getName());
    }
}