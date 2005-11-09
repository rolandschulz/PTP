package org.eclipse.ptp.rtsystem.ompi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.internal.core.CoreUtils;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNodesEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessAttributeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeProcessesEvent;

public class OMPIProxyRuntimeClient extends ProxyRuntimeClient implements IRuntimeProxy, IProxyRuntimeEventListener {
	protected Queue events = new Queue();
	protected BitSet waitEvents = new BitSet();
	
	public OMPIProxyRuntimeClient() {
		super();
		super.addRuntimeEventListener(this);
	}
	
	public int runJob(String prog, String[] args, int numProcs, boolean debug) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NEWJOB);
		run(prog, args, numProcs, debug);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNewJobEvent)event).getJobID();
	}
	
	public int getJobProcesses(int jobID) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_PROCS);
		getProcesses(jobID);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeProcessesEvent)event).getNumProcs();
	}
	
	public String[] getProcessAttributeBlocking(int jobID, int procID, String key) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_PROCATTR);
		getProcessAttribute(jobID, procID, key);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeProcessAttributeEvent)event).getValues();
	}
	
	public int getNumNodesBlocking(int machineID) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NODES);
		getNodes(machineID);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNodesEvent)event).getNumNodes();
	}
	
	public void startup() {
		System.out.println("OMPIProxyRuntimeClient - firing up proxy, waiting for connecting.  Please wait!  This can take a minute . . .");
		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_CONNECTED);
			sessionCreate();
			
			Thread runThread = new Thread("Proxy Server Thread") {
				public void run() {
					String cmd;
					
					Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
					
					//int port = preferences.getInt(PreferenceConstants.ORTE_SERVER_PORT);
					String proxyPath = preferences.getString(PreferenceConstants.ORTE_SERVER_PATH);
				
					Runtime rt = Runtime.getRuntime ();
					
					cmd = proxyPath + " --port="+getSessionPort();
					
					System.out.println("RUNNING PROXY SERVER COMMAND: '"+cmd+"'");
					
					try {
						Process process = rt.exec(cmd);
						InputStreamReader reader = new InputStreamReader (process.getInputStream ());
						BufferedReader buf_reader = new BufferedReader (reader);
						
						String line;
						while ((line = buf_reader.readLine ()) != null)
							System.out.println ("ORTE PROXY SERVER: "+line);
					} catch(Exception e) {
						String err;
						err = "Error running proxy server with command: '"+cmd+"'.";
						e.printStackTrace();
						System.out.println(err);
						CoreUtils.showErrorDialog("Running Proxy Server", err, null);
					}
				}
			};
			runThread.start();
			
			System.out.println("Waiting on accept.");
			waitForRuntimeEvent();
		} catch (IOException e) {
			System.err.println("Exception starting up proxy. :(");
			System.exit(1);
		}
		
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		String orted_path = preferences.getString(PreferenceConstants.ORTE_ORTED_PATH);
		System.out.println("ORTED path = ."+orted_path+".");
		if(orted_path == "") {
			String err = "Some error occurred trying to spawn the ORTEd (ORTE daemon).  Check the "+
				"PTP/Open MPI preferences page and be certain that the path and arguments "+
				"are correct.";
			System.err.println(err);
			CoreUtils.showErrorDialog("ORTEd Start Failure", err, null);
			return;
		}
		
		String ompi_bin_path = orted_path.substring(0, orted_path.lastIndexOf("/"));
		
		String orted_args = preferences.getString(PreferenceConstants.ORTE_ORTED_ARGS);
		String orted_full = orted_path + " " + orted_args;
		System.out.println("ORTED = "+orted_full);
		/* start the orted */
		String[] split_args = orted_args.split("\\s");
		for (int x=0; x<split_args.length; x++)
	         System.out.println("["+x+"] = "+split_args[x]);
		String[] split_path = orted_path.split("\\/");
		for(int x=0; x<split_path.length; x++)
			System.out.println("["+x+"] = "+split_path[x]);

		String str_arg, arg_array_as_string;
		
		arg_array_as_string = new String("");
		
		for(int i=0; i<split_args.length; i++) {
			arg_array_as_string = arg_array_as_string + " " + split_args[i];
		}
	
		str_arg = ompi_bin_path + " " + orted_path + "  " + split_path[split_path.length - 1] + arg_array_as_string;
		
		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sendCommand("STARTDAEMON", str_arg);
			waitForRuntimeEvent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void shutdown() {
		try {
			System.out.println("OMPIProxyRuntimeClient shutting down server...");
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sessionFinish();
			waitForRuntimeEvent();
			System.out.println("OMPIProxyRuntimeClient shut down.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	private void setWaitEvent(int eventID) {
		waitEvents.set(eventID);
		waitEvents.set(IProxyRuntimeEvent.EVENT_RUNTIME_ERROR); // always check for errors
	}
	
	private synchronized IProxyRuntimeEvent waitForRuntimeEvent() throws IOException {
    		IProxyRuntimeEvent event = null;
    		
    		try {
        		System.out.println("OMPIProxyRuntimeClient waiting on " + waitEvents.toString());
        		while (this.events.isEmpty())
        			wait();
        		System.out.println("OMPIProxyRuntimeClient awoke!");
        		event = (IProxyRuntimeEvent) this.events.removeItem();
        		if (event instanceof ProxyRuntimeErrorEvent) {
        	   		waitEvents.clear();
        			throw new IOException(((ProxyRuntimeErrorEvent)event).getErrorMessage());
        		}
    		} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
    		}
    		
    		waitEvents.clear();
 
    		return event;
	}

	/*
	 * Only handle events we're interested in
	 */
    public synchronized void handleEvent(IProxyRuntimeEvent e) {
		System.out.println("OMPIProxyRuntimeClient got event: " + e.toString());
		
		if (waitEvents.get(e.getEventID())) {
			System.out.println("OMPIProxyRuntimeClient notifying...");
			this.events.addItem(e);
			notifyAll();
		}
    }

}
