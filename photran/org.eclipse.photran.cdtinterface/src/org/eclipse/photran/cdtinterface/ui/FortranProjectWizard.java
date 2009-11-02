/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

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
import org.eclipse.photran.core.FProjectNature;

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
//		super(UIMessages.getString("Fortran Project"),  //$NON-NLS-1$
//			  UIMessages.getString("Create Fortran project of selected type")); //$NON-NLS-1$
        super("Fortran Project", "Create a Fortran project of the selected type");
	}

	public String[] getNatures()
	{
		return new String[] { FProjectNature.F_NATURE_ID, CProjectNature.C_NATURE_ID };
	}

	/**
	 * This method is called immediately after the createIProject() method in
	 * the CDTCommonProjectWizard class
	 */
	protected IProject continueCreation(IProject prj)
	{
		try
		{
            // Add C nature to the project
            CProjectNature.addCNature(prj, new NullProgressMonitor());
            
            // Add Fortran nature to the project
            FProjectNature.addFNature(prj, new NullProgressMonitor());
		}
		catch (CoreException e) {}
		
		return prj;
	}	
	
    /**
     * This method is called within the performFinish() method of the
     * CDTCommonProjectWizard class. Among other things, it sets the Photran
     * nature first in the project's nature list. This ensures that the project
     * will be displayed as a Fortran project in the Photran navigator. 
     */	
    protected boolean setCreated() throws CoreException 
    {
        ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
        
        ICProjectDescription des = mngr.getProjectDescription(newProject, false);
        if(des.isCdtProjectCreating())
        {
            des = mngr.getProjectDescription(newProject, true);
            des.setCdtProjectCreated();
            mngr.setProjectDescription(newProject, des, false, null);
            return true;
        }
        
        // Access the .project file in the recently-created project
        final IFile projFile = (IFile)newProject.findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
        if(projFile.exists() && projFile.getLocation() != null) // JO: Prevent NPE on remote projects
        {
            try 
            {
                // Create a list of lines in .project file
                RandomAccessFile raFile = new RandomAccessFile(projFile.getLocation().toOSString(), "rws");
                ArrayList lineList = new ArrayList();
                String line;
                while((line = raFile.readLine()) != null) 
                    lineList.add(line);
                raFile.close();
                
                // Find the natures in the list
                Iterator itr = lineList.iterator();
                int first_index = 0, phot_index = 0;
                while (itr.hasNext()) 
                {
                    line = (String)itr.next();
                    if(line.trim().equals("<natures>"))
                        first_index = lineList.indexOf(line) + 1;
                    else if(line.contains("photran"))
                        phot_index = lineList.indexOf(line);
                }
                
                // Swap the photran nature with the first nature
                String temp = (String)lineList.get(first_index);
                lineList.set(first_index, lineList.get(phot_index));
                lineList.set(phot_index, temp);
                
                // Write the new lines to the .project file
                itr = lineList.iterator();
                StringBuffer content = new StringBuffer("");
                while (itr.hasNext()) 
                {
                    content.append((String)itr.next() + "\n");
                }
                projFile.setContents(new ByteArrayInputStream(content.toString().getBytes()), IFile.FORCE, null);

                // Deallocate
                lineList.clear();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (CoreException e) {
                e.printStackTrace();
            }            
        }       
        return false;
    }	
}
