/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Project nature for Fortran projects.
 * 
 * @author Matt Scarpino
 */
/*
 * This file copied (mostly) from org.eclipse.cdt.core.CProjectNature.
 */
public class FProjectNature implements IProjectNature
{
    public static final String F_NATURE_ID = "org.eclipse.photran.core.fnature"; //$NON-NLS-1$

    private IProject fProject;

    public FProjectNature() {}

    public FProjectNature(IProject project)
    {
        setProject(project);
    }

    public static void addFNature(IProject project, IProgressMonitor mon) throws CoreException
    {
        addNature(project, F_NATURE_ID, mon);
    }

    public static void removeFNature(IProject project, IProgressMonitor mon) throws CoreException
    {
        removeNature(project, F_NATURE_ID, mon);
    }

    /**
     * Utility method for adding a nature to a project.
     * 
     * @param project the project to add the nature
     * @param natureId the id of the nature to assign to the project
     * @param monitor a progress monitor to indicate the duration of the operation, or
     *            <code>null</code> if progress reporting is not required.
     * 
     */
    public static void addNature(IProject project, String natureId, IProgressMonitor monitor)
        throws CoreException
    {
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        for (int i = 0; i < prevNatures.length; i++)
            if (natureId.equals(prevNatures[i]))
                return;
        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
        newNatures[0] = natureId;
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
    }

    /**
     * Utility method for removing a project nature from a project.
     * 
     * @param project the project to remove the nature from
     * @param natureId the nature id to remove
     * @param monitor a progress monitor to indicate the duration of the operation, or
     *            <code>null</code> if progress reporting is not required.
     */
    public static void removeNature(IProject project, String natureId, IProgressMonitor monitor)
        throws CoreException
    {
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        List newNatures = new ArrayList(Arrays.asList(prevNatures));
        newNatures.remove(natureId);
        description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
        project.setDescription(description, monitor);
    }

    /**
     * @see IProjectNature#configure
     */
    public void configure() throws CoreException
    {
    }

    /**
     * @see IProjectNature#deconfigure
     */
    public void deconfigure() throws CoreException
    {
    }

    /**
     * @see IProjectNature#getProject
     */
    public IProject getProject()
    {
        return fProject;
    }

    /**
     * @see IProjectNature#setProject
     */
    public void setProject(IProject project)
    {
        fProject = project;
    }
}
