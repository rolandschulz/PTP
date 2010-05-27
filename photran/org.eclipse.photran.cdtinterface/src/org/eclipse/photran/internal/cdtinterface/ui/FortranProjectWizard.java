/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC Photran modifications
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.ui;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.internal.cdtinterface.CDTInterfacePlugin;
import org.eclipse.photran.internal.cdtinterface.core.FortranLanguage;
import org.eclipse.photran.internal.core.FProjectNature;

/**
 * Wizard to create a new Fortran project.
 * 
 * @author ???
 * @author Matt Scarpino - 7/20/2009 - Updated to include Fortran nature in project
 */
public class FortranProjectWizard extends CDTCommonProjectWizard
{
    public FortranProjectWizard()
    {
        super(Messages.FortranProjectWizard_Title, Messages.FortranProjectWizard_Description);
    }

    @Override
    public String[] getNatures()
    {
        return new String[] { FProjectNature.F_NATURE_ID, CProjectNature.C_NATURE_ID };
    }

    /**
     * Forces the Fortran Project wizard to display only Fortran project types.
     */
    @Override
    public String[] getLanguageIDs()
    {
        return new String[] { FortranLanguage.LANGUAGE_ID };
    }

    /**
     * Sets the project to have both a C nature and a Fortran nature.
     * <p>
     * This method is called immediately after the createIProject() method in the
     * {@link CDTCommonProjectWizard} class
     */
    @Override
    protected IProject continueCreation(IProject prj)
    {
        try
        {
            CProjectNature.addCNature(prj, new NullProgressMonitor());
            FProjectNature.addFNature(prj, new NullProgressMonitor());
        }
        catch (CoreException e)
        {
        }

        return prj;
    }

    /**
     * This method is called within the performFinish() method of the {@link CDTCommonProjectWizard}
     * class.  Among other things, it sets the Photran nature first in the project's nature list.
     * This ensures that the project will be displayed as a Fortran project in the Photran navigator.
     */
    @Override
    protected boolean setCreated() throws CoreException
    {
        ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
        ICProjectDescription des = mngr.getProjectDescription(newProject, false);
        if (des.isCdtProjectCreating())
        {
            des = mngr.getProjectDescription(newProject, true);
            des.setCdtProjectCreated();
            mngr.setProjectDescription(newProject, des, false, null);
            return true;
        }

        setFortranNatureFirst();
        return false;
    }

    private void setFortranNatureFirst()
    {
        // Access the .project file in the recently-created project
        final IFile projFile = (IFile)newProject.findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
        if (projFile.exists() && projFile.getLocation() != null) // JO: Prevent NPE on remote
                                                                 // projects
        {
            try
            {
                // Create a list of lines in .project file
                RandomAccessFile raFile = new RandomAccessFile(projFile.getLocation().toOSString(),
                    "rws"); //$NON-NLS-1$
                ArrayList lineList = new ArrayList();
                String line;
                while ((line = raFile.readLine()) != null)
                    lineList.add(line);
                raFile.close();

                // Find the natures in the list
                Iterator itr = lineList.iterator();
                int first_index = 0, phot_index = 0;
                while (itr.hasNext())
                {
                    line = (String)itr.next();
                    if (line.trim().equals("<natures>")) //$NON-NLS-1$
                        first_index = lineList.indexOf(line) + 1;
                    else if (line.contains("photran")) //$NON-NLS-1$
                        phot_index = lineList.indexOf(line);
                }

                // Swap the photran nature with the first nature
                String temp = (String)lineList.get(first_index);
                lineList.set(first_index, lineList.get(phot_index));
                lineList.set(phot_index, temp);

                // Write the new lines to the .project file
                itr = lineList.iterator();
                StringBuffer content = new StringBuffer(""); //$NON-NLS-1$
                while (itr.hasNext())
                {
                    content.append((String)itr.next() + "\n"); //$NON-NLS-1$
                }
                projFile.setContents(new ByteArrayInputStream(content.toString().getBytes()),
                    IFile.FORCE, null);

                // Deallocate
                lineList.clear();
            }
            catch (FileNotFoundException e)
            {
                CDTInterfacePlugin.log(e);
                e.printStackTrace();
            }
            catch (IOException e)
            {
                CDTInterfacePlugin.log(e);
                e.printStackTrace();
            }
            catch (CoreException e)
            {
                CDTInterfacePlugin.log(e);
                e.printStackTrace();
            }
        }
    }
}
