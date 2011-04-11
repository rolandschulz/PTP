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

package org.eclipse.ptp.rm.lml.ui.providers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * SWT-Conversion of class ImagePanel
 * 
 * Just a composite with an background image loaded from an url
 */
public class ImageComp extends Composite {
	
	private Image img;//Image which is painted on this Composite
	
	private static HashMap<URL, Image> urlToImage = new HashMap<URL, Image>();
	
	private double prefwidth, prefheight;//Preferred with and height 0..1 in percentage of parent component
	
	/**
	 * @param parent parent Component
	 * @param style SWT-Style
	 * @param imageurl url of background image
	 * @param pwidth preferred percentage width 0..1
	 * @param pheight preferred percentage height 0..1
	 * @throws IOException
	 */
	public ImageComp(Composite parent, int style, URL imageurl, double pwidth, double pheight) throws IOException {
		
		super(parent, style );
		
		if (urlToImage.containsKey(imageurl)) {
			init(urlToImage.get(imageurl));
		}
		else {
			Image img = new Image(Display.getCurrent(), imageurl.openConnection().getInputStream());
			urlToImage.put(imageurl, img);
			init(img);
		}
		
		setPreferredPercentageSize(pwidth, pheight);
	}
	
	/**
	 * Set percentage values for height an width
	 * Defines dimensions of pictures
	 * @param pwith
	 * @param pheight
	 */
	public void setPreferredPercentageSize(double pwidth, double pheight) {
		prefwidth = pwidth;
		prefheight = pheight;
	}
	
	
	public Point computeSize(int wHint, int hHint) {
		
		return computeSize(wHint, hHint, true);
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		
		Point psize = this.getParent().getSize();//size of parent component
		
		return new Point( (int)Math.round(psize.x*prefwidth), (int)Math.round(psize.y*prefheight) );
	}
	
	
	
	/**
	 * @param parent from Composite constructor
	 * @param style from Composite constructor
	 * @param pimg Image-instance
	 * @throws IOException
	 */
	public void init(Image pimg) {

		setLayout(new FillLayout());

		img = pimg;
		
		//Paint image if component is painted
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {

				e.gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height,
						0, 0, getSize().x, getSize().y);

			}
		});

	}
	
	public Image getImage() {
		return img;
	}
	
	public static void disposeAll() {
		for (Image img: urlToImage.values()) {
			img.dispose();
		}
		urlToImage.clear();
	}
	
}
