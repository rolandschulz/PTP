package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
/**
 * This class contains a BorderLayout, which is loosely patterned after the old
 * AWT BorderLayout. It uses the <code>BorderData</code> class to determine
 * positioning of controls. To position controls, call <code>control.setLayoutData()</code>,
 * passing the <code>BorderData</code> of your choice.
 * 
 * For example:
 * 
 * <code>
 *  shell.setLayoutData(new BorderLayout());
 *  Button button = new Button(shell, SWT.PUSH); 
 *  button.setLayoutData(BorderData.NORTH);
 * </code>
 * 
 * Note that you can add as many controls to the same direction as you like, but
 * the last one added for the direction will be the one displayed.
 */
public class BorderLayout2 extends Layout {
	private Control north;
	private Control south;
	private Control east;
	private Control west;
	private Control center;

	public static enum BorderData{
		NORTH, SOUTH, WEST, EAST, CENTER
	}

	/**
	 * Computes the size for this BorderLayout.
	 * 
	 * @param composite the composite that contains the controls
	 * @param wHint width hint in pixels for the minimum width
	 * @param hHint height hint in pixels for the minimum height
	 * @param flushCache if true, flushes any cached values
	 * @return Point
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
	 *      int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		getControls(composite);
		int width = 0, height = 0;

		// The width is the width of the west control
		// plus the width of the center control
		// plus the width of the east control.
		// If this is less than the width of the north
		// or the south control, however, use the largest
		// of those three widths.
		width += west == null ? 0 : getSize(west, flushCache).x;
		width += east == null ? 0 : getSize(east, flushCache).x;
		width += center == null ? 0 : getSize(center, flushCache).x;

		if (north != null) {
			Point pt = getSize(north, flushCache);
			width = Math.max(width, pt.x);
		}
		if (south != null) {
			Point pt = getSize(south, flushCache);
			width = Math.max(width, pt.x);
		}

		// The height is the height of the north control
		// plus the height of the maximum height of the
		// west, center, and east controls
		// plus the height of the south control.
		height += north == null ? 0 : getSize(north, flushCache).y;
		height += south == null ? 0 : getSize(south, flushCache).y;

		int heightOther = center == null ? 0 : getSize(center, flushCache).y;
		if (west != null) {
			Point pt = getSize(west, flushCache);
			heightOther = Math.max(heightOther, pt.y);
		}
		if (east != null) {
			Point pt = getSize(east, flushCache);
			heightOther = Math.max(heightOther, pt.y);
		}
		height += heightOther;

		// Respect the wHint and hHint
		return new Point(Math.max(width, wHint), Math.max(height, hHint));
	}

	/**
	 * This does the work of laying out our controls.
	 * 
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
		getControls(composite);
		Rectangle rect = composite.getClientArea();
		int left = rect.x, right = rect.width, top = rect.y, bottom = rect.height;
		if (north != null) {
			Point pt = getSize(north, flushCache);
			north.setBounds(left, top, rect.width, pt.y);
			top += pt.y;
		}
		if (south != null) {
			Point pt = getSize(south, flushCache);
			south.setBounds(left, rect.height - pt.y, rect.width, pt.y);
			bottom -= pt.y;
		}
		if (east != null) {
			Point pt = getSize(east, flushCache);
			east.setBounds(rect.width - pt.x, top, pt.x, (bottom - top));
			right -= pt.x;
		}
		if (west != null) {
			Point pt = getSize(west, flushCache);
			west.setBounds(left, top, pt.x, (bottom - top));
			left += pt.x;
		}
		if (center != null) {
			center.setBounds(left, top, (right - left), (bottom - top));
		}
	}

	protected Point getSize(Control control, boolean flushCache) {
		return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
	}

	protected void getControls(Composite composite) {
		// Iterate through all the controls, setting
		// the member data according to the BorderData.
		// Note that we overwrite any previously set data.
		// Note also that we default to CENTER
		Control[] children = composite.getChildren();
		for (int i = 0, n = children.length; i < n; i++) {
			Control child = children[i];
			BorderData borderData = (BorderData) child.getLayoutData();
			if (borderData == BorderData.NORTH)
				north = child;
			else if (borderData == BorderData.SOUTH)
				south = child;
			else if (borderData == BorderData.EAST)
				east = child;
			else if (borderData == BorderData.WEST)
				west = child;
			else
				center = child;
		}
	}

	//Send questions, comments, bug reports, etc. to the authors:

	//Rob Warner (rwarner@interspatial.com)
	//Robert Harris (rbrt_harris@yahoo.com)

	public static void main(String[] args) {

		BorderData bd = BorderData.NORTH;
		Object bdo = bd;
		System.out.println(bdo == BorderData.SOUTH);

		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new BorderLayout2());
		Button b1 = new Button(shell, SWT.PUSH);
		b1.setText("North");
		b1.setLayoutData(BorderData.NORTH);
		Button b2 = new Button(shell, SWT.PUSH);
		b2.setText("South");
		b2.setLayoutData(BorderData.SOUTH);
		Button b3 = new Button(shell, SWT.PUSH);
		b3.setText("East");
		b3.setLayoutData(BorderData.EAST);
		Button b4 = new Button(shell, SWT.PUSH);
		b4.setText("WestWest");
		b4.setLayoutData(BorderData.WEST);
		Button b5 = new Button(shell, SWT.PUSH);
		b5.setText("Center");
		b5.setLayoutData(BorderData.CENTER);
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}


