/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * A field editor for a directory path type preference. A standard directory
 * dialog appears when the user presses the change button.
 */
public class RelativeDirectoryFieldEditor extends StringButtonFieldEditor {
	
	IPath relativeRoot = null;
	
    public RelativeDirectoryFieldEditor(String name, String labelText, Composite parent) {
        init(name, labelText);
        setErrorMessage(JFaceResources
                .getString("DirectoryFieldEditor.errorMessage"));//$NON-NLS-1$
        setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
        setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
        createControl(parent);
    }
    
	public IPath getRelativeRoot() {
		return relativeRoot;
	}
	
	public void setRelativeRoot(IPath relativeRoot) {
		this.relativeRoot = relativeRoot;
	}
	
	private IPath getPathFromText() {
		String path = getTextControl().getText();
		
		if (path != null) {
			path = path.trim();
		} else {
			path = "";//$NON-NLS-1$
		}
		
		if ( (path.length() == 0) && (! isEmptyStringAllowed())) {
			return null;
		}
		
		IPath ipath = new Path(path);
		return ipath;
	}
	
	private IPath getAbsolutePathFromText() {
		IPath ipath = getPathFromText();
		
		if (ipath == null) {
			return null;
		}
		
		if (! ipath.isAbsolute()) {
			if (relativeRoot != null) {
				ipath = relativeRoot.append(ipath);
			} else {
				return null;
			}
		}
		
		return ipath;
	}
	
	protected boolean checkState() {

		String msg = null;

		IPath path = getPathFromText();
		if (path == null) {
			msg = getErrorMessage();
		} else {
			path = getAbsolutePathFromText();
			if (relativeRoot == null) {
				msg = getErrorMessage();		
			} else {
				if (! path.toFile().exists()) {
					msg = getErrorMessage();					
				} else if (! path.toFile().isDirectory()) {
					msg = getErrorMessage();					
				}
			}
		}

		if (msg != null) { 
			// Error
			showErrorMessage(msg);
			return false;
		} else {	
			// OK!
			clearErrorMessage();
			return true;
		}

	}
	
	
	protected String changePressed() {
		IPath path = getAbsolutePathFromText();
		
		if (path != null) {
			File file = path.toFile();
			if (file.exists()) {
				
			} else {
				path = relativeRoot;
			}
		} else {
			path = relativeRoot;
		}

		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
		dialog.setFilterPath(path.toOSString());


		String dirName = dialog.open();
		if (dirName != null) {
			dirName = dirName.trim();
			IPath newPath = new Path(dirName);
			int matches = newPath.matchingFirstSegments(relativeRoot);
			if (matches == relativeRoot.segmentCount()) {
				newPath = newPath.removeFirstSegments(matches);
				newPath = newPath.makeRelative();
			}
			return newPath.toOSString();
		} else {
			return null;
		}
	}

}
