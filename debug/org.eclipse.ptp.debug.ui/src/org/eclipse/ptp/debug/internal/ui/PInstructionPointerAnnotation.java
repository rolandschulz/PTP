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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Clement chu
 * 
 */
public class PInstructionPointerAnnotation extends MarkerAnnotation {
	private IAnnotationModel annotationModel = null;
	private BitList tasks = null;
	private Position position = null;
	private IMarker marker = null;

	public PInstructionPointerAnnotation(IMarker marker, Position position, IAnnotationModel annotationModel) {
		super(marker);
		this.marker = marker;
		this.position = position;
		this.annotationModel = annotationModel;
	}
	public IAnnotationModel getAnnotationModel() {
		return annotationModel;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public Position getPosition() {
		return position;
	}
	public void setMarker(IMarker marker) {
		this.marker = marker;
	}
	public IMarker getMarker() {
		return marker;
	}
	public void setMessage(String message) {
		try {
			getMarker().setAttribute(IMarker.MESSAGE, message);
		} catch (CoreException e) {
		}
		setText(message);
	}
	public void setMessage(boolean isRegister) {
		int[] tasks = getTasks().toArray();
		if (tasks.length == 0) {
			setMessage("");
			deleteMarker();
			return;
		}
		String msg = "Suspended on " + (isRegister ? "registered" : "unregistered") + " " + (tasks.length == 1 ? "process" : "processes") + ": ";
		msg += PDebugUIUtils.arrayToString(tasks);
		setMessage(msg);
	}
	public String getMessage() {
		return getText();
	}
	public void setTasks(BitList tasks) {
		this.tasks = tasks;
	}
	public BitList getTasks() {
		return tasks;
	}
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
	public void removeTasks(BitList aTasks) {
		tasks.andNot(aTasks);
	}
	public boolean isEmpty() {
		return tasks.isEmpty();
	}
	public boolean contains(BitList aTasks) {
		return tasks.intersects(aTasks);
	}
	public int[] containTasks(BitList aTasks) {
		aTasks.and(tasks);
		return aTasks.toArray();
	}
	public boolean deleteMarker() {
		IMarker marker = getMarker();
		if (marker.exists()) {
			try {
				marker.delete();
			} catch (CoreException e) {
				System.err.println("################# PInstructionPointerAnnotation - deleteMarker(): " + e.getMessage());
				return false;
			}
		}
		return true;
	}
}
