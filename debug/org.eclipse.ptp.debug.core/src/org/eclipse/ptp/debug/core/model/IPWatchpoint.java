package org.eclipse.ptp.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Represents a watchpoint (expression breakpoint)
 * 
 */
public interface IPWatchpoint extends IPBreakpoint, ILineBreakpoint {
	public static final String EXPRESSION = "org.eclipse.ptp.debug.core.expression"; //$NON-NLS-1$
	public static final String READ = "org.eclipse.ptp.debug.core.read"; //$NON-NLS-1$
	public static final String WRITE = "org.eclipse.ptp.debug.core.write"; //$NON-NLS-1$

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getExpression() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public boolean isReadType() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public boolean isWriteType() throws CoreException;
}
