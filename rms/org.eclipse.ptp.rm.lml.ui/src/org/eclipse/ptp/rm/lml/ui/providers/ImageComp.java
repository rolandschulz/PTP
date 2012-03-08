/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch,FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.providers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout;
import org.eclipse.ptp.rm.lml.ui.providers.support.BorderLayout.BorderData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * This is the SWT-Conversion of class ImagePanel.
 * It is just a composite with a background image loaded from an URL.
 */
public class ImageComp extends Composite {

	public static void disposeAll() {
		for (final Image image : urlToImage.values()) {
			image.dispose();
		}
		urlToImage.clear();
	}

	/**
	 * Image which is painted on this Composite
	 */
	private Image image;

	private static HashMap<URL, Image> urlToImage = new HashMap<URL, Image>();

	/**
	 * Preferred width and height 0..1 in
	 * percentage of parent component.
	 */
	private double prefWidth, prefHeight;

	/**
	 * Create a composite with an image painted on it.
	 * 
	 * @param parent
	 *            parent Component
	 * @param style
	 *            SWT-Style
	 * @param imageUrl
	 *            url of background image
	 * @param width
	 *            preferred percentage width 0..1
	 * @param height
	 *            preferred percentage height 0..1
	 * @throws IOException
	 */
	public ImageComp(Composite parent, int style, URL imageUrl, double width, double height) throws IOException {

		super(parent, style);

		if (urlToImage.containsKey(imageUrl)) {
			init(urlToImage.get(imageUrl));
		}
		else {
			final Image image = new Image(Display.getCurrent(), imageUrl.openConnection().getInputStream());
			urlToImage.put(imageUrl, image);
			init(image);
		}

		setPreferredPercentageSize(width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int)
	 */
	@Override
	public Point computeSize(int width, int height) {

		return computeSize(width, height, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int cWidth, int cHeight, boolean changed) {

		// size of parent component
		final Point point = this.getParent().getSize();

		double width = 0;
		double height = 0;

		final BorderData layoutData = (BorderData) getLayoutData();

		if (layoutData.field == BorderLayout.NFIELD || layoutData.field == BorderLayout.SFIELD) {
			height = point.y * prefHeight;
		}
		if (layoutData.field == BorderLayout.EFIELD || layoutData.field == BorderLayout.WFIELD) {
			width = point.x * prefWidth;
		}

		return new Point((int) Math.round(width), (int) Math.round(height));
	}

	/**
	 * A ImageComp is mainly used in a nodedisplay for showing
	 * images aligned to nodedisplays. This function returns the
	 * alignment corresponding to the constants defined in the
	 * BorderLayout-class.
	 * 
	 * @return integer in correspondence with constants of the BorderLayout-class, -1 if something went wrong
	 */
	public int getAlignment() {
		if (getLayoutData() instanceof BorderData) {
			return ((BorderData) getLayoutData()).field;
		}

		return -1;
	}

	/**
	 * @return the currently painted image of this composite.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Set percentage values for height an width
	 * Defines dimensions of pictures
	 * 
	 * @param pwith
	 * @param height
	 */
	public void setPreferredPercentageSize(double width, double height) {
		prefWidth = width;
		prefHeight = height;
	}

	/**
	 * Set layout definitions and make sure that
	 * the created image is painted.
	 * 
	 * @param parent
	 *            from Composite constructor
	 * @param style
	 *            from Composite constructor
	 * @param img
	 *            Image-instance
	 * @throws IOException
	 */
	protected void init(Image img) {

		setLayout(new FillLayout());

		image = img;

		// Paint image if component is painted
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				e.gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, getSize().x, getSize().y);

			}
		});

	}

}
