package org.eclipse.ptp.rm.lml.ui.providers;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A listener, which paints DisplayNode-instances within a panel. This is used for the lowest level
 * rectangles in a nodedisplay. It is faster to paint rectangles than using a gridlayout and inserting nodedisplay-
 * composites for every rectangle. As a result this implementation causes less general layouts
 * (for example: it is more difficult to react for cursor-focus on these rectangles,
 * texts as titles have to be painted
 * and can not be inserted by using a layout-manager)
 * 
 * 
 * @author karbach
 * 
 */
public class RectPaintListener implements Listener {

	private final ArrayList<DisplayNode> dispnodes;// nodes which are painted in the composite
	private final HashMap<DisplayNode, Color> dispnodetocolor;// map for fast access to displaynode-colors
	private final HashMap<DisplayNode, Rectangle> dispnodetorectangle;// map containing positions of dispnodes, they might change in
																		// every paint
	private int COLUMNCOUNT;// count of columns in the grid
	private final NodedisplayComp nodecomp;// The composite which is painted by this listener

	public Color bordercolor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

	public int marginWidth = 1;// space around the grid in x-direction
	public int marginHeight = 1;// space around the grid in y-direction
	public int horizontalSpacing = 1;// space between two rectangles in x
	public int verticalSpacing = 1;// space between two rectangles in y

	public int normalborder = 0;// Border shown if displaynodes are not focussed
	public int mouseborder = 1;// Border shown if displaynodes are focussed

	// Minimal size of drawed rectangles
	public int minRectWidth = 5;
	public int minRectHeight = 5;

	private int rowcount, rectwidth, rectheight;// Parameters, which are changed when painting, they can be used by
												// getDisplayNodeAtPos

	private final ObjectStatus objstatus;

	private final Composite usingListener;

	/**
	 * Create the listener, initialize attributes and generate dispnodetocolor-map
	 * 
	 * @param pdispnodes
	 *            nodes, which should be painted
	 * @param pcolumncount
	 *            count of columns in the grid
	 * @param pnodecomp
	 *            NodedisplayComp, in which this listener is used
	 * @param pusingListener
	 *            Composite, which uses this listener
	 */
	public RectPaintListener(ArrayList<DisplayNode> pdispnodes, int pcolumncount, NodedisplayComp pnodecomp,
			Composite pusingListener) {
		dispnodes = pdispnodes;
		COLUMNCOUNT = pcolumncount;
		nodecomp = pnodecomp;

		dispnodetocolor = nodecomp.generateDisplayNodeToColorMap(dispnodes);

		dispnodetorectangle = new HashMap<DisplayNode, Rectangle>();

		objstatus = nodecomp.getLguiItem().getObjectStatus();

		usingListener = pusingListener;
	}

	/**
	 * Calculate minimum needed size for this painting.
	 * Send to root-element to increase its minimum size.
	 */
	public void calculateResize() {

		updateRectSize();

		increaseMinSize();
	}

	/**
	 * Pass a relative mouse-position on the composite, which uses this listener.
	 * Then the displaynode on focus will be returned. If no node is focussed
	 * null is returned.
	 * 
	 * @param px
	 *            x-position of cursor within the composite
	 * @param py
	 *            y-position of cursor within the composite
	 * @return focussed DisplayNode or null, if nothing is focussed
	 */
	public DisplayNode getDisplayNodeAtPos(int px, int py) {

		final int x = px - marginWidth;
		final int y = py - marginHeight;

		if (x < 0 || y < 0)
			return null;// Outside grid, left or top

		if (rectwidth == 0 || rectheight == 0)
			return null;

		final int col = x / rectwidth;
		final int row = y / rectheight;

		if (col >= COLUMNCOUNT || row >= rowcount)// Outside grid, right or bottom
			return null;

		final int index = row * COLUMNCOUNT + col;

		if (index >= dispnodes.size())
			return null;

		return dispnodes.get(index);
	}

	public void handleEvent(Event event) {

		updateRectSize();

		for (int x = 0; x < COLUMNCOUNT; x++) {

			for (int y = 0; y < rowcount; y++) {
				// get index of displaynode
				final int index = y * COLUMNCOUNT + x;
				if (index >= dispnodes.size())
					break;

				// Rectangle frame
				final Rectangle r = new Rectangle(marginWidth + rectwidth * x, marginHeight + rectheight * y,
						rectwidth - horizontalSpacing, rectheight - verticalSpacing);

				// Paint outer rectangle
				event.gc.setBackground(bordercolor);
				event.gc.fillRectangle(r.x, r.y, r.width, r.height);

				final DisplayNode dispnode = dispnodes.get(index);
				// Paint it
				if (objstatus.isAnyMousedown()
						&& !objstatus.isMousedown(dispnode.getConnectedObject())) {
					// Change color
					event.gc.setBackground(
							ColorConversion.getColor(nodecomp.getLguiItem().getOIDToObject().getColorById(null)));
				}
				else
					event.gc.setBackground(dispnodetocolor.get(dispnode));

				int border = normalborder;

				if (objstatus.isMouseover(dispnode.getConnectedObject())) {
					border = mouseborder;
				}
				event.gc.fillRectangle(r.x + border, r.y + border, r.width - 2 * border, r.height - 2 * border);

				dispnodetorectangle.put(dispnode, r);// save the current rectangle
			}

		}
	}

	/**
	 * @return minimal composite-size to show rectangles with minRectWidth and minRectHeight
	 */
	protected Point getMinimalSize() {
		return new Point(minRectWidth * COLUMNCOUNT + 2 * marginWidth,
				minRectHeight * rowcount + 2 * marginHeight);
	}

	/**
	 * Increases surrounding composite-size if needed.
	 * Takes minRectWidth and minRectHeight to calculate
	 * minimal composite-size. Increases the size if needed
	 * by setting new minsize of this nodedisplay.
	 */
	protected void increaseMinSize() {

		final Point oldSize = usingListener.getSize();
		final Point minSize = getMinimalSize();

		nodecomp.increaseMinWidth(minSize.x - oldSize.x);
		nodecomp.increaseMinHeight(minSize.y - oldSize.y);
	}

	/**
	 * Generate size of drawed rectangles.
	 * 
	 * @return size of painted rectangles
	 */
	protected void updateRectSize() {

		int w, h;

		final Point size = usingListener.getSize();
		// Generate available size
		w = size.x - marginWidth * 2;
		h = size.y - marginHeight * 2;

		if (COLUMNCOUNT <= 0)
			COLUMNCOUNT = 1;

		// Calculate how many rows have to be painted
		rowcount = dispnodes.size() / COLUMNCOUNT;
		if (dispnodes.size() % COLUMNCOUNT != 0)
			rowcount++;

		if (rowcount == 0)
			return;

		rectwidth = w / COLUMNCOUNT;

		rectheight = h / rowcount;
	}

}
