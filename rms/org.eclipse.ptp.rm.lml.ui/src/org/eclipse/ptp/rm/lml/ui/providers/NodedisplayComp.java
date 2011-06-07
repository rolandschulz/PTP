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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.AlignType;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.internal.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.PictureType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLCheck.SchemeAndData;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus.Updatable;
import org.eclipse.ptp.rm.lml.ui.providers.BorderLayout.BorderData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * SWT-Translation of NodedisplayPanel, which is a Swing-Component
 * 
 * Composite to create output of a nodedisplay.
 * 
 * Inner composites are NodedisplayComp again (recursive)
 * 
 * A NodedisplayComp represents one physical element within the nodedisplay-tag.
 * This might be a row, midplane, node or cpu. Through NodedisplayComp the reduced
 * collapsed tree of lml is fully expanded to maxlevel. Every visible component
 * in the nodedisplay is shown by a NodedisplayComp.
 * There is one exception: For performance reasons the lowest-level rectangles are
 * painted directly. So for the rectangles (physical elements) in the lowest level
 * no composites are created.
 * 
 * The look of the nodedisplay is defined by the lml-Nodedisplay-Layout.
 * 
 */
public class NodedisplayComp extends LguiWidget implements Updatable {

	/**
	 * Class for comparing integer-values in ascending or descending way.
	 */
	public static class NumberComparator implements Comparator<Integer> {

		private final boolean ascending;// if true => sort ascending, otherwise descending

		public NumberComparator(boolean ascending) {
			this.ascending = ascending;
		}

		public int compare(Integer o1, Integer o2) {
			if (ascending) {
				return o1 - o2;
			}
			else
				return o2 - o1;
		}

	}

	/**
	 * Parses through scheme and generates suitable preferences
	 * for every level of the nodedisplay-tree
	 * 
	 * @param scheme
	 *            scheme of nodedisplay
	 * @return preferences for every level
	 */
	public static ArrayList<Nodedisplayelement> generatePrefsFromScheme(SchemeType scheme) {
		final int depth = LMLCheck.getDeepestSchemeLevel(scheme);

		final ArrayList<Nodedisplayelement> res = new ArrayList<Nodedisplayelement>();
		final ObjectFactory objf = new ObjectFactory();// For creating lml-objects

		final int[] maxcounts = new int[depth];
		for (int i = 0; i < maxcounts.length; i++) {
			maxcounts[i] = 0;
		}
		// Search for maximum amount of elements defined in scheme
		findMaximum(scheme, 1, maxcounts);

		for (int i = 0; i < depth; i++) {

			int cols = 8;
			if (maxcounts[i] < cols) {
				cols = maxcounts[i];
			}

			final Nodedisplayelement apref = objf.createNodedisplayelement();
			apref.setCols(BigInteger.valueOf(cols));

			// Only level 1 shows title
			if (i != 1)
				apref.setShowtitle(false);
			// bigger Level-0 gap between level1-panels
			if (i == 0) {
				apref.setHgap(BigInteger.valueOf(3));
				apref.setHgap(BigInteger.valueOf(4));
			}

			res.add(apref);
		}

		// For last level, which does not have inner elements
		res.add(NodedisplayAccess.getDefaultLayout());

		return res;
	}

	/**
	 * @return default GridData instance, which simulates Swing-behaviour of gridlayout
	 */
	public static GridData getDefaultGridData() {
		final GridData griddata = new GridData();
		griddata.grabExcessHorizontalSpace = true;
		griddata.grabExcessVerticalSpace = true;
		griddata.horizontalAlignment = GridData.FILL;
		griddata.verticalAlignment = GridData.FILL;

		return griddata;

	}

	/**
	 * Find maximum count of elements per level
	 * Traverses the whole scheme
	 * 
	 * @param scheme
	 *            lml-scheme
	 * @param level
	 *            current level (first is 1)
	 * @param maxcounts
	 *            array with length at least scheme.depth
	 */
	private static void findMaximum(Object scheme, int level, int[] maxcounts) {

		final List subschemes = LMLCheck.getLowerSchemeElements(scheme);

		int sum = 0;

		for (final Object subscheme : subschemes) {

			final SchemeElement sub = (SchemeElement) subscheme;

			if (sub.getList() != null) {// list-attribute
				sum += LMLCheck.getNumbersFromNumberlist(sub.getList()).length;
			}
			else {// min- max-attributes

				final int min = sub.getMin().intValue();
				int max = min;
				if (sub.getMax() != null) {
					max = sub.getMax().intValue();
				}

				final int step = sub.getStep().intValue();

				sum += (max - min) / step + 1;
			}
			// Recursive call for lower level
			findMaximum(sub, level + 1, maxcounts);
		}

		if (sum > 0 && sum > maxcounts[level - 1]) {
			maxcounts[level - 1] = sum;
		}

	}

