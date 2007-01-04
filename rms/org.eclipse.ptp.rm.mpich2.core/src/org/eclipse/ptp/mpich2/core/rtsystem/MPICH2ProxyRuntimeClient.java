package org.eclipse.ptp.mpich2.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.rtsystem.proxy.ProxyRuntimeClient;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent;
import org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEventListener;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeErrorEvent;
import org.eclipse.ptp.rtsystem.proxy.event.ProxyRuntimeNewJobEvent;

public class MPICH2ProxyRuntimeClient extends ProxyRuntimeClient implements IRuntimeProxy, IProxyRuntimeEventListener {
	protected Queue events = new Queue();
	protected BitSet waitEvents = new BitSet();
	private final String proxyPath;
	private final boolean launchManually;
	
	public MPICH2ProxyRuntimeClient(String proxyPath, boolean launchManually) {
		super();
		this.proxyPath = proxyPath;
		this.launchManually = launchManually;
		super.addRuntimeEventListener(this);
		setProxyName("MPICH2");
	}
	
	public int runJob(String[] args) throws IOException {
		setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_NEWJOB);
		run(args);
		IProxyRuntimeEvent event = waitForRuntimeEvent();
		return ((ProxyRuntimeNewJobEvent)event).getJobID();
	}
	
	public boolean startup(final IProgressMonitor monitor) {
		if (super.startup(monitor) == false) {
			return false;
		}

		try {
			setWaitEvent(IProxyRuntimeEvent.EVENT_RUNTIME_OK);
			sendCommand("STARTDAEMON");
			waitForRuntimeEvent();
		} catch (IOException e) {
			System.err.println("Exception starting up proxy daemon. :(");
			try {
				sessionFinish();
			} catch (IOException e1) {
				PTPCorePlugin.log(e1);
			}
			return false;
		}
		return true;
	}

}
