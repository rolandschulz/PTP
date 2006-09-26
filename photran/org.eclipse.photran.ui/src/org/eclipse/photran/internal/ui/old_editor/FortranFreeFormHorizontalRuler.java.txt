package org.eclipse.photran.internal.ui.old_editor;

import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * @author nchen
 * @author cheah
 * 
 * Basic ruler that extends as wide as the editor
 * 
 * Based on the work of the zen.xhtml.editor that was released in the Eclipse newsgroups
 * Zenil - org.zen.xhtml.editor
 */
public class FortranFreeFormHorizontalRuler extends AbstractHorizontalRuler {

    protected static RGB DARK_GRAY = new RGB(0x80, 0x80, 0x80);
    protected static RGB LIGHT_GRAY = new RGB(0xDD, 0xDD, 0xDD);
    
    protected static Color fDarkGrayColor = new Color(null, DARK_GRAY);
    protected static Color fLightGrayColor = new Color(null, LIGHT_GRAY);

    /**
	 * Construct the Horizontal ruler
	 * @param ruler 
	 * @param the parent control
	 */
	public FortranFreeFormHorizontalRuler(IVerticalRuler ruler, Composite parent) {
		super(parent, SWT.NO_BACKGROUND);
		verticalRuler = ruler;
	}

	/**
	 * Paints the ruler
	 * @param g the Graphics context
	 */
	protected void paintComponent(GC g) {
		// we need the vertical ruler width so that we know where to start positioning the 
		// horizontal ruler. The width of the vertical ruler depends on the number of digits 
		// for the line numbers
		int verticalRulerWidth = verticalRuler.getWidth();
		Point size = getSize();

		if (fWidth != size.x) {
			fWidth = size.x;
		}

		avgCharWidth = g.getFontMetrics().getAverageCharWidth();

		//This gives the maximum number of characters that can
		//be accomadated on the screen.Based on this value we can 
		//draw the the horizontal ruler string 
		int maxChars = (fWidth) / avgCharWidth;

		//First draw from start till end in grey color
		int height = size.y;
		g.fillRectangle(0, 0, fWidth, height);

		g.setBackground(fWhiteColor);

		//Then draw from start till start of vertical scrollbar in white
		g.fillRectangle(verticalRulerWidth, 0,
				fWidth - fStyledText.getVerticalBar().getSize().x
						- verticalRulerWidth, height);

		g.setClipping(verticalRulerWidth, 0,
				fWidth - fStyledText.getVerticalBar().getSize().x
						- verticalRulerWidth, height);

		g.setForeground(fDarkGrayColor);

		fDisplayText = ruler(maxChars, fStyledText.getHorizontalPixel());

		int column = getCurrentColumn();

		/* as drawing is zero relative */
		column--;

		//Get the relative starting position
		//from where the calculation of cursor 
		//pointer position can start
		int scroll = fStyledText.getHorizontalPixel();

		//some few pixels remaining
		int rem = scroll % avgCharWidth;

		// gets the scroll in indexes
		scroll /= avgCharWidth;

		//this gives the relative column position 
		//where the cursor pointer can be drawn 
		column -= scroll;

		//calculate the pixel position at which the
		//cursor pointer can be drawn
		int x = column * g.getFontMetrics().getAverageCharWidth() - rem;

		g.setBackground(fBlackColor);

		//start the drawing not from 0, but from -rem,as
		//to accomadate these pixels properly,and so that
		//horizontal ruler offset is in exact alignment with
		//the editor styled text offset 
		g.drawString(fDisplayText, 0 - rem + verticalRulerWidth, 0, true);

		//Draw the rectangular pointer
		g.setBackground(fLightGrayColor);
		g.fillRectangle(x + verticalRulerWidth, 0, g.getFontMetrics().getAverageCharWidth(), height);

		g.setForeground(fBlackColor);

		char c = 0;

		int index = column;

		//Highlight the character on which cursor is present
		c = (index < 0 || index >= fDisplayText.length()) ? ' ' : fDisplayText.charAt(index);
		g.drawString(String.valueOf(c), x + verticalRulerWidth, 0, true);
	}

	/**
	 * Return the ruler string to be drawn.
	 *
	 * @param maxCol the maximum colum number
	 * @param start  the  start position ,which is the start position of the document
	 */
	protected String ruler(int maxCol, int start) {

		//start is in pixels,
		//Convert it to index.
		start /= avgCharWidth;

		//Now start gives the position from which
		//to start the calculation of the horizontal ruler string   

		StringBuffer horizontalString = new StringBuffer(BLANK);

		int begin = 1;

		for (int i = begin + start; i <= maxCol + start; i++) {

			if ((i % 10) == 0) {
				horizontalString.append((i / 10) % 10);
				continue;
			}

			if ((i % 5) == 0) {
				horizontalString.append(PLUS);
				continue;
			}

			horizontalString.append(MINUS);
		}
		return horizontalString.toString();
	}
}
