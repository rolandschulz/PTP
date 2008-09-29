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
import org.eclipse.swt.widgets.FileDialog;

public class RelativeFileFieldEditor extends StringButtonFieldEditor {
	
	IPath relativeRoot = null;
	String[] extensions = null;

	public RelativeFileFieldEditor(String name, String labelText, Composite parent) {
        init(name, labelText);
        setErrorMessage(JFaceResources
                .getString("FileFieldEditor.errorMessage"));//$NON-NLS-1$
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
	
    public void setFileExtensions(String[] extensions) {
        this.extensions = extensions;
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
				msg = JFaceResources.getString("FileFieldEditor.errorMessage2");//$NON-NLS-1$
			} else {
				if (! path.toFile().exists()) {
					msg = getErrorMessage();					
				} else if (! path.toFile().isFile()) {
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
        IPath start = null;
        
        if (path != null) {
        	File file = path.toFile();
        	if (file.exists()) {
        		start = path.removeLastSegments(1);
        	} else {
            	start = relativeRoot;
        	}
        } else {
        	start = relativeRoot;
        }
        
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        if (path != null) {
			dialog.setFileName(path.lastSegment());
		}
		dialog.setFilterPath(start.toOSString());
        if (extensions != null) {
			dialog.setFilterExtensions(extensions);
		}
        
        String fileName = dialog.open();
        if (fileName != null) {
            fileName = fileName.trim();
            IPath newPath = new Path(fileName);
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
