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
package org.eclipse.ptp.ui.views;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewNodeEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.utils.core.BitSetIterable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author Daniel (JD) Barboza
 * 
 *         Based on original work by Greg Watson and Clement Chu
 * 
 */
public class MachinesNodesView extends ViewPart {

	private final class MachineChildListener implements IMachineChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IChangedNodeEvent)
		 */
		public void handleEvent(final IChangedNodeEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.INewNodeEvent)
		 */
		public void handleEvent(final INewNodeEvent e) {
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					for (IPNode node : e.getNodes()) {
						// create graphical representation of the node
						NodeGraphicalRepresentation nodegr = new NodeGraphicalRepresentation(node.getName(), node.getID());
						// associate it with the correspondent IPMachine
						for (MachineGraphicalRepresentation machine : machinesGraphicalRepresentations) {
							if (machine.getMachineID().equals(node.getMachine().getID())) {
								machine.addNode(nodegr);
								nodesHashMap.put(node.getID(), nodegr);
								break;
							}
						}
					}
				}
			});
			refreshView();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IRemoveNodeEvent)
		 */
		public void handleEvent(final IRemoveNodeEvent e) {
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					for (IPNode node : e.getNodes()) {
						// delete the node graphical view on the corresponding
						// IPMachine
						for (MachineGraphicalRepresentation machine : machinesGraphicalRepresentations) {
							if (machine.getMachineID().equals(node.getMachine().getID())) {
								machine.removeNode(node.getID());
								nodesHashMap.remove(node.getID());
								break;
							}
						}
					}
				}
			});
			refreshView();
		}
	}

	private class MachineGraphicalRepresentation {
		private ArrayList<NodeGraphicalRepresentation> nodes = null;
		private Rectangle rectangle = null;
		private String machineName = ""; //$NON-NLS-1$
		private String machineID = ""; //$NON-NLS-1$
		private static final int WIDTH = 50;
		private static final int HEIGHT = 50;
		private Color color = null;
		private boolean halted;
		private boolean selected = false;

		@SuppressWarnings("unused")
		public MachineGraphicalRepresentation(String machineName, Color color, int Ox, int Oy) {
			this.machineName = machineName;
			this.color = color;
			nodes = new ArrayList<NodeGraphicalRepresentation>();
			rectangle = new Rectangle(Ox, Oy, WIDTH, HEIGHT);
		}

		@SuppressWarnings("unused")
		public MachineGraphicalRepresentation(String machineName, Color color, int Ox, int Oy, int width, int height) {
			this.machineName = machineName;
			this.color = color;
			nodes = new ArrayList<NodeGraphicalRepresentation>();
			rectangle = new Rectangle(Ox, Oy, width, height);
		}

		public MachineGraphicalRepresentation(String machineName, String machineID) {
			this.machineName = machineName;
			this.setMachineID(machineID);
			nodes = new ArrayList<NodeGraphicalRepresentation>();
			rectangle = new Rectangle(0, 0, WIDTH, HEIGHT);
			halted = false;
		}

		@SuppressWarnings("unused")
		public MachineGraphicalRepresentation(String machineName, String machineID, Color color) {
			this.machineName = machineName;
			this.setMachineID(machineID);
			nodes = new ArrayList<NodeGraphicalRepresentation>();
			rectangle = new Rectangle(0, 0, WIDTH, HEIGHT);
			this.color = color;
			halted = false;
		}

		public boolean addNode(NodeGraphicalRepresentation node) {
			return nodes.add(node);
		}

		public boolean containsPoint(int x, int y) {
			return rectangle.contains(x, y);
		}

		@SuppressWarnings("unused")
		public Color getColor() {
			return color;
		}

		public Rectangle getGraphicalRepresentation() {
			return rectangle;
		}

		public String getMachineID() {
			return machineID;
		}

		public String getMachineName() {
			return machineName;
		}

		public ArrayList<NodeGraphicalRepresentation> getNodes() {
			return nodes;
		}

		public boolean isHalted() {
			return halted;
		}

		public boolean isSelected() {
			return selected;
		}

		@SuppressWarnings("unused")
		public boolean removeNode(NodeGraphicalRepresentation node) {
			return nodes.remove(node);
		}

		public void removeNode(String nodeID) {
			for (int i = 0; i < nodes.size(); i++) {
				if (nodes.get(i).getNodeID().equals(nodeID)) {
					nodes.remove(i);
					break;
				}
			}
		}

		@SuppressWarnings("unused")
		public void setColor(Color color) {
			this.color = color;
		}

		public void setHalted(boolean halted) {
			this.halted = halted;
		}

		public void setMachineID(String nodeID) {
			this.machineID = nodeID;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}
	}

	private class MachineNodesCanvas extends Canvas {
		private Composite composite = null;
		Point origin = new Point(0, 0);
		ScrollBar verticalBar = null;
		ScrollBar horizontalBar = null;
		int WIDTH = 1000;
		int HEIGHT = 3000;

		public MachineNodesCanvas(Composite composite, int style) {
			super(composite, style);
			this.composite = composite;
			/*
			 * Limit the width size based on screen width
			 */
			if (getDisplay().getPrimaryMonitor() != null)
				WIDTH = getDisplay().getPrimaryMonitor().getBounds().width;
			installListeners();
		}

		protected void handleMouseDown(Event e) {
			/*
			 * Check if the user clicked on some element of our view. First,
			 * check if the y coordinate matches with some of our machine
			 * representations, considering that our nodes y's range will be the
			 * same as their respective machines.
			 */
			int mouseX = e.x - origin.x;
			int mouseY = e.y - origin.y;
			Object selection = null;
			for (MachineGraphicalRepresentation machinegr : machinesGraphicalRepresentations) {
				if ((mouseY > machinegr.getGraphicalRepresentation().y)
						&& (mouseY < machinegr.getGraphicalRepresentation().y + machinegr.getGraphicalRepresentation().height)) {
					/*
					 * mouseY belongs to this machine representation's Y range.
					 * Now we check the machine and its nodes to see if we have
					 * a match
					 */
					if (machinegr.containsPoint(mouseX, mouseY))
						selection = machinegr;
					else {
						for (NodeGraphicalRepresentation nodegr : machinegr.getNodes()) {
							if (nodegr.containsPoint(mouseX, mouseY)) {
								selection = nodegr;
								break;
							}
						}
					}
					break;
				}
			}
			if (selection != elementSelected) {
				elementSelected = selection;
				refreshView();
			}
		}

		protected void handlePaint(Event event) {
			Rectangle clientArea = getClientArea();

			int renderWidth = clientArea.width;
			int renderHeight = clientArea.height;

			if (event.height == 0 || (renderWidth == 0 && renderHeight == 0)) {
				// Check if there is work to do
				return;
			}

			Image imageBuffer = null;
			GC newGC = null;
			imageBuffer = new Image(getDisplay(), WIDTH, HEIGHT);
			newGC = new GC(imageBuffer, SWT.LEFT_TO_RIGHT);

			/*
			 * set font to our predefined default font
			 */
			newGC.setFont(defaultFont);

			/*
			 * set some parameters regarding font size
			 */
			int fontHeight = defaultFont.getFontData()[0].getHeight();

			/*
			 * If some graphical element is selected, write additional info
			 * about it on the top of the view.
			 */
			String additionalInfo = null;
			if (elementSelected != null) {
				additionalInfo = Messages.MachinesNodesView_0;
				newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				if (elementSelected instanceof MachineGraphicalRepresentation) {
					MachineGraphicalRepresentation machine = (MachineGraphicalRepresentation) elementSelected;
					additionalInfo += Messages.MachinesNodesView_1 + machine.getMachineName() + Messages.MachinesNodesView_2
							+ machine.getMachineID();
				} else if (elementSelected instanceof NodeGraphicalRepresentation) {
					NodeGraphicalRepresentation node = (NodeGraphicalRepresentation) elementSelected;
					additionalInfo += Messages.MachinesNodesView_3 + node.getNodeName() + Messages.MachinesNodesView_2
							+ node.getNodeID();
					if (node.getNumberOfJobs() > 0) {
						additionalInfo += Messages.MachinesNodesView_4;
						for (String jobID : node.getJobsIDs())
							additionalInfo += jobID + " "; //$NON-NLS-1$
					}
				}

				// mark this element to receive different background color
				if (elementSelected instanceof MachineGraphicalRepresentation)
					((MachineGraphicalRepresentation) elementSelected).setSelected(true);
				else
					((NodeGraphicalRepresentation) elementSelected).setSelected(true);
			}

			/*
			 * Some constants used to paint machines and nodes
			 */
			int Ox = 10;
			int x = Ox;
			int Oy = 10;
			int y = Oy;
			int nodeWidth = fontHeight * 2;
			int nodeHeight = fontHeight * 2;

			/*
			 * It seems that 2* fontHeight is equivalent to the exact height
			 * size of the font in pixels. This space will be used to draw the
			 * info string later
			 */
			y = y + (fontHeight * 4);

			/*
			 * paint machines and processes
			 */
			for (MachineGraphicalRepresentation machinegr : machinesGraphicalRepresentations) {
				if (!machinegr.isHalted()) {
					if (machinegr.isSelected()) {
						newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
						machinegr.setSelected(false);
					} else
						newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_RED));

					machinegr.getGraphicalRepresentation().x = x;
					machinegr.getGraphicalRepresentation().y = y;
					newGC.fillGradientRectangle(x, y, machinegr.getGraphicalRepresentation().width,
							machinegr.getGraphicalRepresentation().height, false);
					int currentX = machinegr.getGraphicalRepresentation().x + MachineGraphicalRepresentation.WIDTH + 10;
					int currentY = machinegr.getGraphicalRepresentation().y;
					// paint process of this node
					if (machinegr.getNodes() != null) {
						/*
						 * If the machine has more than 5 nodes, we draw them in
						 * 2 rows. otherwise, draw them in a single row
						 */
						if (machinegr.getNodes().size() > 5) {
							int totalSize = machinegr.getNodes().size();
							int half = totalSize / 2;
							int i;
							int xRow = currentX;
							for (i = 0; i < half + (totalSize % 2); i++) {
								NodeGraphicalRepresentation nodegr = machinegr.getNodes().get(i);
								// adjusting node graphical representation
								nodegr.getGraphicalRepresentation().width = nodeWidth;
								nodegr.getGraphicalRepresentation().height = nodeHeight;
								nodegr.getGraphicalRepresentation().x = currentX;
								nodegr.getGraphicalRepresentation().y = currentY;
								if (nodegr.isSelected()) {
									newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
									nodegr.setSelected(false);
								} else
									newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
								newGC.fillRectangle(nodegr.getGraphicalRepresentation());
								newGC.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
								if (nodegr.getNumberOfJobs() > 9)
									newGC.drawString("+", currentX + fontHeight / 2, currentY); //$NON-NLS-1$
								else
									newGC.drawString(String.valueOf(nodegr.getNumberOfJobs()), currentX + fontHeight / 2, currentY);
								newGC.drawRectangle(nodegr.getGraphicalRepresentation());
								currentX = currentX + NodeGraphicalRepresentation.WIDTH + 1;
							}

							// updatex and y to paint next row
							//
							currentX = xRow;
							currentY += nodeHeight + 5;
							for (int j = i; j < totalSize; j++) {
								NodeGraphicalRepresentation nodegr = machinegr.getNodes().get(j);
								// adjusting node graphical representation
								nodegr.getGraphicalRepresentation().width = nodeWidth;
								nodegr.getGraphicalRepresentation().height = nodeHeight;
								nodegr.getGraphicalRepresentation().x = currentX;
								nodegr.getGraphicalRepresentation().y = currentY;
								if (nodegr.isSelected()) {
									newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
									nodegr.setSelected(false);
								} else
									newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
								newGC.fillRectangle(nodegr.getGraphicalRepresentation());
								newGC.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
								if (nodegr.getNumberOfJobs() > 9)
									newGC.drawString("+", currentX + fontHeight / 2, currentY); //$NON-NLS-1$
								else
									newGC.drawString(String.valueOf(nodegr.getNumberOfJobs()), currentX + fontHeight / 2, currentY);
								newGC.drawRectangle(nodegr.getGraphicalRepresentation());
								currentX = currentX + NodeGraphicalRepresentation.WIDTH + 1;
							}
						}
						/*
						 * If the machine has less than 5 nodes, paint all of
						 * them in a single row.
						 */
						else
							for (NodeGraphicalRepresentation nodegr : machinegr.getNodes()) {
								// adjusting node graphical representation
								nodegr.getGraphicalRepresentation().width = nodeWidth;
								nodegr.getGraphicalRepresentation().height = nodeHeight;
								nodegr.getGraphicalRepresentation().x = currentX;
								nodegr.getGraphicalRepresentation().y = currentY;
								if (nodegr.isSelected()) {
									newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
									nodegr.setSelected(false);
								} else
									newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
								newGC.fillRectangle(nodegr.getGraphicalRepresentation());
								newGC.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
								if (nodegr.getNumberOfJobs() > 9)
									newGC.drawString("+", currentX + fontHeight / 2, currentY); //$NON-NLS-1$
								else
									newGC.drawString(String.valueOf(nodegr.getNumberOfJobs()), currentX + fontHeight / 2, currentY);
								newGC.drawRectangle(nodegr.getGraphicalRepresentation());
								currentX = currentX + NodeGraphicalRepresentation.WIDTH + 1;
							}
					}
					// update y to print machine name
					y = y + MachineGraphicalRepresentation.HEIGHT + 10;
					newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					newGC.drawString(machinegr.getMachineName(), x, y);
					// update y to paint next machine
					y = y + 30;
				}
			}
			/*
			 * We write the info string now, thus it'll be on top and always
			 * visible. Note that we use x instead of the relative position
			 * because we want this string to be 'scrollable' on the
			 * horizontal., but not on the vertical
			 */
			if (additionalInfo != null) {
				newGC.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
				newGC.drawString(additionalInfo, x, Oy - origin.y);
			}

			event.gc.drawImage(imageBuffer, origin.x, origin.y);
			newGC.dispose();
			imageBuffer.dispose();
		}

		protected void handleResize(Event event) {
			Rectangle rect = getBounds();
			Rectangle client = getClientArea();
			horizontalBar.setMaximum(WIDTH);
			verticalBar.setMaximum(HEIGHT);
			horizontalBar.setThumb(Math.min(rect.width, client.width));
			verticalBar.setThumb(Math.min(rect.height, client.height));
			int hPage = WIDTH - client.width;
			int vPage = HEIGHT - client.height;
			int hSelection = horizontalBar.getSelection();
			int vSelection = verticalBar.getSelection();
			if (hSelection >= hPage) {
				if (hPage <= 0)
					hSelection = 0;
				origin.x = -hSelection;
			}
			if (vSelection >= vPage) {
				if (vPage <= 0)
					vSelection = 0;
				origin.y = -vSelection;
			}
			redraw();
		}

		protected void installListeners() {

			listener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						// handleDispose(event);
						break;
					case SWT.KeyDown:
						// handleKeyDown(event);
						break;
					case SWT.KeyUp:
						// handleKeyUp(event);
						break;
					case SWT.MouseDown:
						handleMouseDown(event);
						break;
					case SWT.MouseUp:
						// handleMouseUp(event);
						break;
					case SWT.MouseDoubleClick:
						// handleMouseDoubleClick(event);
						break;
					case SWT.MouseMove:
						// handleMouseMove(event);
						break;
					case SWT.MouseHover:
						// handleMouseHover(event);
						break;
					case SWT.Paint:
						handlePaint(event);
						break;
					case SWT.Resize:
						handleResize(event);
						break;
					case SWT.FocusOut:
						// handleFocusOut(event);
						break;
					}
				}
			};
			addListener(SWT.Dispose, listener);
			addListener(SWT.KeyDown, listener);
			addListener(SWT.KeyUp, listener);
			addListener(SWT.MouseDown, listener);
			addListener(SWT.MouseUp, listener);
			addListener(SWT.MouseDoubleClick, listener);
			addListener(SWT.MouseMove, listener);
			addListener(SWT.MouseHover, listener);
			addListener(SWT.Paint, listener);
			addListener(SWT.Resize, listener);
			addListener(SWT.FocusOut, listener);

			verticalBar = this.getVerticalBar();
			horizontalBar = this.getHorizontalBar();

			if (horizontalBar != null) {
				horizontalBar.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						int hSelection = horizontalBar.getSelection();
						origin.x = -hSelection;
						redraw();
					}
				});
			}

			if (verticalBar != null) {
				verticalBar.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						int vSelection = verticalBar.getSelection();
						origin.y = -vSelection;
						redraw();
					}
				});
			}

		}
	}

	private final class MMChildListener implements IModelManagerChildListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
		 */
		public void handleEvent(IChangedResourceManagerEvent e) {
			boolean needRefresh = false;
			for (IPResourceManager rm : e.getResourceManagers()) {
				if ((rm.getState() == ResourceManagerAttributes.State.STOPPED)
						|| (rm.getState() == ResourceManagerAttributes.State.ERROR)) {
					/*
					 * refresh the view, removing the resource manager machines,
					 * but not removing machine listeners.
					 */
					for (IPMachine machine : rm.getMachines()) {
						String machineID = machine.getID();
						for (MachineGraphicalRepresentation machinegr : machinesGraphicalRepresentations)
							if (machinegr.getMachineID().equals(machineID)) {
								machinegr.setHalted(true);
								needRefresh = true;
								break;
							}

					}
				}
				/*
				 * otherwise, reactivate machines of the started / starting
				 * resource manager
				 */
				else {
					for (IPMachine machine : rm.getMachines()) {
						String machineID = machine.getID();
						for (MachineGraphicalRepresentation machinegr : machinesGraphicalRepresentations)
							if (machinegr.getMachineID().equals(machineID)) {
								machinegr.setHalted(false);
								needRefresh = true;
								break;
							}
					}
				}
			}
			if (needRefresh)
				refreshView();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.INewResourceManagerEvent)
		 */
		public void handleEvent(INewResourceManagerEvent e) {
			/*
			 * Add resource manager child listener so we get notified when new
			 * machines are added to the model.
			 */
			final IPResourceManager rm = e.getResourceManager();
			rm.addChildListener(resourceManagerListener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public void handleEvent(IRemoveResourceManagerEvent e) {
			/*
			 * Removed resource manager child listener when resource manager is
			 * removed.
			 */
			e.getResourceManager().removeChildListener(resourceManagerListener);

		}
	}

	private class NodeGraphicalRepresentation {
		private Rectangle rectangle = null;
		private String nodeName = ""; //$NON-NLS-1$
		private String nodeID = ""; //$NON-NLS-1$
		private static final int WIDTH = 20;
		private static final int HEIGHT = 20;
		private boolean selected = false;
		private ArrayList<String> jobsIDs = null;

		public NodeGraphicalRepresentation(String nodeName, String nodeID) {
			this.nodeName = nodeName;
			this.nodeID = nodeID;
			rectangle = new Rectangle(0, 0, WIDTH, HEIGHT);
			jobsIDs = new ArrayList<String>();
		}

		public void addJob(String jobID) {
			if (!jobsIDs.contains(jobID))
				jobsIDs.add(jobID);
		}

		public boolean containsPoint(int x, int y) {
			return rectangle.contains(x, y);
		}

		public Rectangle getGraphicalRepresentation() {
			return rectangle;
		}

		public ArrayList<String> getJobsIDs() {
			return jobsIDs;
		}

		public String getNodeID() {
			return nodeID;
		}

		public String getNodeName() {
			return nodeName;
		}

		public int getNumberOfJobs() {
			return jobsIDs.size();
		}

		public boolean isSelected() {
			return selected;
		}

		public void removeJob(String jobID) {
			jobsIDs.remove(jobID);
		}

		@SuppressWarnings("unused")
		public void setGraphicalRepresentation(Rectangle ret) {
			rectangle = ret;
		}

		@SuppressWarnings("unused")
		public void setNodeID(String nodeID) {
			this.nodeID = nodeID;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

	}

	private class RefreshWorkbenchJob extends WorkbenchJob {
		private final ReentrantLock waitLock = new ReentrantLock();
		private final List<Boolean> refreshList = new ArrayList<Boolean>();

		public RefreshWorkbenchJob() {
			super(Messages.MachinesNodesView_5);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			boolean refreshAll = isRefreshAll();

			if (!canvas.isDisposed()) {
				canvas.redraw();
			}

			// if last refresh object is true and previous refresh is false,
			// then refresh again
			boolean lastValue = isRefreshAll();
			waitLock.lock();
			try {
				refreshList.clear();
				if (refreshAll != lastValue && !refreshAll) {
					refreshList.add(new Boolean(true));
					schedule();
				}
			} finally {
				waitLock.unlock();
			}
			return Status.OK_STATUS;
		}

		public void schedule(boolean refresh_all, boolean force) {
			waitLock.lock();
			try {
				if (force)
					refreshList.clear();
				refreshList.add(new Boolean(refresh_all));
			} finally {
				waitLock.unlock();
			}
			schedule();
		}

		@Override
		public boolean shouldSchedule() {
			int size = size();

			return (size == 1);
		}

		private boolean isRefreshAll() {
			waitLock.lock();
			try {
				return refreshList.get(refreshList.size() - 1).booleanValue();
			} finally {
				waitLock.unlock();
			}
		}

		private int size() {
			waitLock.lock();
			try {
				return refreshList.size();
			} finally {
				waitLock.unlock();
			}
		}
	}

	private final class RMChildListener implements IResourceManagerChildListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IChangedJobEvent)
		 */
		public void handleEvent(IChangedJobEvent e) {
			boolean needRefresh = false;
			for (IPJob job : e.getJobs()) {
				if ((job.getState() == JobAttributes.State.STARTING) || job.getState() == JobAttributes.State.RUNNING) {
					/*
					 * Add job to the graphical representation of its node
					 */
					final BitSet procJobRanks = job.getProcessJobRanks();
					for (Integer procJobRank : new BitSetIterable(procJobRanks)) {
						final String nodeId = job.getProcessNodeId(procJobRank);
						if (nodeId != null) {
							nodesHashMap.get(nodeId).addJob(job.getID());
							needRefresh = true;
						}
					}
				} else {
					/*
					 * remove job from the graphical representation of its node
					 */
					final BitSet procJobRanks = job.getProcessJobRanks();
					for (Integer procJobRank : new BitSetIterable(procJobRanks)) {
						final String nodeId = job.getProcessNodeId(procJobRank);
						if (nodeId != null) {
							nodesHashMap.get(nodeId).removeJob(job.getID());
							needRefresh = true;
						}
					}
				}
				if (needRefresh) {
					refreshView();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #
		 * handleEvent(org.eclipse.ptp.core.elements.events.IChangedMachineEvent
		 * )
		 */
		public void handleEvent(IChangedMachineEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(final INewJobEvent e) {
			boolean needRefresh = false;
			for (IPJob job : e.getJobs()) {
				/*
				 * Add job to the graphical representation of its node
				 */
				final BitSet procJobRanks = job.getProcessJobRanks();
				for (Integer procJobRank : new BitSetIterable(procJobRanks)) {
					final String nodeId = job.getProcessNodeId(procJobRank);
					if (nodeId != null) {
						nodesHashMap.get(nodeId).addJob(job.getID());
						needRefresh = true;
					}
				}
			}
			if (needRefresh)
				refreshView();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewMachineEvent)
		 */
		public void handleEvent(final INewMachineEvent e) {
			boolean needRefresh = false;
			for (IPMachine machine : e.getMachines()) {
				/*
				 * check if the machine wasn't added already on the start of the
				 * view
				 */
				if (!machineRepresentationExists(machine.getID())) {
					/*
					 * Add us as a child listener so we get notified of node
					 * events
					 */
					machine.addChildListener(machineListener);
					MachineGraphicalRepresentation machinegr = new MachineGraphicalRepresentation(machine.getName(),
							machine.getID());
					machinesGraphicalRepresentations.add(machinegr);
					needRefresh = true;
				}
			}
			// refresh if needed
			if (needRefresh)
				refreshView();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
			boolean needRefresh = false;
			for (IPJob job : e.getJobs()) {
				/*
				 * remove job from the graphical representation of its node
				 */
				final BitSet procJobRanks = job.getProcessJobRanks();
				for (Integer procJobRank : new BitSetIterable(procJobRanks)) {
					final String nodeId = job.getProcessNodeId(procJobRank);
					if (nodeId != null) {
						nodesHashMap.get(nodeId).removeJob(job.getID());
						break;
					}
				}
			}
			if (needRefresh)
				refreshView();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #
		 * handleEvent(org.eclipse.ptp.core.elements.events.IRemoveMachineEvent)
		 */
		public void handleEvent(final IRemoveMachineEvent e) {
			/*
			 * Update views when a machine is removed.
			 */
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					for (IPMachine machine : e.getMachines()) {
						for (int i = 0; i < machinesGraphicalRepresentations.size(); i++) {
							if (machinesGraphicalRepresentations.get(i).getMachineID().equals(machine.getID())) {
								machinesGraphicalRepresentations.remove(i);
								break;
							}
						}
						/*
						 * Remove child listener
						 */
						machine.removeChildListener(machineListener);
					}

				}
			});
			refreshView();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
		}
	}

	private final IModelManagerChildListener modelManagerListener = new MMChildListener();
	private final IResourceManagerChildListener resourceManagerListener = new RMChildListener();
	private final IMachineChildListener machineListener = new MachineChildListener();
	private final ArrayList<MachineGraphicalRepresentation> machinesGraphicalRepresentations = new ArrayList<MachineGraphicalRepresentation>();
	private final Hashtable<String, NodeGraphicalRepresentation> nodesHashMap = new Hashtable<String, NodeGraphicalRepresentation>();

	protected RefreshWorkbenchJob refreshWorkbench = new RefreshWorkbenchJob();
	private MachineNodesCanvas canvas = null;
	private Listener listener = null;
	private Font defaultFont = null;
	private Object elementSelected = null;

	public MachinesNodesView() {
		super();
	}

	@Override
	public void createPartControl(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		canvas = new MachineNodesCanvas(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		FontData[] fd = composite.getDisplay().getSystemFont().getFontData();
		for (int i = 0; i < fd.length; i++) {
			fd[i].setHeight(8);
		}
		defaultFont = new Font(composite.getDisplay(), fd);

		/*
		 * Wait until the view has been created before registering for events
		 */
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();

		synchronized (mm) {
			/*
			 * Add us to any existing RM's. I guess it's possible we could miss
			 * a RM if a new event arrives while we're doing this, but is it a
			 * problem?
			 */
			for (IPResourceManager rm : mm.getUniverse().getResourceManagers()) {
				rm.addChildListener(resourceManagerListener);
				/*
				 * We need to get the current state of the nodes on this
				 * resource manager, browsing through the machines and adding
				 * them to our view
				 */
				for (int i = 0; i < rm.getMachines().length; i++) {
					IPMachine machine = rm.getMachines()[i];
					machine.addChildListener(machineListener);
					MachineGraphicalRepresentation machinegr = new MachineGraphicalRepresentation(machine.getName(),
							machine.getID());
					for (IPNode node : machine.getNodes()) {
						// create graphical representation of the node
						NodeGraphicalRepresentation nodegr = new NodeGraphicalRepresentation(node.getName(), node.getID());
						machinegr.addNode(nodegr);
						machinesGraphicalRepresentations.add(machinegr);
					}
				}
			}
			mm.addListener(modelManagerListener);
		}

		refreshView();
	}

	/*
	 * Method required so the class can extends ViewPart
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

	private boolean machineRepresentationExists(String machineID) {
		for (MachineGraphicalRepresentation machinegr : machinesGraphicalRepresentations)
			if (machinegr.getMachineID().equals(machineID))
				return true;
		return false;
	}

	private void refreshView() {
		refreshWorkbench.schedule(true, true);
	}
}
