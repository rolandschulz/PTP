package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;

/**
 * This interface defines functions, which must be provided
 * by a view, which shows a nodedisplay. A nodedisplay is
 * a graphical overview of a parallel system's state. It
 * has functions to zoom into parts of the view and to
 * update the output if LML-data changed.
 */
public interface INodedisplayView {

	/**
	 * Decreases the size of painted rectangles.
	 */
	public void decreaseRectangles();

	/**
	 * @return minimal size of painted rectangles
	 */
	public int getMinimalRectangleSize();

	/**
	 * @return currently shown nodedisplaycomp
	 */
	public NodedisplayComp getRootNodedisplay();

	/**
	 * Set node with impname as implicit name as root-node within this nodedisplay-panel.
	 * Call this function only if model did not changed.
	 * 
	 * @param impname
	 *            implicit name of a node, which identifies every node within a nodedisplay
	 * @return true, if root was changed, otherwise false
	 */
	public boolean goToImpname(String impname);

	/**
	 * Increases the size of painted rectangles.
	 */
	public void increaseRectangles();

	/**
	 * The stack which saves the last zoom-levels is restarted
	 */
	public void restartZoom();

	/**
	 * Define the minimum size of rectangles painted
	 * within this nodedisplay
	 * 
	 * @param size
	 *            width and height of painted rectangles
	 */
	public void setMinimalRectangleSize(int size);

	/**
	 * Update view and repaint current data.
	 * This is done by creating a completely new nodedisplay.
	 * Tries to go to the implicit name, which was shown
	 * before.
	 */
	public void update();

	/**
	 * Call this update if lguiitem changes. This update
	 * is calles if another system is monitored.
	 * 
	 * @param lgui
	 *            new data-manager
	 */
	public void update(ILguiItem lgui);

	/**
	 * Main update function. Sets a new lgui-handler
	 * and a new nodedisplay-model.
	 * 
	 * @param lgui
	 *            lguihandler
	 * @param pmodel
	 *            nodedisplay model
	 */
	public void update(ILguiItem lgui, Nodedisplay pmodel);

	/**
	 * Set a child-element as root-element. This causes
	 * going into a mor detailed view of this part of the
	 * nodedisplay.
	 */
	public void zoomIn(String impname);

	/**
	 * Go one level higher in zoomstack
	 */
	public void zoomOut();

}
