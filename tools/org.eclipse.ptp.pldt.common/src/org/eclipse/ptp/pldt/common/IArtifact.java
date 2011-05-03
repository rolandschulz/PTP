/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.common;

import org.eclipse.ptp.pldt.common.util.SourceInfo;

/**
 * Artifacts contain information about something found in source code.
 * 
 * @author tibbitts
 * 
 */
public interface IArtifact
{
	public int getColumn();

	public void setColumn(int col);

	public String getDescription();

	public String getFileName();

	public String getId();

	public int getLine();

	public void setLine(int lineNo);

	public String getPrimaryfileName();

	public String getShortName();

	public void setShortName(String name);

	public SourceInfo getSourceInfo();

}
