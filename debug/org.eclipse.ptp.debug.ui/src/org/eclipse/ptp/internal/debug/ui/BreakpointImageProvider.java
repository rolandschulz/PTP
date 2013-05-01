/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 *
 */
public class BreakpointImageProvider implements IAnnotationImageProvider {
	private IDebugModelPresentation debugModelPresentation = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getManagedImage(org.eclipse.jface.text.source.Annotation)
	 */
	public Image getManagedImage(Annotation annotation) {
		if (annotation instanceof MarkerAnnotation) {
			MarkerAnnotation markerAnnotation = (MarkerAnnotation)annotation;
			IMarker marker = markerAnnotation.getMarker();
			if (marker != null && marker.exists())
				return getPresentation().getImage(marker);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getImageDescriptorId(org.eclipse.jface.text.source.Annotation)
	 */
	public String getImageDescriptorId(Annotation annotation) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationImageProvider#getImageDescriptor(java.lang.String)
	 */
	public ImageDescriptor getImageDescriptor(String imageDescritporId) {
		return null;
	}
	
	/** Get IDebugModelPresentation
	 * @return
	 */
	private IDebugModelPresentation getPresentation() {
		if (debugModelPresentation == null)
			debugModelPresentation = DebugUITools.newDebugModelPresentation();
		
		return debugModelPresentation;
	}
}
