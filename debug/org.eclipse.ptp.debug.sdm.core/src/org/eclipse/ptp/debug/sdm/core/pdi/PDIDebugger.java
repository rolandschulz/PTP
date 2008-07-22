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
package org.eclipse.ptp.debug.sdm.core.pdi;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.ExtFormat;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDILocation;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIErrorInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.sdm.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * @author clement
 * 
 */
public class PDIDebugger extends ProxyDebugClient implements IPDIDebugger {
	private class ProxyNotifier extends Observable {
		/**
		 * @param event
		 */
		public void notify(IProxyDebugEvent event) {
			setChanged();
			notifyObservers(event);
		}
	}
	
	private int bpid = 0;
	private ProxyNotifier proxyNotifier = new ProxyNotifier();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#commandRequest(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void commandRequest(BitList tasks, String command) throws PDIException {
		try {
			debugCLIHandle(tasks, command);
		} catch (IOException e) {
			throw new PDIException(null, "Error on sending generic command: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIMemoryBlockManagement#createDataReadMemory(org.eclipse.ptp.core.util.BitList, long, java.lang.String, int, int, int, int, java.lang.Character)
	 */
	public void createDataReadMemory(BitList tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols,
			Character asChar) throws PDIException {
		try {
			setDataReadMemoryCommand(tasks, offset, address, getFormat(wordFormat), wordSize, rows, cols, asChar);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting data read memory: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIMemoryBlockManagement#createDataWriteMemory(org.eclipse.ptp.core.util.BitList, long, java.lang.String, int, int, java.lang.String)
	 */
	public void createDataWriteMemory(BitList tasks, long offset, String address, int wordFormat, int wordSize, String value)
			throws PDIException {
		try {
			setDataWriteMemoryCommand(tasks, offset, address, getFormat(wordFormat), wordSize, value);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting data write memory: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#dataEvaluateExpression(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void dataEvaluateExpression(BitList tasks, String expr) throws PDIException {
		try {
			debugDataEvaluateExpression(tasks, expr);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on evaluating data expression: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#deleteBreakpoint(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void deleteBreakpoint(BitList tasks, int bpid) throws PDIException {
		try {
			debugDeleteBreakpoint(tasks, bpid);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on deleting breakpoint: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#deleteVariable(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void deleteVariable(BitList tasks, String var) throws PDIException {
		try {
			debugVariableDelete(tasks, var);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on deleting variable: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#disconnect(java.util.Observer)
	 */
	public void disconnect(Observer observer) throws PDIException {
		stopDebugger();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#evaluateExpression(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void evaluateExpression(BitList tasks, String expr) throws PDIException {
		try {
			debugEvaluateExpression(tasks, expr);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on evaluating expression: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#initialize(java.util.List)
	 */
	public void initialize(ILaunchConfiguration configuration, List<String> args, IProgressMonitor monitor) throws PDIException {
		int port = 0;
		
		/*
		 * If there is an existing port specified, use it if possible.
		 */
		for (String arg : args) {
			if (arg.startsWith("--port=")) {
				try {
					port = Integer.parseInt(arg.substring(7, arg.length()));
				} catch (NumberFormatException e) {
				}
				break;
			}
		}
		
		try {
			doInitialize(port);
		} catch (IOException e) {
			throw new PDIException(null, "Error on getting proxy port number: " + e.getMessage()); //$NON-NLS-1$
		}
		
		IResourceManagerControl rm = getResourceManager(configuration);
		if (rm != null) {
			port = getSessionPort();
			IResourceManagerConfiguration conf = rm.getConfiguration();
			if (conf instanceof AbstractRemoteResourceManagerConfiguration) {
				AbstractRemoteResourceManagerConfiguration remConf = (AbstractRemoteResourceManagerConfiguration)conf;
				if (remConf.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
					IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remConf.getRemoteServicesId());
					if (remoteServices != null) {
						IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
						if (connMgr != null) {
							IRemoteConnection conn = connMgr.getConnection(remConf.getConnectionName());
							if (conn != null) {
								try {
									/*
									 * Bind remote port to all interfaces. This allows the sdm master
									 * process running on a cluster node to use the tunnel. 
									 * 
									 * FIXME: Since this requires a special option to be enabled in sshd
									 * on the head node (GatewayPorts), I'd like this to go way.
									 */
									port = conn.forwardRemotePort("", getSessionPort(), monitor);
								} catch (RemoteConnectionException e) {
									throw new PDIException(null, e.getMessage());
								}
								if (monitor.isCanceled()) {
									return;
								}
							} else {
								throw new PDIException(null, "Error getting connection"); //$NON-NLS-1$
							}
						} else {
							throw new PDIException(null, "Error getting connection manager"); //$NON-NLS-1$
						}
					} else {
						throw new PDIException(null, "Error getting remote services for connection"); //$NON-NLS-1$
					}
				}
			}
			args.add("--port=" + port); //$NON-NLS-1$
		} else {
			throw new PDIException(null, "Error getting resource manager"); //$NON-NLS-1$
		}
	}

	/**
	 * Get resource manager from a launch configuration
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private IResourceManagerControl getResourceManager(ILaunchConfiguration configuration) throws PDIException {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		String rmUniqueName;
		try {
			rmUniqueName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String)null);
		} catch (CoreException e) {
			throw new PDIException(null, e.getMessage());
		}
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED &&
					rm.getUniqueName().equals(rmUniqueName)) {
				return (IResourceManagerControl)rm;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#getErrorAction(int)
	 */
	public int getErrorAction(int errorCode) {
		switch (errorCode) {
		case ISDMErrorCodes.DBGERR_NOBACKEND:
		case ISDMErrorCodes.DBGERR_DEBUGGER:
		case ISDMErrorCodes.DBGERR_NOFILEDIR:
		case ISDMErrorCodes.DBGERR_CHDIR:
			return IPDIErrorInfo.DBG_FATAL;
			// case ISDMErrorCodes.DBGERR_INPROGRESS:
		case ISDMErrorCodes.DBGERR_UNKNOWN_TYPE:
		case ISDMErrorCodes.DBGERR_NOFILE:
		case ISDMErrorCodes.DBGERR_NOBP:
			return IPDIErrorInfo.DBG_NORMAL;
		case ISDMErrorCodes.DBGERR_UNKNOWN_VARIABLE:
			return IPDIErrorInfo.DBG_IGNORE;
		default:
			return IPDIErrorInfo.DBG_WARNING;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.debug.client.AbstractProxyDebugClient#handleEvent(org.eclipse.ptp.proxy.event.IProxyExtendedEvent)
	 */
	public void handleEvent(IProxyExtendedEvent e) {
		if (e instanceof IProxyDebugEvent) {
			proxyNotifier.notify((IProxyDebugEvent) e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#isConnected(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean isConnected(IProgressMonitor monitor) throws PDIException {
		try {
			if (waitConnect(monitor)) {
				sessionHandleEvents();
				return true;
			}
			disconnect(null);
			return false;
		} catch (IOException e) {
			disconnect(null);
			throw new PDIException(null, "Error on connecting proxy: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#listArguments(org.eclipse.ptp.core.util.BitList, int, int)
	 */
	public void listArguments(BitList tasks, int low, int high) throws PDIException {
		try {
			debugListArguments(tasks, low, high);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on listing arguments: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#listGlobalVariables(org.eclipse.ptp.core.util.BitList)
	 */
	public void listGlobalVariables(BitList tasks) throws PDIException {
		try {
			debugListGlobalVariables(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on listing global variables: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIThreadManagement#listInfoThreads(org.eclipse.ptp.core.util.BitList)
	 */
	public void listInfoThreads(BitList tasks) throws PDIException {
		try {
			debugListInfoThreads(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on listing thread info: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#listLocalVariables(org.eclipse.ptp.core.util.BitList)
	 */
	public void listLocalVariables(BitList tasks) throws PDIException {
		try {
			debugListLocalVariables(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on listing local variables: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISignalManagement#listSignals(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void listSignals(BitList tasks, String name) throws PDIException {
		try {
			debugListSignals(tasks, name);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on listing signal: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIStackframeManagement#listStackFrames(org.eclipse.ptp.core.util.BitList, int, int)
	 */
	public void listStackFrames(BitList tasks, int low, int depth) throws PDIException {
		try {
			debugListStackframes(tasks, low, depth);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on listing stack frames: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#register(java.util.Observer)
	 */
	public void register(Observer observer) {
		proxyNotifier.addObserver(observer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#restart(org.eclipse.ptp.core.util.BitList)
	 */
	public void restart(BitList tasks) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - restart() yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse.ptp.core.util.BitList, boolean)
	 */
	public void resume(BitList tasks, boolean passSignal) throws PDIException {
		try {
			debugGo(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on resuming tasks: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	public void resume(BitList tasks, IPDILocation location) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - resume(IPDILocation) yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDISignal)
	 */
	public void resume(BitList tasks, IPDISignal signal) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - resume(IPDISignal) yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#retrieveAIF(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void retrieveAIF(BitList tasks, String expr) throws PDIException {
		try {
			debugEvaluateExpression(tasks, expr);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on getting aif: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#retrievePartialAIF(org.eclipse.ptp.core.util.BitList, java.lang.String, java.lang.String, boolean, boolean)
	 */
	public void retrievePartialAIF(BitList tasks, String expr, String key, boolean listChildren, boolean express)
			throws PDIException {
		try {
			debugGetPartialAIF(tasks, expr, key, listChildren, express);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on getting partial aif: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISignalManagement#retrieveSignalInfo(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void retrieveSignalInfo(BitList tasks, String arg) throws PDIException {
		try {
			debugSignalInfo(tasks, arg);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on getting signal info: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIThreadManagement#retrieveStackInfoDepth(org.eclipse.ptp.core.util.BitList)
	 */
	public void retrieveStackInfoDepth(BitList tasks) throws PDIException {
		try {
			debugStackInfoDepth(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on getting stack info depth: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#retrieveVariableType(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public void retrieveVariableType(BitList tasks, String var) throws PDIException {
		try {
			debugGetType(tasks, var);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on getting variable type: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIThreadManagement#selectThread(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void selectThread(BitList tasks, int tid) throws PDIException {
		try {
			debugSetThreadSelect(tasks, tid);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting thread id: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setAddressBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint)
	 */
	public void setAddressBreakpoint(BitList tasks, IPDIAddressBreakpoint bpt) throws PDIException {
		throw new PDIException(tasks, "Not implement PDIDebugger - setAddressBreakpoint() yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setConditionBreakpoint(org.eclipse.ptp.core.util.BitList, int, java.lang.String)
	 */
	public void setConditionBreakpoint(BitList tasks, int bpid, String condition) throws PDIException {
		try {
			debugConditionBreakpoint(tasks, bpid, condition);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting condition breakpoint: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIStackframeManagement#setCurrentStackFrame(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void setCurrentStackFrame(BitList tasks, int level) throws PDIException {
		try {
			debugSetCurrentStackframe(tasks, level);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting current stack frame level: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setEnabledBreakpoint(org.eclipse.ptp.core.util.BitList, int, boolean)
	 */
	public void setEnabledBreakpoint(BitList tasks, int bpid, boolean enabled) throws PDIException {
		try {
			if (enabled)
				debugEnableBreakpoint(tasks, bpid);
			else
				debugDisableBreakpoint(tasks, bpid);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting enabling breakpoint: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setExceptionpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint)
	 */
	public void setExceptionpoint(BitList tasks, IPDIExceptionpoint bpt) throws PDIException {
		throw new PDIException(tasks, "Not implement PDIDebugger - setExceptionpoint() yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setFunctionBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint)
	 */
	public void setFunctionBreakpoint(BitList tasks, IPDIFunctionBreakpoint bpt) throws PDIException {
		try {
			IPDICondition condition = bpt.getCondition();
			int id = bpt.getBreakpointID();
			if (id == -1) {
				id = newBreakpointId();
				bpt.setBreakpointID(id);
			}
			// System.err.println("++Func Bpt - file: " +
			// getFilename(bpt.getLocator().getFile()) + ", func: " +
			// bpt.getLocator().getFunction());
			debugSetFuncBreakpoint(tasks, id, bpt.isTemporary(), bpt.isHardware(), getFilename(bpt.getLocator().getFile()), bpt
					.getLocator().getFunction(), (condition != null ? condition.getExpression() : ""),
					(condition != null ? condition.getIgnoreCount() : 0), 0);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting function breakpoint: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setLineBreakpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint)
	 */
	public void setLineBreakpoint(BitList tasks, IPDILineBreakpoint bpt) throws PDIException {
		try {
			IPDICondition condition = bpt.getCondition();
			int id = bpt.getBreakpointID();
			if (id == -1) {
				id = newBreakpointId();
				bpt.setBreakpointID(id);
			}
			// System.err.println("++Line Bpt - file: " +
			// getFilename(bpt.getLocator().getFile()) + ", line: " +
			// bpt.getLocator().getLineNumber());
			debugSetLineBreakpoint(tasks, id, bpt.isTemporary(), bpt.isHardware(), getFilename(bpt.getLocator().getFile()), bpt
					.getLocator().getLineNumber(), (condition != null ? condition.getExpression() : ""), //$NON-NLS-1$
					(condition != null ? condition.getIgnoreCount() : 0), 0);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting line breakpoint: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setWatchpoint(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint)
	 */
	public void setWatchpoint(BitList tasks, IPDIWatchpoint bpt) throws PDIException {
		try {
			String expression = bpt.getWatchExpression();
			boolean access = bpt.isReadType() && bpt.isWriteType();
			boolean read = !bpt.isWriteType() && bpt.isReadType();
			IPDICondition condition = bpt.getCondition();
			int id = bpt.getBreakpointID();
			if (id == -1) {
				id = newBreakpointId();
				bpt.setBreakpointID(id);
			}
			debugSetWatchpoint(tasks, id, expression, access, read, (condition != null ? condition.getExpression() : ""), //$NON-NLS-1$
					(condition != null ? condition.getIgnoreCount() : 0));
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on setting wacthpoint: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#start(org.eclipse.ptp.core.util.BitList)
	 */
	public void start(BitList tasks) throws PDIException {
		resume(tasks, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#startDebugger(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public void startDebugger(String app, String path, String dir, String[] args) throws PDIException {
		try {
			debugStartSession(app, path, dir, args);
		} catch (IOException e) {
			throw new PDIException(null, "Error on starting debugger: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepInto(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepInto(BitList tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_INTO);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on stepping into: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepIntoInstruction(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepIntoInstruction(BitList tasks, int count) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepIntoInstruction() yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOver(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepOver(BitList tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_OVER);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on stepping over: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOverInstruction(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepOverInstruction(BitList tasks, int count) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepOverInstruction() yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.aif.IAIF)
	 */
	public void stepReturn(BitList tasks, IAIF aif) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepReturn(IAIF) yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse.ptp.core.util.BitList, int)
	 */
	public void stepReturn(BitList tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_FINISH);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on stepping return: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepUntil(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	public void stepUntil(BitList tasks, IPDILocation location) throws PDIException {
		throw new PDIException(null, "Not implement PDIDebugger - stepUntil(IPDILocation) yet"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#stopDebugger()
	 */
	public void stopDebugger() throws PDIException {
		try {
			doShutdown();
		} catch (IOException e) {
			throw new PDIException(null, "Error on stopping debugger: " + e.getMessage()); //$NON-NLS-1$
		} finally {
			proxyNotifier.deleteObservers();
			finalize();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#suspend(org.eclipse.ptp.core.util.BitList)
	 */
	public void suspend(BitList tasks) throws PDIException {
		try {
			debugInterrupt(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on suspending tasks: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#terminate(org.eclipse.ptp.core.util.BitList)
	 */
	public void terminate(BitList tasks) throws PDIException {
		try {
			debugTerminate(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, "Error on terminating tasks: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * Extract file name part of a path
	 * 
	 * @param fullPath
	 * @return file name
	 */
	private String getFilename(String fullPath) {
		IPath path = new Path(fullPath);
		if (path.isEmpty())
			return ""; //$NON-NLS-1$
		return path.lastSegment();
	}

	/**
	 * Convert word format to character representation
	 * 
	 * @param wordFormat
	 * @return character representation
	 */
	private String getFormat(int wordFormat) {
		switch (wordFormat) {
		case ExtFormat.UNSIGNED:
			return "u"; //$NON-NLS-1$
		case ExtFormat.FLOAT:
			return "f"; //$NON-NLS-1$
		case ExtFormat.ADDRESS:
			return "a"; //$NON-NLS-1$
		case ExtFormat.INSTRUCTION:
			return "i"; //$NON-NLS-1$
		case ExtFormat.CHAR:
			return "c"; //$NON-NLS-1$
		case ExtFormat.STRING:
			return "s"; //$NON-NLS-1$
		case ExtFormat.DECIMAL:
			return "d"; //$NON-NLS-1$
		case ExtFormat.BINARY:
			return "t"; //$NON-NLS-1$
		case ExtFormat.OCTAL:
			return "o"; //$NON-NLS-1$
		case ExtFormat.HEXADECIMAL:
		default:
			return "x"; //$NON-NLS-1$
		}
	}

	/**
	 * Generate a new unique breakpoint ID
	 * 
	 * @return unique breakpoint ID
	 */
	private int newBreakpointId() {
		return bpid++;
	}
}
