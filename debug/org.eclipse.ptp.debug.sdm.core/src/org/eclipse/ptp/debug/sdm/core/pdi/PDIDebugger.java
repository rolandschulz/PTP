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
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.debug.core.ExtFormat;
import org.eclipse.ptp.debug.core.TaskSet;
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
import org.eclipse.ptp.debug.sdm.core.SDMDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.SDMPreferenceConstants;
import org.eclipse.ptp.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.debug.sdm.core.proxy.ProxyDebugClient;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
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
	private final ProxyNotifier proxyNotifier = new ProxyNotifier();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIDebugger#commandRequest(org.eclipse
	 * .ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void commandRequest(TaskSet tasks, String command) throws PDIException {
		try {
			debugCLIHandle(tasks, command);
		} catch (IOException e) {
			throw new PDIException(null, Messages.PDIDebugger_0 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIMemoryBlockManagement#createDataReadMemory
	 * (org.eclipse.ptp.core.util.TaskSet, long, java.lang.String, int, int,
	 * int, int, java.lang.Character)
	 */
	/**
	 * @since 4.0
	 */
	public void createDataReadMemory(TaskSet tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols,
			Character asChar) throws PDIException {
		try {
			setDataReadMemoryCommand(tasks, offset, address, getFormat(wordFormat), wordSize, rows, cols, asChar);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_1 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIMemoryBlockManagement#
	 * createDataWriteMemory(org.eclipse.ptp.core.util.TaskSet, long,
	 * java.lang.String, int, int, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void createDataWriteMemory(TaskSet tasks, long offset, String address, int wordFormat, int wordSize, String value)
			throws PDIException {
		try {
			setDataWriteMemoryCommand(tasks, offset, address, getFormat(wordFormat), wordSize, value);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_2 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#deleteBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void deleteBreakpoint(TaskSet tasks, int bpid) throws PDIException {
		try {
			debugDeleteBreakpoint(tasks, bpid);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_3 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#deletePartialExpression
	 * (org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void deletePartialExpression(TaskSet tasks, String var) throws PDIException {
		try {
			debugDeletePartialExpression(tasks, var);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_4 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIDebugger#disconnect(java.util.Observer
	 * )
	 */
	public void disconnect(Observer observer) throws PDIException {
		stopDebugger();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#evaluateExpression
	 * (org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void evaluateExpression(TaskSet tasks, String expr) throws PDIException {
		try {
			debugEvaluateExpression(tasks, expr);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_5 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#
	 * evaluatePartialExpression(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, java.lang.String, boolean, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public void evaluatePartialExpression(TaskSet tasks, String expr, String exprId, boolean listChildren, boolean express)
			throws PDIException {
		try {
			debugEvaluatePartialExpression(tasks, expr, exprId, listChildren, express);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_6 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.debug.client.AbstractProxyDebugClient#handleEvent
	 * (org.eclipse.ptp.proxy.event.IProxyExtendedEvent)
	 */
	@Override
	public void handleEvent(IProxyExtendedEvent e) {
		if (e instanceof IProxyDebugEvent) {
			proxyNotifier.notify((IProxyDebugEvent) e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIDebugger#initialize(java.util.List)
	 */
	public void initialize(ILaunchConfiguration configuration, List<String> args, IProgressMonitor monitor) throws PDIException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);

		try {
			int port = 0;

			/*
			 * If there is an existing port specified, use it if possible.
			 */
			for (String arg : args) {
				if (arg.startsWith("--port=")) { //$NON-NLS-1$
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
				throw new PDIException(null, Messages.PDIDebugger_7 + e.getMessage());
			}

			IResourceManagerControl rm = getResourceManager(configuration);
			if (rm != null) {
				port = getSessionPort();
				IResourceManagerConfiguration conf = rm.getConfiguration();
				if (conf instanceof IRemoteResourceManagerConfiguration) {
					IRemoteResourceManagerConfiguration remConf = (IRemoteResourceManagerConfiguration) conf;
					if (remConf.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
						IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(
								remConf.getRemoteServicesId(), progress.newChild(5));
						if (remoteServices != null) {
							IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
							if (connMgr != null) {
								IRemoteConnection conn = connMgr.getConnection(remConf.getConnectionName());
								if (conn != null) {
									try {
										/*
										 * Bind remote port to all interfaces.
										 * This allows the sdm master process
										 * running on a cluster node to use the
										 * tunnel.
										 * 
										 * FIXME: Since this requires a special
										 * option to be enabled in sshd on the
										 * head node (GatewayPorts), I'd like
										 * this to go way.
										 */
										port = conn.forwardRemotePort("", getSessionPort(), progress.newChild(5)); //$NON-NLS-1$
									} catch (RemoteConnectionException e) {
										throw new PDIException(null, e.getMessage());
									}
									if (progress.isCanceled()) {
										return;
									}
								} else {
									throw new PDIException(null, Messages.PDIDebugger_8);
								}
							} else {
								throw new PDIException(null, Messages.PDIDebugger_9);
							}
						} else {
							throw new PDIException(null, Messages.PDIDebugger_10);
						}
					}
				}
				args.add("--port=" + port); //$NON-NLS-1$
			} else {
				throw new PDIException(null, Messages.PDIDebugger_11);
			}

			Preferences store = SDMDebugCorePlugin.getDefault().getPluginPreferences();

			if (store.getBoolean(SDMPreferenceConstants.SDM_DEBUG_ENABLED)) {
				int level = store.getInt(SDMPreferenceConstants.SDM_DEBUG_LEVEL);
				if ((level & SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL) == SDMPreferenceConstants.DEBUG_LEVEL_PROTOCOL) {
					getDebugOptions().PROTOCOL_TRACING = true;
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIDebugger#isConnected(org.eclipse.core
	 * .runtime.IProgressMonitor)
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
			throw new PDIException(null, Messages.PDIDebugger_12 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#listArguments(org
	 * .eclipse.ptp.core.util.TaskSet, int, int)
	 */
	/**
	 * @since 4.0
	 */
	public void listArguments(TaskSet tasks, int low, int high) throws PDIException {
		try {
			debugListArguments(tasks, low, high);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_13 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#listGlobalVariables
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void listGlobalVariables(TaskSet tasks) throws PDIException {
		try {
			debugListGlobalVariables(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_14 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIThreadManagement#listInfoThreads(org
	 * .eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void listInfoThreads(TaskSet tasks) throws PDIException {
		try {
			debugListInfoThreads(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_15 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#listLocalVariables
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void listLocalVariables(TaskSet tasks) throws PDIException {
		try {
			debugListLocalVariables(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_16 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDISignalManagement#listSignals(org.eclipse
	 * .ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void listSignals(TaskSet tasks, String name) throws PDIException {
		try {
			debugListSignals(tasks, name);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_17 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIStackframeManagement#listStackFrames
	 * (org.eclipse.ptp.core.util.TaskSet, int, int)
	 */
	/**
	 * @since 4.0
	 */
	public void listStackFrames(TaskSet tasks, int low, int depth) throws PDIException {
		try {
			debugListStackframes(tasks, low, depth);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_18 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIDebugger#register(java.util.Observer)
	 */
	public void register(Observer observer) {
		proxyNotifier.addObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#restart(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void restart(TaskSet tasks) throws PDIException {
		throw new PDIException(null, Messages.PDIDebugger_19);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse
	 * .ptp.core.util.TaskSet, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public void resume(TaskSet tasks, boolean passSignal) throws PDIException {
		try {
			debugGo(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_20 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	/**
	 * @since 4.0
	 */
	public void resume(TaskSet tasks, IPDILocation location) throws PDIException {
		throw new PDIException(null, Messages.PDIDebugger_21);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#resume(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.model.IPDISignal)
	 */
	/**
	 * @since 4.0
	 */
	public void resume(TaskSet tasks, IPDISignal signal) throws PDIException {
		throw new PDIException(null, Messages.PDIDebugger_22);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIThreadManagement#retrieveStackInfoDepth
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void retrieveStackInfoDepth(TaskSet tasks) throws PDIException {
		try {
			debugStackInfoDepth(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_24 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIVariableManagement#retrieveVariableType
	 * (org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void retrieveVariableType(TaskSet tasks, String var) throws PDIException {
		try {
			debugGetType(tasks, var);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_25 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIThreadManagement#selectThread(org.
	 * eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void selectThread(TaskSet tasks, int tid) throws PDIException {
		try {
			debugSetThreadSelect(tasks, tid);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_26 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setAddressBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint)
	 */
	/**
	 * @since 4.0
	 */
	public void setAddressBreakpoint(TaskSet tasks, IPDIAddressBreakpoint bpt) throws PDIException {
		throw new PDIException(tasks, Messages.PDIDebugger_27);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#
	 * setConditionBreakpoint(org.eclipse.ptp.core.util.TaskSet, int,
	 * java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void setConditionBreakpoint(TaskSet tasks, int bpid, String condition) throws PDIException {
		try {
			debugConditionBreakpoint(tasks, bpid, condition);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_28 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIStackframeManagement#setCurrentStackFrame
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void setCurrentStackFrame(TaskSet tasks, int level) throws PDIException {
		try {
			debugSetCurrentStackframe(tasks, level);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_29 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setEnabledBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet, int, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public void setEnabledBreakpoint(TaskSet tasks, int bpid, boolean enabled) throws PDIException {
		try {
			if (enabled)
				debugEnableBreakpoint(tasks, bpid);
			else
				debugDisableBreakpoint(tasks, bpid);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_30 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setExceptionpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint)
	 */
	/**
	 * @since 4.0
	 */
	public void setExceptionpoint(TaskSet tasks, IPDIExceptionpoint bpt) throws PDIException {
		throw new PDIException(tasks, Messages.PDIDebugger_31);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setFunctionBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint)
	 */
	/**
	 * @since 4.0
	 */
	public void setFunctionBreakpoint(TaskSet tasks, IPDIFunctionBreakpoint bpt) throws PDIException {
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
					.getLocator().getFunction(), (condition != null ? condition.getExpression() : ""), //$NON-NLS-1$
					(condition != null ? condition.getIgnoreCount() : 0), 0);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_32 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setLineBreakpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint)
	 */
	/**
	 * @since 4.0
	 */
	public void setLineBreakpoint(TaskSet tasks, IPDILineBreakpoint bpt) throws PDIException {
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
			throw new PDIException(tasks, Messages.PDIDebugger_33 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIBreakpointManagement#setWatchpoint
	 * (org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint)
	 */
	/**
	 * @since 4.0
	 */
	public void setWatchpoint(TaskSet tasks, IPDIWatchpoint bpt) throws PDIException {
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
			throw new PDIException(tasks, Messages.PDIDebugger_34 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#start(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void start(TaskSet tasks) throws PDIException {
		resume(tasks, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIDebugger#startDebugger(java.lang.String
	 * , java.lang.String, java.lang.String, java.lang.String[])
	 */
	public void startDebugger(String app, String path, String dir, String[] args) throws PDIException {
		try {
			debugStartSession(app, path, dir, args);
		} catch (IOException e) {
			throw new PDIException(null, Messages.PDIDebugger_35 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepInto(org.eclipse
	 * .ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void stepInto(TaskSet tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_INTO);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_36 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepIntoInstruction
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void stepIntoInstruction(TaskSet tasks, int count) throws PDIException {
		throw new PDIException(null, Messages.PDIDebugger_37);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOver(org.eclipse
	 * .ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void stepOver(TaskSet tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_OVER);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_38 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepOverInstruction
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void stepOverInstruction(TaskSet tasks, int count) throws PDIException {
		throw new PDIException(null, Messages.PDIDebugger_39);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.model.aif.IAIF)
	 */
	/**
	 * @since 4.0
	 */
	public void stepReturn(TaskSet tasks, IAIF aif) throws PDIException {
		throw new PDIException(null, Messages.PDIDebugger_40);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepReturn(org.eclipse
	 * .ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public void stepReturn(TaskSet tasks, int count) throws PDIException {
		try {
			debugStep(tasks, count, ProxyDebugClient.STEP_FINISH);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_41 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#stepUntil(org.eclipse
	 * .ptp.core.util.TaskSet, org.eclipse.ptp.debug.core.pdi.IPDILocation)
	 */
	/**
	 * @since 4.0
	 */
	public void stepUntil(TaskSet tasks, IPDILocation location) throws PDIException {
		throw new PDIException(null, Messages.PDIDebugger_42);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDIDebugger#stopDebugger()
	 */
	public void stopDebugger() throws PDIException {
		try {
			doShutdown();
		} catch (IOException e) {
			throw new PDIException(null, Messages.PDIDebugger_43 + e.getMessage());
		} finally {
			proxyNotifier.deleteObservers();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#suspend(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void suspend(TaskSet tasks) throws PDIException {
		try {
			debugInterrupt(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_44 + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.IPDIExecuteManagement#terminate(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void terminate(TaskSet tasks) throws PDIException {
		try {
			debugTerminate(tasks);
		} catch (IOException e) {
			throw new PDIException(tasks, Messages.PDIDebugger_45 + e.getMessage());
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
			rmUniqueName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
					(String) null);
		} catch (CoreException e) {
			throw new PDIException(null, e.getMessage());
		}
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED && rm.getUniqueName().equals(rmUniqueName)) {
				return (IResourceManagerControl) rm;
			}
		}
		return null;
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
