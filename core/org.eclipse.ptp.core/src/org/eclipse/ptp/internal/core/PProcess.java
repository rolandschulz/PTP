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
package org.eclipse.ptp.internal.core;

import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IProcessListener;

public class PProcess extends Parent implements IPProcess {
    protected String NAME_TAG = "process ";
    
    private String pid = null;    
    private String status = null;
    private String exitCode = null;
    private String signalName = null;
    private boolean isTerminated = false;
    //private List outputList = new ArrayList();
    private OutputTextFile outputFile = null;   
    
    private IProcessListener listener = null;
    
    /* the node that this process is running on, or was scheduled on / will be, etc */
    protected IPNode node;
    
	public PProcess(IPElement element, String name, String key, String pid, String status, String exitCode, String signalName) {
		super(element, name, key, P_PROCESS);
		this.pid = pid;
		this.exitCode = exitCode;
		setStatus(status);
		IPJob job = getJob();
		//outputFile = new OutputTextFile(processNumber, job.getOutputStoreDirectory(), job.getStoreLine());
	}
	
	public IPJob getJob() {
		IPElement current = this;
		do {
			if(current instanceof IPJob) return (IPJob) current;
		} while((current = current.getParent()) != null);
		return null;
	}
	
	public String getProcessNumber() {
	    return getKey();
	}
	
	public void setStatus(String status) {
	    this.status = status==null?"unknown":status;
        if (listener != null && status != null)
            listener.changeStatus(status);
	}
	
	public void setExitCode(String exitCode) {
	    this.exitCode = exitCode;
        if (listener != null && exitCode != null)
            listener.changeExitCode(exitCode);
	}

	public void setSignalName(String signalName) {
	    this.signalName = signalName;
        if (listener != null && signalName != null)
            listener.changeSignalName(signalName);
	}

	public void setPid(String pid) {
	    this.pid = pid;
	}
	
	public String getPid() {
	    return pid;
	}
	public String getExitCode() {
	    return exitCode;
	}
	public String getSignalName() {
	    return signalName;
	}
	public String getStatus() {
	    return status;
	}
	
    public boolean isTerminated() {
        return isTerminated;
    }
    
    public void removeProcess() {
        ((IPNode)getParent()).removeChild(this);
    }
    
    public void setTerminated(boolean isTerminated) {
        this.isTerminated = isTerminated;
    }

    public void addOutput(String output) {
        //outputList.add(output);
        //outputList.add("random output from process: " + (counter++));
        outputFile.write(output + "\n");
        if (listener != null)
            listener.addOutput(output + "\n");
    }
    
    public String getContents() {
        //String[] array = new String[outputList.size()];
		//return (String[]) outputList.toArray( array );
        return outputFile.getContents();
    }
    
    public String[] getOutputs() {
        //String[] array = new String[outputList.size()];
		//return (String[]) outputList.toArray( array );
        return null;
    }

    public void clearOutput() {
        outputFile.delete();
        //outputList.clear();
    }
    
    public void addProcessListener(IProcessListener listener) {
        this.listener = listener;
    }
    
    public void removerProcessListener() {
        listener = null;
    }
    
    public boolean isAllStop() {
        return getStatus().startsWith(EXITED);
    }
    
	public void setNode(IPNode node) {
		this.node = node;
		if(node != null) node.addChild(this);
	}

	public IPNode getNode() {
		return this.node;
	}    
}