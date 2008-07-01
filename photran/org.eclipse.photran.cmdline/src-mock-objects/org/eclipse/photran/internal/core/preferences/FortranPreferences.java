package org.eclipse.photran.internal.core.preferences;

public final class FortranPreferences
{
    public static final class BooleanPreference
    {
        private boolean value;
        
        public BooleanPreference(boolean value) { this.value = value; }
        
        public boolean getValue() { return value; }
    }

    private FortranPreferences() {}

    public static final BooleanPreference ENABLE_VPG_LOGGING = new BooleanPreference(false);
}
