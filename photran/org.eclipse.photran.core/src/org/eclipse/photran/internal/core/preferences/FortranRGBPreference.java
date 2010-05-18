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

import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * A workspace preference which holds an {@link RGB} (color) value.
 * 
 * @author Jeff Overbey
 * 
 * @see FortranPreference
 */
@SuppressWarnings("deprecation")
public final class FortranRGBPreference extends FortranPreference
{
    private RGB defaultValue;

    public FortranRGBPreference(String name, RGB defaultValue)
    {
        super("rgb", name); //$NON-NLS-1$
        this.defaultValue = defaultValue;
    }

    public final void setDefault()
    {
        getPreferenceStore().setDefault(getName(), StringConverter.asString(defaultValue));
    }

    /**
     * Sets the value of this preference
     * @param preferences
     */
    public void setValue(RGB value)
    {
        getPreferenceStore().setValue(getName(), StringConverter.asString(value));
        savePreferenceStore();
    }

    /**
     * @return the value of this preference
     */
    public RGB getValue()
    {
        try
        {
            return StringConverter.asRGB(getPreferenceStore().getString(getName()));
        }
        catch (DataFormatException e)
        {
            setValue(defaultValue);
            return defaultValue;
        }
    }
}