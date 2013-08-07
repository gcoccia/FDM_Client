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
* Revision for this file: $Revision: 1.7 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GradsGrid.java,v $
*/
package org.iges.grads.server.dap;

import dods.dap.Server.*;
import dods.dap.*;
import java.io.*;
import java.util.*;

/**
 * Represents a server-side Grid object for the GrADS Data server.
 * All datatypes that appear in DDS'es for GrADS datasets must have
 * server-side implementations, even if its just a shell.
 * @author Joe Wielgosz (joew@cola.iges.org)
 *
 * Last modified: $Date: 2008/07/22 17:22:28 $ 
 * Revision for this file: $Revision: 1.7 $
 * Release name: $Name: v2_0 $
 * Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GradsGrid.java,v $
 */
public class GradsGrid 
    extends SDGrid {
    
  /** Constructs a new GradsGrid. */
  public GradsGrid() { 
    super(); 
  }

  /**
   * Constructs a new GradsGrid with name n.
   * @param n the name of the variable.
   */
  public GradsGrid(String n) { 
    super(n); 
  }
 
    /** Read a value from the named dataset for this variable. 
        @param datasetName String identifying the file or other data store
        from which to read a vaue for this variable.
        @param specialO not used in this implementation
        @return always false in this implementation
    */
    public boolean read(String datasetName, Object specialO)
	throws NoSuchVariableException, IOException, EOFException {
	
	// First read the contents of the grid
        SDArray contents = (SDArray)getVar(0);
        if(contents.isProject())
            contents.read(datasetName, specialO);
	
	// Then read the map arrays for each dimension
        for(int i = 0; i < contents.numDimensions(); i++){
            SDArray map = (SDArray)getVar(i+1);
            if(map.isProject())
                map.read(datasetName,specialO);
        }
   	    
	// Flag read operation as complete.
	setRead(true);
	
	// False means no more data to read.
	return false;
    }


    /** Returns a list of dimension vectors for this grid.
     *  Why this isn't in the DODS API, I don't know..
     */
    public Vector getDimensions() {
	return new Vector(mapVars);
    }



    
}











