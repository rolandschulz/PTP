package org.eclipse.ptp.core;

public interface IPProcess extends IPElement {
    public static final String STARTING = "starting";
    public static final String RUNNING = "running";
    public static final String EXITED = "exited";
    public static final String EXITED_SIGNALLED = "exited-signalled";
    public static final String STOPPED = "stopped";
    public static final String ERROR = "error";
    
    public boolean isTerminated();
    public void setTerminated(boolean isTerminate);
    
    public String getProcessNumber();
    public String getPid();
    public String getStatus();
    public String getExitCode();
    public String getSignalName();
    
    public void setPid(String pid);
    public void setStatus(String status);
    public void setExitCode(String code);
    public void setSignalName(String signalName);
    
    public void removeProcess();
    
    public String getContents();
    public String[] getOutputs();
    public void clearOutput();
    public void addOutput(String output);
    
    public void addProcessListener(IProcessListener listener);
    public void removerProcessListener(); 
    
    /* returns the parent job that this process is encompassed by */
    public IPJob getJob();
    
    /* sets the node that this process is running on */
    public void setNode(IPNode node);
    
    /* returns the node that this process is running on */
    public IPNode getNode();
}
