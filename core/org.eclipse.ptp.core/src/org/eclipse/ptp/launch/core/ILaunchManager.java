package org.eclipse.ptp.launch.core;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.pdt.mi.MISession;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IPJob;

/**
 * @author Clement
 *
 */
public interface ILaunchManager {
    public static final int STATE_ERROR = -1;
    public static final int STATE_RUN = 1;
    public static final int STATE_EXIT = 3;
    public static final int STATE_ABORT = 2;
    public static final int STATE_START = 0;
    public static final int STATE_STOPPED = 4;
    
    public static final int EVENT_PROCESS_OUTPUT = 1;
    public static final int EVENT_EXEC_STATUS_CHANGE = 2;
    public static final int EVENT_SYS_STATUS_CHANGE = 3;
    public static final int EVENT_ERROR = 4;
    public static final int EVENT_UPDATED_STATUS = 5;
    
    public static final int ALL_PROCESSES_STOPPED = 6;

    public boolean isParallelPerspectiveOpen();
    
    public void shutdown();
    public MISession getSession();
    public IPMachine getMachine();
    public IPUniverse getUniverse();
    public IPJob getProcessRoot();
    
    public void addParallelLaunchListener(IParallelLaunchListener listener);
    public void removeParallelLaunchListener(IParallelLaunchListener listener);
    
    public int getCurrentState();
    public boolean isMPIRuning();
    public boolean hasProcessRunning();
    public void mpirun(String[] args) throws CoreException;
    public void mpistatus() throws CoreException;
    public void mpiabort() throws CoreException;
    public void mpiexit() throws CoreException;
    public void mpisysstatus() throws CoreException;
    public void createMPISession() throws CoreException;
    public void execMI(ILaunch launch, File workingDirectory, String[] envp, String[] args, IProgressMonitor pm) throws CoreException;
    
    public void setPDTConfiguration(ILaunchConfiguration config);
    public ILaunchConfiguration getPDTConfiguration();
}
