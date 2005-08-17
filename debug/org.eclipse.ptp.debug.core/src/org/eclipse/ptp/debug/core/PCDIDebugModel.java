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
/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.ptp.debug.core;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CWatchpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.internal.core.IPDebugInternalConstants;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

/**
 * Provides utility methods for creating debug sessions, targets and breakpoints
 * specific to the CDI debug model.
 */
public class PCDIDebugModel {
	
	private static Hashtable dTargets = new Hashtable();
	private static Hashtable dProcesses = new Hashtable();

	/**
	 * Returns the identifier for the CDI debug model plug-in
	 * 
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return PTPDebugCorePlugin.getUniqueIdentifier();
	}
	
	public static void removeDebugTarget(ILaunch launch, int i) {
		String name = "Proc " + i;
		launch.removeDebugTarget((IDebugTarget) dTargets.get(name));
		launch.removeProcess((IProcess) dProcesses.get(name));
	}

	/**
	 * Creates and returns a debug target for the given CDI target, with the specified name, and associates it with the given process for console I/O. The debug
	 * target is added to the given launch.
	 * 
	 * @param launch the launch the new debug target will be contained in
	 * @param project the project to use to persist breakpoints.
	 * @param cdiTarget the CDI target to create a debug target for
	 * @param name the name to associate with this target, which will be returned from <code>IDebugTarget.getName</code>.
	 * @param debuggeeProcess the process to associate with the debug target, which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param file the executable to debug.
	 * @param allowTerminate allow terminate().
	 * @param allowDisconnect allow disconnect().
	 * @param stopInMain place temporary breakpoint at main()
	 * @param resumeTarget resume target.
	 * @return a debug target
	 * @throws DebugException
	 */
	public static IDebugTarget newDebugTarget( final ILaunch launch, final IProject project, final IPCDITarget cdiTarget, final String name, final IProcess debuggeeProcess, final IBinaryObject file, final boolean allowTerminate, final boolean allowDisconnect, final boolean stopInMain, final boolean resumeTarget ) throws DebugException {
		final IDebugTarget[] target = new IDebugTarget[1];
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run( IProgressMonitor m ) throws CoreException {
				boolean stop = launch.getLaunchConfiguration().getAttribute( IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false );
				target[0] = new PDebugTarget( launch, project, cdiTarget, name, debuggeeProcess, file, allowTerminate, allowDisconnect );
				
				dTargets.put(name, target[0]);
				dProcesses.put(name, debuggeeProcess);
				
				ICDITargetConfiguration config = cdiTarget.getConfiguration();
				if ( config.supportsBreakpoints() && stop ) {
					stopInMain( (PDebugTarget)target[0] );
				}
				if ( config.supportsResume() && resumeTarget ) {
					target[0].resume();
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run( r, null );
		}
		catch( CoreException e ) {
			PTPDebugCorePlugin.log( e );
			throw new DebugException( e.getStatus() );
		}
		return target[0];
	}

	/**
	 * Creates and returns a line breakpoint for the source defined by the given source handle, at the given line number. The marker associated with the
	 * breakpoint will be created on the specified resource.
	 * 
	 * @param sourceHandle
	 *            the handle to the breakpoint source
	 * @param resource
	 *            the resource on which to create the associated breakpoint marker
	 * @param lineNumber
	 *            the line number on which the breakpoint is set - line numbers are 1 based, associated with the source file in which the breakpoint is set
	 * @param enabled
	 *            whether to enable or disable this breakpoint
	 * @param ignoreCount
	 *            the number of times this breakpoint will be ignored
	 * @param condition
	 *            the breakpoint condition
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @return a line breakpoint
	 * @throws CoreException
	 *             if this method fails. Reasons include:
	 *             <ul>
	 *             <li>Failure creating underlying marker. The exception's status contains the underlying exception responsible for the failure.</li>
	 *             </ul>
	 */
	public static ICLineBreakpoint createLineBreakpoint( String sourceHandle, IResource resource, int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.LINE_NUMBER, new Integer( lineNumber ) );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		return new CLineBreakpoint( resource, attributes, register );
	}

