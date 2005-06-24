/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.model;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.internal.core.IPDebugInternalConstants;

/**
 * The super class of all C/C++ debug model elements.
 */
abstract public class PDebugElement extends PlatformObject implements ICDebugElement, ICDebugElementStatus {

	/**
	 * Debug target associated with this element
	 */
	private PDebugTarget fDebugTarget;

	/**
	 * The severity code of this element's status 
	 */
	private int fSeverity = ICDebugElementStatus.OK;

	/**
	 * The message of this element's status 
	 */
	private String fMessage = null;

	/**
	 * The current state of this element.
	 */
	private CDebugElementState fState = CDebugElementState.UNDEFINED;

	/**
	 * The previous state of this element.
	 */
	private CDebugElementState fOldState = CDebugElementState.UNDEFINED;

	/**
	 * The current state info.
	 */
	private Object fCurrentStateInfo = null;

	/**
	 * Constructor for CDebugElement.
	 */
	public PDebugElement( PDebugTarget target ) {
		setDebugTarget( target );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return PCDIDebugModel.getPluginIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	protected void setDebugTarget( PDebugTarget target ) {
		fDebugTarget = target;
	}

	/**
	 * Convenience method to log errors
	 */
	protected void logError( Exception e ) {
		DebugPlugin.log( e );
	}

	/**
	 * Convenience method to log errors
	 *  
	 */
	protected void logError( String message ) {
		DebugPlugin.logMessage( message, null );
	}

	/**
	 * Fires a debug event
	 * 
	 * @param event The debug event to be fired to the listeners
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	protected void fireEvent( DebugEvent event ) {
		DebugPlugin.getDefault().fireDebugEventSet( new DebugEvent[]{ event } );
	}

	protected void fireEventSet( DebugEvent[] events ) {
		DebugPlugin.getDefault().fireDebugEventSet( events );
	}

	/**
	 * Fires a debug event marking the creation of this element.
	 */
	public void fireCreationEvent() {
		fireEvent( new DebugEvent( this, DebugEvent.CREATE ) );
	}

	public DebugEvent createCreateEvent() {
		return new DebugEvent( this, DebugEvent.CREATE );
	}

	/**
	 * Fires a debug event marking the RESUME of this element with the associated detail.
	 * 
	 * @param detail The int detail of the event
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	public void fireResumeEvent( int detail ) {
		fireEvent( new DebugEvent( this, DebugEvent.RESUME, detail ) );
	}

	public DebugEvent createResumeEvent( int detail ) {
		return new DebugEvent( this, DebugEvent.RESUME, detail );
	}

	/**
	 * Fires a debug event marking the SUSPEND of this element with the associated detail.
	 * 
	 * @param detail The int detail of the event
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	public void fireSuspendEvent( int detail ) {
		fireEvent( new DebugEvent( this, DebugEvent.SUSPEND, detail ) );
	}

	public DebugEvent createSuspendEvent( int detail ) {
		return new DebugEvent( this, DebugEvent.SUSPEND, detail );
	}

	/**
	 * Fires a debug event marking the termination of this element.
	 */
	public void fireTerminateEvent() {
		fireEvent( new DebugEvent( this, DebugEvent.TERMINATE ) );
	}

	public DebugEvent createTerminateEvent() {
		return new DebugEvent( this, DebugEvent.TERMINATE );
	}

	/**
	 * Fires a debug event marking the CHANGE of this element with the specifed detail code.
	 * 
	 * @param detail
	 *            one of <code>STATE</code> or <code>CONTENT</code>
	 */
	public void fireChangeEvent( int detail ) {
		fireEvent( new DebugEvent( this, DebugEvent.CHANGE, detail ) );
	}

	public DebugEvent createChangeEvent( int detail ) {
		return new DebugEvent( this, DebugEvent.CHANGE, detail );
	}

	/**
	 * Returns the CDI session associated with this element.
	 * 
	 * @return the CDI session
	 */
	public ICDISession getCDISession() {
		return getCDITarget().getSession();
	}

	/**
	 * Returns the underlying CDI target associated with this element.
	 * 
	 * @return the underlying CDI target
	 */
	public IPCDITarget getCDITarget() {
		return (IPCDITarget)getDebugTarget().getAdapter( IPCDITarget.class );
	}

	/**
	 * Throws a new debug exception with a status code of <code>REQUEST_FAILED</code>.
	 * 
	 * @param message Failure message
	 * @param e Exception that has occurred (<code>can be null</code>)
	 * @throws DebugException The exception with a status code of <code>REQUEST_FAILED</code>
	 */
	public static void requestFailed( String message, Exception e ) throws DebugException {
		requestFailed( message, e, DebugException.REQUEST_FAILED );
	}

	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code> with the given underlying exception. 
	 * 
	 * @param message Failure message
	 * @param e underlying exception that has occurred
	 * @throws DebugException The exception with a status code of <code>TARGET_REQUEST_FAILED</code>
	 */
	public static void targetRequestFailed( String message, CDIException e ) throws DebugException {
		requestFailed( MessageFormat.format( "Target request failed: {0}.", new String[]{ message } ), e, DebugException.TARGET_REQUEST_FAILED ); //$NON-NLS-1$
	}

	/**
	 * Throws a new debug exception with the given status code.
	 * 
	 * @param message Failure message
	 * @param e Exception that has occurred (<code>can be null</code>)
	 * @param code status code
	 * @throws DebugException a new exception with given status code
	 */
	public static void requestFailed( String message, Throwable e, int code ) throws DebugException {
		throwDebugException( message, code, e );
	}

	/**
	 * Throws a new debug exception with a status code of <code>TARGET_REQUEST_FAILED</code>.
	 * 
	 * @param message Failure message
	 * @param e Throwable that has occurred
	 * @throws DebugException The exception with a status code of <code>TARGET_REQUEST_FAILED</code>
	 */
	public static void targetRequestFailed( String message, Throwable e ) throws DebugException {
		throwDebugException( MessageFormat.format( "Target request failed: {0}.", new String[]{ message } ), DebugException.TARGET_REQUEST_FAILED, e ); //$NON-NLS-1$
	}

	/**
	 * Throws a new debug exception with a status code of <code>NOT_SUPPORTED</code>.
	 * 
	 * @param message Failure message
	 * @throws DebugException The exception with a status code of <code>NOT_SUPPORTED</code>.
	 */
	public static void notSupported( String message ) throws DebugException {
		throwDebugException( message, DebugException.NOT_SUPPORTED, null );
	}

	/**
	 * Throws a debug exception with the given message, error code, and underlying exception.
	 */
	protected static void throwDebugException( String message, int code, Throwable exception ) throws DebugException {
		throw new DebugException( new Status( IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), code, message, exception ) );
	}

