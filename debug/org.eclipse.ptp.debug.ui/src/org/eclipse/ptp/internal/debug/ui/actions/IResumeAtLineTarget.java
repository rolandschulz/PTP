package org.eclipse.ptp.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public interface IResumeAtLineTarget {	
	/** Resume at line
	 * @param part
	 * @param selection
	 * @param target
	 * @throws CoreException
	 */
	public void resumeAtLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException;
	/** Can resume at line
	 * @param part
	 * @param selection
	 * @param target
	 * @return
	 */
	public boolean canResumeAtLine(IWorkbenchPart part, ISelection selection, ISuspendResume target);
}
