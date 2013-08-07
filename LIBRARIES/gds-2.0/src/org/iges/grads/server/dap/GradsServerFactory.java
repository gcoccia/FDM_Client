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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GradsServerFactory.java,v $
*/
package org.iges.grads.server.dap;
import dods.dap.*;

/** A factory for GrADS Data server-side data objects 
 *  The only supported types are Float32, Float64, String, Sequence, 
 *  Grid, and Array.
 * @author Joe Wielgosz (joew@cola.iges.org)
 *
 * Last modified: $Date: 2008/07/22 17:22:28 $ 
 * Revision for this file: $Revision: 1.5 $
 * Release name: $Name: v2_0 $
 * Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/GradsServerFactory.java,v $
*/
public class GradsServerFactory 
    extends DefaultFactory {

    /** Returns a unique instance of this class */
    public static GradsServerFactory getFactory() {
	if (factory == null) {
	    factory = new GradsServerFactory();
	}
	return factory;
    }

    private static GradsServerFactory factory;

    /** 
     * Construct a new DFloat32.
     * @return the new DFloat32
     */
    public DFloat32 newDFloat32() {
	return new GenericFloat32();
    }

    /**
     * Construct a new DFloat32 with name n.
     * @param n the variable name
     * @return the new DFloat32
     */
    public DFloat32 newDFloat32(String n) {
	return new GenericFloat32(n);
    }

    /** 
     * Construct a new DFloat64.
     * @return the new DFloat64
     */
    public DFloat64 newDFloat64() {
	return new GenericFloat64();
    }

    /**
     * Construct a new DFloat64 with name n.
     * @param n the variable name
     * @return the new DFloat64
     */
    public DFloat64 newDFloat64(String n) {
	return new GenericFloat64();
    }


    /** 
     * Construct a new DString.
     * @return the new DString
     */
    public DString newDString() {
	return new GenericString();
    }

    /**
     * Construct a new DString with name n.
     * @param n the variable name
     * @return the new DString
     */
    public DString newDString(String n) {
	return new GenericString();
    }

    /** 
     * Construct a new DInt32.
     * @return the new DInt32
     */
    public DInt32 newDInt32() {
	return new GenericInt32();
    }

    /**
     * Construct a new DInt32 with name n.
     * @param n the variable name
     * @return the new DInt32
     */
    public DInt32 newDInt32(String n) {
	return new GenericInt32(n);
    }

    /** 
     * Construct a new DArray.
     * @return the new DArray
     */
    public DArray newDArray() {
	return new GradsArray();
    }

    /**
     * Construct a new DArray with name n.
     * @param n the variable name
     * @return the new DArray
     */
    public DArray newDArray(String n) {
	return new GradsArray(n);
    }

    /** 
     * Construct a new DGrid.
     * @return the new DGrid
     */
    public DGrid newDGrid() {
	return new GradsGrid();
    }

    /**
     * Construct a new DGrid with name n.
     * @param n the variable name
     * @return the new DGrid
     */
    public DGrid newDGrid(String n) {
	return new GradsGrid(n);
    }

    /** 
     * Construct a new DSequence.
     * @return the new DSequence
     */
    public DSequence newDSequence() {
	return new GradsSequence();
    }

    /**
     * Construct a new DSequence with name n.
     * @param n the variable name
     * @return the new DSequence
     */
    public DSequence newDSequence(String n) {
	return new GradsSequence(n);
    }
}

