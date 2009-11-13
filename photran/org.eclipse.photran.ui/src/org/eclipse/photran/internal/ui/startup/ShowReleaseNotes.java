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
            openWebBrowser();
    }
    
    private boolean shouldShowReleaseNotes()
    {
        if (FortranPreferences.RELEASE_NOTES_SHOWN.getValue() == true)
        {
            // The release notes were already displayed once; don't show them again
            return false;
        }
        else
        {
            FortranPreferences.RELEASE_NOTES_SHOWN.setValue(true);
            
            // Make sure the preference was actually set.
            // If there was a problem saving the preference, we shouldn't show the release notes,
            // since we might have already done that and we don't want to annoy the user
            return FortranPreferences.RELEASE_NOTES_SHOWN.getValue();
        }
    }

    private void openWebBrowser()
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
                        .openURL(new URL(getURL()));
                }
                catch (Throwable e)
                {
                    // Ignore
                }
            }
        });
    }
    
    private String getURL()
    {
        Version version = FortranUIPlugin.getDefault().getBundle().getVersion();
        return "http://www.eclipse.org/photran/welcome/"
            + version.getMajor()
            + "."
            + version.getMinor()
            + "/?version="
            + version.toString()
            + "&os="
            + Platform.getOS()
            + "&arch="
            + Platform.getOSArch();
    }
}
