package org.eclipse.ptp.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;

public interface IPWatchpoint extends IPBreakpoint, ILineBreakpoint {
	public static final String EXPRESSION = "org.eclipse.ptp.debug.core.expression";	
	public static final String WRITE = "org.eclipse.ptp.debug.core.write";	
	public static final String READ = "org.eclipse.ptp.debug.core.read";
	boolean isWriteType() throws CoreException;
	boolean isReadType() throws CoreException;
	String getExpression() throws CoreException;
}
