package org.eclipse.ptp.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public interface IResumeAtLineTarget {	
	public void resumeAtLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) throws CoreException;
	public boolean canResumeAtLine( IWorkbenchPart part, ISelection selection, ISuspendResume target );
}
