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
* Last modified: $Date: 2008/07/22 17:22:27 $ 
* Revision for this file: $Revision: 1.7 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/GradsSubsetter.java,v $
*/
package org.iges.grads.server;

import java.io.*;

import dods.dap.Server.*;

import org.iges.anagram.*;

/** Generic interface for modules that handle subsetting of GrADS datasets. */
public abstract class GradsSubsetter 
    extends AbstractModule {

    public String getModuleID() {
	return "subset";
    }

    /** @param tool Used to access other GrADS-specific modules */
    public void setTool(GradsTool tool) {
	this.tool = tool;
    }

    public void configure(Setting setting) {
    }

    /** @param bufferSize Size of memory buffer to use when streaming
     * subsets from disk */
    public void setBufferSize(int bufferSize) {
	this.bufferSize = bufferSize;
    }

    /** Streams a subset to the output stream given, using the
     *  CEEvaluator given.
     * @param useASCII If true, print ASCII text; if false, send
     * DODS/3.2 binary stream.
     * @param subsetLimit maximum allowable size for the subset
     * @throws ModuleException if subsetLimit bytes have already been
     * written and there is still more data; or, if any errors occur
     * during the subset operation
    */
    public abstract void subset(DataHandle data, 
				CEEvaluator ce,
				long subsetLimit,
				boolean useASCII,
				OutputStream out) 
	throws ModuleException;

    protected int bufferSize;

    protected GradsTool tool;
    
}
