/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchMatch
 * Version: 1.7
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchMatch;
import org.eclipse.search.ui.text.Match;

public class RemoteSearchMatchAdapter extends Match {

	public RemoteSearchMatchAdapter(RemoteSearchMatch match) throws CoreException {
		super(new TypeInfoSearchElement(match.getName(), match.getTypeInfo()), match.getOffset(), match.getLength());
	}

	IIndexFileLocation getLocation() {
		return ((RemoteSearchElement)getElement()).getLocation();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof RemoteSearchMatchAdapter))
			return false;
		RemoteSearchMatchAdapter other = (RemoteSearchMatchAdapter)obj;
		return getElement().equals(other.getElement())
			&& getOffset() == other.getOffset()
			&& getLength() <= other.getLength();
	}
}
