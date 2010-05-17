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
import org.eclipse.photran.internal.cdtinterface.CDTInterfacePlugin;
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
    private FortranView view;
    private ArrayList<IProject> projects;

    /**
     * Callback invoked to initialize this action.
     */
    public void init(IViewPart v)
    {
        view = (FortranView)v;
        projects = new ArrayList<IProject>();
    }

    /**
     * Callback invoked when the workbench selection changes.
     * <p>
     * Determines if any of the selected resources are C/C++ projects, and, if so, adds them to
     * {@link #projects}.
     */
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

    /**
     * Callback invoked to run this action.
     * <p>
     * Adds the Fortran nature to selected C/C++ projects, and refreshes the view to display the new
     * nature image (i.e., to make sure the project is displayed with an &quot;F&quot; icon).
     */
    @SuppressWarnings("restriction")
    public void run(IAction action)
    {
        if (!projects.isEmpty())
        {
            addFortranNatureToSelectedProjects();
            view.getViewer().refresh();
            projects.clear();
        }
    }

    private void addFortranNatureToSelectedProjects()
    {
        for (IProject project : projects)
            addFortranNatureTo(project);
    }

    private void addFortranNatureTo(IProject project)
    {
        try
        {
            if (!project.hasNature(FProjectNature.F_NATURE_ID))
            {
                IProjectDescription description = project.getDescription();
                String[] natures = description.getNatureIds();
                String[] newNatures = new String[natures.length + 1];
                System.arraycopy(natures, 0, newNatures, 1, natures.length);
                newNatures[0] = FProjectNature.F_NATURE_ID;
                description.setNatureIds(newNatures);
                project.setDescription(description, null);
            }
        }
        catch (CoreException e)
        {
            CDTInterfacePlugin.log(e);
            e.printStackTrace();
        }
    }
}