	/**
	 * Creates and returns an address breakpoint for the source defined by the
	 * given source handle, at the given address. The marker associated with the
	 * breakpoint will be created on the specified resource.
	 * 
	 * @param module the module name the breakpoint is set in
	 * @param sourceHandle the handle to the breakpoint source
	 * @param resource the resource on which to create the associated breakpoint marker
	 * @param address the address on which the breakpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return an address breakpoint
	 * @throws CoreException if this method fails. Reasons include:
	 *             <ul>
	 *             <li>Failure creating underlying marker. The exception's
	 *             status contains the underlying exception responsible for the
	 *             failure.</li>
	 *             </ul>
	 */
	public static ICAddressBreakpoint createAddressBreakpoint( String module, String sourceHandle, IResource resource, IAddress address, boolean enabled, int ignoreCount, String condition, boolean register ) throws CoreException {
		return createAddressBreakpoint( module, sourceHandle, resource, -1, address, enabled, ignoreCount, condition, register );
	}

	/**
	 * Creates and returns an address breakpoint for the source defined by the
	 * given source handle, at the given address. The marker associated with the
	 * breakpoint will be created on the specified resource.
	 * 
	 * @param module the module name the breakpoint is set in
	 * @param sourceHandle the handle to the breakpoint source
	 * @param resource the resource on which to create the associated breakpoint marker
	 * @param lineNumber the line number in the source file
	 * @param address the address on which the breakpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return an address breakpoint
	 * @throws CoreException if this method fails. Reasons include:
	 *             <ul>
	 *             <li>Failure creating underlying marker. The exception's
	 *             status contains the underlying exception responsible for the
	 *             failure.</li>
	 *             </ul>
	 */
	public static ICAddressBreakpoint createAddressBreakpoint( String module, String sourceHandle, IResource resource, int lineNumber, IAddress address, boolean enabled, int ignoreCount, String condition, boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.CHAR_START, new Integer( -1 ) );
		attributes.put( IMarker.CHAR_END, new Integer( -1 ) );
		attributes.put( IMarker.LINE_NUMBER, new Integer( lineNumber ) );
		attributes.put( ICLineBreakpoint.ADDRESS, address.toHexAddressString() );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		attributes.put( ICBreakpoint.MODULE, module );
		return new CAddressBreakpoint( resource, attributes, register );
	}

	/**
	 * Creates and returns a watchpoint for the source defined by the given
	 * source handle, at the given expression. The marker associated with the
	 * watchpoint will be created on the specified resource.
	 * 
	 * @param sourceHandle the handle to the watchpoint source
	 * @param resource the resource on which to create the associated watchpoint marker
	 * @param writeAccess whether this is write watchpoint
	 * @param readAccess whether this is read watchpoint
	 * @param expression the expression on which the watchpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return a watchpoint
	 * @throws CoreException if this method fails. Reasons include:
	 *             <ul>
	 *             <li>Failure creating underlying marker. The exception's
	 *             status contains the underlying exception responsible for the
	 *             failure.</li>
	 *             </ul>
	 */
	public static ICWatchpoint createWatchpoint( String sourceHandle, IResource resource, boolean writeAccess, boolean readAccess, String expression, boolean enabled, int ignoreCount, String condition, boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		attributes.put( ICWatchpoint.EXPRESSION, expression );
		attributes.put( ICWatchpoint.READ, new Boolean( readAccess ) );
		attributes.put( ICWatchpoint.WRITE, new Boolean( writeAccess ) );
		return new CWatchpoint( resource, attributes, register );
	}

	/**
	 * Creates and returns a watchpoint for the source defined by the given
	 * source handle, at the given expression. The marker associated with the
	 * watchpoint will be created on the specified resource.
	 * 
	 * @param sourceHandle the handle to the watchpoint source
	 * @param resource the resource on which to create the associated watchpoint marker
	 * @param charStart the first character index associated with the watchpoint, or
	 *            -1 if unspecified, in the source file in which the watchpoint
	 *            is set
	 * @param charEnd the last character index associated with the watchpoint, or -1
	 *            if unspecified, in the source file in which the watchpoint is
	 *            set
	 * @param lineNumber the lineNumber on which the watchpoint is set, or -1 if
	 *            unspecified - line numbers are 1 based, associated with the
	 *            source file in which the watchpoint is set
	 * @param writeAccess whether this is write watchpoint
	 * @param readAccess whether this is read watchpoint
	 * @param expression the expression on which the watchpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return a watchpoint
	 * @throws CoreException if this method fails. Reasons include:
	 *             <ul>
	 *             <li>Failure creating underlying marker. The exception's
	 *             status contains the underlying exception responsible for the
	 *             failure.</li>
	 *             </ul>
	 */
	public static ICWatchpoint createWatchpoint( String sourceHandle, IResource resource, int charStart, int charEnd, int lineNumber, boolean writeAccess, boolean readAccess, String expression, boolean enabled, int ignoreCount, String condition, boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.CHAR_START, new Integer( charStart ) );
		attributes.put( IMarker.CHAR_END, new Integer( charEnd ) );
		attributes.put( IMarker.LINE_NUMBER, new Integer( lineNumber ) );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		attributes.put( ICWatchpoint.EXPRESSION, expression );
		attributes.put( ICWatchpoint.READ, new Boolean( readAccess ) );
		attributes.put( ICWatchpoint.WRITE, new Boolean( writeAccess ) );
		return new CWatchpoint( resource, attributes, register );
	}

	/**
	 * Creates and returns a breakpoint for the function defined by the given
	 * name. The marker associated with the breakpoint will be created on the
	 * specified resource.
	 * 
	 * @param sourceHandle the handle to the breakpoint source
	 * @param resource the resource on which to create the associated breakpoint marker
	 * @param function the name of the function this breakpoint suspends execution in
	 * @param charStart the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param lineNumber the lineNumber on which the breakpoint is set, or -1 if
	 *            unspecified - line numbers are 1 based, associated with the
	 *            source file in which the breakpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return an address breakpoint
	 * @throws CoreException if this method fails. Reasons include:
	 *             <ul>
	 *             <li>Failure creating underlying marker. The exception's
	 *             status contains the underlying exception responsible for the
	 *             failure.</li>
	 *             </ul>
	 */
	public static ICFunctionBreakpoint createFunctionBreakpoint( String sourceHandle, IResource resource, String function, int charStart, int charEnd, int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.CHAR_START, new Integer( charStart ) );
		attributes.put( IMarker.CHAR_END, new Integer( charEnd ) );
		attributes.put( IMarker.LINE_NUMBER, new Integer( lineNumber ) );
		attributes.put( ICLineBreakpoint.FUNCTION, function );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		return new CFunctionBreakpoint( resource, attributes, register );
	}

	/**
	 * Returns the line breakpoint that is already registered with the
	 * breakpoint manager for a source with the given handle and the given
	 * resource at the given line number.
	 * 
	 * @param sourceHandle the source handle
	 * @param resource the breakpoint resource
	 * @param lineNumber the line number
	 * @return the line breakpoint that is already registered with the
	 *         breakpoint manager or <code>null</code> if no such breakpoint
	 *         is registered
	 * @exception CoreException if unable to retrieve the associated marker attributes (line number).
	 */
	public static ICLineBreakpoint lineBreakpointExists( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
		String modelId = getPluginIdentifier();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for( int i = 0; i < breakpoints.length; i++ ) {
			if ( !(breakpoints[i] instanceof ICLineBreakpoint) ) {
				continue;
			}
			ICLineBreakpoint breakpoint = (ICLineBreakpoint)breakpoints[i];
			if ( sameSourceHandle( sourceHandle, breakpoint.getSourceHandle() ) ) {
				if ( breakpoint.getMarker().getResource().equals( resource ) ) {
					if ( breakpoint.getLineNumber() == lineNumber ) {
						return breakpoint;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the watchpoint that is already registered with the breakpoint
	 * manager for a source with the given handle and the given resource at the
	 * given expression.
	 * 
	 * @param sourceHandle the source handle
	 * @param resource the breakpoint resource
	 * @param expression the expression
	 * @return the watchpoint that is already registered with the breakpoint
	 *         manager or <code>null</code> if no such watchpoint is
	 *         registered
	 * @exception CoreException if unable to retrieve the associated marker attributes (line number).
	 */
	public static ICWatchpoint watchpointExists( String sourceHandle, IResource resource, String expression ) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = CWatchpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for( int i = 0; i < breakpoints.length; i++ ) {
			if ( !(breakpoints[i] instanceof ICWatchpoint) ) {
				continue;
			}
			ICWatchpoint breakpoint = (ICWatchpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType ) ) {
				if ( sameSourceHandle( sourceHandle, breakpoint.getSourceHandle() ) ) {
					if ( breakpoint.getMarker().getResource().equals( resource ) ) {
						if ( breakpoint.getExpression().equals( expression ) ) {
							return breakpoint;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the function breakpoint that is already registered with the
	 * breakpoint manager for a source with the given handle and the given
	 * resource with the given function name.
	 * 
	 * @param sourceHandle the source handle
	 * @param resource the breakpoint resource
	 * @param function the fully qualified function name
	 * @return the breakpoint that is already registered with the breakpoint
	 *         manager or <code>null</code> if no such breakpoint is
	 *         registered
	 * @exception CoreException if unable to retrieve the associated marker attributes (line number).
	 */
	public static ICFunctionBreakpoint functionBreakpointExists( String sourceHandle, IResource resource, String function ) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = CFunctionBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for( int i = 0; i < breakpoints.length; i++ ) {
			if ( !(breakpoints[i] instanceof ICFunctionBreakpoint) ) {
				continue;
			}
			ICFunctionBreakpoint breakpoint = (ICFunctionBreakpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType ) ) {
				if ( sameSourceHandle( sourceHandle, breakpoint.getSourceHandle() ) ) {
					if ( breakpoint.getMarker().getResource().equals( resource ) ) {
						if ( breakpoint.getFunction() != null && breakpoint.getFunction().equals( function ) ) {
							return breakpoint;
						}
					}
				}
			}
		}
		return null;
	}

	protected static void stopInMain( PDebugTarget target ) throws DebugException {
		System.out.println("PCDIDebugModel.stopInMain()");
		ICDILocation location = target.getCDITarget().createFunctionLocation( "", "main" ); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			target.setInternalTemporaryBreakpoint( location );
		}
		catch( DebugException e ) {
			String message = MessageFormat.format( DebugCoreMessages.getString( "CDebugModel.0" ), new String[]{ e.getStatus().getMessage() } ); //$NON-NLS-1$
			IStatus newStatus = new Status( IStatus.WARNING, e.getStatus().getPlugin(), IPDebugInternalConstants.STATUS_CODE_QUESTION, message, null );
			if ( !CDebugUtils.question( newStatus, target ) ) {
				target.terminate();
				throw new DebugException( new Status( IStatus.OK, e.getStatus().getPlugin(), e.getStatus().getCode(), e.getStatus().getMessage(), null ) );
			}
		}
	}

	private static boolean sameSourceHandle( String handle1, String handle2 ) {
		if ( handle1 == null || handle2 == null )
			return false;
		IPath path1 = new Path( handle1 );
		IPath path2 = new Path( handle2 );
		if ( path1.isValidPath( handle1 ) && path2.isValidPath( handle2 ) ) {
			return path1.equals( path2 );
		}
		// If handles are not file names ????
		return handle1.equals( handle2 );
	}
}