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
package org.eclipse.ptp.debug.internal.core;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

/**
 * The breakpoint manager manages all breakpoints set to the associated 
 * debug target.
 */
public class PBreakpointManager implements IBreakpointManagerListener, ICDIEventListener, IAdaptable {

	private class BreakpointMap {

		/**
		 * Maps CBreakpoints to CDI breakpoints.
		 */
		private HashMap fCBreakpoints;

		/**
		 * Maps CDI breakpoints to CBreakpoints.
		 */
		private HashMap fCDIBreakpoints;

		protected BreakpointMap() {
			fCBreakpoints = new HashMap( 10 );
			fCDIBreakpoints = new HashMap( 10 );
		}

		protected synchronized void put( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
			fCBreakpoints.put( breakpoint, cdiBreakpoint );
			fCDIBreakpoints.put( cdiBreakpoint, breakpoint );
		}

		protected ICDIBreakpoint getCDIBreakpoint( ICBreakpoint breakpoint ) {
			return (ICDIBreakpoint)fCBreakpoints.get( breakpoint );
		}

		protected ICBreakpoint getCBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			return (ICBreakpoint)fCDIBreakpoints.get( cdiBreakpoint );
		}

		protected synchronized void removeCBreakpoint( ICBreakpoint breakpoint ) {
			if ( breakpoint != null ) {
				ICDIBreakpoint cdiBreakpoint = (ICDIBreakpoint)fCBreakpoints.remove( breakpoint );
				if ( cdiBreakpoint != null )
					fCDIBreakpoints.remove( cdiBreakpoint );
			}
		}

