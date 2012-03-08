/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.navigation;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

import org.eclipse.ptp.internal.rdt.core.miners.RemoteFoldingRegionsHandler.Branch;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteFoldingRegionsHandler.StatementRegion;

public class FoldingRegionsResult implements Serializable {
	private static final long serialVersionUID = 1L;
	public Stack<StatementRegion> iral;
	public List<Branch> branches;
}
