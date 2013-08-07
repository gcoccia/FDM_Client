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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/dap/DummyFunction.java,v $
*/
package org.iges.grads.server.dap;

import java.io.*;
import java.util.*;


import dods.dap.*;
import dods.dap.Server.*;

/** A hack for the pre-JavaDODS 1.1 implementation of constraint expression
 *  parsing. When evaluated, the DummyFunction just prints its name and
 *  arguments. This is the only way to get this information from the JavaDODS
 *  1.0 API. 
 */
public class DummyFunction
    implements BoolFunction {

    public DummyFunction(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    /** Get the value of this function. If the values of
	all the arguments are guaranteed not to change, the value may be
	computed only once. If the values might change (i.e., they depend on
	values of sequences or remote variables) the function will be rerun
	for each call to this method.
	@return The value of the clause.
	@exception InvalidOperatorException is thrown if the function cannot 
	be evaluated on the given clauses
    */
    public String printArgs(List children)
	throws InvalidOperatorException, RegExpException,
	       NoSuchVariableException, SBHException, IOException,
	       SDODSException { 
	StringWriter value = new StringWriter();
	PrintWriter w = new PrintWriter(value);
	w.print(name);
	w.print(":");
	for (int i = 0; i < children.size(); i++) {
	    w.print(" ");
	    BaseType result = ((SubClause)children.get(i)).evaluate();
	    if (result instanceof DString) {
		w.print(((DString)result).getValue());
	    } else if (result instanceof DFloat64) {
		w.print(((DFloat64)result).getValue());
	    } else if (result instanceof DInt32) {
		w.print(((DInt32)result).getValue());
	    } 
	}
	w.close();
	return value.toString();
    }

   /** Get the value of this function. If the values of
	all the arguments are guaranteed not to change, the value may be
	computed only once. If the values might change (i.e., they depend on
	values of sequences or remote variables) the function will be rerun
	for each call to this method.
	@return The value of the clause.
	@exception InvalidOperatorException is thrown if the function cannot 
	be evaluated on the given clauses
   */
    public boolean evaluate(List args) { 
	return true;
    }

    public void checkArgs(List args) {
    }

    String name;
}
