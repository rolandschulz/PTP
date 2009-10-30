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

public class NullProgressMonitor implements IProgressMonitor
{
    public void beginTask(String string, int unknown2) {}
    public void subTask(String string) {}
    public void done() {}
    public boolean isCanceled() { return false; }
    public void internalWorked(double work) {}
    public void setCanceled(boolean value) {}
    public void setTaskName(String name) {}
    public void worked(int work) {}
}