		protected synchronized void removeCDIBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			if ( cdiBreakpoint != null ) {
				ICBreakpoint breakpoint = (ICBreakpoint)fCDIBreakpoints.remove( cdiBreakpoint );
				if ( breakpoint != null )
					fCBreakpoints.remove( breakpoint );
			}
		}

		protected ICBreakpoint[] getAllCBreakpoints() {
			Set set = fCBreakpoints.keySet();
			return (ICBreakpoint[])set.toArray( new ICBreakpoint[set.size()] );
		}

		protected ICDIBreakpoint[] getAllCDIBreakpoints() {
			Set set = fCDIBreakpoints.keySet();
			return (ICDIBreakpoint[])set.toArray( new ICDIBreakpoint[set.size()] );
		}

		protected void dispose() {
			fCBreakpoints.clear();
			fCDIBreakpoints.clear();
		}
	}

	private PDebugTarget fDebugTarget;

	private BreakpointMap fMap;
	
	private boolean fSkipBreakpoint= false;

	public PBreakpointManager( PDebugTarget target ) {
		super();
		setDebugTarget( target );
		fMap = new BreakpointMap();
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener( this );
		getDebugTarget().getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( PBreakpointManager.class.equals( adapter ) )
			return this;
		if ( PDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( ICDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( IDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		return null;
	}

	public PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	private void setDebugTarget( PDebugTarget target ) {
		fDebugTarget = target;
	}

	protected ICDITarget getCDITarget() {
		return getDebugTarget().getCDITarget();
	}

	public void dispose() {
		getDebugTarget().getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener( this );
		removeAllBreakpoints();
		getBreakpointMap().dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source != null && source.getTarget().equals( getDebugTarget().getCDITarget() ) ) {
				if ( event instanceof ICDICreatedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointCreatedEvent( (ICDIBreakpoint)source );
				}
				else if ( event instanceof ICDIDestroyedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointDestroyedEvent( (ICDIBreakpoint)source );
				}
				else if ( event instanceof ICDIChangedEvent ) {
					if ( source instanceof ICDIBreakpoint )
						handleBreakpointChangedEvent( (ICDIBreakpoint)source );
				}
			}
		}
	}

	public boolean isTargetBreakpoint( ICBreakpoint breakpoint ) {
		IResource resource = breakpoint.getMarker().getResource();
		if ( breakpoint instanceof ICAddressBreakpoint )
			return supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
		if ( breakpoint instanceof ICLineBreakpoint ) {
			try {
				String handle = breakpoint.getSourceHandle();
				ISourceLocator sl = getSourceLocator();
				if ( sl instanceof ICSourceLocator )
					return ( ((ICSourceLocator)sl).findSourceElement( handle ) != null );
			}
			catch( CoreException e ) {
				return false;
			}
		}
		else {
			IProject project = resource.getProject();
			if ( project != null && project.exists() ) {
				ISourceLocator sl = getSourceLocator();
				if ( sl instanceof ICSourceLocator )
					return ((ICSourceLocator)sl).contains( project );
				if ( project.equals( getProject() ) )
					return true;
				return CDebugUtils.isReferencedProject( getProject(), project );
			}
		}
		return true;
	}

	public boolean isCDIRegistered( ICBreakpoint breakpoint ) {
		return (getBreakpointMap().getCDIBreakpoint( breakpoint ) != null);
	}

	public boolean supportsAddressBreakpoint( ICAddressBreakpoint breakpoint ) {
		try {
			return ( getExecFilePath().toOSString().equals( breakpoint.getSourceHandle() ) );
		}
		catch( CoreException e ) {
		}
		return false;
	}

	public IFile getCDIBreakpointFile( ICDIBreakpoint cdiBreakpoint ) {
		IBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint instanceof ICLineBreakpoint && !(breakpoint instanceof ICAddressBreakpoint) ) {
			IResource resource = ((ICLineBreakpoint)breakpoint).getMarker().getResource();
			if ( resource instanceof IFile )
				return (IFile)resource;
		}
		return null;
	}

	public ICBreakpoint getBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
		return getBreakpointMap().getCBreakpoint( cdiBreakpoint );
	}

	public IAddress getBreakpointAddress( ICLineBreakpoint breakpoint ) {
		if ( breakpoint != null ) {
			try {
				return fDebugTarget.getAddressFactory().createAddress( breakpoint.getAddress() );
			}
			catch( CoreException e ) {
			}
			catch( NumberFormatException e ) {
			}
//			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
//			if ( cdiBreakpoint instanceof ICDILocationBreakpoint ) {
//				ICDILocator locator = ((ICDILocationBreakpoint)cdiBreakpoint).getLocator();
//				if ( locator != null ) {
//					IAddressFactory factory = getDebugTarget().getAddressFactory();
//					BigInteger address = locator.getAddress();
//					if ( address != null )
//						return factory.createAddress( address );
//				}	
//			}
		}
		return fDebugTarget.getAddressFactory().getZero();
	}

	public void setBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		doSetBreakpoint( breakpoint );
	}

	protected void doSetBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		try {
			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
			if ( cdiBreakpoint == null ) {
				if ( breakpoint instanceof ICFunctionBreakpoint )
					setFunctionBreakpoint( (ICFunctionBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICAddressBreakpoint )
					setAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICLineBreakpoint )
					setLineBreakpoint( (ICLineBreakpoint)breakpoint );
				else if ( breakpoint instanceof ICWatchpoint )
					setWatchpoint( (ICWatchpoint)breakpoint );
			}
		}
		catch( CoreException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.0" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
		catch( NumberFormatException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.1" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
		catch( CDIException e ) {
			targetRequestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.2" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
	}

	public void removeBreakpoint( final ICBreakpoint breakpoint ) throws DebugException {
		doRemoveBreakpoint( breakpoint );
	}

	protected void doRemoveBreakpoint( ICBreakpoint breakpoint ) throws DebugException {
		final ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint != null ) {
			final ICDITarget cdiTarget = getCDITarget();
			DebugPlugin.getDefault().asyncExec( new Runnable() {				
				public void run() {
					try {
						cdiTarget.deleteBreakpoints( new ICDIBreakpoint[]{ cdiBreakpoint } );
					}
					catch( CDIException e ) {
					} 
				}
			} );			
		}
	}

	public void changeBreakpointProperties( final ICBreakpoint breakpoint, final IMarkerDelta delta ) throws DebugException {
		doChangeBreakpointProperties( breakpoint, delta );
	}

	protected void doChangeBreakpointProperties( ICBreakpoint breakpoint, IMarkerDelta delta ) throws DebugException {
		final ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		if ( cdiBreakpoint == null )
			return;
		ICDITarget cdiTarget = getCDITarget();
		try {
			final boolean enabled = breakpoint.isEnabled();
			boolean oldEnabled = ( delta != null ) ? delta.getAttribute( IBreakpoint.ENABLED, true ) : enabled;
			int ignoreCount = breakpoint.getIgnoreCount();
			int oldIgnoreCount = ( delta != null ) ? delta.getAttribute( ICBreakpoint.IGNORE_COUNT, 0 ) : ignoreCount;
			String condition = breakpoint.getCondition();
			String oldCondition = ( delta != null ) ? delta.getAttribute( ICBreakpoint.CONDITION, "" ) : condition; //$NON-NLS-1$
			String[] newThreadIs = getThreadNames( breakpoint );
			Boolean enabled0 = null;
			ICDICondition condition0 = null;
			if ( enabled != oldEnabled && enabled != cdiBreakpoint.isEnabled() ) {
				enabled0 = ( enabled ) ? Boolean.TRUE : Boolean.FALSE;
			}
			if ( ignoreCount != oldIgnoreCount || condition.compareTo( oldCondition ) != 0 || areThreadFiltersChanged( newThreadIs, cdiBreakpoint ) ) {
				final ICDICondition cdiCondition = cdiTarget.createCondition( ignoreCount, condition, newThreadIs  );
				if ( !cdiCondition.equals( cdiBreakpoint.getCondition() ) ) {
					condition0 = cdiCondition;
				}
			}
			if ( enabled0 != null || condition0 != null ) {
				changeBreakpointPropertiesOnTarget( cdiBreakpoint, enabled0, condition0 );
			}
		}
		catch( CoreException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.4" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
		catch( CDIException e ) {
			requestFailed( MessageFormat.format( InternalDebugCoreMessages.getString( "CBreakpointManager.4" ), new String[] { e.getMessage() } ), e ); //$NON-NLS-1$
		}
	}

	private void changeBreakpointPropertiesOnTarget( final ICDIBreakpoint breakpoint, final Boolean enabled, final ICDICondition condition ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				if ( enabled != null ) {
					try {
						breakpoint.setEnabled( enabled.booleanValue() );
					}
					catch( CDIException e ) {
					}
				}
				if ( condition != null ) {
					try {
						breakpoint.setCondition( condition );
					}
					catch( CDIException e ) {
					}
				}
			}
		} );			
	}

	private void handleBreakpointCreatedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		if ( cdiBreakpoint instanceof ICDIWatchpoint )
			doHandleWatchpointCreatedEvent( (ICDIWatchpoint)cdiBreakpoint );
		else if ( cdiBreakpoint instanceof ICDILocationBreakpoint )
			doHandleLocationBreakpointCreatedEvent( (ICDILocationBreakpoint)cdiBreakpoint );
		if ( !cdiBreakpoint.isTemporary() && !DebugPlugin.getDefault().getBreakpointManager().isEnabled() ) {
			try {
				cdiBreakpoint.setEnabled( false );
			}
			catch( CDIException e ) {
				// ignore
			}
		}
	}

	protected void doHandleLocationBreakpointCreatedEvent( ICDILocationBreakpoint cdiBreakpoint ) {
		if ( cdiBreakpoint.isTemporary() )
			return;
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint == null ) {
			breakpoint = createLocationBreakpoint( cdiBreakpoint );
		}
		if ( breakpoint != null ) {
			try {
				if ( breakpoint instanceof ICLineBreakpoint ) {
					ICDILocator locator = cdiBreakpoint.getLocator();
					if ( locator != null ) {
						BigInteger address = locator.getAddress();
						if ( address != null ) {
							((ICLineBreakpoint)breakpoint).setAddress( address.toString() );				
						}
					}
				}
			}
			catch( CoreException e1 ) {
			}
			try {
				breakpoint.setTargetFilter( getDebugTarget() );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
		}
	}

	protected void doHandleWatchpointCreatedEvent( ICDIWatchpoint cdiWatchpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiWatchpoint );
		if ( breakpoint == null ) {
			try {
				breakpoint = createWatchpoint( cdiWatchpoint );
			}
			catch( CDIException e ) {
			}
			catch( CoreException e ) {
			}
		}
		if ( breakpoint != null ) {
			try {
				breakpoint.setTargetFilter( getDebugTarget() );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
		}
	}

	private void handleBreakpointDestroyedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		getBreakpointMap().removeCDIBreakpoint( cdiBreakpoint );
		if ( breakpoint != null ) {
			if ( isFilteredByTarget( breakpoint, getDebugTarget() ) ) {
				try {
					breakpoint.removeTargetFilter( getDebugTarget() );
				}
				catch( CoreException e ) {
				}
			}
			getBreakpointNotifier().breakpointsRemoved( getDebugTarget(), new IBreakpoint[] { breakpoint } );
		}
	}

	private void handleBreakpointChangedEvent( final ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		if ( breakpoint != null ) {
			Map map = new HashMap( 3 );
			try {
				if ( !fSkipBreakpoint && DebugPlugin.getDefault().getBreakpointManager().isEnabled() ) {
						map.put( IBreakpoint.ENABLED, new Boolean( cdiBreakpoint.isEnabled() ) );
				}
				else {
					map.put( IBreakpoint.ENABLED, new Boolean( breakpoint.isEnabled() ) );
				}
			}
			catch( CDIException e ) {
			}
			catch( CoreException e ) {
			}
			try {
				map.put( ICBreakpoint.IGNORE_COUNT, new Integer( cdiBreakpoint.getCondition().getIgnoreCount() ) );
			}
			catch( CDIException e ) {
			}
			try {
				map.put( ICBreakpoint.CONDITION, cdiBreakpoint.getCondition().getExpression() );
			}
			catch( CDIException e ) {
			}
			getBreakpointNotifier().breakpointChanged( getDebugTarget(), breakpoint, map );
		}
	}

	private void removeAllBreakpoints() {
		ICDITarget cdiTarget = getCDITarget();
		try {
			cdiTarget.deleteAllBreakpoints();
		}
		catch( CDIException e ) {
			// ignore
		}
		ICBreakpoint[] breakpoints = getBreakpointMap().getAllCBreakpoints();
		getBreakpointNotifier().breakpointsRemoved( getDebugTarget(), breakpoints );
	}

	private void setLocationBreakpointOnTarget( final ICBreakpoint breakpoint, final ICDITarget target, final ICDILocation location, final ICDICondition condition, final boolean enabled ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					// FIXME: Shouldn't be doing this. The breakpoint management needs to be redesigned.
					ICDIBreakpoint cdiBreakpoint = null;
					synchronized ( getBreakpointMap() ) {
						cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
						if ( cdiBreakpoint == null ) {
							if ( breakpoint instanceof ICFunctionBreakpoint ) {
								cdiBreakpoint = target.setFunctionBreakpoint( ICDIBreakpoint.REGULAR,
										(ICDIFunctionLocation)location, condition, true );								
							} else if ( breakpoint instanceof ICAddressBreakpoint ) {
								cdiBreakpoint = target.setAddressBreakpoint( ICDIBreakpoint.REGULAR,
										(ICDIAddressLocation)location, condition, true );
								
							} else if ( breakpoint instanceof ICLineBreakpoint ) {
								cdiBreakpoint = target.setLineBreakpoint( ICDIBreakpoint.REGULAR,
										(ICDILineLocation)location, condition, true );
							}
							getBreakpointMap().put( breakpoint, cdiBreakpoint );
						}
					}
					if ( cdiBreakpoint != null && !enabled ) {
						cdiBreakpoint.setEnabled( false );
					}
				}
				catch( CDIException e ) {
				} 
			}
		} );
	}

	private void setFunctionBreakpoint( ICFunctionBreakpoint breakpoint ) throws CDIException, CoreException {
		final boolean enabled = breakpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		String function = breakpoint.getFunction();
		String fileName = breakpoint.getFileName();
		final ICDIFunctionLocation location = cdiTarget.createFunctionLocation( fileName, function );
		final ICDICondition condition = createCondition( breakpoint );
		setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
	}

	private void setAddressBreakpoint( ICAddressBreakpoint breakpoint ) throws CDIException, CoreException, NumberFormatException {
		final boolean enabled = breakpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		String address = breakpoint.getAddress();
		if ( address.startsWith( "0x" ) ) { //$NON-NLS-1$
			final ICDIAddressLocation location = cdiTarget.createAddressLocation( new BigInteger ( breakpoint.getAddress().substring( 2 ), 16 ) );
			final ICDICondition condition = createCondition( breakpoint );
			setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
		}
	}

	private void setLineBreakpoint( ICLineBreakpoint breakpoint ) throws CDIException, CoreException {
		boolean enabled = breakpoint.isEnabled();
		ICDITarget cdiTarget = getCDITarget();
		String handle = breakpoint.getSourceHandle();
		IPath path = convertPath( handle );
		ICDILineLocation location = cdiTarget.createLineLocation( path.toPortableString(), breakpoint.getLineNumber() );
		ICDICondition condition = createCondition( breakpoint );
		setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
	}

	private void setWatchpointOnTarget( final ICWatchpoint watchpoint, final ICDITarget target, final int accessType, final String expression, final ICDICondition condition, final boolean enabled ) {
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					ICDIWatchpoint cdiWatchpoint = null;
					synchronized ( getBreakpointMap() ) {
						if ( getBreakpointMap().getCDIBreakpoint( watchpoint ) == null ) {
							cdiWatchpoint = target.setWatchpoint( ICDIBreakpoint.REGULAR, accessType, expression, condition );
							getBreakpointMap().put( watchpoint, cdiWatchpoint );
						}
					}
					if ( !enabled ) {
						cdiWatchpoint.setEnabled( false );
					}
				}
				catch( CDIException e ) {
				} 
			}
		} );
	}

	private void setWatchpoint( ICWatchpoint watchpoint ) throws CDIException, CoreException {
		final boolean enabled = watchpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		int accessType = 0;
		accessType |= (watchpoint.isWriteType()) ? ICDIWatchpoint.WRITE : 0;
		accessType |= (watchpoint.isReadType()) ? ICDIWatchpoint.READ : 0;
		final int accessType1 = accessType;
		final String expression = watchpoint.getExpression();
		final ICDICondition condition = createCondition( watchpoint );
		setWatchpointOnTarget( watchpoint, cdiTarget, accessType1, expression, condition, enabled );
	}

	protected BreakpointMap getBreakpointMap() {
		return fMap;
	}

	protected void targetRequestFailed( String message, Throwable e ) throws DebugException {
		requestFailed0( message, e, DebugException.TARGET_REQUEST_FAILED );
	}

	protected void requestFailed( String message, Throwable e ) throws DebugException {
		requestFailed0( message, e, DebugException.REQUEST_FAILED );
	}

	private void requestFailed0( String message, Throwable e, int code ) throws DebugException {
		throw new DebugException( new Status( IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), code, message, e ) );
	}

	private ICLineBreakpoint createLocationBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) {
		ICLineBreakpoint breakpoint = null;
		try {
			ICDILocator location = cdiBreakpoint.getLocator();
			if ( !isEmpty( location.getFile() ) ) {
				ISourceLocator locator = getSourceLocator();
			}
			else if ( !isEmpty( location.getFunction() ) ) {
				breakpoint = createFunctionBreakpoint( cdiBreakpoint );
			}
			else if ( !location.getAddress().equals( BigInteger.ZERO ) ) {
				breakpoint = createAddressBreakpoint( cdiBreakpoint );
			}
		}
		catch( CDIException e ) {
		}
		catch( CoreException e ) {
		}
		return breakpoint;
	}

	private ICLineBreakpoint createLineBreakpoint( String sourceHandle, IResource resource, ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		ICLineBreakpoint breakpoint = PCDIDebugModel.createLineBreakpoint( sourceHandle, 
																		  resource, 
																		  cdiBreakpoint.getLocator().getLineNumber(), 
																		  cdiBreakpoint.isEnabled(), 
																		  cdiBreakpoint.getCondition().getIgnoreCount(), 
																		  cdiBreakpoint.getCondition().getExpression(), 
																		  false );
		ICDILocator locator = cdiBreakpoint.getLocator();
		if ( locator != null ) {
			BigInteger address = locator.getAddress();
			if ( address != null ) {
				breakpoint.setAddress( address.toString() );				
			}
		}
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICFunctionBreakpoint createFunctionBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		IPath execFile = getExecFilePath();
		String sourceHandle = execFile.toOSString();
		ICFunctionBreakpoint breakpoint = PCDIDebugModel.createFunctionBreakpoint( sourceHandle, 
																				  getProject(), 
																				  cdiBreakpoint.getLocator().getFunction(),
																				  -1,
																				  -1,
																				  -1,
																				  cdiBreakpoint.isEnabled(), 
																				  cdiBreakpoint.getCondition().getIgnoreCount(), 
																				  cdiBreakpoint.getCondition().getExpression(), 
																				  false );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICAddressBreakpoint createAddressBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		IPath execFile = getExecFilePath();
		String sourceHandle = execFile.toOSString();
		IAddress address = getDebugTarget().getAddressFactory().createAddress( cdiBreakpoint.getLocator().getAddress() );
		ICAddressBreakpoint breakpoint = PCDIDebugModel.createAddressBreakpoint( sourceHandle, 
																				getProject(), 
																				address, 
																				cdiBreakpoint.isEnabled(), 
																				cdiBreakpoint.getCondition().getIgnoreCount(), 
																				cdiBreakpoint.getCondition().getExpression(), 
																				false );
		getBreakpointMap().put( breakpoint, cdiBreakpoint );
		((CBreakpoint)breakpoint).register( true );
		return breakpoint;
	}

	private ICWatchpoint createWatchpoint( ICDIWatchpoint cdiWatchpoint ) throws CDIException, CoreException {
		IPath execFile = getExecFilePath();
		String sourceHandle = execFile.toOSString();
		ICWatchpoint watchpoint = PCDIDebugModel.createWatchpoint( sourceHandle, 
																  getProject(), 
																  cdiWatchpoint.isWriteType(), 
																  cdiWatchpoint.isReadType(), 
																  cdiWatchpoint.getWatchExpression(), 
																  cdiWatchpoint.isEnabled(), 
																  cdiWatchpoint.getCondition().getIgnoreCount(), 
																  cdiWatchpoint.getCondition().getExpression(), 
																  false );
		getBreakpointMap().put( watchpoint, cdiWatchpoint );
		((CBreakpoint)watchpoint).register( true );
		return watchpoint;
	}

	private ISourceLocator getSourceLocator() {
		return getDebugTarget().getLaunch().getSourceLocator();
	}

	private IProject getProject() {
		return getDebugTarget().getProject();
	}

	private IPath getExecFilePath() {
		return getDebugTarget().getExecFile().getPath();
	}

	private PBreakpointNotifier getBreakpointNotifier() {
		return PBreakpointNotifier.getInstance();
	}

	private boolean isEmpty( String str ) {
		return !( str != null && str.trim().length() > 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
	 */
	public void breakpointManagerEnablementChanged( boolean enabled ) {
		doSkipBreakpoints( !enabled );
	}

	public void skipBreakpoints( boolean enabled ) {
		if ( fSkipBreakpoint != enabled && (DebugPlugin.getDefault().getBreakpointManager().isEnabled() || !enabled) ) {
			fSkipBreakpoint = enabled;
			doSkipBreakpoints( enabled );
		}
	}

	private void doSkipBreakpoints( boolean enabled ) {
		ICBreakpoint[] cBreakpoints = getBreakpointMap().getAllCBreakpoints();
		for ( int i = 0; i < cBreakpoints.length; ++i ) {
			try {
				if ( cBreakpoints[i].isEnabled() ) {
					ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( cBreakpoints[i] );
					if ( cdiBreakpoint != null ) {
						cdiBreakpoint.setEnabled( !enabled );
					}
				}
			}
			catch( CoreException e ) {
				// ignore
			}
			catch( CDIException e ) {
				// ignore
			}
		}
	}

	private boolean isFilteredByTarget( ICBreakpoint breakpoint, ICDebugTarget target ) {
		boolean result = false;
		try {
			ICDebugTarget[] tfs = breakpoint.getTargetFilters();
			result = Arrays.asList( tfs ).contains( target );
		}
		catch( CoreException e ) {
			// ignore
		}
		return result;
	}

	private boolean areThreadFiltersChanged( String[] newIds, ICDIBreakpoint cdiBreakpoint ) {
		try {
			String[] oldIds = cdiBreakpoint.getCondition().getThreadIds();
			if ( oldIds.length != newIds.length )
				return true;
			List list = Arrays.asList( oldIds );
			for ( int i = 0; i < newIds.length; ++i ) {
				if ( !list.contains( newIds[i] ) ) {
					return true;
				}
			}
		}
		catch( CDIException e ) {
		}
		return false;
	}

	private String[] getThreadNames( ICBreakpoint breakpoint ) {
		try {
			ICThread[] threads = breakpoint.getThreadFilters( getDebugTarget() );
			if ( threads == null )
				return new String[0];				
			String[] names = new String[threads.length];
			for ( int i = 0; i < threads.length; ++i ) {
				names[i] = threads[i].getName();
			}
			return names;
		}
		catch( DebugException e ) {
		}
		catch( CoreException e ) {
		}
		return new String[0];
	}

	private ICDICondition createCondition( ICBreakpoint breakpoint ) throws CoreException, CDIException {
		return getCDITarget().createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition(), getThreadNames( breakpoint ) );
	}

	private IPath convertPath( String sourceHandle ) {
		IPath path = null;
		if ( Path.EMPTY.isValidPath( sourceHandle ) ) {
			ISourceLocator sl = getSourceLocator();
			if ( path == null ) {
				path = new Path( sourceHandle );
			}
		}
		return path;
	}
}
