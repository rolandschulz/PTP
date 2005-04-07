/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.tools.views;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.ParseException;
import java.util.StringTokenizer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.internal.core.model.CFloatingPointValue;
import org.eclipse.cdt.debug.internal.core.model.CIndexedValue;
import org.eclipse.cdt.debug.internal.core.model.CVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;


/**
 * Basic 2D vector field visualization plugin.
 * 
 * matt sottile / matt@lanl.gov
 */

public class VectorViewer extends Viewer {
	private Composite viewer;
	
	private Color white;
	private Color red;
	private Color black;

	private Rectangle drawBounds;

	class VectorDataField {
		public int width, height;
		public float x[][], y[][];
		public float yscale;
		public float xscale;
		
		public VectorDataField(int w, int h, float[][] xv, float[][] yv) {
			width = w;
			height = h;
			x = xv;
			y = yv;
			xscale = (float) 1.0;
			yscale = (float) 1.0;
		}
		
		public VectorDataField(int w, int h) {
			this(w, h, new float[w][h], new float[w][h]);
		}
	}
	
	private VectorDataField vdata;
	
    /**
	 * read a 2D data set from filename with dimensions width w and height h
	 */
	private float[][] read_dataset(String filename, int w, int h) {
		String line;
		StringTokenizer stok;
		BufferedReader in;
		float[][] data = new float[w][h];
		int i = 0;
		int j = 0;

		try {
			in = new BufferedReader(new FileReader(filename));

			line = in.readLine();
			while (line != null) {
				stok = new StringTokenizer(line, " ");

				while (stok.hasMoreTokens()) {
					data[i][j] = (new Float(stok.nextToken())).floatValue();
					i++;
				}
				i = 0;

				j++;
				line = in.readLine();
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
		}
		return data;
	}

	private void updateDataset(VectorDataField v) {
		float max_x, min_x, max_y, min_y;
		
		max_x = v.x[0][0];
		min_x = v.x[0][0];
		max_y = v.y[0][0];
		min_y = v.y[0][0];
		
		for (int i = 0; i < v.width; i++) {
			for (int j = 0; j < v.height; j++) {
				if (v.x[i][j] < min_x) min_x = v.x[i][j];
				if (v.y[i][j] < min_y) min_y = v.y[i][j];
				if (v.x[i][j] > max_x) max_x = v.x[i][j];
				if (v.y[i][j] > max_y) max_y = v.y[i][j];
			}
		}
		
		if (max_x == min_x)
			v.xscale = (float)1.0;
		else
			v.xscale = 1/(max_x - min_x);
		
		if (max_y == min_y)
			v.yscale = (float)1.0;
		else
			v.yscale = 1/(max_y - min_y);
	}
	
	private int parseRepeats(StreamTokenizer s) throws IOException, ParseException {
		int reps;
		
		if (s.nextToken() != '<') {
			s.pushBack();
			return 0;
		}
		
		if (s.nextToken() != StreamTokenizer.TT_WORD || !s.sval.matches("repeats"))
			throw new ParseException("Expected 'repeats'", 0);

		if (s.nextToken() != StreamTokenizer.TT_WORD)
			throw new ParseException("Expected <number>", 0);
		
		try {
			reps = Integer.parseInt(s.sval);
		} catch (NumberFormatException e) {
			throw new ParseException("Not a valid number '" + s.sval + "'", 0);
		}
		
		if (s.nextToken() != StreamTokenizer.TT_WORD || !s.sval.matches("times"))
			throw new ParseException("Expected 'times'", 0);
		
		if (s.nextToken() != '>')
			throw new ParseException("Expected '>'", 0);
		
		return reps;
	}
	
	private void parseYVals(StreamTokenizer s, int x, float[][] res) throws IOException, ParseException {
		int y = 0;
		float num;
		
		if (s.nextToken() != '{')
			throw new ParseException("Expected '{'", 0);
		
		for (;;) {
			if (s.nextToken() != StreamTokenizer.TT_WORD)
				throw new ParseException("Expected <number>", 0);
			
			try {
				if (s.sval.startsWith("nan") || s.sval.startsWith("-nan")) {
					num = Float.NaN;
				} else {
					num = Float.parseFloat(s.sval);
				}
			} catch (NumberFormatException e) {
				throw new ParseException("Not a valid number '" + s.sval + "'", 0);
			}

			res[x][y++] = num;
				
			int reps = parseRepeats(s);
			
			while (--reps > 0) {
				res[x][y++] = num;
			}
			
			if (s.nextToken() == '}')
				break;
			
			if (s.ttype != ',')
				throw new ParseException("Expected ','", 0);
		}
	}
	
	private void parseXVals(StreamTokenizer s, int[] dims, float[][] res) throws IOException, ParseException {
		int x = 0;
		
		if (s.nextToken() != '{')
			throw new ParseException("Expected '{'", 0);
			
		for (;;) {
			parseYVals(s, x, res);
			
			int reps = parseRepeats(s);
			
			while (--reps > 0) {
				x++;
				for (int y = 0; y < dims[1]; y++) {
					res[x][y] = res[x-1][y];
				}
			}
			
			x++;
			
			if (s.nextToken() == '}')
				break;
			
			if (s.ttype != ',')
				throw new ParseException("Expected ','", 0);
		}
	}

	private float[][] convertToArray(String val, int[] dims) {
		int x = 0;
		int y = 0;
		boolean startrep = false;
		int repeats = 0;
		float[][] res = new float[dims[0]][dims[1]];
		
		StringReader s = new StringReader(val);
		StreamTokenizer st = new StreamTokenizer(s);
		st.resetSyntax();
		st.wordChars('\u0021', '\u007e');
		st.whitespaceChars('\u0000', '\u0020');
		st.ordinaryChar('{');
		st.ordinaryChar('}');
		st.ordinaryChar(',');
		st.ordinaryChar('<');
		st.ordinaryChar('>');

		try {
			parseXVals(st, dims, res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println("parse error: " + e.getMessage());
			System.out.println("string: " + val);
			System.out.println("last token was: " + st.ttype + " (" + st.toString() + ")");
			return null;
		}
		
		return res;
	}
	
	private VectorDataField convertToVectorDataField(String valX, String valY, int[] dims) {
		float[][] vx = convertToArray(valX, dims);
		float[][] vy = convertToArray(valY, dims);
		
		VectorDataField v = null;
		
		if (vx != null && vy != null) {
			v = new VectorDataField(dims[0], dims[1], vx, vy);
		}
		return v;
	}
	
	private VectorDataField convertToVectorData(IVariable varX, IVariable varY) {
		VectorDataField v = null;
		String valueX = null;
		String valueY = null;
		int[] dims;

		if (varX instanceof CVariable && varY instanceof CVariable) {
			CVariable cvarX = (CVariable)varX;
			CVariable cvarY = (CVariable)varY;

			try {
				if (cvarX.getType().isArray() && cvarY.getType().isArray()) {
					dims = cvarX.getType().getArrayDimensions();
					int[] dims2 = cvarX.getType().getArrayDimensions();
					if (dims.length == 2 && dims2.length == 2 && dims[0] == dims2[0] && dims[1] == dims2[1]) {
						try {
							ICDITarget ctg = cvarX.getCDITarget();
							ICDIThread cth = ctg.getCurrentThread();
							ICDIStackFrame cs[] = cth.getStackFrames();
							for (int f = 0; f < cs.length; f++) {
								valueX = ctg.evaluateExpressionToString(cs[f], varX.getName());
							}
						} catch (CDIException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			
						try {
							ICDITarget ctg = cvarY.getCDITarget();
							ICDIThread cth = ctg.getCurrentThread();
							ICDIStackFrame cs[] = cth.getStackFrames();
							for (int f = 0; f < cs.length; f++) {
								valueY = ctg.evaluateExpressionToString(cs[f], varY.getName());
							}
						} catch (CDIException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						if (valueX != null && valueY != null) {
							v = convertToVectorDataField(valueX, valueY, dims);
							updateDataset(v);
						}
					}
				}
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			try {
				if (cvalX.getType().isArray() && cvalY.getType().isArray()) {
					int[] dimsX = cvalX.getType().getArrayDimensions();
					int[] dimsY = cvalX.getType().getArrayDimensions();
					if (dimsX.length == 2 && dimsY.length == 2 && dimsX[0] == dimsY[0] && dimsX[1] == dimsY[1]) {
						v = new VectorDataField(dimsX[0], dimsX[1]);
						for (int x = 0; x < dimsX[0]; x++) {
							IVariable vXx = cvalX.getVariable(x);
							IVariable vYx = cvalY.getVariable(x);
							CIndexedValue cvx = (CIndexedValue)vXx.getValue();
							CIndexedValue cvy = (CIndexedValue)vYx.getValue();
							for (int y = 0; y < dimsX[1]; y++) {
								IVariable vXy = cvx.getVariable(y);
								IVariable vYy = cvy.getVariable(y);
								IValue vx = vXy.getValue();
								IValue vy = vYy.getValue();
								if (vx instanceof CFloatingPointValue) {
									CFloatingPointValue fx = (CFloatingPointValue)vx;
									try {
										v.x[x][y] = fx.getFloatingPointValue().floatValue();
									} catch (CDIException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
								if (vy instanceof CFloatingPointValue) {
									CFloatingPointValue fy = (CFloatingPointValue)vy;
									try {
										v.y[x][y] = fy.getFloatingPointValue().floatValue();
									} catch (CDIException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}

							}
						}
						updateDataset(v);
					}
				}
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/
		}

		return v;
	}

	/**
	 * The constructor.
	 */
	public VectorViewer(Composite parent) {
		drawBounds = null;
		vdata = null;
		viewer = new Composite(parent, SWT.NONE);
		white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		black = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		viewer.setBackground(white);
		viewer.addPaintListener(new PaintListener() {
		    	public void paintControl(PaintEvent e) {
		    		if (e.widget instanceof Composite) {
		    			drawBounds = ((Composite) e.widget).getBounds();
		    		} else {
		    			System.out.println("Unexpected event source: "+e.widget.toString());
		    		}
		    		paintCanvas(e.gc);
		    	}
	    });
	}

    public void paintCanvas(GC gc)
    {
    		int cellWidth, cellHeight;
    		int x_scale = 40;
    		int y_scale = 40;
    		
    		if (drawBounds == null || vdata == null) return;
    	
    		//if (vdata == null) {
    		//	update_dataset();
    		//}
    		
    		cellWidth = drawBounds.width / vdata.width;
    		cellHeight = drawBounds.height / vdata.height;
    		
		gc.setBackground(red);
		gc.setForeground(black);
		
		for (int y = 0; y < vdata.height; y++) {
			for (int x = 0; x < vdata.width; x++) {
				gc.drawLine((int)((x*cellWidth)+(cellWidth/2)),
						(int)((y*cellHeight)+(cellHeight/2)),
						(int)((x*cellWidth)+(cellWidth/2)+(vdata.x[x][y] * vdata.xscale * x_scale)),
						(int)((y*cellHeight)+(cellHeight/2)+(vdata.y[x][y] * vdata.yscale * y_scale)));
			}
		}
		
    }
    
    /**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#getInput()
	 */
	public Object getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#getSelection()
	 */
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh() {
		viewer.redraw();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#setInput(java.lang.Object)
	 */
	public void setInput(Object input) {
		if (input != null) {
			VectorView v = (VectorView)input;
			IVariable x = v.getXVariable();
			IVariable y = v.getYVariable();
			if (x != null & y != null) {
				vdata = convertToVectorData(x, y);
				refresh();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		// TODO Auto-generated method stub
		
	}

}