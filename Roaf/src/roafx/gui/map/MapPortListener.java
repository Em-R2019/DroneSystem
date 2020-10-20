/***********************************************************************
 *            The Real Object Application Framework (ROAF)             *
 *        Copyright (C) 2010 Kristof Beiglböck / kbeigl@roaf.de        *
 *                                                                     *
 *  This file is part of the Real Object Application Framework, ROAF.  *
 *                                                                     *
 *  The ROAF is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published  * 
 *  by the Free Software Foundation, either version 3 of the License,  *
 *  or (at your option) any later version.                             *
 *                                                                     *
 *  The ROAF is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.               *
 *  See the GNU General Public License for more details.               *
 *                                                                     *
 *  You should have received a copy of the GNU General Public License  *
 *  along with the ROAF.  If not, see <http://www.gnu.org/licenses/>.  *
 ***********************************************************************/
package roafx.gui.map;

import java.awt.geom.Rectangle2D;
import java.util.EventListener;
import javax.swing.event.ChangeEvent;

/**
 * @author Kristof Beiglböck <a href="http://roaf.de" target="_blank" title="The ROAF">@ The ROAF</a>
 * @version 1.0
 */
public interface MapPortListener extends EventListener
{
    /**
     * Invoked when the map port has changed.
     * Returns the current map port in decimal coordinates.
     */
    public void mapPortChangedTo( ChangeEvent e, Rectangle2D.Double mapPortGrid );
}
