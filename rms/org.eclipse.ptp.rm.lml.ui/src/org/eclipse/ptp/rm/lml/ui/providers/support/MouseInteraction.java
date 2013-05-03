/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers.support;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.ptp.internal.rm.lml.core.model.Node;
import org.eclipse.ptp.internal.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.LMLNodeData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * This class is used by ui.provider classes, which react on mouse input.
 * It creates tooltips for the widgets and implements actions, which are
 * called on mouse-events.
 * 
 * @author karbach
 * 
 */
public class MouseInteraction {

	/**
	 * LML handler
	 */
	private final ILguiItem lguiItem;

	/**
	 * Tooltip instance for the control with mouse interaction.
	 * This default tooltip allows more flexibility
	 */
	private final DefaultToolTip toolTip;

	// TODO make this attribute configurable in the XSD
	// e.g. the objects' tag could have a special attribute
	// for this reason with a default set to "empty".
	/**
	 * The name of the special empty job
	 * Has to be configured anyhow later. This is
	 * not a good solution.
	 */
	private final String emptyJobName = "empty"; //$NON-NLS-1$

	/**
	 * Create a mouse interactor with a control to write tooltips on.
	 * Needs an lguiItem to handle LML-specific operations.
	 * 
	 * @param lguiItem
	 *            LML-handler
	 * @param interactControl
	 *            interacting control
	 */
	public MouseInteraction(ILguiItem lguiItem, Control interactControl) {
		this.lguiItem = lguiItem;

		toolTip = new DefaultToolTip(interactControl);
		toolTip.setText(null);
		toolTip.setShift(new Point(20, 20));
		toolTip.setHideOnMouseDown(false);
		toolTip.deactivate();
	}

	/**
	 * Method is called, when the mouse clicks on
	 * a node.
	 * 
	 * @param focussed
	 *            the node focussed by the mouse
	 */
	public void mouseDownAction(Node<LMLNodeData> focussed) {
		if (focussed != null && !isNodeEmpty(focussed) && lguiItem.getObjectStatus() != null) {
			lguiItem.getObjectStatus().mouseDown(lguiItem.getOIDToObject().getObjectByLMLNode(focussed.getData()));
		}

		setToolTipText(getToolTipText(focussed));
	}

	/**
	 * Method is called, when the mouse clicks on
	 * an LML-object of any kind.
	 * 
	 * @param focussed
	 *            the focussed object (node, job, anything connected to an object)
	 */
	public void mouseDownAction(ObjectType focussed) {
		if (focussed != null && !isEmptyJob(focussed) && lguiItem.getObjectStatus() != null) {
			lguiItem.getObjectStatus().mouseDown(focussed);
		}

		setToolTipText(getToolTipText(focussed));
	}

	/**
	 * Method is called, when a mouse exits a nodepanel.
	 */
	public void mouseExitAction() {
		if (lguiItem.getObjectStatus() != null) {
			lguiItem.getObjectStatus().mouseExitLast();
		}

		setToolTipText(getToolTipText((Node<LMLNodeData>) null));
	}

	/**
	 * Method is called, when a mouse moves over a node.
	 * 
	 * @param focussed
	 *            focussed node.
	 */
	public void mouseMoveAction(Node<LMLNodeData> focussed) {
		if (lguiItem.getObjectStatus() != null) {
			if (focussed != null && !isNodeEmpty(focussed)) {
				lguiItem.getObjectStatus().mouseOver(lguiItem.getOIDToObject().getObjectByLMLNode(focussed.getData()));
			} else {
				lguiItem.getObjectStatus().mouseExitLast();
			}
		}
		setToolTipText(getToolTipText(focussed));
	}

	/**
	 * Method is called, when the mouse moves over
	 * an LML-object of any kind.
	 * 
	 * @param focussed
	 *            the focussed object (node, job, anything connected to an object)
	 */
	public void mouseMoveAction(ObjectType focussed) {
		if (lguiItem.getObjectStatus() != null) {
			if (focussed != null && !isEmptyJob(focussed)) {
				lguiItem.getObjectStatus().mouseOver(focussed);
			} else {
				lguiItem.getObjectStatus().mouseExitLast();
			}
		}
		setToolTipText(getToolTipText(focussed));
	}

