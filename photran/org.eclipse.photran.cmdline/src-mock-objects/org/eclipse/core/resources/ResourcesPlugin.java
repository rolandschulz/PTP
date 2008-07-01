package org.eclipse.core.resources;

public class ResourcesPlugin
{
    private static IWorkspace ws = new IWorkspace()
    {
        public IWorkspaceRoot getRoot()
        {
            return new WorkspaceRoot();
        }
    };
    
    public static IWorkspace getWorkspace()
    {
        return ws;
    }
}
