/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.launch.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pdt.mi.MIException;
import org.eclipse.pdt.mi.MIPlugin;
import org.eclipse.pdt.mi.MISession;
import org.eclipse.pdt.mi.command.CommandFactory;
import org.eclipse.pdt.mi.command.MIExecAbort;
import org.eclipse.pdt.mi.command.MIExecRun;
import org.eclipse.pdt.mi.command.MIExecStatus;
import org.eclipse.pdt.mi.command.MISysStatus;
import org.eclipse.pdt.mi.event.MIErrorEvent;
import org.eclipse.pdt.mi.event.MIExecStatusChangeEvent;
import org.eclipse.pdt.mi.event.MIProcessOutputEvent;
import org.eclipse.pdt.mi.event.MISysStatusChangeEvent;
import org.eclipse.pdt.mi.output.MIProcessDescription;
import org.eclipse.pdt.mi.output.MISystemDescription;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.internal.console.OutputConsole;
import org.eclipse.ptp.internal.core.PNode;
import org.eclipse.ptp.internal.core.PProcess;
import org.eclipse.ptp.internal.core.PMachine;
import org.eclipse.ptp.internal.core.PJob;
import org.eclipse.ptp.launch.core.ILaunchManager;
import org.eclipse.ptp.launch.core.IParallelLaunchListener;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.views.ParallelProcessesView;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.progress.IProgressService;

/**
 *  
 */
public class LaunchManager implements ILaunchManager {
    protected CommandFactory factory = new CommandFactory();

    protected List listeners = new ArrayList(2);
    protected int currentState = STATE_EXIT;
    //protected IPMachine machine = null;
    protected IPJob processRoot = null;
    protected MISession session = null;
    protected OutputConsole outputConsole = null;
    protected boolean isPerspectiveOpen = false;
    protected ILaunchConfiguration config = null;

    public boolean isParallelPerspectiveOpen() {
        return isPerspectiveOpen;
    }

    public void setPDTConfiguration(ILaunchConfiguration config) {
        this.config = config;
    }

    public ILaunchConfiguration getPDTConfiguration() {
        return config;
    }

    public LaunchManager() {
        ParallelPlugin.getDefault().addPerspectiveListener(perspectiveListener);
        //testing();
    }
    
    public void testing() {
    	    processRoot = new PJob();
        
        int pn = 0;
        int nn = 20;
        int ppn = 3;
        for (int h = 0; h < nn; h++) {
            IPNode pNode = new PNode(processRoot, "" + h);
            for (int i = 0; i < ppn; i++) {
                IPProcess p = new PProcess(pNode, "" + (pn++), "123", "starting", null, null);
                for (int j = 0; j < 100; j++) {
                    p.addOutput("random output from process: " + j);
                }
                pNode.addChild(p);
            }
            processRoot.addChild(pNode);
        }

        final int tpn = nn * ppn;
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                }