	/**
	 * Method is called, when a click on a node ends.
	 * 
	 * @param focussed
	 *            the node connected to the panel, on which
	 *            the mouse was released
	 */
	public void mouseUpAction(Node<LMLNodeData> focussed) {
		if (focussed != null && !isNodeEmpty(focussed) && lguiItem.getObjectStatus() != null) {
			lguiItem.getObjectStatus().mouseUp(lguiItem.getOIDToObject().getObjectByLMLNode(focussed.getData()));
		}
		setToolTipText(getToolTipText(focussed));
	}

	/**
	 * Method is called, when the mouse click on
	 * an LML-object of any kind ends.
	 * 
	 * @param focussed
	 *            the focussed object (node, job, anything connected to an object)
	 */
	public void mouseUpAction(ObjectType focussed) {
		if (focussed != null && !isEmptyJob(focussed) && lguiItem.getObjectStatus() != null) {
			lguiItem.getObjectStatus().mouseUp(focussed);
		}
		setToolTipText(getToolTipText(focussed));
	}

	/**
	 * Generate the text shown for a node, which is covered by
	 * mouse-cursor.
	 * 
	 * @param focussed
	 *            the node which is covered
	 * @return tooltiptext, which should be shown for the focussed node
	 */
	private String getToolTipText(Node<LMLNodeData> focussed) {
		String info = ""; //$NON-NLS-1$
		if (focussed == null) {
			return null;
		}
		// Get connected object to a the focussed node
		final ObjectType object = lguiItem.getOIDToObject().getObjectByLMLNode(focussed.getData());
		info += getToolTipText(object);

		return info;
	}

	/**
	 * Generate the tooltip text gathered from the connected ObjectType instance only.
	 * 
	 * @param object
	 *            a job or another referenced object, for which a tooltip is needed
	 * @return tooltiptext, which should be shown for this object
	 */
	private String getToolTipText(ObjectType object) {
		String info = ""; //$NON-NLS-1$
		if (object == null) {
			return null;
		}
		if (isEmptyJob(object)) {
			info = Messages.MouseInteraction_0;
		}
		else if (object.getName() != null) {
			info = "Job: " + object.getName(); //$NON-NLS-1$
		}
		else {
			info = "Job: " + object.getId(); //$NON-NLS-1$
		}

		return info;
	}

	/**
	 * Check if <code>object</code> refers to the special empty job.
	 * 
	 * @param object
	 *            an ObjectType
	 * @return true, if object is connected to empty job, false otherwise
	 */
	private boolean isEmptyJob(ObjectType object) {
		if (object == null) {
			return true;
		}
		if (object.getId() == null) {
			return true;
		}
		if (object.getId().equals(emptyJobName)) {
			return true;
		}

		return false;
	}

	/**
	 * Check if given node is connected to empty jobs.
	 * 
	 * @param node
	 *            the node, which is checked
	 * @return <code>true</code>, if anything in request-chain is <code>null</code> or if object-id is
	 *         "empty"
	 */
	private boolean isNodeEmpty(Node<LMLNodeData> node) {
		if (node == null) {
			return true;
		}
		// Get connected object for the focussed node
		return isEmptyJob(lguiItem.getOIDToObject().getObjectByLMLNode(node.getData()));
	}

	/**
	 * Checks if text is null. In this case the toolTip is deactivated.
	 * Otherwise the tooltip is activated again. The given text is always
	 * set as tooltip text.
	 * 
	 * @param text
	 *            the tooltip, which should be shown for the control
	 */
	private void setToolTipText(String text) {
		if (text == null) {
			toolTip.deactivate();
		}
		else {
			toolTip.activate();
		}

		toolTip.setText(text);
	}

}
