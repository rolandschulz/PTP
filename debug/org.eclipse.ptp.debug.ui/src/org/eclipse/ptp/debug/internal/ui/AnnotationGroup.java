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
package org.eclipse.ptp.debug.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;

/**
 * @author Clement chu
 * 
 */
public class AnnotationGroup {
	private List annotationList = new ArrayList();

	public boolean contains(Annotation annotation) {
		return annotationList.contains(annotation);
	}
	public void addAnnotation(PInstructionPointerAnnotation annotation) {
		if (!contains(annotation))
			annotationList.add(annotation);
	}
	public void removeAnnotations(Collection removedAnnotations) {
		annotationList.removeAll(removedAnnotations);
	}
	public void removeAnnotation(PInstructionPointerAnnotation annotation) {
		if (contains(annotation))
			annotationList.remove(annotation);
	}
	public void removeAnnotations() {
		removeAllMarkers();
		annotationList.clear();
	}
	public int size() {
		return annotationList.size();
	}
	public Iterator getAnnotationIterator() {
		return annotationList.iterator();
	}
	public boolean isEmpty() {
		return annotationList.isEmpty();
	}
	public void removeAllMarkers() {
		for (Iterator i = annotationList.iterator(); i.hasNext();) {
			((PInstructionPointerAnnotation) i.next()).deleteMarker();
		}
	}
	public void retrieveAllMarkers() {
		for (Iterator i = annotationList.iterator(); i.hasNext();) {
			PInstructionPointerAnnotation annotation = (PInstructionPointerAnnotation) i.next();
			try {
				String type = annotation.getType();
				if (type.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT))
					type = IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT;
				
				annotation.setMarker(createMarker(annotation.getMarker().getResource(), type));
				annotation.getAnnotationModel().addAnnotation(annotation, annotation.getPosition());
				annotation.setMessage(!type.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT));
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
		}
	}
	public IMarker createMarker(IResource resource, String type) throws CoreException {
		return resource.createMarker(type);
	}
}
