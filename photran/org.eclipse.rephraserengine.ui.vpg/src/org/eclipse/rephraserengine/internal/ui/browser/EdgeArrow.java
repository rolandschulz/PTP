/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abhishek Sharma, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.ui.browser;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * An edge in the {@link EdgesTab}.
 * 
 * @author Abhishek Sharma
 */
class EdgeArrow
{
    public static final int arrowAngle = 30;
    public static final int arrowLength = 15;

    private Point source;
    private Point target;
    private Point midpoint;
    private Double angle;

    public EdgeArrow(Rectangle srcRect, Rectangle sinkRect)
    {
        setTargetAndSourcePoint(srcRect, sinkRect);
        setMidpoint();
        setAngleBetweenLineAndXAxis();
    }

    private void setTargetAndSourcePoint(Rectangle srcRect, Rectangle sinkRect)
    {
        if (srcRect.y < sinkRect.y)
        {
            target = new Point(sinkRect.x, sinkRect.y);
            source = new Point(srcRect.x, srcRect.y + srcRect.height);

        }
        else if (srcRect.y > sinkRect.y)
        {
            target = new Point(sinkRect.x, sinkRect.y + sinkRect.height);
            source = new Point(srcRect.x, srcRect.y);
        }
        else
        {
            target = new Point(sinkRect.x, (sinkRect.height) / 2 + sinkRect.y);
            source = new Point(srcRect.x, (srcRect.height) / 2 + srcRect.y);
        }
    }

    private void setMidpoint()
    {
        midpoint = new Point((source.x + target.x) / 2, (target.y + source.y) / 2);
    }

    private void setAngleBetweenLineAndXAxis()
    {
        // atan2 -returns the angle between the line drawn between the source and sink rectangles
        // and the positive x-axis
        angle = Math.atan2((target.y - source.y), (target.x - source.x));
    }

    public void drawOn(GC gc)
    {
        drawLine(gc);
        drawArrow(gc);
    }

    private void drawLine(GC gc)
    {
        gc.drawLine(source.x, source.y, target.x, target.y);
    }

    private void drawArrow(GC gc)
    {
        int degreesBetweenArrowsAndLine = getDegrees(angle) + arrowAngle + 180;
        double radiansBetweenArrowsAndLine = getRadians(degreesBetweenArrowsAndLine);

        Point arrowPoint1 = new Point(midpoint.x
            + (int)(arrowLength * Math.cos(radiansBetweenArrowsAndLine)), midpoint.y
            + (int)(arrowLength * Math.sin(radiansBetweenArrowsAndLine)));

        // refers to the degrees between the second arrowpoint and the line between source and sink
        degreesBetweenArrowsAndLine = getDegrees(angle) - arrowAngle + 180;
        radiansBetweenArrowsAndLine = getRadians(degreesBetweenArrowsAndLine);

        // sets the x and y coordinates of arrowpoint1 in the direction obtained from the degrees
        // calculated
        // relative to the line drawn between source and sink rectangle
        Point arrowPoint2 = new Point(midpoint.x
            + (int)(arrowLength * Math.cos(radiansBetweenArrowsAndLine)), midpoint.y
            + (int)(arrowLength * Math.sin(radiansBetweenArrowsAndLine)));

        gc.setLineWidth(gc.getLineWidth() + 2);
        gc.drawLine(midpoint.x, midpoint.y, arrowPoint1.x, arrowPoint1.y);
        gc.drawLine(midpoint.x, midpoint.y, arrowPoint2.x, arrowPoint2.y);
        gc.setLineWidth(gc.getLineWidth() - 2);
    }

    private double getRadians(double d)
    {
        return (d) * Math.PI / 180;
    }

    private int getDegrees(Double radians)
    {
        double degrees = (180 / Math.PI) * radians;
        return (int)(degrees);
    }
}
