package org.eclipse.ptp.debug.core;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

public class PCDIDebugModel {
	public static String getPluginIdentifier() {
		return PTPDebugCorePlugin.getUniqueIdentifier();
	}
	
	public static IPDebugTarget newDebugTarget(final ILaunch iLaunch, final IPCDITarget iPCDITarget, final IProcess[] iProcs) throws DebugException {
		final IDebugTarget[] target = new IDebugTarget[1];
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run( IProgressMonitor m ) throws CoreException {
				boolean stop = iLaunch.getLaunchConfiguration().getAttribute( IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false );
				target[0] = new PDebugTarget(iLaunch, iPCDITarget, iProcs);
				ICDITargetConfiguration config = iPCDITarget.getConfiguration();
				if ( config.supportsBreakpoints() && stop ) {
					stopInMain( (PDebugTarget)target[0] );
				}
				boolean resumeTarget = true;
				if ( config.supportsResume() && resumeTarget ) {
					target[0].resume();
				}

				
				
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run( r, null );
		}
		catch( CoreException e ) {
			System.out.println("IWorkspaceRunnable Error");
			CDebugCorePlugin.log( e );
			throw new DebugException( e.getStatus() );
		}
		return (IPDebugTarget) target[0];

		
		//PDebugTarget debugTarget = new PDebugTarget(l, t, iprocs);
		//return debugTarget;
	}
	
	protected static void stopInMain( PDebugTarget target ) throws DebugException {
		ICDILocation location = target.getPCDITarget().createLocation( "", "main", 0 ); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			target.setInternalTemporaryBreakpoint( location );
		}
		catch( DebugException e ) {
			//String message = MessageFormat.format( DebugCoreMessages.getString( "PDebugModel.0" ), new String[]{ e.getStatus().getMessage() } ); //$NON-NLS-1$
			//IStatus newStatus = new Status( IStatus.WARNING, e.getStatus().getPlugin(), ICDebugInternalConstants.STATUS_CODE_QUESTION, message, null );
			//if ( !CDebugUtils.question( newStatus, target ) ) {
			//	target.terminate();
			//	throw new DebugException( new Status( IStatus.OK, e.getStatus().getPlugin(), e.getStatus().getCode(), e.getStatus().getMessage(), null ) );
			//}
			e.printStackTrace();
		}
	}
}
