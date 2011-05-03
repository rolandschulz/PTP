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
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.AlignType;
import org.eclipse.ptp.rm.lml.internal.core.elements.DataElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfodataType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplayelement;
import org.eclipse.ptp.rm.lml.internal.core.elements.NodedisplaylayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectFactory;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.PictureType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeElement;
import org.eclipse.ptp.rm.lml.internal.core.elements.SchemeType;
import org.eclipse.ptp.rm.lml.internal.core.model.LMLColor;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;
import org.eclipse.ptp.rm.lml.internal.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus.Updatable;
import org.eclipse.ptp.rm.lml.internal.core.nodedisplay.LMLCheck;
import org.eclipse.ptp.rm.lml.internal.core.nodedisplay.LMLCheck.SchemeAndData;
import org.eclipse.ptp.rm.lml.ui.providers.BorderLayout2.BorderData;
import org.eclipse.ptp.rm.lml.ui.views.NodedisplayView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

/**
 * SWT-Translation of NodedisplayPanel, which is a Swing-Component
 * 
 * Composite to create output of a nodedisplay.
 * 
 * Inner composites are NodedisplayComp again (recursive)
 * 
 * A NodedisplayComp represents one physical element within the nodedisplay-tag.
 * This might be a row, midplane, node or cpu. Through NodedisplayComp the
 * reduced collapsed tree of lml is fully expanded to maxlevel. Every visible
 * component in the nodedisplay is shown by a NodedisplayComp. There is one
 * exception: For performance reasons the lowest-level rectangles are painted
 * directly. So for the rectangles (physical elements) in the lowest level no
 * composites are created.
 * 
 * The look of the nodedisplay is defined by the lml-Nodedisplay-Layout.
 * 
 * 
 * @author karbach
 * 
 */
public class NodedisplayComp extends LguiWidget implements Updatable {

	// TODO What for?
	private NodedisplayView nodeview;// parent composite for zooming

	// Parameters which are not in apref yet
	private static int iconwidth = 15;

	private String title;// implicit name of this node
	private Color jobColor;// Color of the job, to which this panel is connected

	private Font fontObject;
	// TODO eliminating
	private Nodedisplay model;// surrounding lml-Nodedisplay

	private DisplayNode node;// Node-model which has to be displayed
	private int maxLevel; // Deepest level, which should be displayed

	private Composite pictureFrame;// this panel contains pictures as direct
									// children and the mainpanel in center
	private Composite mainPanel;// Important panel where everything but pictures
								// is in

	private Composite innerPanel;
	private Label titleLabel;// For title-line

	// Settings for lower level
	private Nodedisplayelement apref;

	// current level, which is displayed
	private int currentLevel;

	private GridData gridData;// layout definitions for gridlayouts, makes inner
								// panels resize

	private Color backgroundColor;// Current backgroundcolor
	private Color titleBackgroundColor;

	// Borders
	private BorderComposite borderFrame;// The only frame which has a border,
										// borderwidth changes when mouse
										// touches the panel

	private ArrayList<ImageComp> pictures;
	private Image centerPic;// Picture in center is special

	private ArrayList<NodedisplayComp> innerComps;// Save created inner
													// NodedisplayComp-instances
													// for disposing them if
													// needed

	private static final int httpsport = 4444;// Special port for https-access

	/**
	 * Class for comparing integer-values in ascending or descending way.
	 * 
	 * @author karbach
	 * 
	 */
	public static class NumberComparator implements Comparator<Integer> {

		private final boolean ascending;// if true => sort ascending, otherwise
										// descending

		public NumberComparator(boolean ascending) {
			this.ascending = ascending;
		}

		public int compare(Integer o1, Integer o2) {
			if (ascending) {
				return o1 - o2;
			} else {
				return o2 - o1;
			}
		}

	}

