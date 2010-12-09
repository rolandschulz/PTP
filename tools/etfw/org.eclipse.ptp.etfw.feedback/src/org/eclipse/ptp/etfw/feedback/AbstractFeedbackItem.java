/**
 * 
 */
package org.eclipse.ptp.etfw.feedback;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem;

/**
 * Code to be shared amongst implementors of IFeedbackItem - generic stuff
 * 
 * @author beth
 * @since 3.0
 * 
 */
public abstract class AbstractFeedbackItem implements IFeedbackItem {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getIFile()
	 */
	public IFile getIFile() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String filename = getFile(); // assumes this contains project name
		Path path = new Path(filename);
		IFile file = root.getFile(path); // works when filename contains project name
		return file;
	}

	/**
	 * from SampleFeedbackParser.getResource <br>
	 * Works for remote projects/files too
	 * 
	 * @param projName
	 * @param filename
	 * @return
	 * @since 3.0
	 */
	private static IResource getResourceInProject(String projName, String filename) {
		ResourcesPlugin.getWorkspace();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject proj = root.getProject(projName);
		IResource res = proj.findMember(filename);
		boolean exists = res.exists();

		// IFile file=root.getFile(new Path(filename)); // works when filename
		// contains project name
		return res;
	}

}
