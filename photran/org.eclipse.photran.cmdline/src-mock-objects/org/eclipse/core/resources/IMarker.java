/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("rawtypes")
public interface IMarker
{
    public static final String TEXT = null;
    public static final String CHAR_START = null;
    public static final String CHAR_END = null;
	public static final String MESSAGE = null;
	public static final String USER_EDITABLE = null;
    public static final String SEVERITY = null;
    public static final String SEVERITY_ERROR = null;
    void setAttribute(String charStart, int startOffset);
	void setAttributes(Map attribs);
	public void delete() throws CoreException;
}