	// parent composite for zooming
	private NodedisplayView fNodedisplayView;

	private String title;// implicit name of this node
	private Color jobColor;// Color of the job, to which this panel is connected

	private Font fontObject;
	private Nodedisplay fNodedisplay;// surrounding lml-Nodedisplay

	private DisplayNode fDisplayNode;// Node-model which has to be displayed
	private int maxLevel; // Deepest level, which should be displayed

	private Composite pictureFrame;// this panel contains pictures as direct children and the mainpanel in center

	private Composite mainPanel;// Important panel where everything but pictures is in

	private Composite innerPanel = null;

	private Label titleLabel;// For title-line
	// Settings for lower level
	private Nodedisplayelement apref;

	// current level, which is displayed
	private int currentLevel;

	private GridData gridData;// layout definitions for gridlayouts, makes inner panels resize
	private Color backgroundColor;// Current backgroundcolor

	private Color titleBackgroundColor;

	// Borders
	private BorderComposite borderFrame;// The only frame which has a border, borderwidth changes when mouse touches the panel

	private ArrayList<ImageComp> pictures;

	private Image centerPic;// Picture in center is special

	private ArrayList<NodedisplayComp> innerComps;// Save created inner NodedisplayComp-instances for disposing them if needed

	private static final int httpsport = 4444;// Special port for https-access

	private NodedisplayComp parentNodedisplay = null;// Reference to parent nodedisplay

	private int minWidth = 0, minHeight = 0;// Minimal width and height of this nodedisplay

	private int x = 0, y = 0;// Position of this nodedisplay within surrounding grid

	// Saves rectpaintlistener, which is needed for fast painting of inner rectangles
	private RectPaintListener rectpaintlistener = null;

	// The name of the special empty job
	private final String emptyJobName = "empty";

