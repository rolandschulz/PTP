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
package org.eclipse.core.runtime;

public interface IProgressMonitor
{
    int UNKNOWN = -1;

    void beginTask(String string, int unknown2);
    void subTask(String string);
    void done();
    boolean isCanceled();
    void internalWorked(double work);
    void setCanceled(boolean value);
    void setTaskName(String name);
    void worked(int work);
}