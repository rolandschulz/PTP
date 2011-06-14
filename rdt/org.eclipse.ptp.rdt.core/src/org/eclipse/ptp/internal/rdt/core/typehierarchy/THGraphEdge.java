/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.THGraphEdge
 * Version: 1.1
 */
package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import java.io.Serializable;

public class THGraphEdge implements Serializable {
	private static final long serialVersionUID = 1L;

	private THGraphNode fFrom;
	private THGraphNode fTo;
	
	public THGraphEdge(THGraphNode from, THGraphNode to) {
		fFrom= from;
		fTo= to;
	}
	
	public THGraphNode getStartNode() {
		return fFrom;
	}
	
	public THGraphNode getEndNode() {
		return fTo;
	}
}
