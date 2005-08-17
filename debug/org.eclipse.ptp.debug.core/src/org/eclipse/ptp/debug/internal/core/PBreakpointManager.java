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
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.ptp.debug.internal.core; 

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
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
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceLookupDirector;

public class PBreakpointManager implements IBreakpointsListener, IBreakpointManagerListener, ICDIEventListener, IAdaptable {

	static private class BreakpointInProgess {
		
		private boolean fDeleted = false;
		
		boolean isDeleted() {
			return fDeleted;
		}
		
		void delete() {
			fDeleted = true;
		}
	}

	static final protected BreakpointInProgess BREAKPOINT_IN_PROGRESS = new BreakpointInProgess(); 

	private class BreakpointMap {

		/**
		 * Maps CBreakpoints to CDI breakpoints.
		 */
		private HashMap fCBreakpoints;

		/**
		 * Maps CDI breakpoints to CBreakpoints.
		 */
		private HashMap fCDIBreakpoints;

		private BreakpointMap() {
			fCBreakpoints = new HashMap( 10 );
			fCDIBreakpoints = new HashMap( 10 );
		}

		void register( ICBreakpoint breakpoint ) {
			fCBreakpoints.put( breakpoint, BREAKPOINT_IN_PROGRESS );
		}

		void put( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
			fCBreakpoints.put( breakpoint, cdiBreakpoint );
			fCDIBreakpoints.put( cdiBreakpoint, breakpoint );
		}

		ICDIBreakpoint getCDIBreakpoint( ICBreakpoint breakpoint ) {
			Object b = fCBreakpoints.get( breakpoint );
			return ( b instanceof ICDIBreakpoint ) ? (ICDIBreakpoint)b : null;
		}

		ICBreakpoint getCBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			ICBreakpoint breakpoint = (ICBreakpoint)fCDIBreakpoints.get( cdiBreakpoint );
			if ( breakpoint == null ) {
				ICBreakpoint[] bip = getBreakpointsInProgress();
				for ( int i = 0; i < bip.length; ++i ) {
					if ( isSameBreakpoint( bip[i], cdiBreakpoint ) ) {
						breakpoint = bip[i];
						break;
					}
				}
			}
			return breakpoint;
		}

