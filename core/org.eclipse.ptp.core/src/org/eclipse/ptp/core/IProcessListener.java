package org.eclipse.ptp.core;

/**
 * @author Clement
 *
 */
public interface IProcessListener {
    public void changeStatus(String status);
    public void changeExitCode(String exitcode);    
    public void changeSignalName(String signalName);    
    public void addOutput(String output);
}
