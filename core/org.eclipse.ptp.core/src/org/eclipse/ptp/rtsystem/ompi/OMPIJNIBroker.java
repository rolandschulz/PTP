package org.eclipse.ptp.rtsystem.ompi;

import org.eclipse.ptp.internal.core.CoreUtils;

public class OMPIJNIBroker {
	
	/* control system functions */
	public native String OMPIGetError();
	public native int OMPIInit();
	public native int OMPIStartDaemon(String ompi_bin_path, String orted_path, String orted_bin, String[] args);
	public native void OMPIShutdown();
	public native void OMPIFinalize();
	public native void OMPIProgress();
	public native int OMPIRun(String[] args);
	public native void OMPITerminateJob(int jobID);
	/* these next few are control system functions because they pertain processes and jobs but they have to
	 * do with monitoring these things - don't get confused :)
	 */
	
	/**
	 * Returns an array of Job names representing all the known Jobs in this Universe.
	 */
	public native String[] OMPIGetJobs();
	
	/**
	 * Given a Job name this method returns an array of the Processes contained within that Job. 
	 * @param jobName The Job name.
	 * @return An array of Process names contained within the Job.
	 */
	
	public native String[] OMPIGetProcesses(String jobName);
	/**
	 * Given a Process name and an attribute key name, this method searches for the associated
	 * value. 
	 * @param procName The name of the Processes to look for its associated attribute.
	 * @param attrib The attribute to look for on the associated Process.
	 * @return The value of the attribute or null if that attribute does not exist.
	 */
	public native String OMPIGetProcessesAttribute(String procName, String attrib);
	
	/* monitoring system functions */
	/**
	 * Returns all the Machines that are visible by this Universe, by name.
	 */
	public native String OMPIGetMachines();
	
	/**
	 * Given a Machine name, find all the Nodes that comprise this Machine.
	 * @param machineName The name of the Machine to look for Nodes contained within.
	 * @return The names (array) of the Nodes that are part of this Machine.
	 */
	public native String[] OMPIGetNodes(String machineName);

	/**
	 * Given the name of a Node, find out what Machine this Node is part of.
	 * @param nodeName The name of the Node to find.
	 * @return The name of the Machine that this Node is part of.
	 */
	public native String OMPIGetNodeMachineName(String nodeName);
	
	/**
	 * Given a Node (by name), look for the value of the attribute.  The attribute amounts to a
	 * property of the node such as state, owner, operating system, etc.
	 * @param nodeName The name of the Node to look for its associated attribute.
	 * @param attrib The attribute to look for on the associated Node.
	 * @return The value of the attribute or null if that attribute does not exist.
	 */
	public native String OMPIGetNodeAttribute(String nodeName, String attrib);
	
	
	private static boolean successful_load = false;
	
	static {
        try { 
        		System.loadLibrary("ptp_ompi_jni");
        		successful_load = true;
        } catch(UnsatisfiedLinkError e) {
        		String str = "Unable to load library 'ptp_ompi_jni'.  Make sure "+
        				"the library exists and the VM arguments point to the directory where "+
        				"it resides.  In the 'Run...' set the VM Args to something like "+
        				"-Djava.library.path=[home directory]/[eclipse workspace]/org.eclipse.ptp.core/ompi";
        		System.err.println(str);
        		CoreUtils.showErrorDialog("Dynamic Library Load Failed", str, null);
        		successful_load = false;
        }
	}
	
	public boolean libraryLoaded() { return successful_load; }
}