	/**
	 * easy constructor for a nodedisplay as root-node
	 * 
	 * @param lgui
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param pmodel
	 *            lml-model for the nodedisplay, which should be shown in this
	 *            panel
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
	 * Call this constructor for inner or lower elements, rellevel is counted to
	 * zero with every level
	 * 
	 * @param lgui
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param pmodel
	 *            lml-model, which has data for this Nodedisplay
	 * @param pnodeview
	 *            root-nodedisplay is needed for zooming
	 * @param pnode
	 *            current node, which is root-data-element of this
	 *            NodedisplayComp
	 * @param rellevel
	 *            relative level, rellevel==0 means show no lower elements
	 */
	public NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview, int rellevel,
			Composite parent, int style) {

		super(lgui, parent, style);

		// Calculate maxlevel with rellevel

		// Set Preferences
		calculateCurrentlevel(pnode);

		maxLevel = currentLevel + rellevel;

		init(pmodel, pnode, pnodeview);
	}

	/**
	 * Call this constructor for start, maxlevel is chosen from lml-file
	 * 
	 * @param lgui
	 *            wrapper instance around LguiType-instance -- provides easy
	 *            access to lml-information
	 * @param pmodel
	 *            lml-model, which has data for this Nodedisplay
	 * @param pnodeview
	 *            root-nodedisplay is needed for zooming
	 * @param pnode
	 *            current node, which is root-data-element of this
	 *            NodedisplayComp
	 * @param style
	 *            SWT Style
	 */
	public NodedisplayComp(ILguiItem lgui, Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview, int style) {

		super(lgui, pnodeview, style);

		// get maxlevel from lml-file
		calculateCurrentlevel(pnode);

		model = pmodel;
		node = pnode;

		apref = findLayout();// Searches for corresponding layout-definitions

		maxLevel = 10;
		if (apref.getMaxlevel() != null) {
			maxLevel = apref.getMaxlevel().intValue();
		}

		init(pmodel, pnode, pnodeview);
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
	 * @return implicit name of node within nodedisplay, which is shown by this
	 *         NodedisplayPanel
	 */
	public String getShownImpname() {
		if (node == null) {
			return null;
		}

		return node.getFullImplicitName();
	}

	/**
	 * Part of constructor, which is equal in two constructors therefore
	 * outsourced
	 * 
	 * @param pmodel
	 */
	private void init(Nodedisplay pmodel, DisplayNode pnode, NodedisplayView pnodeview) {

		nodeview = pnodeview;

		setLayout(new FillLayout());

		innerComps = new ArrayList<NodedisplayComp>();
		// Do extra stuff when disposing
		// Remove this nodedisplay and its children from Objectstatus
		this.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				removeUpdatable();

				if (titleLabel != null) {
					titleLabel.dispose();
				}
			}

		});

		// Transfer parameters
		node = pnode;
		model = pmodel;
		if (apref == null) {
			apref = findLayout();// Searches for corresponding
									// layout-definitions
		}

		// Check if maxlevel is bigger than deepest possible level within this
		// scheme-part
		if (node != null) {
			// calculation of absolute maximal level within this part of tree
			// realmax=current node level + deepest possible level
			int realmax = LMLCheck.getSchemeLevel(node.getScheme()) + LMLCheck.getDeepestSchemeLevel(node.getScheme()) - 1;
			if (realmax < maxLevel) {
				maxLevel = realmax;
			}
		}

		backgroundColor = ColorConversion.getColor(LMLColor.stringToColor(apref.getBackground()));

		pictureFrame = new Composite(this, SWT.None);
		pictureFrame.setLayout(new BorderLayout2());
		// Redo layout when resized
		pictureFrame.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				pictureFrame.layout(true);
			}

		});

		insertPictures();

		// if(centerpic==null)
		// mainpanel=new JPanel();
		// else{
		// ImagePanel picturepanel=new ImagePanel(centerpic.getPic(),
		// centerpic.getPercentWidth(), centerpic.getPercentHeight());
		// picturepanel.setAlign(centerpic.getAlign());
		//
		// mainpanel=picturepanel;
		// }

		mainPanel = new Composite(pictureFrame, SWT.None);

		if (node != null) {
			jobColor = ColorConversion.getColor(lgui.getOIDToObject().getColorById(node.getData().getOid()));
		} else {
			jobColor = ColorConversion.getColor(lgui.getOIDToObject().getColorById(null));
		}

		fontObject = this.getDisplay().getSystemFont();

		// Create output
		mainPanel.setLayout(new BorderLayout2());
		mainPanel.setLayoutData(BorderData.CENTER);

		// Insert title if needed
		if (node != null) {
			title = apref.isShowfulltitle() ? node.getFullImplicitName() : node.getImplicitName();
		} else {
			title = lgui.getNodedisplayAccess().getNodedisplayTitel(0);
		}

		// Get an imageicon for showing power load
		ImageIcon icon = getPowerLoadImageIcon();

		if (apref.isShowtitle()) {

			titleBackgroundColor = ColorConversion.getColor(LMLColor.stringToColor(apref.getTitlebackground()));

			titleLabel = new Label(mainPanel, SWT.None);
			titleLabel.setText(title);
			titleLabel.setFont(fontObject);
			titleLabel.setBackground(titleBackgroundColor);
			titleLabel.setLayoutData(BorderData.NORTH);

			addZoomFunction();

			// titlelabel.setOpaque(true);

			// if(icon!=null){
			// titlelabel.setImage(icon.getImage());
			// }

		}

		innerPanel = null;

		if (node != null) {
			insertInnerPanel();
		} else {
			insertInnerPanel(0, lgui.getNodedisplayAccess().getNodedisplayScheme(0), lgui.getNodedisplayAccess()
					.getNodedisplayData(0), new ArrayList<Integer>());
		}

		lgui.getObjectStatus().addComponent(this);

		addListener();

		pictureFrame.setBackground(backgroundColor);

	}

	/**
	 * Remove recursively this component and all inner NodedisplayComp-instances
	 * from ObjectStatus. Call this function before this Composite is disposed
	 */
	private void removeUpdatable() {

		lgui.getObjectStatus().removeComponent(this);

		for (NodedisplayComp nd : innerComps) {
			nd.removeUpdatable();
		}
	}

	/**
	 * Add Listener to titlelabel, which allow to zoom in and zoom out
	 */
	private void addZoomFunction() {

		// Zoom in by clicking on title-panels
		titleLabel.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {

			}

			public void mouseDown(MouseEvent e) {

				titleLabel.setBackground(titleBackgroundColor);

				if (node == null) {
					return;
				}

				if (nodeview.getRootNodedisplay() == NodedisplayComp.this) {
					nodeview.zoomOut();
				} else {
					nodeview.zoomIn(node.getFullImplicitName());
				}

			}

			public void mouseDoubleClick(MouseEvent e) {

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
	 * Create an icon which displayes power load
	 * 
	 * @return ImageIcon for placing into a title-label
	 */
	private ImageIcon getPowerLoadImageIcon() {

		ImageIcon icon = null;

		if (lgui.getOIDToObject().getObjectById(title) != null) {
			// Show power usage
			double curpower = 0;// current power usage in percent
			double maxpower = 1;// maximal power usage

			// get power information from info-tags
			List<InfoType> infos = lgui.getOIDToInformation().getInfosById(title);

			if (infos != null) {
				for (InfoType inftype : infos) {// <info>
					List<InfodataType> data = inftype.getData();
					for (InfodataType date : data) {// <data>
						if (date.getKey().equals("maxpower")) {// <data
																// key="maxpower"
																// value="100"/>
							maxpower = Double.parseDouble(date.getValue());
						} else if (date.getKey().equals("curpower")) {
							curpower = Double.parseDouble(date.getValue());
						}
					}
				}

				// Create Buffered Image filled with a pie-chart
				// PieChart pc=new PieChart(PieChart.anydataToPercentValues(new
				// double[]{maxpower-curpower, curpower}),
				// new Color[]{Color.gray,
				// getColorForPowerPie(curpower/maxpower)}, null);
				//
				// BufferedImage bufImage = new
				// BufferedImage(iconwidth,iconwidth,BufferedImage.TYPE_INT_ARGB
				// );
				// Graphics g = bufImage.getGraphics();
				//
				// pc.paint(g, new Dimension(iconwidth,iconwidth));
				//
				// icon=new ImageIcon(bufImage);
			}
		}

		return icon;
	}

	// Some colors for pie charts presenting power usage
	private static Color lowcolor, middlecolor, highcolor;

	/**
	 * 
	 * @param percent
	 *            power usage in percent
	 * @return Color for usage within pie chart, for fast overview of power load
	 */
	private static Color getColorForPowerPie(double percent) {

		if (lowcolor == null) {
			lowcolor = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
			middlecolor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
			highcolor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		}

		if (percent < 0.3) {
			return lowcolor;
		}

		if (percent < 0.6) {
			return middlecolor;
		}

		return highcolor;
	}

	/**
	 * Search for pictures in layout-definition and add them at right position
	 */
	private void insertPictures() {

		pictures = new ArrayList<ImageComp>();

		// insert pictures
		List<PictureType> lmlpictures = apref.getImg();
		for (PictureType picture : lmlpictures) {

			URL aurl = null;

			try {
				// Get picture from url and put it into a panel
				try {
					URL httpURL = new URL(picture.getSrc());
					aurl = new URL("https", httpURL.getHost(), httpsport, httpURL.getFile());
				} catch (MalformedURLException e) {
					continue;
				}
			} catch (AccessControlException er) {
				aurl = null;
			}

			if (aurl == null) {
				continue;
			}

			ImageComp icomp;
			try {
				icomp = new ImageComp(pictureFrame, SWT.None, aurl, picture.getWidth(), picture.getHeight());
			} catch (IOException e) {
				continue;
			}

			if (node == null) {
				icomp.setBackground(backgroundColor);
			} else {
				icomp.setBackground(this.getParent().getBackground());
			}

			// Set inner alignment
			// ipan.setAlign(picture.getInneralign());

			if (picture.getAlign() == AlignType.CENTER) {
				centerPic = icomp.getImage();
			} else {
				pictures.add(icomp);
			}

			switch (picture.getAlign()) {
			case WEST:
				icomp.setLayoutData(BorderData.WEST);
				break;
			case EAST:
				icomp.setLayoutData(BorderData.EAST);
				break;
			case NORTH:
				icomp.setLayoutData(BorderData.NORTH);
				break;
			case SOUTH:
				icomp.setLayoutData(BorderData.SOUTH);
				break;
			}
		}
	}

	/**
	 * Search for a layout-section for this nodedisplay-panel
	 * 
	 * @return lml-Nodedisplay-layout-section for this displaynode, or
	 *         default-layout if no layout is defined
	 */
	public Nodedisplayelement findLayout() {

		ArrayList<NodedisplaylayoutType> allnodedisplaylayouts = lgui.getNodedisplayAccess().getLayouts(model.getId());

		// Is there any Layout for this nodedisplay?
		if (allnodedisplaylayouts == null || allnodedisplaylayouts.size() == 0) {
			return NodedisplayAccess.getDefaultLayout();
		}
		// Later it should be possible to choose one of possibly multiple
		// defined layouts
		NodedisplaylayoutType first = allnodedisplaylayouts.get(0);

		if (node == null) {// Root-level => return el0-Nodedisplayelement
			if (first.getEl0() != null) {
				return first.getEl0();
			} else {
				return NodedisplayAccess.getDefaultLayout();
			}
		}

		if (first.getEl0() == null) {
			return NodedisplayAccess.getDefaultLayout();
		}

		// Copy level-numbers
		ArrayList<Integer> levelnrs = LMLCheck.copyArrayList(node.getLevelNrs());

		// deeper-level => traverse layout-tree
		Nodedisplayelement res = LMLCheck.getNodedisplayElementByLevels(levelnrs, first.getEl0());

		if (res == null) {
			return NodedisplayAccess.getDefaultLayout();
		} else {
			return res;
		}

	}

	/**
	 * Add listener which react when user focuses this panel This method is not
	 * needed, if elements on the lowest level are painted with rects instead of
	 * painting composites.
	 */
	private void addListener() {

		if (currentLevel == maxLevel) {// Only insert listeners for the lowest
										// level-composites, which are painted
										// as rectangles

			MouseMoveListener mousemove = new MouseMoveListener() {

				public void mouseMove(MouseEvent e) {
					if (node != null) {
						lgui.getObjectStatus().mouseover(node.getConnectedObject());
					}
				}
			};

			MouseListener mouselistener = new MouseListener() {

				public void mouseUp(MouseEvent e) {
					if (e.x >= 0 && e.x <= getSize().x && e.y >= 0 && e.y <= getSize().y) {
						if (node != null) {
							lgui.getObjectStatus().mouseup(node.getConnectedObject());
						}
					}
				}

				public void mouseDown(MouseEvent e) {
					if (node != null) {
						lgui.getObjectStatus().mousedown(node.getConnectedObject());
					}
				}

				public void mouseDoubleClick(MouseEvent e) {

				}
			};

			Listener mouseexit = new Listener() {

				public void handleEvent(Event event) {
					lgui.getObjectStatus().mouseExitLast();
				}

			};

			borderFrame.addMouseMoveListener(mousemove);
			borderFrame.addMouseListener(mouselistener);
			borderFrame.addListener(SWT.MouseExit, mouseexit);

			innerPanel.addMouseMoveListener(mousemove);
			innerPanel.addMouseListener(mouselistener);
			innerPanel.addListener(SWT.MouseExit, mouseexit);
		}
	}

	/**
	 * Find maximum count of elements per level Traverses the whole scheme
	 * 
	 * @param scheme
	 *            lml-scheme
	 * @param level
	 *            current level (first is 1)
	 * @param maxcounts
	 *            array with length at least scheme.depth
	 */
	private static void findMaximum(Object scheme, int level, int[] maxcounts) {

		List subschemes = LMLCheck.getLowerSchemeElements(scheme);

		int sum = 0;

		for (Object subscheme : subschemes) {

			SchemeElement sub = (SchemeElement) subscheme;

			if (sub.getList() != null) {// list-attribute
				sum += LMLCheck.getNumbersFromNumberlist(sub.getList()).length;
			} else {// min- max-attributes

				int min = sub.getMin().intValue();
				int max = min;
				if (sub.getMax() != null) {
					max = sub.getMax().intValue();
				}

				int step = sub.getStep().intValue();

				sum += (max - min) / step + 1;
			}
			// Recursive call for lower level
			findMaximum(sub, level + 1, maxcounts);
		}

		if (sum > 0 && sum > maxcounts[level - 1]) {
			maxcounts[level - 1] = sum;
		}

	}

	/**
	 * Parses through scheme and generates suitable preferences for every level
	 * of the nodedisplay-tree
	 * 
	 * @param scheme
	 *            scheme of nodedisplay
	 * @return preferences for every level
	 */
	public static ArrayList<Nodedisplayelement> generatePrefsFromScheme(SchemeType scheme) {
		int depth = LMLCheck.getDeepestSchemeLevel(scheme);

		ArrayList<Nodedisplayelement> res = new ArrayList<Nodedisplayelement>();
		ObjectFactory objf = new ObjectFactory();// For creating lml-objects

		int[] maxcounts = new int[depth];
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

			Nodedisplayelement apref = objf.createNodedisplayelement();
			apref.setCols(BigInteger.valueOf(cols));

			// Only level 1 shows title
			if (i != 1) {
				apref.setShowtitle(false);
			}
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
	 * Generates DisplayNodes for every visible component, which is a child of
	 * the element identified by the level-ids.
	 * 
	 * @param ascheme
	 *            current lml-scheme describing a node
	 * @param adata
	 *            current lml-data for this node
	 * @param levels
	 *            ids for every level to identify a node in the lml-tree (1,1,1)
	 *            means first cpu in first nodecard in first row
	 * @param model
	 *            full LML-Model for a nodedisplay
	 * @param highestRowfirst
	 *            defines how the result will be sorted, true=> descending,
	 *            false ascending
	 * @return list of DisplayNodes
	 */
	public ArrayList<DisplayNode> getLowerDisplayNodes(Object ascheme, Object adata, ArrayList<Integer> levels, Nodedisplay model,
			boolean highestRowfirst) {

		int alevel = levels.size();
		ArrayList<DisplayNode> res = new ArrayList<DisplayNode>();

		HashMap<Integer, SchemeElement> schemesForIds = new HashMap<Integer, SchemeElement>();

		ArrayList<Integer> numbers = new ArrayList<Integer>();// Indices within
																// scheme of
																// lower
																// elements
		List lscheme = LMLCheck.getLowerSchemeElements(ascheme);
		for (int i = 0; i < lscheme.size(); i++) {

			// getNumbers
			SchemeElement lascheme = (SchemeElement) lscheme.get(i);
			if (lascheme.getList() != null) {// get list-elements

				int[] anumbers = LMLCheck.getNumbersFromNumberlist(lascheme.getList());
				for (int j = 0; j < anumbers.length; j++) {
					numbers.add(anumbers[j]);
					schemesForIds.put(anumbers[j], lascheme);
				}

			} else {// min- max-attributes

				int min = lascheme.getMin().intValue();
				int max = min;
				if (lascheme.getMax() != null) {
					max = lascheme.getMax().intValue();
				}

				int step = lascheme.getStep().intValue();

				for (int j = min; j <= max; j += step) {
					numbers.add(j);
					schemesForIds.put(j, lascheme);
				}

			}
		}

		Collections.sort(numbers, new NumberComparator(!highestRowfirst));

		// Process numbers
		for (int j = 0; j < numbers.size(); j++) {
			int aid = numbers.get(j);
			// Get all information for new DisplayNode
			SchemeAndData schemedata = LMLCheck.getSchemeAndDataByLevels(aid, adata, ascheme);

			DataElement lowdata = schemedata.data;

			if (LMLCheck.getDataLevel(adata) < LMLCheck.getSchemeLevel(ascheme) // Was
																				// adata
																				// already
																				// not
																				// at
																				// the
																				// right
																				// level
																				// (inheritance
																				// of
																				// attributes)
																				// ?
					&& adata instanceof DataElement) {
				lowdata = (DataElement) adata;// Then do not go deeper , makes
												// no sense
			}

			levels.add(aid);

			DisplayNode dispnode = new DisplayNode(lgui, lgui.getNodedisplayAccess().getTagname(model.getId(), alevel + 1),
					lowdata, schemesForIds.get(aid), levels, model);

			levels.remove(levels.size() - 1);

			res.add(dispnode);
		}

		return res;

	}

	/**
	 * A listener, which paints dispnodes within a panel. This is used for the
	 * lowest level rectangles. It is faster to paint rects than using a
	 * gridlayout and inserting nodedisplay- composites for every rectangle.
	 * Problem is less general layouts (for example: it is more difficult to
	 * react for cursor-focus on these rectangles, texts as titles have to be
	 * painted and cant be inserted by using a layout-manager)
	 * 
	 * 
	 * @author karbach
	 * 
	 */
	private class RectPaintListener implements Listener {

		private final ArrayList<DisplayNode> dispnodes;// nodes which are
														// painted in the
														// composite
		private final HashMap<DisplayNode, Color> dispnodetocolor;// map for
																	// fast
																	// access to
																	// displaynode-colors
		private final HashMap<DisplayNode, Rectangle> dispnodetorectangle;// map
																			// containing
																			// positions
																			// of
																			// dispnodes,
																			// they
																			// might
																			// change
																			// in
																			// every
																			// paint
		private int COLUMNCOUNT;// count of columns in the grid
		private final Composite composite;// The composite which is painted by
											// this listener

		public Color bordercolor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

		public int marginWidth = 1;// space around the grid in x-direction
		public int marginHeight = 1;// space around the grid in y-direction
		public int horizontalSpacing = 1;// space between two rectangles in x
		public int verticalSpacing = 1;// space between two rectangles in y

		public int normalborder = 0;// Border shown if displaynodes are not
									// focussed
		public int mouseborder = 1;// Border shown if displaynodes are focussed

		/**
		 * Create the listener, initialize attributes and generate
		 * dispnodetocolor-map
		 * 
		 * @param pdispnodes
		 *            nodes, which should be painted
		 * @param pcolumncount
		 *            count of columns in the grid
		 * @param pcomposite
		 *            composite, to which this listener is added
		 */
		public RectPaintListener(ArrayList<DisplayNode> pdispnodes, int pcolumncount, Composite pcomposite) {
			dispnodes = pdispnodes;
			COLUMNCOUNT = pcolumncount;
			composite = pcomposite;

			dispnodetocolor = new HashMap<DisplayNode, Color>();
			for (DisplayNode dispnode : dispnodes) {
				dispnodetocolor.put(dispnode,
						ColorConversion.getColor(lgui.getOIDToObject().getColorById(dispnode.getData().getOid())));
			}

			dispnodetorectangle = new HashMap<DisplayNode, Rectangle>();
		}

		/**
		 * Pass a relative mouse-position on the composite, which uses this
		 * listener. Then the displaynode on focus will be returned. If no node
		 * is focussed null is returned.
		 * 
		 * @param px
		 *            x-position of cursor within the composite
		 * @param py
		 *            y-position of cursor within the composite
		 * @return focussed DisplayNode or null, if nothing is focussed
		 */
		public DisplayNode getDisplayNodeAtPos(int px, int py) {

			int x = px - marginWidth;
			int y = py - marginHeight;

			if (x < 0 || y < 0) {
				return null;// Outside grid, left or top
			}

			if (rectwidth == 0 || rectheight == 0) {
				return null;
			}

			int col = x / rectwidth;
			int row = y / rectheight;

			if (col >= COLUMNCOUNT || row >= rowcount) {
				return null;
			}

			int index = row * COLUMNCOUNT + col;

			if (index >= dispnodes.size()) {
				return null;
			}

			return dispnodes.get(index);
		}

		private int w, h, rowcount, rectwidth, rectheight;// Parameters, which
															// are changed when
															// painting, they
															// can be used by
															// getDisplayNodeAtPos

		public void handleEvent(Event event) {

			Point size = composite.getSize();
			// Generate available size
			w = size.x - marginWidth * 2;
			h = size.y - marginHeight * 2;

			if (COLUMNCOUNT <= 0) {
				COLUMNCOUNT = 1;
			}

			// Calculate how many rows have to be painted
			rowcount = dispnodes.size() / COLUMNCOUNT;
			if (dispnodes.size() % COLUMNCOUNT != 0) {
				rowcount++;
			}

			if (rowcount == 0) {
				return;
			}

			rectwidth = w / COLUMNCOUNT;

			rectheight = h / rowcount;

			for (int x = 0; x < COLUMNCOUNT; x++) {

				for (int y = 0; y < rowcount; y++) {
					// get index of displaynode
					int index = y * COLUMNCOUNT + x;
					if (index >= dispnodes.size()) {
						break;
					}

					// Rectangle frame
					Rectangle r = new Rectangle(marginWidth + rectwidth * x, marginHeight + rectheight * y, rectwidth
							- horizontalSpacing, rectheight - verticalSpacing);

					// Paint outer rectangle
					event.gc.setBackground(bordercolor);
					event.gc.fillRectangle(r.x, r.y, r.width, r.height);

					DisplayNode dispnode = dispnodes.get(index);
					// Paint it
					if (lgui.getObjectStatus().isAnyMousedown()
							&& !lgui.getObjectStatus().isMousedown(dispnode.getConnectedObject())) {// Change
																									// color
						event.gc.setBackground(ColorConversion.getColor(lgui.getOIDToObject().getColorById(null)));
					} else {
						event.gc.setBackground(dispnodetocolor.get(dispnode));
					}

					int border = normalborder;

					if (lgui.getObjectStatus().isMouseover(dispnode.getConnectedObject())) {
						border = mouseborder;
					}
					// System.out.println("fillRec ("+(r.x+border)+", "+(r.y+border)+") "+(r.width-2*border)+"X"+(r.height-2*border
					// ));
					event.gc.fillRectangle(r.x + border, r.y + border, r.width - 2 * border, r.height - 2 * border);

					dispnodetorectangle.put(dispnode, r);// save the current
															// rectangle
				}

			}
		}

	}

	/**
	 * Call this function if displaynode != null
	 */
	private void insertInnerPanel() {
		insertInnerPanel(node.getLevel(), node.getScheme(), node.getData(), node.getLevelNrs());
	}

	/**
	 * If inner panels exist and maxlevel is greater than alevel, then insert
	 * lower level-panels
	 */
	private void insertInnerPanel(int alevel, Object ascheme, Object adata, ArrayList<Integer> levels) {

		// At least insert one panel with backgroundcolor as bordercomposite
		borderFrame = new BorderComposite(mainPanel, SWT.NONE);
		// Bordercolor is defined by this parameter
		Color bordercolor = ColorConversion.getColor(LMLColor.stringToColor(apref.getBordercolor()));

		borderFrame.setBorderColor(bordercolor);
		borderFrame.setBorderWidth(apref.getBorder().intValue());
		borderFrame.setLayoutData(BorderData.CENTER);

		if (maxLevel <= alevel) {// Do other panels have to be inserted?
			innerPanel = new Composite(borderFrame, SWT.NONE);
			innerPanel.setBackground(jobColor);
			return;
		}

		innerPanel = new Composite(borderFrame, SWT.NONE);

		innerPanel.setBackground(backgroundColor);

		if (centerPic != null) {
			innerPanel.setBackgroundImage(centerPic);
		}

		// if( (apref.isHighestrowfirst() && !apref.isHighestcolfirst()) ||
		// (!apref.isHighestrowfirst() && apref.isHighestcolfirst()) ){
		// innerpanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		// }

		// Generate all displaynodes for elements in lml-tree which are childs
		// of the current node
		ArrayList<DisplayNode> dispnodes = getLowerDisplayNodes(ascheme, adata, levels, model, apref.isHighestrowfirst());

		if (alevel == maxLevel - 1) {// Paint rects instead of use composites
										// for lowest-level-rectangles

			final RectPaintListener listener = new RectPaintListener(dispnodes, apref.getCols().intValue(), innerPanel);
			listener.horizontalSpacing = apref.getHgap().intValue();
			listener.verticalSpacing = apref.getVgap().intValue();

			listener.normalborder = apref.getBorder().intValue();
			listener.mouseborder = apref.getMouseborder().intValue();

			listener.bordercolor = bordercolor;

			// Add all listeners to the innerpanel now
			innerPanel.addListener(SWT.Paint, listener);

			innerPanel.addMouseMoveListener(new MouseMoveListener() {

				public void mouseMove(MouseEvent e) {

					DisplayNode focussed = listener.getDisplayNodeAtPos(e.x, e.y);

					if (focussed != null) {
						lgui.getObjectStatus().mouseover(focussed.getConnectedObject());
					} else {
						lgui.getObjectStatus().mouseExitLast();
					}

				}
			});

			innerPanel.addMouseListener(new MouseListener() {

				public void mouseUp(MouseEvent e) {
					DisplayNode focussed = listener.getDisplayNodeAtPos(e.x, e.y);

					if (focussed != null) {
						lgui.getObjectStatus().mouseup(focussed.getConnectedObject());
					}
				}

				public void mouseDown(MouseEvent e) {
					DisplayNode focussed = listener.getDisplayNodeAtPos(e.x, e.y);

					if (focussed != null) {
						lgui.getObjectStatus().mousedown(focussed.getConnectedObject());
					}
				}

				public void mouseDoubleClick(MouseEvent e) {
				}
			});

			innerPanel.addListener(SWT.MouseExit, new Listener() {

				public void handleEvent(Event event) {
					lgui.getObjectStatus().mouseExitLast();
				}

			});
		} else { // insert lower nodedisplays, nest composites

			// Set Gridlayout only if needed
			GridLayout layout = new GridLayout(apref.getCols().intValue(), true);
			layout.horizontalSpacing = apref.getHgap().intValue();
			layout.verticalSpacing = apref.getVgap().intValue();

			layout.marginWidth = 1;
			layout.marginHeight = 1;

			innerPanel.setLayout(layout);

			for (DisplayNode dispnode : dispnodes) {

				NodedisplayComp ainner = new NodedisplayComp(lgui, model, dispnode, nodeview, maxLevel - currentLevel - 1,
						innerPanel, SWT.NONE);
				innerComps.add(ainner);

				if (gridData == null) {
					gridData = new GridData();
					gridData.grabExcessHorizontalSpace = true;
					gridData.grabExcessVerticalSpace = true;
					gridData.horizontalAlignment = GridData.FILL;
					gridData.verticalAlignment = GridData.FILL;
				}

				ainner.setLayoutData(gridData);

			}
		}

		// innerpanel.setOpaque(false);

		// mainpanel.add(innerpanel, BorderLayout.CENTER);
	}

	/**
	 * Show title or name for this panel
	 */
	public void showTitle() {
		titleLabel.setVisible(true);
		apref.setShowtitle(true);
	}

	/**
	 * Hide title or name for this panel
	 */
	public void hideTitle() {
		titleLabel.setVisible(false);
		apref.setShowtitle(false);
	}

	// Draw background picture if set
	// public void paintComponent(Graphics g){
	// super.paintComponent(g);
	//
	// //Draw Center-Picture
	// if(centerpic != null){
	// // g.drawImage(centerpic.getPic(), mainpanel.getX(), mainpanel.getY(),
	// (int)Math.round(centerpic.getPercentWidth()*mainpanel.getWidth()) ,
	// // (int)Math.round(centerpic.getPercentHeight()*mainpanel.getHeight() ),
	// null);
	//
	// centerpic.paintComponent(g);
	// }
	// }

	// Needed for actions at firstpaint, right after reading the LML-model
	private final boolean firstpaint = true;

	// public void paint(Graphics g){
	//
	// if(firstpaint){//This is needed for picture-resizing on first paint
	//
	// firstpaint=false;
	//
	// if(pictures.size()==0 && centerpic==null)//if there are no pictures=>just
	// call paint
	// super.paint(g);
	// else{//otherwise first resizePictures, then call repaint, for repainting
	// these panels
	// //This method is needed, because in constructor the pictures can not be
	// resized, because the size of the panel is unknown before first paint
	// resizePictures();
	// }
	//
	// }
	// else{
	//
	// super.paint(g);
	// }
	// }

	public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown) {

		if (node == null) {
			if (innerPanel != null) {
				innerPanel.redraw();
			}
			return;
		}

		ObjectType conobject = node.getConnectedObject();
		if (currentLevel == maxLevel) {

			if (lgui.getObjectStatus().isMouseover(conobject)) {
				borderFrame.setBorderWidth(apref.getMouseborder().intValue());
			} else {
				borderFrame.setBorderWidth(apref.getBorder().intValue());
			}

			if (lgui.getObjectStatus().isAnyMousedown() && !lgui.getObjectStatus().isMousedown(conobject)) {// Change
																											// color
				innerPanel.setBackground(ColorConversion.getColor(lgui.getOIDToObject().getColorById(null)));
			} else {
				innerPanel.setBackground(jobColor);
			}
		} else if (currentLevel == maxLevel - 1) {// For rectangle-paint of
													// lowest-level-elements
			innerPanel.redraw();
		}
	}

	// *************************************Test-methods

	/**
	 * Load Lgui-object-hierarchy from file
	 * 
	 * @return Object of LguiType parsed out of an lml-File
	 * @throws JAXBException
	 */
	public static LguiType parseLML(URL xml, URL xsd) throws JAXBException {
		// Causes errors while used in applet

		JAXBContext jc = JAXBContext.newInstance("org.eclipse.ptp.rm.lml.internal.core.elements");

		Unmarshaller unmar = jc.createUnmarshaller();

		if (xsd != null) {
			Schema mySchema;
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				mySchema = sf.newSchema(xsd);
			} catch (SAXException saxe) {
				// ...(error handling)
				mySchema = null;
			}

			// Connect schema to unmarshaller
			unmar.setSchema(mySchema);
		}

		// Validate lml-file and unmarshall in one step
		JAXBElement<LguiType> doc = (JAXBElement<LguiType>) unmar.unmarshal(xml);
		// Get root-element
		LguiType lml = doc.getValue();

		return lml;
	}

	public static void main(String[] args) throws URISyntaxException {

		Display display = new Display();// display environment, is needed for
										// every swt-application
		Shell shell = new Shell(display);// Root container for dialogs and
											// subcomponents, widgets

		// Load Lgui-data
		String urlpath = "file:///private/InstiArbeit/LLView/LML/src/LLapp/data/juropa_layout_lml_1parts.xml";

		LguiType lml = null;
		try {
			lml = parseLML(new URL(urlpath), null);
		} catch (MalformedURLException e) {
			System.out.println("URL for lml-file is not valid: " + urlpath);
		} catch (JAXBException e) {
			System.out.println("Problem by parsing lml-file. File is not valid.");
		}
		System.out.println("LML-file parsed");

		LguiItem lmlmanager = new LguiItem(lml);

		// Search first nodedisplay
		Nodedisplay nodedisplaymodel = null;
		for (JAXBElement<?> el : lml.getObjectsAndRelationsAndInformation()) {
			if (el.getValue() instanceof Nodedisplay) {
				nodedisplaymodel = (Nodedisplay) el.getValue();
				break;
			}
		}

		// Allow access to LLapp-files from webserver
		// JarControll jc=new JarControll();
		// jc.allowAllCertificatesAndHostnames();
		// LLapp.authenticate();//set http-authentication

		shell.setLayout(new FillLayout());

		NodedisplayView nodeview = new NodedisplayView(lmlmanager, nodedisplaymodel, shell);

		shell.setSize(800, 600);
		shell.open();// show shell

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}

		// Dispose everything created on my own
		nodeview.dispose();
		ImageComp.disposeAll();
		ColorConversion.disposeColors();
		display.dispose();

	}

}