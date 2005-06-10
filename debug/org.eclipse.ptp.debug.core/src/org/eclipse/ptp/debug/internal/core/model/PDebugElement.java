package org.eclipse.ptp.debug.internal.core.model;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;

abstract public class PDebugElement extends PlatformObject implements ICDebugElement, ICDebugElementStatus {

	private PDebugTarget fDebugTarget;
	
	/**
	 * The current state of this element.
	 */
	private CDebugElementState fState = CDebugElementState.UNDEFINED;

	/**
	 * The previous state of this element.
	 */
	private CDebugElementState fOldState = CDebugElementState.UNDEFINED;
	
	public static void targetRequestFailed( String message, CDIException e ) throws DebugException {
		requestFailed( MessageFormat.format( "Target request failed: {0}.", new String[]{ message } ), e, DebugException.TARGET_REQUEST_FAILED ); //$NON-NLS-1$
	}

	protected static void throwDebugException( String message, int code, Throwable exception ) throws DebugException {
		throw new DebugException( new Status( IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), code, message, exception ) );
	}
	
	public static void requestFailed( String message, Throwable e, int code ) throws DebugException {
		throwDebugException( message, code, e );
	}
	
	public IPCDISession getPCDISession() {
		return (IPCDISession) getPCDITarget().getSession();
	}
	
	public IPCDITarget getPCDITarget() {
		return (IPCDITarget)getDebugTarget().getAdapter( IPCDITarget.class );
	}
	
	public String getModelIdentifier() {
		return PCDIDebugModel.getPluginIdentifier();
	}
	
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	
	protected void setDebugTarget( PDebugTarget target ) {
		fDebugTarget = target;
	}
	
	protected synchronized void setState( CDebugElementState state ) throws IllegalArgumentException {
		fOldState = fState;
		fState = state;
	}
	
	public DebugEvent createCreateEvent() {
		return new DebugEvent( this, DebugEvent.CREATE );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElement#getState()
	 */
	public CDebugElementState getState() {
		return fState;
	}
	
	protected void fireEventSet( DebugEvent[] events ) {
		DebugPlugin.getDefault().fireDebugEventSet( events );
	}
}