                for (int i = 0; i < tpn; i++) {
                    IPProcess p = processRoot.findProcess("" + i);
                    p.setStatus(IPProcess.EXITED_SIGNALLED);
                    ParallelProcessesView.getInstance().refresh(p);
                    //System.out.println("Called updated");
                }
            }
        };
        new Thread(runnable).start();

        System.out.println("Created all dumy nodes and processes");
    }

    private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
        public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            if (perspective.getId().equals(UIUtils.PPerspectiveFactory_ID)) {
                isPerspectiveOpen = false;
                //System.out.println("Close: " + perspective.getId());
    			IWorkbench workbench = ParallelPlugin.getDefault().getWorkbench();    			
    			IProgressService progressService = workbench.getProgressService();
    		    			
    			final IRunnableWithProgress runnable = new IRunnableWithProgress() {
    				public void run(IProgressMonitor monitor) throws InvocationTargetException {
    					if (!monitor.isCanceled()) {
    						try {
    		                    mpiexit();
    						} catch (CoreException e) {
    							throw new InvocationTargetException(e);
    						}
    					}
    				}		
    			};			
    			try {
    				progressService.busyCursorWhile(runnable);
    			} catch (InterruptedException e) {
                    System.out.println("Closing Parallel Perspective: " + e.getMessage());
    			} catch (InvocationTargetException e2) {
                    System.out.println("Closing Parallel Perspective: " + e2.getMessage());
    			}
            }
        }

        public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {            
            if (perspective.getId().equals(UIUtils.PPerspectiveFactory_ID)) {
                isPerspectiveOpen = true;

                //System.out.println("Open: " + perspective.getId());
                /*
                 * if (page instanceof WorkbenchPage) { Perspective perspect =
                 * ((WorkbenchPage)page).findPerspective(perspective);
                 * IActionSetDescriptor[] actionSetDesciptors =
                 * perspect.getActionSets();
                 * 
                 * List visibleActionSets = new ArrayList(); for (int i=0; i
                 * <actionSetDesciptors.length; i++) { if
                 * (!actionSetDesciptors[i].getId().equals(NewSearchUI.ACTION_SET_ID))
                 * visibleActionSets.add(actionSetDesciptors[i]); }
                 * IActionSetDescriptor[] newActionSets = new
                 * IActionSetDescriptor[visibleActionSets.size()];
                 * visibleActionSets.toArray(newActionSets);
                 * perspect.setActionSets(newActionSets); }
                 */

                /*
                try {
                    createMPISession();
                } catch (CoreException e) {
                    System.out.println("Cannot creation MPI session: " + e.getMessage());
                }
                */
            }
        }

        public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            //System.out.println("Active: " + perspective.getId());
            if (perspective.getId().equals(UIUtils.PPerspectiveFactory_ID)) {
                isPerspectiveOpen = true;
                //System.out.println("Active: " + perspective.getId());
                try {
                    createMPISession();
                } catch (CoreException e) {
                    System.out.println("Cannot creation MPI session: " + e.getMessage());
                }
            }
        }

        public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
            //if (perspective.getId().equals(UIUtils.PPerspectiveFactory_ID))
            //System.out.println("Changed: " + perspective.getId());
        }
    };

    private Observer mpiObserver = new Observer() {
        public synchronized void update(Observable o, Object arg) {
            if (arg instanceof MIExecStatusChangeEvent) {
                MIExecStatusChangeEvent ev = (MIExecStatusChangeEvent) arg;
                MIProcessDescription[] pdes = ev.getMIProcessDescription();
                for (int i = 0; i < pdes.length; i++) {
                    IPProcess process = getProcessRoot().findProcess(pdes[i].getRank());
                    if (process != null) {
                        process.setExitCode(pdes[i].getExitCode());
                        process.setSignalName(pdes[i].getSignalName());
                        process.setStatus(pdes[i].getStatus());
                        fireEvent(process, EVENT_EXEC_STATUS_CHANGE);
                        if (process.getParent().isAllStop()) {
                            fireEvent(process.getParent(), ALL_PROCESSES_STOPPED);
                            if (processRoot.isAllStop()) {
                                fireState(STATE_STOPPED);
                                clearUsedMemory();
                            }
                        }
                        break;
                    }
                    //System.out.println("================== exit code: " +
                    // pd.getExitCode() + ", node: " + pd.getNode() + ", pid: "
                    // + pd.getPid() + ", status: " + pd.getStatus() + ", rank:
                    // " + pd.getRank());
                }
            } else if (arg instanceof MIProcessOutputEvent) {
                MIProcessOutputEvent ev = (MIProcessOutputEvent) arg;
                IPProcess process = getProcessRoot().findProcess(String.valueOf(ev.getProcNumber()));
                if (process != null) {
                    if (process.getStatus().equals(IPProcess.STARTING)) {
                        process.setStatus(IPProcess.RUNNING);
                        fireEvent(process, EVENT_EXEC_STATUS_CHANGE);
                    }
                    process.addOutput(ev.getOutput());
                    //fireEvent(process, EVENT_PROCESS_OUTPUT);
                }
                //System.out.println("+++++++++++++++++++++ node: " +
                // ev.getProcNumber() + ", output: " + ev.getOutput());
            } else if (arg instanceof MISysStatusChangeEvent) {
                try {
                    mpisysstatus();
                } catch (CoreException e) {
                    System.out.println("+++++++ Observer - mpisysstatus err: " + e.getMessage());
                }
                fireEvent(null, EVENT_SYS_STATUS_CHANGE);
            } else if (arg instanceof MIErrorEvent) {
                String err = ((MIErrorEvent) arg).getMessage();
                System.out.println("MIErrorEvent: " + err);
                /*
                 * try { mpiabort(); } catch (CoreException e) { } finally {
                 * setCurrentState(STATE_ERROR);
                 * //UIUtils.showErrorDialog("MIErrorEvent", err, null); }
                 */
                fireEvent(err, EVENT_ERROR);
            }
        }
    };

    public void shutdown() {
        ParallelPlugin.getDefault().removePerspectiveListener(perspectiveListener);
        clearAll();
        mpiObserver = null;
        perspectiveListener = null;
        listeners.clear();
        listeners = null;
    }

    public void clearAll() {
        if (session == null)
            return;
        
        removeConsole();
        session.deleteObservers();
        session = null;
        processRoot.removeAllProcesses();
        processRoot = null;
    }

    /*
     * private void terminateDebugProcess() { if (debugProcess != null &&
     * !debugProcess.isTerminated()) { try { debugProcess.terminate(); } catch
     * (DebugException e) { System.out.println("LaunchManager -
     * terminateDebugProcess: " + e.getMessage()); } finally { debugProcess =
     * null; } } }
     */

    public MISession getSession() {
        return session;
    }

    public IPJob getProcessRoot() {
        return processRoot;
    }

    public void addParallelLaunchListener(IParallelLaunchListener listener) {
        listeners.add(listener);
    }

    public void removeParallelLaunchListener(IParallelLaunchListener listener) {
        listeners.remove(listener);
    }

    protected synchronized void fireState(int state) {
        setCurrentState(state);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            IParallelLaunchListener listener = (IParallelLaunchListener) i.next();
            switch (state) {
            case STATE_START:
                listener.start();
                System.out.println("++++++++++++ Started mpictrl ++++++++++++++");
                break;
            case STATE_RUN:
                listener.run();
                break;
            case STATE_EXIT:
                System.out.println("++++++++++++ Exit ++++++++++++++");
                listener.exit();
                break;
            case STATE_ABORT:
                listener.abort();
                break;
            case STATE_STOPPED:
                System.out.println("++++++++++++ Stopped ++++++++++++++");
                listener.stopped();
            }
        }
    }

    protected synchronized void fireEvent(Object object, int event) {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            IParallelLaunchListener listener = (IParallelLaunchListener) i.next();
            switch (event) {
            case EVENT_EXEC_STATUS_CHANGE:
                listener.execStatusChangeEvent(object);
                break;
            case EVENT_SYS_STATUS_CHANGE:
                listener.sysStatusChangeEvent(object);
                break;
            case EVENT_PROCESS_OUTPUT:
                listener.processOutputEvent(object);
                break;
            case EVENT_ERROR:
                listener.errorEvent(object);
                break;
            case EVENT_UPDATED_STATUS:
                listener.updatedStatusEvent();
                break;
            case ALL_PROCESSES_STOPPED:    
                listener.execStatusChangeEvent(object);
                break;
            }
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    protected String renderLabel(String name) {
        String format = UIMessage.getResourceString("LaunchManager.{0}_({1})");
        String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
        return MessageFormat.format(format, new String[] { name, timestamp });
    }

    protected void isSessionExist() throws CoreException {
        if (session == null) {
            createMPISession();
            //Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, "No MI session is created", null);
            //throw new CoreException(status);
        }
    }

    public boolean isMPIRuning() {
        return (session != null || (processRoot != null && processRoot.hasChildren()));
    }

    public boolean hasProcessRunning() {
        if (isMPIRuning() && !processRoot.isAllStop())
            return true;

        return false;
    }

    public synchronized void mpirun(String[] args) throws CoreException {
        isSessionExist();

        /*
         * if (getCurrentState() != STATE_ERROR && hasProcessRunning()) { Status
         * status = new Status(IStatus.ERROR,
         * ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, "Some processes
         * are stilling running", null); throw new CoreException(status); }
         */

        try {
            MIExecRun execRun = factory.createMIExecRun(args);
            session.postCommand(execRun);
            fireState(STATE_RUN);
        } catch (MIException e) {
            Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, e.getMessage(), e);
            throw new CoreException(status);
        }
    }

    public synchronized void mpisysstatus() throws CoreException {
        isSessionExist();

        try {
            MISysStatus sysStatus = new MISysStatus();
            session.postCommand(sysStatus);
            updateProcessInfo(sysStatus.getMISysStatusInfo().getMISystemDescription());
            fireEvent(null, EVENT_SYS_STATUS_CHANGE);
        } catch (MIException e) {
            Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, "Cannot exec SYS STATUS command", e);
            throw new CoreException(status);
        }
    }

    public synchronized void mpistatus() throws CoreException {
        isSessionExist();

        try {
            MIExecStatus execStatus = factory.createMIExecStatus();
            session.postCommand(execStatus);
            updateProcessInfo(execStatus.getMIExecStatusInfo().getMIProcessDescription());
            fireEvent(null, EVENT_UPDATED_STATUS);
        } catch (MIException e) {
            Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, "Cannot exec STATUS command", e);
            throw new CoreException(status);
        }
    }

    public synchronized void mpiabort() throws CoreException {
        isSessionExist();

        try {
            MIExecAbort execAbort = factory.createMIExecAbort();
            session.postCommand(execAbort);
            fireState(STATE_ABORT);
        } catch (MIException e) {
            Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, "Cannot exec ABORT command", e);
            throw new CoreException(status);
        }
    }

    public synchronized void mpiexit() throws CoreException {
        isSessionExist();

        try {
            //MIExit gdbExit = factory.createMIExit();
            //session.postCommand(gdbExit);
            session.terminate();
            if (getCurrentState() != STATE_EXIT)
                fireState(STATE_EXIT);            
        } catch (Exception e) {
            Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, "Cannot exec EXIT command", e);
            throw new CoreException(status);
        } finally {
            clearAll();            
        }
    }

    public synchronized void createMPISession() throws CoreException {
        if (session == null) {
            MIPlugin miPlugin = MIPlugin.getDefault(ParallelPlugin.getDefault().getPluginPreferences());
            // Turn of the debugging output
            //miPlugin.setDebugging(true);
            try {
                session = miPlugin.createSession();
                session.addObserver(mpiObserver);
                processRoot = new PMachine(session);

                fireState(STATE_START);
                createConsole();

                waitFor();
            } catch (MIException e) {
                Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, UIMessage.getResourceString("LaunchManager.Exception_occurred_executing_command_line"), e);
                throw new CoreException(status);
            } catch (IOException e) {
                Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, UIMessage.getResourceString("LaunchManager.Exception_occurred_executing_command_line"), e);
                throw new CoreException(status);
            }
        }
    }

    public void execMI(final ILaunch launch, File workingDirectory, String[] envp, final String[] args, IProgressMonitor monitor) throws CoreException {
        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 5);
        subMonitor.beginTask("Executing job", 10);
        
        subMonitor.subTask("Creating MPI session");
        createMPISession();
        subMonitor.worked(2);
        
        subMonitor.subTask("Executing run command");
        mpirun(args);
        subMonitor.worked(2);

        subMonitor.subTask("Remove all processes");
        processRoot.removeAllProcesses();
        clearUsedMemory();
        subMonitor.worked(2);
        
        DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
        //DebugPlugin.newProcess(launch, session.getSessionProcess(), renderLabel("mpictrl"));
        subMonitor.subTask("Executing sys status command");
        mpisysstatus();
        subMonitor.worked(2);
	
        subMonitor.subTask("Executing status command");
        mpistatus();
    }

    private void removeConsole() {
        if (outputConsole != null) {
            outputConsole.kill();
            outputConsole = null;
        }
    }

    private void createConsole() {
        if (outputConsole == null) {
            outputConsole = new OutputConsole(renderLabel("mpictrl"), session.getMIConsoleStream());
        }
    }

    private void updateProcessInfo(Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects instanceof MIProcessDescription[]) {
                MIProcessDescription[] pDesc = (MIProcessDescription[]) objects;
                IPNode pNode = processRoot.findNode(pDesc[i].getNode());
                if (pNode == null) {
                    pNode = new PNode(processRoot, pDesc[i].getNode());
                    pNode.addChild(new PProcess(pNode, pDesc[i].getRank(), pDesc[i].getPid(), pDesc[i].getStatus(), pDesc[i].getExitCode(), pDesc[i].getSignalName()));
                    processRoot.addChild(pNode);
                } else {
                    pNode.addChild(new PProcess(pNode, pDesc[i].getRank(), pDesc[i].getPid(), pDesc[i].getStatus(), pDesc[i].getExitCode(), pDesc[i].getSignalName()));
                }
            } else if (objects instanceof MISystemDescription[]) {
                MISystemDescription[] sDesc = (MISystemDescription[]) objects;
                IPNode pNode = processRoot.findNode(sDesc[i].getNode());
                if (pNode == null) {
                    pNode = new PNode(processRoot, sDesc[i].getNode(), sDesc[i].getBprocUser(), sDesc[i].getBprocGroup(), sDesc[i].getBprocState(), sDesc[i].getBprocMode());
                    processRoot.addChild(pNode);
                } else {
                    pNode.setGroup(sDesc[i].getBprocGroup());
                    pNode.setUser(sDesc[i].getBprocUser());
                    pNode.setState(sDesc[i].getBprocState());
                    pNode.setMode(sDesc[i].getBprocMode());
                }
            }
        }
    }

    private void waitFor() {
        Thread waitForThread = new Thread("Wait for finish") {
            public void run() {
                try {
                    if (!session.isTerminated())
                        session.getGDBProcess().waitFor();
                } catch (InterruptedException ie) {
                    // clear interrupted state
                    Thread.interrupted();
                } finally {
                    System.out.println("Launch Manager Exit");
                    clearAll();
                    //if (getCurrentState() != STATE_EXIT)
                      //  fireState(STATE_EXIT);
                }
            }
        };
        waitForThread.start();
    }

    protected void clearUsedMemory() {
        System.out.println("********** clearUsedMemory");
        Runtime rt = Runtime.getRuntime();
        long isFree = rt.freeMemory();
        long wasFree;
        do {
            wasFree = isFree;
            rt.gc();
            isFree = rt.freeMemory();
        } while (isFree > wasFree);
        rt.runFinalization();
    }
}