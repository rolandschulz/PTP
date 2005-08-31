package org.eclipse.ptp.debug.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
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
		setBreakpoints();
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
	
	public void setBreakpoints() {
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
			// DONNY
/*			else if ( breakpoint instanceof ICFunctionBreakpoint )
				setFunctionBreakpoint( (ICFunctionBreakpoint)breakpoint );
			else if ( breakpoint instanceof ICAddressBreakpoint )
				setAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
			else if ( breakpoint instanceof ICWatchpoint )
				setWatchpoint( (ICWatchpoint)breakpoint );
*/
		}
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
		
		ICDILineLocation location = ((IPCDISession) getPCDISession()).createLineLocation( path.lastSegment()/*path.toPortableString()*/, breakpoint.getLineNumber() );
		ICDICondition condition = null; //((IPCDISession) getPCDISession()).createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition(), getThreadNames( breakpoint ) );
		setLocationBreakpointOnTarget( breakpoint, location, condition, enabled );
	}
	
	private void setLocationBreakpointOnTarget( final IPBreakpoint breakpoint, final ICDILocation location, final ICDICondition condition, final boolean enabled ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					if ( breakpoint instanceof IPLineBreakpoint ) {
						((IPCDISession) getPCDISession()).setLineBreakpoint(
								ICDIBreakpoint.REGULAR,
								(ICDILineLocation)location, condition, true);
							
						//target.setLineBreakpoint( ICDIBreakpoint.REGULAR,
						//	(ICDILineLocation)location, condition, true );
						// DONNY
/*					} else if ( breakpoint instanceof ICFunctionBreakpoint ) {
						target.setFunctionBreakpoint( ICDIBreakpoint.REGULAR,
								(ICDIFunctionLocation)location, condition, true );								
					} else if ( breakpoint instanceof ICAddressBreakpoint ) {
							target.setAddressBreakpoint( ICDIBreakpoint.REGULAR,
								(ICDIAddressLocation)location, condition, true );
*/					}
				}
				catch( CDIException e ) {
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
