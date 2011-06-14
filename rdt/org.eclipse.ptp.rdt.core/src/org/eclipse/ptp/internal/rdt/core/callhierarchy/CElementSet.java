/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.callhierarchy.CElementSet
 * Version: 1.3
 */
package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;

public class CElementSet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Set<ICElement> fSet= new LinkedHashSet<ICElement>();
	private int fHashCode;
	
	public CElementSet( ICElement[] elements) {
		fSet.addAll(Arrays.asList(elements));
		fHashCode= 0;
        for (int i = 0; i < elements.length; i++) {
        	fHashCode = 31*fHashCode + elements[i].hashCode();
        }
	}

	public int hashCode() {
		return fHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CElementSet other = (CElementSet) obj;
		if (fHashCode != other.fHashCode) {
			return false;
		}
		if (fSet == null) {
			if (other.fSet != null) {
				return false;
			}
		} 
		else {
			if (fSet.size() != other.fSet.size()) {
				return false;
			}
			for (Iterator<ICElement> iter = fSet.iterator(); iter.hasNext(); ) {
				if (!other.fSet.contains(iter.next())) { 
					return false;
				}
			}
		}
		return true;
	}

	public boolean isEmpty() {
		return fSet.isEmpty();
	}

	public ICElement[] getElements() {
		ArrayList<ICElement> result= new ArrayList<ICElement>(fSet.size());
		for (Iterator<ICElement> iter = fSet.iterator(); iter.hasNext(); ) {
			ICElement element = iter.next();
			result.add(element);
		}
		return result.toArray(new ICElement[result.size()]);
	}
}