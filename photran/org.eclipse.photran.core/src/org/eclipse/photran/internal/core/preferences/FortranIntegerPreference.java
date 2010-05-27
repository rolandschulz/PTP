/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.preferences;

/**
 * A workspace preference which holds an integer value.
 * 
 * @author Timofey Yuvashev
 * 
 * @see FortranPreference
 */
@SuppressWarnings("deprecation")
public class FortranIntegerPreference extends FortranPreference
{
    private int defaultValue;

    private int upperLimit; // Value can be <= upperLimit

    private int lowerLimit; // Value can be >= lowerLimit

    public static final int NO_LIMIT = -1;

    public FortranIntegerPreference(String name, int defaultValue)
    {
        super("integer", name); //$NON-NLS-1$
        this.defaultValue = defaultValue;
        upperLimit = NO_LIMIT;
        lowerLimit = NO_LIMIT;
    }

    public FortranIntegerPreference(String name, int defaultValue, int upperLimit, int lowerLimit)
    {
        super("integer", name); //$NON-NLS-1$
        this.defaultValue = defaultValue;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }

    @Override public void setDefault()
    {
        getPreferenceStore().setDefault(getName(), defaultValue);
    }

    /**
     * Sets the value of this preference
     * @param preferences
     */
    public void setValue(int value)
    {
        if (upperLimit != NO_LIMIT && value > upperLimit) return;
        if (lowerLimit != NO_LIMIT && value < lowerLimit) return;

        /*
         * We store the value as a String due to some problematic behavior with default values. In
         * particular, when I set the value of fixed-form comment column to equal to its default
         * value (72) the preferenceStore would return 0, until the default was re-set. Storing the
         * value as a String solved the problem.
         */
        getPreferenceStore().setValue(getName(), String.valueOf(value));
        savePreferenceStore();
    }

    /**
     * Allowed values will be <= upperLimit
     */
    public void setUpperLimit(int value)
    {
        upperLimit = value;
    }

    /**
     * Allowed values will be >= lowerLimit
     */
    public void setLowerLimit(int value)
    {
        lowerLimit = value;
    }

    public int getUpperLimit()
    {
        return upperLimit;
    }

    public int getLowerLimit()
    {
        return lowerLimit;
    }

    /**
     * @return the value of this preference
     */
    public int getValue()
    {
        /*
         * We store the value as a String due to some problematic behavior with default values. In
         * particular, when I set the value of fixed-form comment column to equal to its default
         * value (72) the preferenceStore would return 0, until the default was re-set. Storing the
         * value as a String solved the problem.
         */
        String val = getPreferenceStore().getString(getName());
        if (val == "") //$NON-NLS-1$
        {
            setValue(defaultValue);
            return defaultValue;
        }
        return Integer.parseInt(val);
    }
}
