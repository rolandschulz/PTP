/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.startup;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.ui.FortranUIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.osgi.framework.Version;

/**
 * Opens the release notes for this version of Photran in a Web browser the first time Photran is
 * started on a particular workspace (and never again after that).
 * <p>
 * Called by Eclipse when the UI plug-in is loaded (see the org.eclipse.ui.startup extension point).
 *
 * @author Jeff Overbey
 */
public final class ShowReleaseNotes implements IStartup
{
    public void earlyStartup()
    {
        if (shouldShowReleaseNotes())
        {
            openWebBrowser(setPreference());
        }
    }

    private boolean shouldShowReleaseNotes()
    {
        if (runningJUnitTests())
        {
            // Don't show the release notes during JUnit test runs (which clear the workspace)
            return false;
        }
        else if (photranVersion().toString().equals(getPreference()))
        {
            // The release notes were already displayed once; don't show them again
            return false;
        }
        else
        {
            /*
            GregorianCalendar today = new GregorianCalendar();
            return !today.after(new GregorianCalendar(2012, GregorianCalendar.JANUARY, 1));
            */

            return true;
        }
    }

    /**
     * @return
     */
    private boolean runningJUnitTests()
    {
        if (System.getenv("TESTING") != null) return true;
        
        String app = System.getProperty("eclipse.application");
        if (app != null && app.toLowerCase().contains("junit")) return true;
        
        return false;
    }

    /** @return the current value of the Release Notes Shown workspace preference */
    private String getPreference()
    {
        return FortranPreferences.RELEASE_NOTES_SHOWN.getValue();
    }

    /** Sets the Release Notes Shown workspace preference, and returns its previous value */
    private String setPreference()
    {
        String lastVersion = getPreference();
        FortranPreferences.RELEASE_NOTES_SHOWN.setValue(photranVersion().toString());
        return lastVersion;
    }

    private void openWebBrowser(final String lastVersion)
    {
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                try
                {
                    PlatformUI.getWorkbench().getBrowserSupport()
                        .createBrowser(
                            IWorkbenchBrowserSupport.LOCATION_BAR
                                | IWorkbenchBrowserSupport.NAVIGATION_BAR,
                            null,
                            "Welcome",
                            null)
                        .openURL(new URL(getURL(lastVersion)));
                }
                catch (Throwable e)
                {
                    // Ignore
                }
            }
        });
    }

    private String getURL(String lastVersion)
    {
        Version version = photranVersion();
        return "http://www.eclipse.org/photran/welcome/"
            + version.getMajor()
            + "."
            + version.getMinor()
            + "/?version="
            + version.toString()
            + "&os="
            + Platform.getOS()
            + "&arch="
            + Platform.getOSArch()
            + "&lastVersion="
            + lastVersion;
    }

    private Version photranVersion()
    {
        return FortranUIPlugin.getDefault().getBundle().getVersion();
    }
}
