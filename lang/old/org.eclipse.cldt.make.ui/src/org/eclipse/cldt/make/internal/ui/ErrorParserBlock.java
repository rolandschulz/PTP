/*******************************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v0.5 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: QNX Software Systems - Move to Make plugin
 ******************************************************************************/
package org.eclipse.cldt.make.internal.ui;

import org.eclipse.cldt.make.core.IMakeBuilderInfo;
import org.eclipse.cldt.make.core.MakeBuilder;
import org.eclipse.cldt.make.core.MakeCorePlugin;
import org.eclipse.cldt.ui.dialogs.AbstractErrorParserBlock;
import org.eclipse.cldt.ui.dialogs.ICOptionContainer;
import org.eclipse.cldt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.widgets.Composite;

public class ErrorParserBlock extends AbstractErrorParserBlock {

	// make builder enabled
	IMakeBuilderInfo fBuildInfo;
	boolean useBuildInfo = false;
	Preferences fPrefs;
	
	public ErrorParserBlock(Preferences preferences) {
		super();
		fPrefs = preferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		if (useBuildInfo == true && fBuildInfo == null) {
			Composite composite = ControlFactory.createComposite(parent, 1);
			setControl(composite);
			ControlFactory.createEmptySpace(composite);
			ControlFactory.createLabel(composite, MakeUIPlugin.getResourceString("ErrorParserBlock.label.missingBuilderInformation")); //$NON-NLS-1$
			return;
		}
		super.createControl(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ErrorParserBlock#getErrorParserIDs(org.eclipse.core.resources.IProject)
	 */
	protected String[] getErrorParserIDs(IProject project) {
		if (getContainer().getProject() != null && fBuildInfo == null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), MakeBuilder.BUILDER_ID);
			} catch (CoreException e) {
			}
		}
		if (fBuildInfo != null) {
			return fBuildInfo.getErrorParsers();
		}
		return new String[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ErrorParserBlock#saveErrorParsers(org.eclipse.core.resources.IProject,
	 *      java.lang.String[])
	 */
	public void saveErrorParsers(IProject project, String[] parserIDs) throws CoreException {
		if (getContainer().getProject() != null && fBuildInfo == null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), MakeBuilder.BUILDER_ID);
			} catch (CoreException e) {
			}
		}
		if (fBuildInfo != null) {
			fBuildInfo.setErrorParsers(parserIDs);
		}
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock#saveErrorParsers(java.lang.String[])
	 */
	protected void saveErrorParsers(String[] parserIDs) throws CoreException {
		fBuildInfo = MakeCorePlugin.createBuildInfo(fPrefs, MakeBuilder.BUILDER_ID, false);
		fBuildInfo.setErrorParsers(parserIDs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.AbstractErrorParserBlock#getErrorParserIDs(boolean)
	 */
	protected String[] getErrorParserIDs(boolean defaults) {
		fBuildInfo = MakeCorePlugin.createBuildInfo(fPrefs, MakeBuilder.BUILDER_ID, defaults);
		return fBuildInfo.getErrorParsers();
	}
	
	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		if (getContainer().getProject() != null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), MakeBuilder.BUILDER_ID);
			} catch (CoreException e) {
			}
			useBuildInfo = true; 
		} else {
		}
	}

}