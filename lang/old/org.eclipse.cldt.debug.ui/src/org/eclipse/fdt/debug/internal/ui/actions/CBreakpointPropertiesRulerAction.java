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
package org.eclipse.fdt.debug.internal.ui.actions;

import org.eclipse.fdt.debug.core.model.ICBreakpoint;
import org.eclipse.fdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.fdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Opens a custom properties dialog to configure the attibutes of a C/C++ breakpoint 
 * from the ruler popup menu.
 */
public class CBreakpointPropertiesRulerAction extends AbstractBreakpointRulerAction {

	/**
	 * Creates the action to modify the breakpoint properties.
	 */
	public CBreakpointPropertiesRulerAction( IWorkbenchPart part, IVerticalRulerInfo info ) {
		setInfo( info );
		setTargetPart( part );
		setText( ActionMessages.getString( "CBreakpointPropertiesRulerAction.Breakpoint_Properties" ) ); //$NON-NLS-1$
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.BREAKPOINT_PROPERTIES_ACTION );
		setId( IInternalCDebugUIConstants.ACTION_BREAKPOINT_PROPERTIES );
	}

	/* (non-Javadoc)
	 * @see Action#run()
	 */
	public void run() {
		if ( getBreakpoint() != null ) {
			PropertyDialogAction action = new PropertyDialogAction( getTargetPart().getSite().getShell(), new ISelectionProvider() {

				public void addSelectionChangedListener( ISelectionChangedListener listener ) {
				}

				public ISelection getSelection() {
					return new StructuredSelection( getBreakpoint() );
				}

				public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
				}

				public void setSelection( ISelection selection ) {
				}
			} );
			action.run();
		}
	}

	/* (non-Javadoc)
	 * @see IUpdate#update()
	 */
	public void update() {
		setBreakpoint( determineBreakpoint() );
		if ( getBreakpoint() == null || !(getBreakpoint() instanceof ICBreakpoint) ) {
			setBreakpoint( null );
			setEnabled( false );
			return;
		}
		setEnabled( true );
	}
}