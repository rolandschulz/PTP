package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.ui.providers.BorderLayout.BorderData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Extend this class to add one function to a NodedisplayView.
 * All function calls from INodedisplayView are delegated to
 * an inner NodedisplayView. This view is inserted as center-
 * composite of a Borderlayout. If you want to add a composite
 * around a nodedisplayview, you can extend this class and add
 * the widgets.
 */
public class NodedisplayViewAddition extends LguiWidget implements INodedisplayView {

	protected NodedisplayView nodedisplayview;// Inner NodedisplayView shown in center of this composite

	/**
	 * Creates a wrapper composite, which acts like a NodedisplayView
	 * but has additional functions and widgets.
	 * 
	 * @param lgui
	 *            data-handler
	 * @param pmodel
	 *            the nodedisplay, which should be shown
	 * @param parent
	 *            parent composite
	 */
	public NodedisplayViewAddition(ILguiItem lgui, Nodedisplay pmodel, Composite parent) {

		super(lgui, parent, SWT.None);

		setLayout(new BorderLayout());

		nodedisplayview = new NodedisplayView(lgui, pmodel, this);
		nodedisplayview.setLayoutData(new BorderData(BorderLayout.MFIELD));
	}

	public void decreaseRectangles() {
		nodedisplayview.decreaseRectangles();
	}

	public int getMinimalRectangleSize() {
		return nodedisplayview.getMinimalRectangleSize();
	}

	public NodedisplayComp getRootNodedisplay() {
		return nodedisplayview.getRootNodedisplay();
	}

	public boolean goToImpname(String impname) {
		return nodedisplayview.goToImpname(impname);
	}

	public void increaseRectangles() {
		nodedisplayview.increaseRectangles();
	}

	public void restartZoom() {
		nodedisplayview.restartZoom();
	}

	public void setMinimalRectangleSize(int size) {
		nodedisplayview.setMinimalRectangleSize(size);
	}

	@Override
	public void update() {
		nodedisplayview.update();
	}

	public void update(ILguiItem lgui) {
		nodedisplayview.update(lgui);
	}

	public void update(ILguiItem lgui, Nodedisplay pmodel) {
		nodedisplayview.update(lgui, pmodel);
	}

	public void zoomIn(String impname) {
		nodedisplayview.zoomIn(impname);
	}

	public void zoomOut() {
		nodedisplayview.zoomOut();
	}

}
