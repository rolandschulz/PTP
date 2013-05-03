/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.core.model;

/**
 * Simple color class collecting values for red, green and blue color intensity.
 * This class is independent from AWT or SWT-implementation and can easily be
 * converted into corresponding instances.
 */
public class LMLColor {
	
	/**
	 * color intensity of red. values from 0 to 255
	 */
	protected int r;
	/**
	 * color intensity of green. values from 0 to 255
	 */
	protected int g;
	/**
	 * color intensity of blue. values from 0 to 255
	 */
	protected int b;
	
	/**
     * The color white.  In the default sRGB space.
     */
    public final static LMLColor white = new LMLColor(255, 255, 255);

    /**
     * The color white.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor WHITE = white;

    /**
     * The color light gray.  In the default sRGB space.
     */
    public final static LMLColor lightGray = new LMLColor(192, 192, 192);

    /**
     * The color light gray.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor LIGHT_GRAY = lightGray;

    /**
     * The color gray.  In the default sRGB space.
     */
    public final static LMLColor gray      = new LMLColor(128, 128, 128);

    /**
     * The color gray.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor GRAY = gray;

    /**
     * The color dark gray.  In the default sRGB space.
     */
    public final static LMLColor darkGray  = new LMLColor(64, 64, 64);

    /**
     * The color dark gray.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor DARK_GRAY = darkGray;

    /**
     * The color black.  In the default sRGB space.
     */
    public final static LMLColor black 	= new LMLColor(0, 0, 0);
    
    /**
     * The color black.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor BLACK = black;
    
    /**
     * The color red.  In the default sRGB space.
     */
    public final static LMLColor red       = new LMLColor(255, 0, 0);

    /**
     * The color red.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor RED = red;

    /**
     * The color pink.  In the default sRGB space.
     */
    public final static LMLColor pink      = new LMLColor(255, 175, 175);

    /**
     * The color pink.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor PINK = pink;

    /**
     * The color orange.  In the default sRGB space.
     */
    public final static LMLColor orange 	= new LMLColor(255, 200, 0);

    /**
     * The color orange.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor ORANGE = orange;

    /**
     * The color yellow.  In the default sRGB space.
     */
    public final static LMLColor yellow 	= new LMLColor(255, 255, 0);

    /**
     * The color yellow.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor YELLOW = yellow;

    /**
     * The color green.  In the default sRGB space.
     */
    public final static LMLColor green 	= new LMLColor(0, 255, 0);

    /**
     * The color green.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor GREEN = green;

    /**
     * The color magenta.  In the default sRGB space.
     */
    public final static LMLColor magenta	= new LMLColor(255, 0, 255);

    /**
     * The color magenta.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor MAGENTA = magenta;

    /**
     * The color cyan.  In the default sRGB space.
     */
    public final static LMLColor cyan 	= new LMLColor(0, 255, 255);

    /**
     * The color cyan.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor CYAN = cyan;

    /**
     * The color blue.  In the default sRGB space.
     */
    public final static LMLColor blue 	= new LMLColor(0, 0, 255);

    /**
     * The color blue.  In the default sRGB space.
     * @since 1.4
     */
    public final static LMLColor BLUE = blue;
    
	
	/**
	 * Create a color-instance by passing RGB-values
	 * @param red red-intensity 0..255
	 * @param green green-intensity 0..255
	 * @param blue blue-intensity 0..255
	 */
	public LMLColor(int red, int green, int blue){
		this.r=red;
		this.g=green;
		this.b=blue;
	}
	
	/**
	 * Create a color from a string. The RGB-values are parsed from the string.
	 * The color is specified like in HTML. The values are concatinated as 
	 * hexadecimal values. The # at the beginning is optional.
	 * @param colorstring Allowed strings: #FFF #0000FF ffeeff cef ...
	 */
	public LMLColor(String colorstring){
		LMLColor color=stringToColor(colorstring);
		
		this.r=color.r;
		this.g=color.g;
		this.b=color.b;
	}
	
	/**
	 * @return intensity of red
	 */
	public int getRed(){
		return r;
	}
	
	/**
	 * @return intensity of green
	 */
	public int getGreen(){
		return g;
	}
	
	/**
	 * @return intensity of blue
	 */
	public int getBlue(){
		return b;
	}
	
	/**
	 * Convert a colorstring into an Color-Object
	 * 
	 * Allowed strings: #FFF #0000FF ffeeff cef ...
	 * 
	 * @param colorstring
	 * @return LMLColor-instance containing the values for red, green and blue
	 */
	public static LMLColor stringToColor(String colorstring){

		if(colorstring==null || colorstring.length()==0){
			return LMLColor.white;
		}

		if(colorstring.charAt(0)=='#'){
			colorstring=colorstring.substring(1);
		}

		int red=0;
		int green=0;
		int blue=0;


		if(colorstring.length()==3){
			red=Integer.parseInt(colorstring.substring(0,1), 16);
			green=Integer.parseInt(colorstring.substring(1,2), 16);
			blue=Integer.parseInt(colorstring.substring(2,3), 16);

			red=red+16*red;
			green=green+16*green;
			blue=blue+16*blue;
		}
		else
			if(colorstring.length()==6){
				red=Integer.parseInt(colorstring.substring(0,2), 16);
				green=Integer.parseInt(colorstring.substring(2,4), 16);
				blue=Integer.parseInt(colorstring.substring(4,6), 16);
			}
			else{
				return LMLColor.black;
			}

		return new LMLColor(red, green, blue);	
	}
	
	@Override
	public String toString() {
		return "#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b); //$NON-NLS-1$
	}

}
