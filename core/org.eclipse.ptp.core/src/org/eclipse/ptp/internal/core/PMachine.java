package org.eclipse.ptp.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.pdt.mi.MISession;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IOutputTextFileContants;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPUniverse;

/**
 * @author Clement
 *
 */
public class PMachine extends Parent implements IPMachine {
	public IPUniverse getPUniverse() {
		IPElement current = this;
		do {
			if (current instanceof IPUniverse) return (IPUniverse) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	/*
    protected String NAME_TAG = "root ";
    
    private MISession miSession = null;
    protected String outputDirPath = null;
    protected int storeLine = 0;
    protected String arch = null;
    
    public PMachine() {
        super(null, "", P_ROOT);
    }

    public PMachine(MISession miSession) {
        this(miSession, "");
    }
    
	public PMachine(MISession miSession, String name) {
		super(null, name, P_ROOT);
		this.miSession = miSession;
		setOutputStore();
	}
	
	private void setOutputStore() {
		Preferences preferences = ParallelPlugin.getDefault().getPluginPreferences();
		outputDirPath = preferences.getString(IOutputTextFileContants.OUTPUT_DIR);
		storeLine = preferences.getInt(IOutputTextFileContants.STORE_LINE);		
        if (outputDirPath == null || outputDirPath.length() == 0)
            outputDirPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(IOutputTextFileContants.DEF_OUTPUT_DIR_NAME).toOSString();
            
        if (storeLine == 0)
            storeLine = IOutputTextFileContants.DEF_STORE_LINE;
        
        File outputDirectory = new File(outputDirPath);
        if (!outputDirectory.exists())
            outputDirectory.mkdir();
	}
	
	public String getOutputStoreDirectory() {
	    return outputDirPath;
	}
	public int getStoreLine() {
	    return storeLine;
	}
	
	public MISession getMISession() {
	    return miSession;
	}
	
	public Process getMPIProcess() {
	    return miSession.getGDBProcess();
	}
	
	public synchronized IPNode[] getNodes() {
	    return (IPNode[])getCollection().toArray(new IPNode[size()]);
	}
	
	public synchronized IPNode[] getSortedNodes() {
	    IPNode[] nodes = getNodes();
	    sort(nodes);
	    return nodes;
	}

	public synchronized IPProcess[] getProcesses() {
	    List array = new ArrayList(0);
	    IPNode[] nodes = getNodes();
        for (int i=0; i<nodes.length; i++)
            array.addAll(nodes[i].getCollection());

        return (IPProcess[])array.toArray(new IPProcess[array.size()]);
	}
	
	public synchronized IPProcess[] getSortedProcesses() {
	    IPProcess[] processes = getProcesses();
	    sort(processes);
	    return processes;
	}
		
	public synchronized IPNode findNode(String nodeNumber) {
        IPElement element = findChild(nodeNumber);
        if (element != null)
            return (IPNode)element;
        return null;
	}
	
	public synchronized IPProcess findProcess(String processNumber) {
        IPNode[] nodes = getNodes();
        for (int i=0; i<nodes.length; i++) {
	        IPProcess process = nodes[i].findProcess(processNumber);
	        if (process != null)
	            return process;
        }
	    return null;
	}
	
	public synchronized IPProcess findProcess(String nodeNumber, String processNumber) {
	    IPNode node = findNode(nodeNumber);
	    if (node != null)
	        return node.findProcess(processNumber);

	    return findProcess(processNumber);
	}
	
	public int totalNodes() {
	    return size();
	}
	public int totalProcesses() {
	    int counter = 0;
        IPNode[] nodes = getNodes();
        for (int i=0; i<nodes.length; i++)
            counter += nodes[i].size();

        return counter;
	}
	
	public void removeAllProcesses() {
	    IPProcess[] processes = getProcesses();
        for (int i=0; i<processes.length; i++)
            processes[i].clearOutput();
        
        removeChildren();
	}
	*/	
	
	/* returns a String representing the architecture in some form */
	
	/*
	public String getArch() {
		return this.arch;
	}
	/* sets the architecture, should be used by instantiating classes and such */
	
	/*
	public void setArch(String arch) {
		this.arch = arch;
	}
	*/
}
