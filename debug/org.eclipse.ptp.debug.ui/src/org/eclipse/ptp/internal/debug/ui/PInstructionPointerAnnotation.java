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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 * 
 */
public class PInstructionPointerAnnotation extends MarkerAnnotation {
	private IAnnotationModel annotationModel = null;
	private TaskSet tasks = null;
	private Position position = null;
	private IMarker marker = null;

	/**
	 * Constructor
	 * 
	 * @param marker
	 * @param position
	 * @param annotationModel
	 */
	public PInstructionPointerAnnotation(IMarker marker, Position position,
			IAnnotationModel annotationModel) {
		super(marker);
		this.marker = marker;
		this.position = position;
		this.annotationModel = annotationModel;
	}

	/**
	 * Add tasks
	 * 
	 * @param aTasks
	 */
	public void addTasks(TaskSet aTasks) {
		if (tasks == null) {
			tasks = aTasks.copy();
		}
		if (tasks.taskSize() < aTasks.taskSize()) {
			aTasks.or(tasks);
			tasks = aTasks.copy();
		} else
			tasks.or(aTasks);
	}

	/**
	 * Contains tasks
	 * 
	 * @param aTasks
	 * @return
	 */
	public boolean contains(TaskSet aTasks) {
		return tasks.intersects(aTasks);
	}

	/**
	 * Get contains tasks
	 * 
	 * @param aTasks
	 * @return
	 */
	public int[] containTasks(TaskSet aTasks) {
		aTasks.and(tasks);
		return aTasks.toArray();
	}

	/**
	 * Delete marker
	 * 
	 * @return true if delete succuss
	 */
	public boolean deleteMarker() {
		IMarker marker = getMarker();
		if (marker.exists()) {
			try {
				marker.delete();
			} catch (CoreException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get annotation model
	 * 
	 * @return
	 */
	public IAnnotationModel getAnnotationModel() {
		return annotationModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.SimpleMarkerAnnotation#getMarker()
	 */
	@Override
	public IMarker getMarker() {
		return marker;
	}

	/**
	 * Get message
	 * 
	 * @return
	 */
	public String getMessage() {
		return getText();
	}

	/**
	 * Get position
	 * 
	 * @return
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Get tasks
	 * 
	 * @return
	 */
	public TaskSet getTasks() {
		return tasks;
	}

	/**
	 * Is no tasks
	 * 
	 * @return true if there is no tasks
	 */
	public boolean isEmpty() {
		return tasks.isEmpty();
	}

	/**
	 * Remove tasks
	 * 
	 * @param aTasks
	 */
	public void removeTasks(TaskSet aTasks) {
		tasks.andNot(aTasks);
	}

	/**
	 * Set marker
	 * 
	 * @param marker
	 */
	public void setMarker(IMarker marker) {
		this.marker = marker;
	}

	/**
	 * Set Message
	 * 
	 * @param isRegister
	 */
	public void setMessage(boolean isRegister) {
		int[] tasks = getTasks().toArray();
		if (tasks.length == 0) {
			setMessage(""); //$NON-NLS-1$
			deleteMarker();
			return;
		}
		String msg = Messages.PInstructionPointerAnnotation2_0;
		if (!isRegister && tasks.length == 1) {
			msg = Messages.PInstructionPointerAnnotation2_1;
		} else if (isRegister && tasks.length > 1) {
			msg = Messages.PInstructionPointerAnnotation2_2;
		} else if (!isRegister && tasks.length > 1) {
			msg = Messages.PInstructionPointerAnnotation2_3;
		}
		msg += PDebugUIUtils.arrayToString(tasks);
		setMessage(msg);
	}

	/**
	 * Set message for annotation
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		try {
			getMarker().setAttribute(IMarker.MESSAGE, message);
		} catch (CoreException e) {
		}
		setText(message);
	}

	/**
	 * Set position
	 * 
	 * @param position
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

	/**
	 * Set tasks
	 * 
	 * @param tasks
	 */
	public void setTasks(TaskSet tasks) {
		this.tasks = tasks;
	}
}
