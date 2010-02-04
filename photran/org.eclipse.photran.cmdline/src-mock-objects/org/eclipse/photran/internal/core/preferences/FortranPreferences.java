/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.preferences;

public final class FortranPreferences
{
    public static final class BooleanPreference
    {
        private boolean value;
        public BooleanPreference(boolean value) { this.value = value; }
        public boolean getValue() { return value; }
    }

    public static final class FortranIntegerPreference
    {
        private int value;
        public FortranIntegerPreference(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    private FortranPreferences() {}

    public static final BooleanPreference ENABLE_VPG_LOGGING = new BooleanPreference(false);
    public static final FortranIntegerPreference FIXED_FORM_COMMENT_COLUMN = new FortranIntegerPreference(72);
}
