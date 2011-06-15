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
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchElementQuery
 * Version: 1.14
 */
package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchElementQuery;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;

public class RemoteSearchElementQueryAdapter extends RemoteSearchQueryAdapter {


	public RemoteSearchElementQueryAdapter(ICIndexSubsystem subsystem, Scope scope, RemoteSearchElementQuery query) {
		super(subsystem, scope, query);
		
	}
	
	@Override
	public String getLabel() {
		ISourceReference element = ((RemoteSearchElementQuery) fQuery).getSourceReference();
		if (element instanceof ICElement)
			return super.getLabel() + " " + ((ICElement)element).getElementName(); //$NON-NLS-1$
		else
			return super.getLabel() + " something."; //$NON-NLS-1$
	}
	
	@Override
	public String getResultLabel(int numMatches) {
		ISourceReference element = ((RemoteSearchElementQuery) fQuery).getSourceReference();
		String label = (element instanceof ICElement) ?
				((ICElement) element).getElementName() : CSearchMessages.PDOMSearchElementQuery_something;
		labelForBinding(label);
		return getResultLabel(label, numMatches);
	}
}
