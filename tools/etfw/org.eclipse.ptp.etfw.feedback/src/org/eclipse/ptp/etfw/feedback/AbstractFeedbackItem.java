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

/**
 * Code to be shared amongst implementors of IFeedbackItem - generic stuff
 * 
 * @author beth
 * @since 5.0
 * 
 */
public abstract class AbstractFeedbackItem implements IFeedbackItem {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem#getIFile()
	 */
	public IFile getIFile() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = null;
		String filename = getFile(); // assumes this contains project name
		Path path = new Path(filename);

		// if filename is fully qualified
		if (path.isAbsolute()) {
			file = root.getFileForLocation(path);
			// if filename starts with '/' but isn't fully absolute
			// e.g. "/proj/filename.ext"... try one more
			if (file == null) {
				file = root.getFile(path);
			}
		} else {
			// works when filename contains project name e.g. "proj/fn.ext"
			file = root.getFile(path);
		}

		if ((file == null) || (!file.exists())) {
			System.out.println("Warning: AbstractFeedbackItem, file " + filename + " does not exist as " + file);
		}
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
