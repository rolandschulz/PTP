package org.eclipse.ptp.debug.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;

public interface IRunToLine {
	public boolean canRunToLine(IFile file, int lineNumber);
	public void runToLine(IFile file, int lineNumber, boolean skipBreakpoints) throws DebugException;
	public boolean canRunToLine(String fileName, int lineNumber);
	public void runToLine(String fileName, int lineNumber, boolean skipBreakpoints) throws DebugException;
}
