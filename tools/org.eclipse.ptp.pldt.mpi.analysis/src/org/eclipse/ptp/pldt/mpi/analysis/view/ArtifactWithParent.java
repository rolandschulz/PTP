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

package org.eclipse.ptp.pldt.mpi.analysis.view;

import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

public class ArtifactWithParent extends Artifact {
	private int parentID;
	private int myID;
	private String name;
	private int index;

	public ArtifactWithParent(String fileName, int line, int column, String funcName, String desc, /* String primaryFileName, */
			SourceInfo sourceInfo, int parentID, int myID, String name, int index) {
		super(fileName, line, column, funcName, sourceInfo);
		this.parentID = parentID;
		this.myID = myID;
		this.name = name;
		this.index = index;
	}

	public int getParentID() {
		return parentID;
	}

	public int getMyID() {
		return myID;
	}

	public String getMyName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
}
