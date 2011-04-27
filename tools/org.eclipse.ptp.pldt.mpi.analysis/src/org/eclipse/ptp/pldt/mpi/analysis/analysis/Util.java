/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {

	public static List Intersect(List A, List B) {
		if (A == null || B == null)
			return null;
		List list = new ArrayList();
		for (Iterator i = A.iterator(); i.hasNext();) {
			Object o = i.next();
			if (B.contains(o))
				list.add(o);
		}
		return list;
	}

	public static boolean equals(List A, List B) {
		if (A == null && B == null)
			return true;
		if (A == null && B != null)
			return false;
		if (A != null && B == null)
			return false;
		if (A.size() != B.size())
			return false;
		for (Iterator i = A.iterator(); i.hasNext();) {
			if (!B.contains(i.next()))
				return false;
		}
		return true;
	}

	public static List Union(List A, List B) {
		if (A == null)
			return B;
		if (B == null)
			return A;
		List list = new ArrayList(A);
		for (Iterator i = B.iterator(); i.hasNext();) {
			Object o = i.next();
			if (!list.contains(o))
				list.add(o);
		}
		return list;
	}

	public static List Union(List A, List B, List C) {
		return Union(Union(A, B), C);
	}

	/*
	 * @ return: A - B
	 */
	public static List Minus(List A, List B) {
		if (B.isEmpty())
			return A;
		List list = new ArrayList();
		for (Iterator i = A.iterator(); i.hasNext();) {
			Object o = i.next();
			if (!B.contains(o))
				list.add(o);
		}
		return list;
	}

	/* to = Union(to, from) */
	public static void addAll(List to, List from) {
		if (from == null)
			return;
		if (from.size() == 0)
			return;
		for (Iterator i = from.iterator(); i.hasNext();) {
			String s = (String) i.next();
			if (!to.contains(s))
				to.add(s);
		}
	}

	public static List copy(List oldlist) {
		List list = new ArrayList();
		for (Iterator i = oldlist.iterator(); i.hasNext();) {
			list.add(i.next());
		}
		return list;
	}
}
