package org.eclipse.photran.core.vpg;

import org.eclipse.core.resources.IPath;
import org.eclipse.core.resources.Util;
import org.eclipse.core.resources.WorkspaceRoot;

public class Activator
{
    private static Activator activator;
    
    private Activator() {}

    public static Activator getDefault()
    {
        if (activator == null) activator = new Activator();
        return activator;
    }

    public IPath getStateLocation()
    {
        return Util.pathFor(WorkspaceRoot.WORKSPACE_STATE_DIR);
    }
}
