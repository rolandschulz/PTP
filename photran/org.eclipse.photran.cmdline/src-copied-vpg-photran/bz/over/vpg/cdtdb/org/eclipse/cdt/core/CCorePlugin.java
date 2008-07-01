package bz.over.vpg.cdtdb.org.eclipse.cdt.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class CCorePlugin
{
    public static final String PLUGIN_ID = "bz.over.vpg.cdtdb"; //$NON-NLS-1$

    private static ResourceBundle fgResourceBundle;

    // -------- static methods --------

    static {
        try {
            fgResourceBundle = ResourceBundle.getBundle("bz.over.vpg.cdtdb.org.eclipse.cdt.internal.core.CCorePluginResources"); //$NON-NLS-1$
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
