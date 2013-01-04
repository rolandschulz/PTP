/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchTextSelectionQuery
 * Version: 1.22
 */
package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchTextSelectionQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.internal.rdt.ui.search.RemoteSearchQueryAdapter;

public class RemoteSearchTextSelectionQueryAdapter extends RemoteSearchQueryAdapter {

	private RemoteSearchTextSelectionQueryAdapter() {
		throw new IllegalStateException();
	}
	
	public RemoteSearchTextSelectionQueryAdapter(ICIndexSubsystem subsystem, Scope scope, RemoteSearchTextSelectionQuery query) {
		super(subsystem, scope, query);
	}

	public String getLabel() {
		if(fQuery == null) {
			return null;
		}
		
		return super.getLabel() + " " + ((RemoteSearchTextSelectionQuery) fQuery).getSelection(); //$NON-NLS-1$
	}
	
	@Override
	public String getResultLabel(int numMatches) {
		if(fQuery == null) {
			return org.eclipse.ptp.rdt.ui.messages.Messages.getString("RemoteSearchPatternQueryAdapter_0"); //$NON-NLS-1$
		}
		
		String label = ((RemoteSearchTextSelectionQuery)fQuery).getSelection();
		label = labelForBinding(label);
		return getResultLabel(label, numMatches);
	}
}
