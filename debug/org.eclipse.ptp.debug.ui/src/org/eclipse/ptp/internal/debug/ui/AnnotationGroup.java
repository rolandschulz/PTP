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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Clement chu
 * 
 */
public class AnnotationGroup {
	private List<PInstructionPointerAnnotation2> annotationList = Collections.synchronizedList(new ArrayList<PInstructionPointerAnnotation2>());

	/** Check the annotation whether it contains in the list
	 * @param annotation
	 * @return true if contains
	 */
	public boolean contains(PInstructionPointerAnnotation2 annotation) {
		synchronized (annotationList) {
			return annotationList.contains(annotation);
		}
	}
	/** Add a new annotation to the list
	 * @param annotation
	 */
	public void addAnnotation(PInstructionPointerAnnotation2 annotation) {
		synchronized (annotationList) {
			if (!contains(annotation))
				annotationList.add(annotation);
		}
	}
	/** Remove annotations from the list
	 * @param removedAnnotations list of annotations
	 */
	public void removeAnnotations(Collection<PInstructionPointerAnnotation2> removedAnnotations) {
		synchronized (annotationList) {
			annotationList.removeAll(removedAnnotations);
		}
	}
	/** Remove annotation from the list
	 * @param annotation
	 */
	public void removeAnnotation(PInstructionPointerAnnotation2 annotation) {
		synchronized (annotationList) {
			if (contains(annotation))
				annotationList.remove(annotation);
		}
	}
	public PInstructionPointerAnnotation2[] getAnnotations() {
		synchronized (annotationList) {
			return annotationList.toArray(new PInstructionPointerAnnotation2[0]);
		}
	}
	/** Remove all annotations
	 * 
	 */
	public void removeAnnotations() {
		synchronized (annotationList) {
			throwAllAnnotations();
			annotationList.clear();
		}
	}
	/** Total annotations
	 * @return total annotations
	 */
	public int size() {
		return annotationList.size();
	}
	/** Is annotation list empty
	 * @return true if there is no annotation stored
	 */
	public boolean isEmpty() {
		return annotationList.isEmpty();
	}
	/** Remove all annotation markers
	 * 
	 */
	public void throwAllAnnotations() {
		synchronized (annotationList) {
			for (PInstructionPointerAnnotation2 annotation : getAnnotations()) {
				annotation.removeAnnotation();
			}
		}
	}
	/** Retrieve all markers
	 * 
	 */
	public void retrieveAllAnnontations() {
		synchronized (annotationList) {
			for (PInstructionPointerAnnotation2 annotation : getAnnotations()) {
				String type = annotation.getType();
				if (type.equals(IPTPDebugUIConstants.SET_ANN_INSTR_POINTER_CURRENT))
					type = IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT;
				
				if (annotation.isMarkDeleted()) {
					annotation.addAnnotationToModel();
					annotation.setMessage(!type.equals(IPTPDebugUIConstants.CURSET_ANN_INSTR_POINTER_CURRENT));
				}
			}
		}
	}
}
