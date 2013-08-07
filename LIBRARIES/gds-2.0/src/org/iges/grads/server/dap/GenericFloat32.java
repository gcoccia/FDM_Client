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
* Revision for this file: $Revision: 1.5 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GenericFloat32.java,v $
*/
package org.iges.grads.server.dap;

import dods.dap.Server.*;
import dods.dap.*;
import java.io.*;


/** Implements the server-side version of Float32 
 * All datatypes that appear in DDS'es for GrADS datasets must have
 * server-side implementations, even if its just a shell.
 *
 * Last modified: $Date: 2008/07/22 17:22:28 $ 
 * Revision for this file: $Revision: 1.5 $
 * Release name: $Name: v2_0 $
 * Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GenericFloat32.java,v $
 */
public class GenericFloat32 
    extends SDFloat32 {
    
    /** Constructs a new <code>GenericFloat32</code>. */
    public GenericFloat32() { 
	super(); 
    }
    
    /**
     * Constructs a new <code>GenericFloat32</code> with name <code>n</code>.
     * @param n the name of the variable.
     */
    public GenericFloat32(String n) { 
	super(n); 
    }
    
        
    /** Dummy procedure
     */
    public boolean read(String datasetName, Object specialO)
	throws NoSuchVariableException, IOException, EOFException {
        setRead(true);
        return false;
    }
}


