/*******************************************************************************
 * Copyright (c) 2009 Eclipse Engineering LLC.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.photran.internal.core.FProjectNature;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Implements the "Convert to Fortran Project" action, which can be invoked by right-clicking on
 * a C/C++ Project in the Fortran Projects view.
 * 
 * @author Matt Scarpino
 */
public class ProjectConversionAction implements IViewActionDelegate
{

    ArrayList projects = null;
    FortranView view;

    public void init(IViewPart v)
    {
        view = (FortranView)v;
        projects = new ArrayList();
    }

    // Add Fortran nature to selected C/C++ projects
    // Refresh view to display nature image
    @SuppressWarnings("restriction")
    public void run(IAction action)
    {
        IProject project;
        if(projects.size() != 0)
        {
            try
            {
                Iterator e = projects.iterator();
                while(e.hasNext()) {
                    project = (IProject)e.next();
                    if(project.hasNature(FProjectNature.F_NATURE_ID))
                        continue;
                    IProjectDescription description = project.getDescription();
                    String[] natures = description.getNatureIds();
                    String[] newNatures = new String[natures.length + 1];
                    System.arraycopy(natures, 0, newNatures, 1, natures.length);
                    newNatures[0] = FProjectNature.F_NATURE_ID;
                    description.setNatureIds(newNatures);
                    project.setDescription(description, null);
                }
                view.getViewer().refresh();
                projects.clear();
            }
            catch (CoreException e)
            {
                e.printStackTrace();
            }
        }
    }

    // Determine if any of the selected resources are C/C++ projects
    // If so, add them to the ArrayList
    public void selectionChanged(IAction action, ISelection selection)
    {
        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Iterator e = structuredSelection.iterator();
            while (e.hasNext())
            {
                Object element = e.next();
                if (element instanceof ICProject)
                    projects.add(((ICProject)element).getProject());
            }
        }
    }
}
