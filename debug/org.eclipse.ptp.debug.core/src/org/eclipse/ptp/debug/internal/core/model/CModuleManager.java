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
package org.eclipse.ptp.debug.internal.core.model; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.internal.core.IPDebugInternalConstants;
 
/**
 * Manages the modules loaded on this debug target.
 */
public class CModuleManager {

	/**
	 * The debug target associated with this manager.
	 */
	private PDebugTarget fDebugTarget;

	/**
	 * The collection of the shared libraries loaded on this target.
	 */
	private ArrayList fModules;

	/** 
	 * Constructor for CModuleManager. 
	 */
	public CModuleManager( PDebugTarget target ) {
		fDebugTarget = target;
		fModules = new ArrayList( 5 );
	}

	public boolean hasModules() {
		return !fModules.isEmpty();
	}

	public ICModule[] getModules() {
		return (ICModule[])fModules.toArray( new ICModule[fModules.size()] );
	}

	public void loadSymbolsForAll() throws DebugException {
		MultiStatus ms = new MultiStatus( PCDIDebugModel.getPluginIdentifier(), IPDebugInternalConstants.STATUS_CODE_ERROR, CoreModelMessages.getString( "CModuleManager.0" ), null ); //$NON-NLS-1$
		Iterator it = fModules.iterator();
		while( it.hasNext() ) {
			ICModule module = (ICModule)it.next();
			try {
				module.loadSymbols();
			}
			catch( DebugException e ) {
				ms.add(  new Status( IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), IPDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
			}
		}
		if ( !ms.isOK() ) {
			throw new DebugException( ms );
		}
	}

	public void loadSymbols( ICModule[] modules ) throws DebugException {
		MultiStatus ms = new MultiStatus( PCDIDebugModel.getPluginIdentifier(), IPDebugInternalConstants.STATUS_CODE_ERROR, CoreModelMessages.getString( "CModuleManager.1" ), null ); //$NON-NLS-1$
		for ( int i = 0; i < modules.length; ++i ) {
			try {
				modules[i].loadSymbols();
			}
			catch( DebugException e ) {
				ms.add(  new Status( IStatus.ERROR, PCDIDebugModel.getPluginIdentifier(), IPDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
			}
		}
		if ( !ms.isOK() ) {
			throw new DebugException( ms );
		}
	}

	public void dispose() {
		Iterator it = fModules.iterator();
		while( it.hasNext() ) {
			((CModule)it.next()).dispose();
		}
		fModules.clear();
	}

	protected PDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	protected void addModules( ICModule[] modules ) {
		fModules.addAll( Arrays.asList( modules ) );
	}

	protected void removeModules( ICModule[] modules ) {
		fModules.removeAll( Arrays.asList( modules ) );
	}

	public void sharedLibraryLoaded( ICDISharedLibrary cdiLibrary ) {
		CModule library = CModule.createSharedLibrary( getDebugTarget(), cdiLibrary );
		synchronized( fModules ) {
			fModules.add( library );
		}
		library.fireCreationEvent();
	}

	public void sharedLibraryUnloaded( ICDISharedLibrary cdiLibrary ) {
		CModule library = find( cdiLibrary );
		if ( library != null ) {
			synchronized( fModules ) {
				fModules.remove( library );
			}
			library.dispose();
			library.fireTerminateEvent();
		}
	}

	public void symbolsLoaded( ICDIObject cdiObject ) {
		CModule module = find( cdiObject );
		if ( module != null ) {
			module.fireChangeEvent( DebugEvent.STATE );
		}
	}

	private CModule find( ICDIObject cdiObject ) {
		Iterator it = fModules.iterator();
		while( it.hasNext() ) {
			CModule module = (CModule)it.next();
			if ( module.equals( cdiObject ) )
				return module;
		}
		return null;
	}
}
