package org.eclipse.ptp.debug.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceLookupDirector;

public class PSession implements IPSession, IBreakpointsListener {
	private IPCDISession pCDISession;
	private IPLaunch pLaunch;
	
	public PSession(IPCDISession session, IPLaunch launch) {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );

		pCDISession = session;
		pLaunch = launch;
		
		initializeBreakpoints();
	}

	public IPCDISession getPCDISession() {
		return pCDISession;
	}

	public void breakpointsAdded(IBreakpoint[] breakpoints) {
		// Auto-generated method stub
		System.out.println("PSession.breakpointsAdded()");
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( breakpoints[i] instanceof IPBreakpoint ) {
				setBreakpoint((IPBreakpoint) breakpoints[i]);
			}
		}
	}

	public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		// Auto-generated method stub
		System.out.println("PSession.breakpointsRemoved()");
	}

	public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		// Auto-generated method stub
		System.out.println("PSession.breakpointsChanged()");
	}
	
	private void initializeBreakpoints() {
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] bps = manager.getBreakpoints( PCDIDebugModel.getPluginIdentifier() );
		for( int i = 0; i < bps.length; i++ ) {
			if ( bps[i] instanceof IPBreakpoint ) {
				setBreakpoint((IPBreakpoint) bps[i]);
			}
		}
	}
	
	private void setBreakpoint( IPBreakpoint breakpoint ) {
		try {
			if ( breakpoint instanceof IPLineBreakpoint )
				setLineBreakpoint( (IPLineBreakpoint)breakpoint );
/*			else if ( breakpoint instanceof IPFunctionBreakpoint )
				setFunctionBreakpoint( (ICFunctionBreakpoint)breakpoint );
			else if ( breakpoint instanceof IPAddressBreakpoint )
				setAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
			else if ( breakpoint instanceof IPWatchpoint )
				setWatchpoint( (ICWatchpoint)breakpoint );
*/		}
		catch( CoreException e ) {
		}
		catch( NumberFormatException e ) {
		}
		catch( CDIException e ) {
		}
	}

	private void setLineBreakpoint( IPLineBreakpoint breakpoint ) throws CDIException, CoreException {
		boolean enabled = breakpoint.isEnabled();
		String handle = breakpoint.getSourceHandle();
		IPath path = convertPath( handle );
		
		ICDILineLocation location = pCDISession.createLineLocation( path.lastSegment(), breakpoint.getLineNumber() );
		ICDICondition condition = null;
		
		setLocationBreakpointOnSession( breakpoint, location, condition, enabled );
	}
	
	private void setLocationBreakpointOnSession( final IPBreakpoint breakpoint, final ICDILocation location, final ICDICondition condition, final boolean enabled ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					if ( breakpoint instanceof IPLineBreakpoint ) {
						IPCDIDebugProcessSet set = pCDISession.getModelManager().getProcessSet(breakpoint.getCurSetId());
						pCDISession.setLineBreakpoint(set, ICDIBreakpoint.REGULAR,
								(ICDILineLocation)location, condition, true);
/*					} else if ( breakpoint instanceof ICFunctionBreakpoint ) {
						target.setFunctionBreakpoint( ICDIBreakpoint.REGULAR,
								(ICDIFunctionLocation)location, condition, true );								
					} else if ( breakpoint instanceof ICAddressBreakpoint ) {
						target.setAddressBreakpoint( ICDIBreakpoint.REGULAR,
								(ICDIAddressLocation)location, condition, true );
*/					}
				} catch( CDIException e ) {
				} catch( CoreException e ) {
				} 
			}
		} );
	}

	private IPath convertPath( String sourceHandle ) {
		IPath path = null;
		if ( Path.EMPTY.isValidPath( sourceHandle ) ) {
			ISourceLocator sl = pLaunch.getSourceLocator();
			if ( sl instanceof CSourceLookupDirector ) {
				path = ((CSourceLookupDirector)sl).getCompilationPath( sourceHandle );
			}
			if ( path == null ) {
				path = new Path( sourceHandle );
			}
		}
		return path;
	}

}
