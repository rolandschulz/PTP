package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.ui.providers.BorderLayout.BorderData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

/**
 * Adds a slider to the top of a nodedisplayview. This
 * slider allows to set the painted rectangle-size.
 * 
 */
public class NodedisplayViewZoomable extends NodedisplayViewAddition {

	private final Slider slider;

	/**
	 * Create a nodedisplay with slider on top to resize painted
	 * rectangles.
	 * 
	 * @param lgui
	 *            lguihandler
	 * @param pmodel
	 *            the nodedisplay shown by this view
	 * @param parent
	 *            parent composite
	 */
	public NodedisplayViewZoomable(ILguiItem lgui, Nodedisplay pmodel, Composite parent) {

		super(lgui, pmodel, parent);

		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		slider = new Slider(this, SWT.HORIZONTAL);
		slider.setLayoutData(new BorderData(BorderLayout.NFIELD));
		slider.setMinimum(1);
		slider.setMaximum(30);
		slider.setIncrement(1);
		slider.setSelection(5);
		slider.setToolTipText("Rectangle size");

		slider.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				nodedisplayview.setMinimalRectangleSize(slider.getSelection());
			}
		});

		addZoomListener();

		checkEmptyScreen();
	}

	@Override
	public void update() {
		super.update();
		nodedisplayview.setMinimalRectangleSize(slider.getSelection());
	}

	@Override
	public void update(ILguiItem lgui) {
		super.update(lgui);
		this.lgui = lgui;
		nodedisplayview.setMinimalRectangleSize(slider.getSelection());
		checkEmptyScreen();
	}

	@Override
	public void update(ILguiItem lgui, Nodedisplay pmodel) {
		super.update(lgui, pmodel);
		this.lgui = lgui;
		nodedisplayview.setMinimalRectangleSize(slider.getSelection());
		checkEmptyScreen();
	}

	/**
	 * Add a listener for zooming-events. On every zoom
	 * the rectangle size is adjusted to the slider-selection.
	 */
	private void addZoomListener() {

		nodedisplayview.addZoomListener(new INodedisplayZoomListener() {

			public void handleEvent(INodedisplayZoomEvent event) {
				nodedisplayview.setMinimalRectangleSize(slider.getSelection());
			}
		});

	}

	/**
	 * Sets visibility depending on available data for showing a nodedisplay.
	 */
	private void checkEmptyScreen() {
		if (lgui == null) {
			slider.setVisible(false);
			nodedisplayview.setVisible(false);
		}
		else {
			slider.setVisible(true);
			nodedisplayview.setVisible(true);
		}
	}

}
