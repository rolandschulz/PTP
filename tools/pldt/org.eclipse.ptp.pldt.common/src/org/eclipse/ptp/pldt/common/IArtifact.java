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
	/**
	 * Get the column number within the line
	 * @return
	 */
	public int getColumn();

	/**
	 * Set the column number within the line
	 * @param col
	 */
	public void setColumn(int col);

	/**
	 * Get text description of the artifact
	 * @return
	 */
	public String getDescription();

	/**
	 * Get string representation of the filename containing the artifact
	 * @return
	 */
	public String getFileName();

	/**
	 * Get unique id
	 * @return
	 */
	public String getId();

	/**
	 * Get line number within file
	 * @return
	 */
	public int getLine();

	/**
	 * Set the line number within the file
	 * @param lineNo
	 */
	public void setLine(int lineNo);

	/**
	 * @return
	 */
	public String getPrimaryfileName();

	/**
	 * Get short name of artifact
	 * @return
	 */
	public String getShortName();

	/**
	 * Set short name for artifact
	 * @param name
	 */
	public void setShortName(String name);

	/**
	 * Get detail info on source code location of artifact
	 * @return
	 */
	public SourceInfo getSourceInfo();

}
