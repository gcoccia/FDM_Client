/* 
* Copyright (C) 2000-2008 Institute for Global Environment and Society /
*                        Center for Ocean-Land-Atmosphere Studies
* Author: Joe Wielgosz <joew@cola.iges.org>
* 
* This file is part of the GrADS Data Server.
* 
* The GrADS Data Server is free software; you can redistribute
* it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; either version
* 2, or (at your option) any later version.
* 
* The GrADS Data Server is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with the GrADS Data Server; see the file COPYRIGHT.  If
* not, write to the Free Software Foundation, Inc., 59 Temple Place -
* Suite 330, Boston, MA 02111-1307, USA.
* 
* You can contact IGES/COLA at 4041 Powder Mill Rd Ste 302, Calverton MD 20705.
* 
* Last modified: $Date: 2008/07/22 17:22:28 $ 
* Revision for this file: $Revision: 1.6 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GradsArray.java,v $
*/
package org.iges.grads.server.dap;

import java.io.*;
import org.iges.util.Bounds;
import dods.dap.Server.*;
import dods.dap.*;

/** An implementation of the DODS Array data type. 
 * @see org.iges.grads.server.GradsGridSubsetter
 * 
 *  Only certain arrays are legal: 
 *  a 3 or 4 dimensional array that represents actual data,
 *  where the array dimensions are either [time][lev][lat][lon] or 
 *  [time][lat][lon]; or
 *  a 1 dimensional array that represents the grid definition of 
 *  lon, lat, lev or time.
 *  All values are Float32, except time which is a Float64 using the
 *  COARDS time convention.
 * * 
 * @author Joe Wielgosz (joew@cola.iges.org)
 * 
 *
 * Last modified: $Date: 2008/07/22 17:22:28 $ 
 * Revision for this file: $Revision: 1.6 $
 * Release name: $Name: v2_0 $
 * Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GradsArray.java,v $
 */
public class GradsArray 
    extends SDArray {

    /** Constructs a new GradsArray. */
    public GradsArray() { 
        super(); 
    }

    /**
    * Constructs a new GradsArray with name n.
    * @param n the name of the variable.
    */
    public GradsArray(String n) { 
        super(n); 
    }

    /** Just a dummy procedure. Data is actually read 
     *  by the serialize() method, so it can be 
     *  written directly to the output stream.
     */
    public boolean read(String datasetName, Object specialO)
	throws NoSuchVariableException, 
	       IOException, 
	       EOFException {

	// Flag read operation as complete.
	setRead(true);
	// False means no more data to read.
	return false;
    }

    /** Translates dimension information stored in this object into a 
     *  Bounds.Grid object for the Dataset.getSubset() method.
     */ 
    public Bounds.Grid calculateBounds() {

	try {
	    // Calculate dimensions of grid
	    if (numDimensions() == 5) {
		return new Bounds.Grid
		    (getDimension(4).getStart(),
		     getDimension(4).getStop(),
		     getDimension(3).getStart(),
		     getDimension(3).getStop(),
		     getDimension(2).getStart(),
		     getDimension(2).getStop(),
		     getDimension(1).getStart(),
		     getDimension(1).getStop(),
		     getDimension(0).getStart(),
		     getDimension(0).getStop());
	    } else if (numDimensions() == 4 &&
		       getDimension(0).getName().equals("ens")) {  
		// variable has E dim but no Z dim
		return new Bounds.Grid
		    (getDimension(3).getStart(),
		     getDimension(3).getStop(),
		     getDimension(2).getStart(),
		     getDimension(2).getStop(),
		     0,
		     0,
		     getDimension(1).getStart(),
		     getDimension(1).getStop(),
		     getDimension(0).getStart(),
		     getDimension(0).getStop());
	    } else if (numDimensions() == 4 && 
		       getDimension(1).getName().equals("lev")) { 
		// variable has Z dim but no E dim
		return new Bounds.Grid
		    (getDimension(3).getStart(),
		     getDimension(3).getStop(),
		     getDimension(2).getStart(),
		     getDimension(2).getStop(),
		     getDimension(1).getStart(),
		     getDimension(1).getStop(),
		     getDimension(0).getStart(),
		     getDimension(0).getStop(),
		     0,
		     0);
	    } else { 
		// variable has no Z dim and no E dim
		return new Bounds.Grid
		    (getDimension(2).getStart(),
		     getDimension(2).getStop(),
		     getDimension(1).getStart(),
		     getDimension(1).getStop(),
		     0,
		     0,
		     getDimension(0).getStart(),
		     getDimension(0).getStop(),
		     0,
		     0);
	    }

	} 
	catch (InvalidParameterException ipe) {
	    throw new RuntimeException("internal dimension mismatch!");
	}
    }
    

    


}
