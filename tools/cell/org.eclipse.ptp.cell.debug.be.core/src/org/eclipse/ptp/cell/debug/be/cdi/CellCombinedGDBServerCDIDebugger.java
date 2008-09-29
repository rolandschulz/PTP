/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.cdi;

import org.eclipse.cdt.debug.mi.core.GDBServerCDIDebugger2;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class CellCombinedGDBServerCDIDebugger extends GDBServerCDIDebugger2 {

	public static String ID = "org.eclipse.ptp.cell.debug.cdi.combinedGDBServerCDIDebugger"; //$NON-NLS-1$
	
	public CellCombinedGDBServerCDIDebugger() {
		super();
		
	}

	protected void startLocalGDBSession(ILaunchConfiguration arg0, Session arg1, IProgressMonitor arg2) throws CoreException {
		super.startLocalGDBSession(arg0, arg1, arg2);
		/*
		MISession miSession = getMISession( arg1 );
		try {
			CommandFactory factory = miSession.getCommandFactory();
			MIGDBSetBreakpointPending set = factory.createMIGDBSetBreakpointPending(true);
			miSession.postCommand( set );
			MIInfo info = set.getMIInfo();
			if ( info == null ) {
				throw new MIException( MIPlugin.getResourceString( "src.common.No_answer" ) ); //$NON-NLS-1$
			}
		}
		catch( MIException e ) {
			// We ignore this exception, for example
			// on GNU/Linux the new-console is an error.
		}
		*/
	}
}