		void removeCDIBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
			if ( cdiBreakpoint != null ) {
				ICBreakpoint breakpoint = (ICBreakpoint)fCDIBreakpoints.remove( cdiBreakpoint );
				if ( breakpoint != null )
					fCBreakpoints.remove( breakpoint );
			}
		}

		boolean isRegistered( ICBreakpoint breakpoint ) {
			return ( fCBreakpoints.get( breakpoint ) != null );
		}

		boolean isInProgress( ICBreakpoint breakpoint ) {
			return ( fCBreakpoints.get( breakpoint ) == BREAKPOINT_IN_PROGRESS );
		}

		ICBreakpoint[] getAllCBreakpoints() {
			Set set = fCBreakpoints.keySet();
			return (ICBreakpoint[])set.toArray( new ICBreakpoint[set.size()] );
		}

		void dispose() {
			fCBreakpoints.clear();
			fCDIBreakpoints.clear();
		}

		private ICBreakpoint[] getBreakpointsInProgress() {
			ArrayList list = new ArrayList();
			Set set = fCBreakpoints.entrySet();
			Iterator it = set.iterator();
			while ( it.hasNext() ) {
				Map.Entry entry = (Map.Entry)it.next();
				if ( entry.getValue() == BREAKPOINT_IN_PROGRESS ) {
					list.add( entry.getKey() );
				}
			}
			return (ICBreakpoint[])list.toArray( new ICBreakpoint[list.size()] );
		}

		private boolean isSameBreakpoint( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
			try {
				if ( breakpoint instanceof ICFunctionBreakpoint && cdiBreakpoint instanceof ICDIFunctionBreakpoint ) {
					return ( ((ICFunctionBreakpoint)breakpoint).getFunction().compareTo( ((ICDIFunctionBreakpoint)cdiBreakpoint).getLocator().getFunction() ) == 0 );
				}
				if ( breakpoint instanceof ICAddressBreakpoint && cdiBreakpoint instanceof ICDIAddressBreakpoint ) {
					IAddressFactory factory = getDebugTarget().getAddressFactory(); 
					return factory.createAddress( ((ICAddressBreakpoint)breakpoint).getAddress() ).equals( factory.createAddress( ((ICDIAddressBreakpoint)cdiBreakpoint).getLocator().getAddress() ) );
				}
				if ( breakpoint instanceof ICLineBreakpoint && cdiBreakpoint instanceof ICDILineBreakpoint ) {
					ICDILocator location = ((ICDILineBreakpoint)cdiBreakpoint).getLocator();
					String file = location.getFile();
					String sourceHandle = file;
					if ( !isEmpty( file ) ) {
						Object sourceElement = getSourceElement( file );
						sourceHandle = ( sourceElement instanceof IFile ) ? ((IFile)sourceElement).getLocation().toOSString() : ((IStorage)sourceElement).getFullPath().toOSString();
						return sourceHandle.equals( ((ICLineBreakpoint)breakpoint).getSourceHandle() ) && location.getLineNumber() == ((ICLineBreakpoint)breakpoint).getLineNumber(); 
					}
				}
			}
			catch( CoreException e ) {
			}
			return false;
		}
	}

	private PDebugTarget fDebugTarget;

	private BreakpointMap fMap;
	
	private boolean fSkipBreakpoint = false;

	public PBreakpointManager( PDebugTarget target ) {
		super();
		fDebugTarget = target;
		fMap = new BreakpointMap();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded( IBreakpoint[] breakpoints ) {
		if ( !isTargetAvailable() )
			return;
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( breakpoints[i] instanceof ICBreakpoint && isTargetBreakpoint( (ICBreakpoint)breakpoints[i] ) )
				breakpointAdded0( breakpoints[i] );			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		if ( !isTargetAvailable() )
			return;
		ArrayList list = new ArrayList( breakpoints.length );
		synchronized( getBreakpointMap() ) {
			for ( int i = 0; i < breakpoints.length; ++i ) {
				if ( breakpoints[i] instanceof ICBreakpoint && !getBreakpointMap().isInProgress( (ICBreakpoint)breakpoints[i] ) )
					list.add( getBreakpointMap().getCDIBreakpoint( (ICBreakpoint)breakpoints[i] ) );			
			}
		}
		if ( list.isEmpty() )
			return;
		final ICDIBreakpoint[] cdiBreakpoints = (ICDIBreakpoint[])list.toArray( new ICDIBreakpoint[list.size()] );
		final ICDITarget cdiTarget = getCDITarget();
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					cdiTarget.deleteBreakpoints( cdiBreakpoints );
				}
				catch( CDIException e ) {
				} 
			}
		} );			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		ArrayList removeList = new ArrayList( breakpoints.length );
		ArrayList installList = new ArrayList( breakpoints.length );
		synchronized ( getBreakpointMap() ) {
			for ( int i = 0; i < breakpoints.length; ++i ) {
				if ( !(breakpoints[i] instanceof ICBreakpoint) || !isTargetAvailable() )
					continue;
				ICBreakpoint b = (ICBreakpoint)breakpoints[i];
				boolean install = false;
				try {
					IPDebugTarget[] tfs = (IPDebugTarget[]) b.getTargetFilters();
					install = Arrays.asList( tfs ).contains( getDebugTarget() );
				}
				catch( CoreException e ) {
				}
				boolean registered = getBreakpointMap().isRegistered( b );
				boolean inProgress = getBreakpointMap().isInProgress( b );
				if ( registered && !inProgress && !install ) {
					removeList.add( b );
				}
				if ( !registered && install ) {
					installList.add( b );
				}
			}
		}
		breakpointsRemoved( (ICBreakpoint[])removeList.toArray( new ICBreakpoint[removeList.size()] ), new IMarkerDelta[0] );
		breakpointsAdded( (ICBreakpoint[])installList.toArray( new ICBreakpoint[removeList.size()] ) );
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( !(breakpoints[i] instanceof ICBreakpoint) || !isTargetAvailable() )
				changeBreakpointProperties( (ICBreakpoint)breakpoints[i], deltas[i] );
		}
	}

	public void breakpointManagerEnablementChanged( boolean enabled ) {
		doSkipBreakpoints( !enabled );
	}

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

	public Object getAdapter( Class adapter ) {
		if ( PBreakpointManager.class.equals( adapter ) )
			return this;
		if ( PDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( IPDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( IDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		return null;
	}

	public PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	public void initialize() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener( this );
		getDebugTarget().getCDISession().getEventManager().addEventListener( this );
	}

	public void dispose() {
		getDebugTarget().getCDISession().getEventManager().removeEventListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener( this );
		removeAllBreakpoints();
		getBreakpointMap().dispose();
	}

	public IAddress getBreakpointAddress( ICLineBreakpoint breakpoint ) {
		BigInteger address = null;
		synchronized ( getBreakpointMap() ) {
			ICDIBreakpoint cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
			if ( cdiBreakpoint instanceof ICDILocationBreakpoint ) {
				ICDILocator locator = ((ICDILocationBreakpoint)cdiBreakpoint).getLocator();
				if ( locator != null ) {
					address = locator.getAddress();
				}
			}
		}
		return ( address != null ) ? getDebugTarget().getAddressFactory().createAddress( address ) : null;
	}

	public IBreakpoint getBreakpoint( ICDIBreakpoint cdiBreakpoint ) {
		Object b = null;
		synchronized ( getBreakpointMap() ) {
			b = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
		}
		return ( b instanceof IBreakpoint ) ? (IBreakpoint)b : null; 
	}

	private void handleBreakpointCreatedEvent( ICDIBreakpoint cdiBreakpoint ) {
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

	private void doHandleLocationBreakpointCreatedEvent( ICDILocationBreakpoint cdiBreakpoint ) {
		if ( cdiBreakpoint.isTemporary() )
			return;
		ICBreakpoint breakpoint = null;
		synchronized( getBreakpointMap() ) {
			breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
			if ( breakpoint == null ) {
				breakpoint = createLocationBreakpoint( cdiBreakpoint );
			}
			if ( breakpoint != null )
				getBreakpointMap().put( breakpoint, cdiBreakpoint );
		}

		if ( breakpoint != null ) {
//			try {
//				if ( breakpoint instanceof ICLineBreakpoint ) {
//					ICDILocator locator = cdiBreakpoint.getLocator();
//					if ( locator != null ) {
//						IAddress address = getDebugTarget().getAddressFactory().createAddress( locator.getAddress() );
//						if ( address != null ) {
//							((ICLineBreakpoint)breakpoint).setAddress( address.toHexAddressString() );				
//						}
//					}
//				}
//			}
//			catch( CoreException e1 ) {
//			}
			
			try {
				breakpoint.setTargetFilter( getDebugTarget() );
				((CBreakpoint)breakpoint).register( true );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
			changeBreakpointProperties( breakpoint, cdiBreakpoint );
		}
	}

	private void doHandleWatchpointCreatedEvent( ICDIWatchpoint cdiWatchpoint ) {
		ICBreakpoint breakpoint = null;
		synchronized( getBreakpointMap() ) {
			breakpoint = getBreakpointMap().getCBreakpoint( cdiWatchpoint );
			if ( breakpoint == null ) {
				try {
					breakpoint = createWatchpoint( cdiWatchpoint );
				}
				catch( CDIException e ) {
				}
				catch( CoreException e ) {
				}
			}
			if ( breakpoint != null )
				getBreakpointMap().put( breakpoint, cdiWatchpoint );
		}

		if ( breakpoint != null ) {
			try {
				breakpoint.setTargetFilter( getDebugTarget() );
				((CBreakpoint)breakpoint).register( true );
			}
			catch( CoreException e ) {
			}
			getBreakpointNotifier().breakpointInstalled( getDebugTarget(), breakpoint );
			changeBreakpointProperties( breakpoint, cdiWatchpoint );
		}
	}

	private void handleBreakpointChangedEvent( ICDIBreakpoint cdiBreakpoint ) {
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

	private void handleBreakpointDestroyedEvent( ICDIBreakpoint cdiBreakpoint ) {
		ICBreakpoint breakpoint = null;
		synchronized( getBreakpointMap() ) {
			breakpoint = getBreakpointMap().getCBreakpoint( cdiBreakpoint );
			getBreakpointMap().removeCDIBreakpoint( cdiBreakpoint );
		}
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

	private BreakpointMap getBreakpointMap() {
		return fMap;
	}

	private void removeAllBreakpoints() {
		ArrayList list = new ArrayList();
		ICBreakpoint[] breakpoints = new ICBreakpoint[0];
		synchronized( getBreakpointMap() ) {
			breakpoints = getBreakpointMap().getAllCBreakpoints();			
			for ( int i = 0; i < breakpoints.length; ++i ) {
				if ( !getBreakpointMap().isInProgress( breakpoints[i] ) )
					list.add( getBreakpointMap().getCDIBreakpoint( breakpoints[i] ) );			
			}
		}
		if ( list.isEmpty() )
			return;
		final ICDIBreakpoint[] cdiBreakpoints = (ICDIBreakpoint[])list.toArray( new ICDIBreakpoint[list.size()] );
		final ICDITarget cdiTarget = getCDITarget();
		DebugPlugin.getDefault().asyncExec( new Runnable() {				
			public void run() {
				try {
					cdiTarget.deleteBreakpoints( cdiBreakpoints );
				}
				catch( CDIException e ) {
				} 
			}
		} );			
		getBreakpointNotifier().breakpointsRemoved( getDebugTarget(), breakpoints );
	}

	private void setBreakpoint( ICBreakpoint breakpoint ) {
		try {
			if ( !getBreakpointMap().isRegistered( breakpoint ) ) {
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
		}
		catch( NumberFormatException e ) {
		}
		catch( CDIException e ) {
		}
	}

	private void setLocationBreakpointOnTarget( final ICBreakpoint breakpoint, final ICDITarget target, final ICDILocation location, final ICDICondition condition, final boolean enabled ) {
		boolean registered = false;
		synchronized ( getBreakpointMap() ) {
			if ( !(registered = getBreakpointMap().isRegistered( breakpoint )) ) {
				getBreakpointMap().register( breakpoint );
			}
		}
		if ( !registered ) {
			DebugPlugin.getDefault().asyncExec( new Runnable() {				
				public void run() {
					try {
						if ( breakpoint instanceof ICFunctionBreakpoint ) {
							target.setFunctionBreakpoint( ICDIBreakpoint.REGULAR,
									(ICDIFunctionLocation)location, condition, true );								
						} else if ( breakpoint instanceof ICAddressBreakpoint ) {
							target.setAddressBreakpoint( ICDIBreakpoint.REGULAR,
									(ICDIAddressLocation)location, condition, true );
							
						} else if ( breakpoint instanceof ICLineBreakpoint ) {
							target.setLineBreakpoint( ICDIBreakpoint.REGULAR,
									(ICDILineLocation)location, condition, true );
						}
					}
					catch( CDIException e ) {
					} 
				}
			} );
		}
	}

	private void setFunctionBreakpoint( ICFunctionBreakpoint breakpoint ) throws CDIException, CoreException {
		boolean enabled = breakpoint.isEnabled();
		ICDITarget cdiTarget = getCDITarget();
		String function = breakpoint.getFunction();
		String fileName = breakpoint.getFileName();
		ICDIFunctionLocation location = cdiTarget.createFunctionLocation( fileName, function );
		ICDICondition condition = createCondition( breakpoint );
		setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
	}

	private void setAddressBreakpoint( ICAddressBreakpoint breakpoint ) throws CDIException, CoreException, NumberFormatException {
		final boolean enabled = breakpoint.isEnabled();
		final ICDITarget cdiTarget = getCDITarget();
		String address = breakpoint.getAddress();
		final ICDIAddressLocation location = cdiTarget.createAddressLocation( new BigInteger ( ( address.startsWith( "0x" ) ) ? address.substring( 2 ) : address, 16 ) ); //$NON-NLS-1$
		final ICDICondition condition = createCondition( breakpoint );
		setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
	}

	private void setLineBreakpoint( ICLineBreakpoint breakpoint ) throws CDIException, CoreException {
		boolean enabled = breakpoint.isEnabled();
		ICDITarget cdiTarget = getCDITarget();
		String handle = breakpoint.getSourceHandle();
		IPath path = convertPath( handle );
		ICDILineLocation location = cdiTarget.createLineLocation( path.lastSegment()/*path.toPortableString()*/, breakpoint.getLineNumber() );
		ICDICondition condition = createCondition( breakpoint );
		setLocationBreakpointOnTarget( breakpoint, cdiTarget, location, condition, enabled );
	}

	private void setWatchpointOnTarget( final ICWatchpoint watchpoint, final ICDITarget target, final int accessType, final String expression, final ICDICondition condition, final boolean enabled ) {
		boolean registered = false;
		synchronized ( getBreakpointMap() ) {
			if ( !(registered = getBreakpointMap().isRegistered( watchpoint )) ) {
				getBreakpointMap().register( watchpoint );
			}
		}
		if ( !registered ) {
			DebugPlugin.getDefault().asyncExec( new Runnable() {				
				public void run() {
					try {
							target.setWatchpoint( ICDIBreakpoint.REGULAR, accessType, expression, condition );
					}
					catch( CDIException e ) {
					} 
				}
			} );
		}
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

	protected ICDITarget getCDITarget() {
		return getDebugTarget().getCDITarget();
	}

	private ICDICondition createCondition( ICBreakpoint breakpoint ) throws CoreException, CDIException {
		return getCDITarget().createCondition( breakpoint.getIgnoreCount(), breakpoint.getCondition(), getThreadNames( breakpoint ) );
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

	private ICLineBreakpoint createLocationBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) {
		ICLineBreakpoint breakpoint = null;
		try {
			ICDILocator location = cdiBreakpoint.getLocator();
			String file = location.getFile();
			if ( !isEmpty( file ) ) {
				Object sourceElement = getSourceElement( file );
				String sourceHandle = file;
				IResource resource = getProject();
				if ( sourceElement instanceof IFile || sourceElement instanceof IStorage ) {
					sourceHandle = ( sourceElement instanceof IFile ) ? ((IFile)sourceElement).getLocation().toOSString() : ((IStorage)sourceElement).getFullPath().toOSString();
					resource = ( sourceElement instanceof IFile ) ? (IResource)sourceElement : ResourcesPlugin.getWorkspace().getRoot();
				}
				breakpoint = createLineBreakpoint( sourceHandle, resource, cdiBreakpoint );
//				else if ( !isEmpty( cdiBreakpoint.getLocation().getFunction() ) ) {
//					breakpoint = createFunctionBreakpoint( cdiBreakpoint );
//				}
//				else if ( ! cdiBreakpoint.getLocation().getAddress().equals( BigInteger.ZERO ) ) {
//					breakpoint = createAddressBreakpoint( cdiBreakpoint );
//				}
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
//		ICDILocator locator = cdiBreakpoint.getLocator();
//		if ( locator != null ) {
//			BigInteger address = locator.getAddress();
//			if ( address != null ) {
//				breakpoint.setAddress( address.toString() );				
//			}
//		}
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
		return breakpoint;
	}

	private ICAddressBreakpoint createAddressBreakpoint( ICDILocationBreakpoint cdiBreakpoint ) throws CDIException, CoreException {
		IPath execFile = getExecFilePath();
		String sourceHandle = execFile.toOSString();
		IAddress address = getDebugTarget().getAddressFactory().createAddress( cdiBreakpoint.getLocator().getAddress() );
		ICAddressBreakpoint breakpoint = PCDIDebugModel.createAddressBreakpoint( sourceHandle,
																				sourceHandle, 
																				ResourcesPlugin.getWorkspace().getRoot(), 
																				address, 
																				cdiBreakpoint.isEnabled(), 
																				cdiBreakpoint.getCondition().getIgnoreCount(), 
																				cdiBreakpoint.getCondition().getExpression(), 
																				false );
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
		return watchpoint;
	}

	private void changeBreakpointProperties( ICBreakpoint breakpoint, IMarkerDelta delta ) {
		ICDIBreakpoint cdiBreakpoint = null;
		synchronized( getBreakpointMap() ) {
			if ( !getBreakpointMap().isInProgress( breakpoint ) )
				cdiBreakpoint = getBreakpointMap().getCDIBreakpoint( breakpoint );
		}
		if ( cdiBreakpoint == null )
			return;
		ICDITarget cdiTarget = getCDITarget();
		try {
			boolean enabled = breakpoint.isEnabled();
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
				ICDICondition cdiCondition = cdiTarget.createCondition( ignoreCount, condition, newThreadIs  );
				if ( !cdiCondition.equals( cdiBreakpoint.getCondition() ) ) {
					condition0 = cdiCondition;
				}
			}
			if ( enabled0 != null || condition0 != null ) {
				changeBreakpointPropertiesOnTarget( cdiBreakpoint, enabled0, condition0 );
			}
		}
		catch( CoreException e ) {
		}
		catch( CDIException e ) {
		}
	}

	private void changeBreakpointProperties( ICBreakpoint breakpoint, ICDIBreakpoint cdiBreakpoint ) {
		Boolean enabled = null;
		try {
			if ( cdiBreakpoint.isEnabled() != !breakpoint.isEnabled() )
				enabled = Boolean.valueOf( breakpoint.isEnabled() );
		}
		catch( CDIException e ) {
		}
		catch( CoreException e ) {
		}
		ICDICondition condition = null;
		try {
			ICDICondition c = createCondition( breakpoint );
			if ( !cdiBreakpoint.getCondition().equals( c ) )
				condition = c;
		}
		catch( CDIException e ) {
		}
		catch( CoreException e ) {
		}
		if ( enabled != null || condition != null )
			changeBreakpointPropertiesOnTarget( cdiBreakpoint, enabled, condition );
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

	public void setBreakpoints() {
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] bps = manager.getBreakpoints( PCDIDebugModel.getPluginIdentifier() );
		for( int i = 0; i < bps.length; i++ ) {
			if ( bps[i] instanceof ICBreakpoint && isTargetBreakpoint( (ICBreakpoint)bps[i] ) && !getBreakpointMap().isRegistered( (ICBreakpoint)bps[i] ) ) {
				if ( bps[i] instanceof ICAddressBreakpoint ) {
					// disable address breakpoints to prevent the debugger to insert them prematurely
					try {
						bps[i].setEnabled( false );
					}
					catch( CoreException e ) {
					}
				}
				breakpointAdded0( bps[i] );
			}
		}
	}

	private void breakpointAdded0( IBreakpoint breakpoint ) {
		if ( !isTargetAvailable() )
			return;
		if ( breakpoint instanceof ICAddressBreakpoint && !supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint ) )
			return;
		setBreakpoint( (ICBreakpoint)breakpoint );
	}

	private boolean isTargetBreakpoint( ICBreakpoint breakpoint ) {
		IResource resource = breakpoint.getMarker().getResource();
		if ( breakpoint instanceof ICAddressBreakpoint )
			return supportsAddressBreakpoint( (ICAddressBreakpoint)breakpoint );
		if ( breakpoint instanceof ICLineBreakpoint ) {
			try {
				String handle = breakpoint.getSourceHandle();
				ISourceLocator sl = getSourceLocator();
				if ( sl instanceof ICSourceLocator )
					return ( ((ICSourceLocator)sl).findSourceElement( handle ) != null );
				else if ( sl instanceof CSourceLookupDirector ) {
					return ( ((CSourceLookupDirector)sl).contains( breakpoint ) );
				}
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
				else if ( sl instanceof CSourceLookupDirector )
					return ((CSourceLookupDirector)sl).contains( project );
				if ( project.equals( getProject() ) )
					return true;
				return CDebugUtils.isReferencedProject( getProject(), project );
			}
		}
		return true;
	}

	public boolean supportsBreakpoint( ICBreakpoint breakpoint ) {
		boolean s = false;
		synchronized( getBreakpointMap() ) {
			s = getBreakpointMap().isRegistered( breakpoint );
		}
		return s;
	}

	public boolean supportsAddressBreakpoint( ICAddressBreakpoint breakpoint ) {
		String module = null;
		try {
			module = breakpoint.getModule();
		}
		catch( CoreException e ) {
		}
		if ( module != null )
			return getExecFilePath().toOSString().equals( module );
		// supporting old breakpoints (> 3.0)
		try {
			return getExecFilePath().toOSString().equals( breakpoint.getSourceHandle() );
		}
		catch( CoreException e ) {
		}
		return false;
	}

	public void skipBreakpoints( boolean enabled ) {
		if ( fSkipBreakpoint != enabled && (DebugPlugin.getDefault().getBreakpointManager().isEnabled() || !enabled) ) {
			fSkipBreakpoint = enabled;
			doSkipBreakpoints( enabled );
		}
	}

	public void watchpointOutOfScope( ICDIWatchpoint cdiWatchpoint ) {
		handleBreakpointDestroyedEvent( cdiWatchpoint );
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

	private IPath convertPath( String sourceHandle ) {
		IPath path = null;
		if ( Path.EMPTY.isValidPath( sourceHandle ) ) {
			ISourceLocator sl = getSourceLocator();
			if ( sl instanceof CSourceLookupDirector ) {
				path = ((CSourceLookupDirector)sl).getCompilationPath( sourceHandle );
			}
			if ( path == null ) {
				path = new Path( sourceHandle );
			}
		}
		return path;
	}

	private IProject getProject() {
		return getDebugTarget().getProject();
	}

	private IPath getExecFilePath() {
		return getDebugTarget().getExecFile().getPath();
	}

	private ISourceLocator getSourceLocator() {
		return getDebugTarget().getLaunch().getSourceLocator();
	}

	protected Object getSourceElement( String file ) {
		Object sourceElement = null;
		ISourceLocator locator = getSourceLocator();
		if ( locator instanceof ICSourceLocator || locator instanceof CSourceLookupDirector ) {
			if ( locator instanceof ICSourceLocator )
				sourceElement = ((ICSourceLocator)locator).findSourceElement( file );
			else
				sourceElement = ((CSourceLookupDirector)locator).getSourceElement( file );
		}
		return sourceElement;
	}

	protected boolean isEmpty( String str ) {
		return !( str != null && str.trim().length() > 0 );
	}

	private boolean isTargetAvailable() {
		return getDebugTarget().getCDITarget().getConfiguration().supportsBreakpoints() && getDebugTarget().isAvailable();
	}

	private PBreakpointNotifier getBreakpointNotifier() {
		return PBreakpointNotifier.getInstance();
	}

	private boolean isFilteredByTarget( ICBreakpoint breakpoint, IPDebugTarget target ) {
		boolean result = false;
		try {
			IPDebugTarget[] tfs = (IPDebugTarget[]) breakpoint.getTargetFilters();
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
}
