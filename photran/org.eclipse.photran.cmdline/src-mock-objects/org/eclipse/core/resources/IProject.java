package org.eclipse.core.resources;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IProject extends IContainer
{
    IPath getFullPath();

    boolean hasNature(String natureId) throws CoreException;
    
    IProject ROOT = new IProject()
    {
        public IPath getFullPath()
        {
            return Util.pathFor(WorkspaceRoot.WORKSPACE_ROOT_DIR);
        }

        public boolean hasNature(String natureId) throws CoreException
        {
            return true;
        }

        public boolean isAccessible()
        {
            return true;
        }

        public void accept(IResourceVisitor visitor) throws CoreException
        {
            // TODO Auto-generated method stub
            
        }

        public String getName()
        {
            return new File(".").getName();
        }

		public IMarker createMarker(String type) throws CoreException
		{
			throw new UnsupportedOperationException();
		}
    };
}
