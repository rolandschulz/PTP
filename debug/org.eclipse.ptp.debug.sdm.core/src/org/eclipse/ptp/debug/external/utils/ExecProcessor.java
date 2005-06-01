package org.eclipse.ptp.debug.external.utils;

public interface ExecProcessor {
	// This method gets called when the process sent us a new input String..
	public void processNewInput(String input);

	// This method gets called when the process sent us a new error String..
	public void processNewError(String error);

	// This method gets called when the process has ended..
	public void processEnded(int exitValue);
}