	/**
	 * Call this constructor for start, maxlevel is chosen from lml-file
	 * 
	 * @param lgui
	 *            wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param pmodel
	 *            lml-model, which has data for this Nodedisplay
	 * @param pnodeview
	 *            root-nodedisplay is needed for zooming
	 * @param pnode
	 *            current node, which is root-data-element of this NodedisplayComp
	 * @param style
	 *            SWT Style
	 */
	public NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview, int style) {

		super(lgui, pnodeview.getScrollPane(), style);

		// get maxlevel from lml-file
		calculateCurrentlevel(pnode);

		fNodedisplay = pmodel;
		fDisplayNode = pnode;

		apref = findLayout();// Searches for corresponding layout-definitions

		maxLevel = 10;
		if (apref.getMaxlevel() != null)
			maxLevel = apref.getMaxlevel().intValue();

		init(pmodel, pnode, pnodeview);

		setScrollBarPreferences();

		addPaintListener();
	}

	/**
	 * easy constructor for a nodedisplay as root-node
	 * 
	 * @param lgui
	 *            wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param pmodel
	 *            lml-model for the nodedisplay, which should be shown in this panel
	 * @param lgui
	 *            complete lml-model containing this nodedisplay
	 * @param parent
	 *            parameter for calling super constructor
	 * @param style
	 *            parameter for calling super constructor
	 */
	public NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, NodedisplayView pnodeview, int style) {

		this(lgui, pmodel, null, pnodeview, style);

	}

	/**
	 * Call this constructor for inner or lower elements, rellevel is counted to zero with every level
	 * 
	 * @param lgui
	 *            wrapper instance around LguiType-instance -- provides easy access to lml-information
	 * @param pmodel
	 *            lml-model, which has data for this Nodedisplay
	 * @param pnode
	 *            current node, which is root-data-element of this NodedisplayComp
	 * @param pnodeview
	 *            root-nodedisplay is needed for zooming
	 * @param pparentNodedisplay
	 *            father of this nodedisplay
	 * @param px
	 *            horizontal position of this nodedisplay in surrounding grid
	 * @param py
	 *            horizontal position of this nodedisplay in surrounding grid
	 * @param rellevel
	 *            relative level, rellevel==0 means show no lower elements
	 * @param parent
	 *            parent composite for SWT constructor
	 * @param style
	 *            SWT-style of this nodedisplay
	 */
	protected NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, DisplayNode pnode,
			NodedisplayView pnodeview, NodedisplayComp pparentNodedisplay,
			int px, int py,
			int rellevel, Composite parent, int style) {

		super(lgui, parent, style);

		// Calculate maxlevel with rellevel

		// Set Preferences
		calculateCurrentlevel(pnode);

		maxLevel = currentLevel + rellevel;
		// Save father reference
		parentNodedisplay = pparentNodedisplay;

		// Save grid-positions
		x = px;
		y = py;

		init(pmodel, pnode, pnodeview);
	}

	/**
	 * Search for a layout-section for this nodedisplay-panel
	 * 
	 * @return lml-Nodedisplay-layout-section for this displaynode, or default-layout if no layout is defined
	 */
	public Nodedisplayelement findLayout() {

		Nodedisplayelement res = null;

		if (lguiItem.getNodedisplayAccess() != null) {
			final ArrayList<NodedisplaylayoutType> allNodedisplayLayouts = lguiItem.getNodedisplayAccess()
					.getLayouts(fNodedisplay.getId());

			// Is there any Layout for this nodedisplay?
			if (allNodedisplayLayouts == null || allNodedisplayLayouts.size() == 0)
				return NodedisplayAccess.getDefaultLayout();
			// Later it should be possible to choose one of possibly multiple defined layouts
			final NodedisplaylayoutType first = allNodedisplayLayouts.get(0);

			if (fDisplayNode == null) {// Root-level => return el0-Nodedisplayelement
				if (first.getEl0() != null) {
					return first.getEl0();
				}
				else
					return NodedisplayAccess.getDefaultLayout();
			}

			if (first.getEl0() == null)
				return NodedisplayAccess.getDefaultLayout();

			// Copy level-numbers
			final ArrayList<Integer> levelnrs = LMLCheck.copyArrayList(fDisplayNode.getLevelNrs());

			// deeper-level => traverse layout-tree
			res = LMLCheck.getNodedisplayElementByLevels(levelnrs, first.getEl0());
		}

		if (res == null)
			return NodedisplayAccess.getDefaultLayout();
		else
			return res;

	}

	/**
	 * Generates a hashmap, which connects DisplayNodes to their SWT-colors.
	 * 
	 * @param displayNodes
	 *            The DisplayNodes, which are keys from the resulting hashmap
	 * @return hashmap, which connects DisplayNodes to their SWT-colors
	 */
	public HashMap<DisplayNode, Color> generateDisplayNodeToColorMap(ArrayList<DisplayNode> displayNodes) {
		final OIDToObject oidToObject = lguiItem.getOIDToObject();

		final HashMap<DisplayNode, Color> displayNodeToColor = new HashMap<DisplayNode, Color>();
		if (oidToObject != null) {
			for (final DisplayNode displayNode : displayNodes) {
				if (displayNode.getData() != null) {
					final String conOID = displayNode.getData().getOid();
					displayNodeToColor.put(displayNode, ColorConversion.getColor(oidToObject.getColorById(conOID)));
				}
			}
		}

		return displayNodeToColor;
	}

	/**
	 * @return lml-data-access to data shown by this nodedisplay
	 */
	public ILguiItem getLguiItem() {
		return lguiItem;
	}

	/**
	 * Generates DisplayNodes for every visible component, which
	 * is a child of the element identified by the level-ids.
	 * 
	 * @param aScheme
	 *            current lml-scheme describing a node
	 * @param aData
	 *            current lml-data for this node
	 * @param levels
	 *            ids for every level to identify a node in the lml-tree (1,1,1) means first cpu in first nodecard in first row
	 * @param nodedisplay
	 *            full LML-Model for a nodedisplay
	 * @param highestRowfirst
	 *            defines how the result will be sorted, true=> descending, false ascending
	 * @return list of DisplayNodes
	 */
	public ArrayList<DisplayNode> getLowerDisplayNodes(Object aScheme, Object aData, ArrayList<Integer> levels,
			Nodedisplay nodedisplay,
			boolean highestRowfirst) {

		final int aLevel = levels.size();
		final ArrayList<DisplayNode> result = new ArrayList<DisplayNode>();

		final HashMap<Integer, SchemeElement> schemesForIds = new HashMap<Integer, SchemeElement>();

		final ArrayList<Integer> numbers = new ArrayList<Integer>();// Indices within scheme of lower elements
		final List<?> lScheme = LMLCheck.getLowerSchemeElements(aScheme);
		for (int i = 0; i < lScheme.size(); i++) {
			// getNumbers
			final SchemeElement laScheme = (SchemeElement) lScheme.get(i);
			if (laScheme.getList() != null) {// get list-elements
				final int[] aNumbers = LMLCheck.getNumbersFromNumberlist(laScheme.getList());
				for (final int aNumber : aNumbers) {
					numbers.add(aNumber);
					schemesForIds.put(aNumber, laScheme);
				}

			} else {// min- max-attributes
				final int min = laScheme.getMin().intValue();
				int max = min;
				if (laScheme.getMax() != null) {
					max = laScheme.getMax().intValue();
				}

				final int step = laScheme.getStep().intValue();
				for (int j = min; j <= max; j += step) {
					numbers.add(j);
					schemesForIds.put(j, laScheme);
				}

			}
		}

		Collections.sort(numbers, new NumberComparator(!highestRowfirst));

		// Process numbers
		for (int j = 0; j < numbers.size(); j++) {
			final int aId = numbers.get(j);
			// Get all information for new DisplayNode
			final SchemeAndData schemeData = LMLCheck.getSchemeAndDataByLevels(aId, aData, aScheme);

			DataElement lowData = schemeData.data;

			if (LMLCheck.getDataLevel(aData) < LMLCheck.getSchemeLevel(aScheme) && aData instanceof DataElement) {
				lowData = (DataElement) aData;// Then do not go deeper , makes no sense
			}

			levels.add(aId);
			DisplayNode displayNode = null;
			if (lguiItem.getNodedisplayAccess() != null) {
				displayNode = new DisplayNode(lguiItem,
						lguiItem.getNodedisplayAccess().getTagname(nodedisplay.getId(), aLevel + 1),
						lowData, schemesForIds.get(aId), levels, nodedisplay);
			}
			levels.remove(levels.size() - 1);

			if (displayNode != null) {
				result.add(displayNode);
			}
		}

		return result;

	}

	/**
	 * @return implicit name of node within nodedisplay, which is shown by this NodedisplayPanel
	 */
	public String getShownImpname() {
		if (fDisplayNode == null) {
			return null;
		}

		return fDisplayNode.getFullImplicitName();
	}

	/**
	 * Hide title or name for this panel
	 */
	public void hideTitle() {
		titleLabel.setVisible(false);
		apref.setShowtitle(false);
	}

	/**
	 * Add height-parameter to minHeight
	 * 
	 * @param height
	 *            difference to be added
	 */
	public synchronized void increaseMinHeight(int height) {
		minHeight += height;

		if (parentNodedisplay != null) {
			// Only first column sends minheight-increases to parent composites
			if (x == 0) {
				parentNodedisplay.increaseMinHeight(height);
			}
		}
	}

	/**
	 * Add width-parameter to minWidth
	 * 
	 * @param width
	 *            difference to be added
	 */
	public synchronized void increaseMinWidth(int width) {
		minWidth += width;

		if (parentNodedisplay != null) {
			// Only first row sends minwidth-increases to parent composites
			if (y == 0) {
				parentNodedisplay.increaseMinWidth(width);
			}
		}
	}

	/**
	 * Calculate minimum size of this nodedisplay, which
	 * is needed to show all painted rectangles in defined
	 * minimum size.
	 * 
	 */
	public void showMinRectangleSizes() {
		callMinSizeCalculation();

		fNodedisplayView.getScrollPane().setMinSize(minWidth, minHeight);
	}

	/**
	 * Show title or name for this panel
	 */
	public void showTitle() {
		titleLabel.setVisible(true);
		apref.setShowtitle(true);
	}

	public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown) {

		if (fDisplayNode == null) {
			if (innerPanel != null) {
				innerPanel.redraw();
			}
			return;
		}

		final ObjectType conobject = fDisplayNode.getConnectedObject();
		if (lguiItem.getObjectStatus() != null) {
			if (currentLevel == maxLevel) {

				if (lguiItem.getObjectStatus().isMouseover(conobject)) {
					borderFrame.setBorderWidth(apref.getMouseborder().intValue());
				}
				else
					borderFrame.setBorderWidth(apref.getBorder().intValue());

				if (lguiItem.getObjectStatus().isAnyMousedown() && !lguiItem.getObjectStatus().isMousedown(conobject)) {// Change
																														// color
					innerPanel.setBackground(ColorConversion.getColor(lguiItem.getOIDToObject().getColorById(null)));
				}
				else
					innerPanel.setBackground(jobColor);
			}
			else if (currentLevel == maxLevel - 1) {// For rectangle-paint of lowest-level-elements
				innerPanel.redraw();
			}

		}
	}

	/**
	 * Adds a dispose-listener, which removes this nodedisplay and its children
	 * from Objectstatus.
	 */
	private void addDisposeAction() {
		this.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				removeUpdatable();

				if (titleLabel != null)
					titleLabel.dispose();
			}

		});
	}

	/**
	 * Adds listeners to innerPanel, which react to user interaction on
	 * lowest level rectangles.
	 */
	private void addMouseListenerToInnerPanel() {

		innerPanel.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {

				final DisplayNode focussed = rectpaintlistener.getDisplayNodeAtPos(e.x, e.y);

				if (lguiItem.getObjectStatus() != null) {
					if (focussed != null && !isDisplayNodeEmpty(focussed)) {
						lguiItem.getObjectStatus().mouseover(focussed.getConnectedObject());
					}
					else {
						lguiItem.getObjectStatus().mouseExitLast();
					}
				}
				innerPanel.setToolTipText(getToolTipText(focussed));

			}
		});

		innerPanel.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				final DisplayNode focussed = rectpaintlistener.getDisplayNodeAtPos(e.x, e.y);

				if (focussed != null && !isDisplayNodeEmpty(focussed) && lguiItem.getObjectStatus() != null) {
					lguiItem.getObjectStatus().mousedown(focussed.getConnectedObject());
				}

				innerPanel.setToolTipText(getToolTipText(focussed));
			}

			public void mouseUp(MouseEvent e) {
				final DisplayNode focussed = rectpaintlistener.getDisplayNodeAtPos(e.x, e.y);

				if (focussed != null && !isDisplayNodeEmpty(focussed) && lguiItem.getObjectStatus() != null) {
					lguiItem.getObjectStatus().mouseup(focussed.getConnectedObject());
				}

				innerPanel.setToolTipText(getToolTipText(focussed));
			}
		});

		innerPanel.addListener(SWT.MouseExit, new Listener() {

			public void handleEvent(Event event) {
				if (lguiItem.getObjectStatus() != null) {
					lguiItem.getObjectStatus().mouseExitLast();
				}

				innerPanel.setToolTipText(getToolTipText(null));
			}

		});

	}

	/**
	 * Adds a listener, which calls showMinRectangleSize everytime
	 * this component is painted. This is needed to change minimum
	 * size in surrounding ScrollPane.
	 */
	private void addPaintListener() {

		final Listener l = new Listener() {

			public void handleEvent(Event event) {
				showMinRectangleSizes();
			}
		};

		innerPanel.addListener(SWT.Paint, l);

	}

	/**
	 * Add Listener to titlelabel, which allow to zoom in and zoom out
	 */
	private void addZoomFunction() {

		// Zoom in by clicking on title-panels
		titleLabel.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {

			}

			public void mouseDown(MouseEvent e) {

				titleLabel.setBackground(titleBackgroundColor);

				if (fDisplayNode == null)
					return;

				if (fNodedisplayView.getRootNodedisplay() == NodedisplayComp.this) {
					fNodedisplayView.zoomOut();
				}
				else
					fNodedisplayView.zoomIn(fDisplayNode.getFullImplicitName());

			}

			public void mouseUp(MouseEvent e) {

			}
		});
		// Show different background if titlelabel is covered by the mouse
		titleLabel.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				titleLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
			}
		});

		titleLabel.addListener(SWT.MouseExit, new Listener() {

			public void handleEvent(Event event) {
				titleLabel.setBackground(titleBackgroundColor);
			}
		});

	}

	/**
	 * Calculates current level and saves it to currentlevel
	 * 
	 * @param pnode
	 */
	private void calculateCurrentlevel(DisplayNode pnode) {
		currentLevel = 0;
		if (pnode != null) {
			currentLevel = pnode.getLevel();
		}
	}

	/**
	 * Forward call to calculate desired minimum size to
	 * all inner components. If this nodedisplay paints rectangles
	 * itself, this method calls the calculateResize-function of
	 * the rectangle-painting-listener.
	 */
	private void callMinSizeCalculation() {

		final Point size = getSize();
		setMinSize(size.x, size.y);

		for (final NodedisplayComp icomp : innerComps) {
			if (icomp.x == 0 || icomp.y == 0) {
				icomp.callMinSizeCalculation();
			}
		}

		if (rectpaintlistener != null) {
			rectpaintlistener.calculateResize();
		}

	}

	/**
	 * Check if maxlevel is bigger than deepest possible level within this scheme-part.
	 * Change this fault by setting maxLevel to maximum possible value.
	 */
	private void checkMaxLevel() {

		if (fDisplayNode != null) {
			// calculation of absolute maximal level within this part of tree
			// realmax=current node level + deepest possible level
			final int realmax = LMLCheck.getSchemeLevel(fDisplayNode.getScheme())
					+ LMLCheck.getDeepestSchemeLevel(fDisplayNode.getScheme()) - 1;
			if (realmax < maxLevel) {
				maxLevel = realmax;
			}
		}

	}

	/**
	 * Creates borderFrame and innerPanel.
	 * 
	 * @param bordercolor
	 *            the color for the border
	 */
	private void createFramePanels(Color bordercolor) {

		// At least insert one panel with backgroundcolor as bordercomposite
		borderFrame = new BorderComposite(mainPanel, SWT.NONE);

		borderFrame.setBorderColor(bordercolor);
		borderFrame.setBorderWidth(apref.getBorder().intValue());
		borderFrame.setLayoutData(new BorderData(BorderLayout.MFIELD));

		innerPanel = new Composite(borderFrame, SWT.NONE);

		innerPanel.setBackground(backgroundColor);

		if (centerPic != null) {
			innerPanel.setBackgroundImage(centerPic);
		}

		// if( (apref.isHighestrowfirst() && !apref.isHighestcolfirst()) || (!apref.isHighestrowfirst() &&
		// apref.isHighestcolfirst()) ){
		// innerpanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		// }

	}

	/**
	 * Initializes the surrounding pictureFrame and inserts pictures for this
	 * nodedisplay-level.
	 */
	private void createPictureFrame() {

		pictureFrame = new Composite(this, SWT.None);
		pictureFrame.setLayout(new BorderLayout());
		// Redo layout when resized
		pictureFrame.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				pictureFrame.layout(true);
			}

		});

		insertPictures();

		pictureFrame.setBackground(backgroundColor);
	}

	/**
	 * @return 1 if layout defines 0 for cols, otherwise the value set by the layout
	 */
	private int getCols() {
		if (apref.getCols().intValue() == 0) {
			return 1;
		}
		else
			return apref.getCols().intValue();
	}

	/**
	 * Converts a URL given as String into a https-URL.
	 * The standard httpsport is set in the result-URL.
	 * 
	 * @param lmlurl
	 *            normal URL as String
	 * @return HTTPS-URL
	 */
	private URL getHttpsURL(String lmlurl) {

		URL res = null;

		try {
			// Get picture from url and put it into a panel
			try {
				final URL httpURL = new URL(lmlurl);
				res = new URL("https", httpURL.getHost(), httpsport, httpURL.getFile());
			} catch (final MalformedURLException e) {
				return null;
			}
		} catch (final AccessControlException er) {
			return null;
		}

		return res;
	}

	/**
	 * Generate the text shown for a displaynode, which is covered by mouse-cursor.
	 * 
	 * @param focussed
	 *            the displaynode which is covered
	 * @return tooltiptext
	 */
	private String getToolTipText(DisplayNode focussed) {
		if (focussed == null)
			return null;
		if (focussed.getConnectedObject() == null)
			return null;
		if (focussed.getConnectedObject().getName() != null)
			return focussed.getConnectedObject().getName();
		return focussed.getConnectedObject().getId();
	}

	/**
	 * Part of constructor, which is equal in two constructors
	 * therefore outsourced
	 * 
	 * @param nodedisplay
	 */
	private void init(Nodedisplay nodedisplay, DisplayNode displayNode, NodedisplayView nodedisplayView) {
		// Transfer parameters
		fNodedisplayView = nodedisplayView;
		fDisplayNode = displayNode;
		fNodedisplay = nodedisplay;

		setLayout(new FillLayout());

		innerComps = new ArrayList<NodedisplayComp>();

		addDisposeAction();

		if (apref == null) {
			apref = findLayout();// Searches for corresponding layout-definitions
		}

		checkMaxLevel();

		backgroundColor = ColorConversion.getColor(LMLColor.stringToColor(apref.getBackground()));

		createPictureFrame();

		fontObject = this.getDisplay().getSystemFont();

		mainPanel = new Composite(pictureFrame, SWT.None);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setLayoutData(new BorderData(BorderLayout.MFIELD));

		if (lguiItem.getOIDToObject() != null) {
			if (fDisplayNode != null) {// Is this nodedisplay the root-nodedisplay?
				jobColor = ColorConversion.getColor(lguiItem.getOIDToObject().getColorById(fDisplayNode.getData().getOid()));
				title = apref.isShowfulltitle() ? fDisplayNode.getFullImplicitName() : fDisplayNode.getImplicitName();

				insertTitleLabel();

				insertInnerPanel();
			}
			else {
				jobColor = ColorConversion.getColor(lguiItem.getOIDToObject().getColorById(null));
				title = lguiItem.getNodedisplayAccess().getNodedisplayTitel(0);

				insertTitleLabel();

				insertInnerPanel(0, fNodedisplay.getScheme(),
						fNodedisplay.getData(), new ArrayList<Integer>());
			}

			lguiItem.getObjectStatus().addComponent(this);
		}

	}

	/**
	 * Adds rectangle-painting listener to innerPanel.
	 * 
	 * @param displayNodes
	 * @param borderColor
	 */
	private void initRectPaintListener(ArrayList<DisplayNode> displayNodes, Color borderColor) {
		rectpaintlistener = new RectPaintListener(displayNodes, apref.getCols().intValue(), this, innerPanel);
		rectpaintlistener.horizontalSpacing = apref.getHgap().intValue();
		rectpaintlistener.verticalSpacing = apref.getVgap().intValue();

		rectpaintlistener.normalborder = apref.getBorder().intValue();
		rectpaintlistener.mouseborder = apref.getMouseborder().intValue();

		rectpaintlistener.bordercolor = borderColor;

		innerPanel.addListener(SWT.Paint, rectpaintlistener);
	}

	/**
	 * Call this function if displaynode != null
	 */
	private void insertInnerPanel() {
		insertInnerPanel(fDisplayNode.getLevel(), fDisplayNode.getScheme(), fDisplayNode.getData(), fDisplayNode.getLevelNrs());
	}

	/**
	 * If inner panels exist and maxlevel is greater than alevel, then insert lower level-panels
	 */
	private void insertInnerPanel(int aLevel, Object aScheme, Object aData, ArrayList<Integer> levels) {

		// Bordercolor is defined by this parameter
		final Color borderColor = ColorConversion.getColor(LMLColor.stringToColor(apref.getBordercolor()));

		createFramePanels(borderColor);

		// Generate all displaynodes for elements in lml-tree which are childs of the current node
		final ArrayList<DisplayNode> displayNodes = getLowerDisplayNodes(aScheme, aData, levels, fNodedisplay,
				apref.isHighestrowfirst());

		if (aLevel == maxLevel - 1) {// Paint rects instead of use composites for lowest-level-rectangles

			initRectPaintListener(displayNodes, borderColor);
			addMouseListenerToInnerPanel();

		}
		else { // insert lower nodedisplays, nest composites

			// Set Gridlayout only if needed
			final int cols = apref.getCols().intValue();
			final GridLayout layout = new GridLayout(cols, true);
			layout.horizontalSpacing = apref.getHgap().intValue();
			layout.verticalSpacing = apref.getVgap().intValue();

			layout.marginWidth = 1;
			layout.marginHeight = 1;

			innerPanel.setLayout(layout);

			int index = 0;

			for (final DisplayNode displayNode : displayNodes) {

				final NodedisplayComp inner = new NodedisplayComp(lguiItem, fNodedisplay, displayNode, fNodedisplayView, this,
						index % cols, index / cols, maxLevel - currentLevel - 1, innerPanel, SWT.NONE);
				innerComps.add(inner);

				if (gridData == null) {
					gridData = getDefaultGridData();
				}

				inner.setLayoutData(gridData);

				index++;
			}
		}
	}

	/**
	 * Search for pictures in layout-definition and add them at right position
	 */
	private void insertPictures() {

		pictures = new ArrayList<ImageComp>();

		// insert pictures
		final List<PictureType> lmlPictures = apref.getImg();
		for (final PictureType picture : lmlPictures) {

			final URL aUrl = getHttpsURL(picture.getSrc());
			if (aUrl == null)
				continue;

			ImageComp imageComposite;
			try {
				imageComposite = new ImageComp(pictureFrame, SWT.None, aUrl, picture.getWidth(), picture.getHeight());
			} catch (final IOException e) {
				continue;
			}

			if (fDisplayNode == null)
				imageComposite.setBackground(backgroundColor);
			else
				imageComposite.setBackground(this.getParent().getBackground());

			// Add borderpics to pictures and save center-pic in centerpic-variable

			if (picture.getAlign() == AlignType.CENTER)
				centerPic = imageComposite.getImage();
			else
				pictures.add(imageComposite);

			switch (picture.getAlign()) {
			case WEST:
				imageComposite.setLayoutData(new BorderData(BorderLayout.WFIELD));
				break;
			case EAST:
				imageComposite.setLayoutData(new BorderData(BorderLayout.EFIELD));
				break;
			case NORTH:
				imageComposite.setLayoutData(new BorderData(BorderLayout.NFIELD));
				break;
			case SOUTH:
				imageComposite.setLayoutData(new BorderData(BorderLayout.SFIELD));
				break;
			}
		}
	}

	/**
	 * If a title should be shown, it will be inserted by this function.
	 * 
	 */
	private void insertTitleLabel() {
		if (apref.isShowtitle()) {
			titleBackgroundColor = ColorConversion.getColor(LMLColor.stringToColor(apref.getTitlebackground()));

			titleLabel = new Label(mainPanel, SWT.None);
			titleLabel.setText(title);
			titleLabel.setFont(fontObject);
			titleLabel.setBackground(titleBackgroundColor);
			titleLabel.setLayoutData(new BorderData(BorderLayout.NFIELD));

			addZoomFunction();
		}
	}

	/**
	 * Check if given displaynode is connected to
	 * empty jobs.
	 * 
	 * @param displayNode
	 *            the displaynode, which is checked
	 * @return true, if anything in request-chain is null or if object-id is "empty"
	 */
	private boolean isDisplayNodeEmpty(DisplayNode displayNode) {
		if (displayNode == null)
			return true;
		if (displayNode.getConnectedObject() == null)
			return true;
		if (displayNode.getConnectedObject().getId() == null)
			return true;
		if (displayNode.getConnectedObject().getId().equals(emptyJobName))
			return true;

		return false;
	}

	/**
	 * Remove recursively this component and all inner NodedisplayComp-instances
	 * from ObjectStatus. Call this function before this Composite is disposed
	 */
	private void removeUpdatable() {

		if (lguiItem.getObjectStatus() != null) {
			lguiItem.getObjectStatus().removeComponent(this);
		}

		for (final NodedisplayComp nd : innerComps) {
			nd.removeUpdatable();
		}
	}

	/**
	 * Set minimum height for this nodedisplay.
	 * At least this height should be used to display it.
	 * No forwarding to parent components.
	 * 
	 * @param height
	 *            minimum height
	 */
	private synchronized void setMinHeight(int height) {
		minHeight = height;
	}

	/**
	 * Set minimum size for this nodedisplay.
	 * At least this size should be used to display it.
	 * Use this function to initialize minWidth and minHeight.
	 * No forwarding to parent components.
	 * 
	 * @param width
	 *            minimum width
	 * @param height
	 *            minimum height
	 */
	private void setMinSize(int width, int height) {
		setMinWidth(width);
		setMinHeight(height);
	}

	/**
	 * Set minimum width for this nodedisplay.
	 * At least this width should be used to display it.
	 * No forwarding to parent components.
	 * 
	 * @param width
	 *            minimum width
	 */
	private synchronized void setMinWidth(int width) {
		minWidth = width;
	}

	/**
	 * Set default preferences for scrollpane.
	 * This function is only used for root-NodedisplayComposites.
	 */
	private void setScrollBarPreferences() {
		final ScrolledComposite scrolledComposite = fNodedisplayView.getScrollPane();
		scrolledComposite.setContent(this);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
	}
}