	protected void infoMessage( Throwable e ) {
		IStatus newStatus = new Status( IStatus.INFO, PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugInternalConstants.STATUS_CODE_INFO, e.getMessage(), null );
		CDebugUtils.info( newStatus, getDebugTarget() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( adapter.equals( IDebugElement.class ) )
			return this;
		if ( adapter.equals( ICDebugElement.class ) )
			return this;
		if ( adapter.equals( PDebugElement.class ) )
			return this;
		if ( adapter.equals( ICDebugElementStatus.class ) )
			return this;
		if ( adapter.equals( ICDISession.class ) )
			return getCDISession();
		if ( adapter.equals( IPDebugTarget.class ) )
			return getDebugTarget();
		return super.getAdapter( adapter );
	}

	protected void setStatus( int severity, String message ) {
		fSeverity = severity;
		fMessage = message;
		if ( fMessage != null )
			fMessage.trim();
	}

	protected void resetStatus() {
		fSeverity = ICDebugElementStatus.OK;
		fMessage = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElementStatus#isOK()
	 */
	public boolean isOK() {
		return (fSeverity == ICDebugElementStatus.OK);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElementStatus#getSeverity()
	 */
	public int getSeverity() {
		return fSeverity;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElementStatus#getMessage()
	 */
	public String getMessage() {
		return fMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElement#getState()
	 */
	public CDebugElementState getState() {
		return fState;
	}

	protected synchronized void setState( CDebugElementState state ) throws IllegalArgumentException {
		fOldState = fState;
		fState = state;
	}

	protected synchronized void restoreState() {
		fState = fOldState;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICDebugElement#getCurrentStateInfo()
	 */
	public Object getCurrentStateInfo() {
		return fCurrentStateInfo;
	}

	protected void setCurrentStateInfo( Object currentStateInfo ) {
		fCurrentStateInfo = currentStateInfo;
	}
}