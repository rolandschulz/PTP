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
import org.eclipse.photran.internal.core.FortranCorePlugin;
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
        if (runningJUnitTests() || releaseNotesHaveAlreadyBeenDisplayed())
        {
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
     * The JUnit runner changes the eclipse.application property to
     * <tt>org.eclipse.pde.junit.runtime.uitestapplication</tt>, so
     * we can test for this to determine whether or not we are running
     * a JUnit Plug-in Test.  (Since the workspace is generally cleared
     * on every run, this would cause the Release Notes to be shown
     * every test run, which should not happen.)
     * 
     * @return true if we are running a JUnit Plug-in Test
     */
    private boolean runningJUnitTests()
    {
        if (FortranCorePlugin.inTestingMode()) return true;
        
        String app = System.getProperty("eclipse.application"); //$NON-NLS-1$
        if (app != null && app.toLowerCase().contains("junit")) return true; //$NON-NLS-1$
        
        return false;
    }

    /**
     * A hidden workspace preference is used to track whether or not
     * the release notes have been shown for this workspace.
     * 
     * @return true iff the Release Notes Shown workspace preference
     * indicates that the release notes have already been shown for
     * this workspace with this version of Photran
     */
    private boolean releaseNotesHaveAlreadyBeenDisplayed()
    {
        return photranVersion().toString().equals(getPreference());
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
                            UIMessages.ShowReleaseNotes_WelcomeTitle,
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
        return "http://www.eclipse.org/photran/welcome/" //$NON-NLS-1$
            + version.getMajor()
            + "." //$NON-NLS-1$
            + version.getMinor()
            + "/?version=" //$NON-NLS-1$
            + version.toString()
            + "&os=" //$NON-NLS-1$
            + Platform.getOS()
            + "&arch=" //$NON-NLS-1$
            + Platform.getOSArch()
            + "&lastVersion=" //$NON-NLS-1$
            + lastVersion;
    }

    private Version photranVersion()
    {
        return FortranUIPlugin.getDefault().getBundle().getVersion();
    }
}
