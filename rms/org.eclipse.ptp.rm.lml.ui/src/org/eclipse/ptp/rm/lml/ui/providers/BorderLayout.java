package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class BorderLayout extends Layout {
	public static class BorderData {
		public int field;

		public BorderData() {
			this.field = MFIELD;
		}

		public BorderData(int field) {
			this.field = field;
		}
	}

	public static final int NWFIELD = 0;
	public static final int NFIELD = 1;
	public static final int NEFIELD = 2;
	public static final int WFIELD = 3;
	public static final int MFIELD = 4;
	public static final int EFIELD = 5;
	public static final int SWFIELD = 6;
	public static final int SFIELD = 7;

	public static final int SEFIELD = 8;

	Point[] points;
	Control[] controls;

	int width;
	int height;
	// Maximum width and height of every column and row of this composite
	private int westmax, centerwidthmax, eastmax, northmax, centerheightmax, southmax;

	/**
	 * Compute the preferred size of this composite excluding
	 * the center-field.
	 * 
	 * @param composite
	 * @return width and height in a Point-instance
	 */
	public Point computeSizeWithoutCenter(Composite composite) {
		getControlsAndPoints(composite.getChildren(), true);

		width = westmax + eastmax;
		height = northmax + southmax;

		return new Point(width, height);
	}

	/**
	 * Sets private attributes westmax, centermax1 ...
	 * Computes maximum widths and heights in every row and column.
	 */
	private void computeMaxima() {

		westmax = max(points[NWFIELD].x, points[WFIELD].x, points[SWFIELD].x);
		centerwidthmax = max(points[NFIELD].x, points[MFIELD].x, points[SFIELD].x);
		eastmax = max(points[NEFIELD].x, points[EFIELD].x, points[SEFIELD].x);

		northmax = max(points[NWFIELD].y, points[NFIELD].y, points[NEFIELD].y);
		centerheightmax = max(points[WFIELD].y, points[MFIELD].y, points[EFIELD].y);
		southmax = max(points[SWFIELD].y, points[SFIELD].y, points[SEFIELD].y);
	}

	private void getControlsAndPoints(Control[] children, boolean flushCache) {
		controls = new Control[9];
		points = new Point[9];
		for (final Control element : children) {

			final Object layoutData = element.getLayoutData();
			if (!(layoutData instanceof BorderData)) {
				continue;
			}
			final BorderData borderData = (BorderData) layoutData;
			if (borderData != null && (borderData.field >= 0 && borderData.field <= 8)) {
				controls[borderData.field] = element;
				points[borderData.field] = element.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			}
		}

		for (int i = 0; i < controls.length; i++) {
			final Control control = controls[i];
			if (control == null) {
				points[i] = new Point(0, 0);
			}
		}

		computeMaxima();
	}

	private int max(int i1, int i2, int i3) {
		final int j = i1 >= i2 ? i1 : i2;
		return j >= i3 ? j : i3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
	 * int, int, boolean)
	 */
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		if (flushCache || points == null) {
			getControlsAndPoints(composite.getChildren(), flushCache);
		}

		width = westmax + centerwidthmax + eastmax;
		height = northmax + centerheightmax + southmax;

		return new Point(wHint == SWT.DEFAULT ? width : wHint, hHint == SWT.DEFAULT ? height : hHint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 * boolean)
	 */
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		if (flushCache || points == null) {
			getControlsAndPoints(composite.getChildren(), flushCache);
		}
		final Rectangle clientArea = composite.getClientArea();

		// Put the rest of space to the center-field
		centerwidthmax = clientArea.width - westmax - eastmax;
		centerheightmax = clientArea.height - northmax - southmax;

		if (controls[NWFIELD] != null) {
			controls[NWFIELD].setBounds(
					clientArea.x,
					clientArea.y,
					westmax,
					northmax);
		}
		if (controls[NFIELD] != null) {
			controls[NFIELD].setBounds(
					clientArea.x + westmax,
					clientArea.y,
					centerwidthmax,
					northmax);
		}
		if (controls[NEFIELD] != null) {
			controls[NEFIELD].setBounds(
					clientArea.x + westmax + centerwidthmax,
					clientArea.y,
					eastmax,
					northmax);
		}
		if (controls[WFIELD] != null) {
			controls[WFIELD].setBounds(
					clientArea.x,
					clientArea.y + northmax,
					westmax,
					centerheightmax);
		}
		if (controls[MFIELD] != null) {
			controls[MFIELD].setBounds(
					clientArea.x + westmax,
					clientArea.y + northmax,
					centerwidthmax,
					centerheightmax);
		}
		if (controls[EFIELD] != null) {
			controls[EFIELD].setBounds(
					clientArea.x + westmax + centerwidthmax,
					clientArea.y + northmax,
					eastmax,
					centerheightmax);
		}
		if (controls[SWFIELD] != null) {
			controls[SWFIELD].setBounds(
					clientArea.x,
					clientArea.y + northmax + centerheightmax,
					westmax,
					southmax);
		}
		if (controls[SFIELD] != null) {
			controls[SFIELD].setBounds(
					clientArea.x + westmax,
					clientArea.y + northmax + centerheightmax,
					centerwidthmax,
					southmax);
		}
		if (controls[SEFIELD] != null) {
			controls[SEFIELD].setBounds(
					clientArea.x + westmax + centerwidthmax,
					clientArea.y + northmax + centerheightmax,
					eastmax,
					southmax);
		}

	}

}
