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
package org.eclipse.ptp.rm.lml.ui.providers.support;

import java.util.HashMap;

import org.eclipse.ptp.rm.lml.core.model.LMLColor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * This class is used to convert swing-colors to swt-colors.
 * The created colors are managed by this class. They dont have to
 * be disposed on your own. Just call disposeColors() for disposing all colors
 * when you are finished with using all of them.
 */
public class ColorConversion {

	/**
	 * Mapping of integer-id of a color to swt-color-instances
	 * The id is created by red<<16 | green<<8 | blue, so color-values are collected in one Integer
	 */
	private static HashMap<Integer, Color> colorMap = new HashMap<Integer, Color>();

	/**
	 * Delete all colors from memory after usage.
	 * Call this method, if the colors created by this class
	 * are not needed any more
	 */
	public static void disposeColors() {
		for (final Color color : colorMap.values()) {
			color.dispose();
		}
		colorMap.clear();
	}

	/**
	 * Create a swt-color defined by red, green and blue color
	 * values. Take the color from the colormap, if the color was
	 * already created. Otherwise create a new color
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @return swt-color defined by the three color-values
	 */
	public static Color getColor(int red, int green, int blue) {

		final int id = getId(red, green, blue);

		if (colorMap.containsKey(id)) {
			return colorMap.get(id);
		}

		final Color result = new Color(Display.getCurrent(), red, green, blue);

		colorMap.put(id, result);

		return result;
	}

	/**
	 * Convert abstract LMLcolor-instance to swt-color.
	 * 
	 * @param lmlColor
	 *            abstract color instance
	 * @return corresponding swt-color
	 */
	public static Color getColor(LMLColor lmlColor) {
		return getColor(lmlColor.getRed(), lmlColor.getGreen(), lmlColor.getBlue());
	}

	/**
	 * Create a integer-ID from three color-values.
	 * 
	 * @param red
	 *            red value 0..255
	 * @param green
	 *            green value 0..255
	 * @param blue
	 *            blue value 0..255
	 * @return id identifying this color
	 */
	private static int getId(int red, int green, int blue) {
		return red << 16 | green << 8 | blue;
	}
}
