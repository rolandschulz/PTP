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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 * 
 */
public class PInstructionPointerAnnotation2 {
	private Annotation annotation = null;
	private IAnnotationModel annotationModel = null;
	private BitList tasks = null;
	private Position position = null;
	private IResource file = null;
	private IMarker marker = null;
	private String type = null;

	/** Constructor
	 * @param marker
	 * @param position
	 * @param annotationModel
	 */
	public PInstructionPointerAnnotation2(IResource file, String type, Position position, IAnnotationModel annotationModel) {
		this.file = file;
		this.type = type;
		this.position = position;
		this.annotationModel = annotationModel;
		addAnnotationToModel();
	}
	private IMarker createMarker() {
		if (file != null && file.exists()) {
			try {
				return file.createMarker(type);
			} catch (CoreException e) {}
		}
		return null;
	}
	public IResource getMakerResource() {
		return file;
	}
	/** Get annotation model
	 * @return
	 */
	public IAnnotationModel getAnnotationModel() {
		return annotationModel;
	}
	/** Get position
	 * @return
	 */
	public Position getPosition() {
		return position;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
		annotation.setType(type);
	}
	public void removeAnnotation() {
		if (isMarkerExists()) {
			try {
				marker.delete();
				marker = null;
			} catch (CoreException e) {}
		}
		annotation.setText("");
		annotation.markDeleted(true);
		annotationModel.removeAnnotation(annotation);
	}
	private boolean isMarkerExists() {
		return (marker != null && marker.exists());
	}
	public boolean isMarkDeleted() {
		return annotation.isMarkedDeleted();
	}
	public void addAnnotationToModel() {
		marker = createMarker();
		if (isMarkerExists()) {
			annotation = new MarkerAnnotation(marker);
		}
		else {
			annotation = new Annotation(type, false, "");
		}
		annotation.markDeleted(false);
		annotationModel.addAnnotation(annotation, position);
	}
	/** Set message for annotation
	 * @param message
	 */
	private void setMessage(String message) {
		if (isMarkerExists()) {
			try {
				marker.setAttribute(IMarker.MESSAGE, message);
			} catch (CoreException e) {}
		}
		annotation.setText(message);
	}
	/** Set Message 
	 * @param isRegister
	 */
	public void setMessage(boolean isRegister) {
		int[] tasks = getTasks().toArray();
		if (tasks.length == 0) {
			removeAnnotation();
			return;
		}
		String msg = "Suspended on " + (isRegister ? "registered" : "unregistered") + " " + (tasks.length == 1 ? "process" : "processes") + ": ";
		msg += PDebugUIUtils.arrayToString(tasks);
		setMessage(msg);
	}
	/** Set tasks
	 * @param tasks
	 */
	public void setTasks(BitList tasks) {
		this.tasks = tasks;
	}
	/** Get tasks
	 * @return
	 */
	public BitList getTasks() {
		return tasks;
	}
	/** Add tasks
	 * @param aTasks
	 */
	public void addTasks(BitList aTasks) {
		if (tasks == null) {
			tasks = aTasks.copy();
		}
		if (tasks.size() < aTasks.size()) {
			aTasks.or(tasks);
			tasks = aTasks.copy();
		} else
			tasks.or(aTasks);
	}
	/** Remove tasks
	 * @param aTasks
	 */
	public void removeTasks(BitList aTasks) {
		tasks.andNot(aTasks);
	}
	/** Is no tasks
	 * @return true if there is no tasks
	 */
	public boolean isEmpty() {
		return tasks.isEmpty();
	}
	/** Contains tasks
	 * @param aTasks
	 * @return
	 */
	public boolean contains(BitList aTasks) {
		return tasks.intersects(aTasks);
	}
	/** Get contains tasks
	 * @param aTasks
	 * @return
	 */
	public int[] containTasks(BitList aTasks) {
		aTasks.and(tasks);
		return aTasks.toArray();
	}
}
