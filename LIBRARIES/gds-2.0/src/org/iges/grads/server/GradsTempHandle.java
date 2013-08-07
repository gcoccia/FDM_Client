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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/GradsTempHandle.java,v $
*/
package org.iges.grads.server;

import java.io.*;
import java.util.*;

import org.iges.anagram.*;

/** Implementation of the TempDataHandle interface for GrADS datasets. 
 * @see org.iges.anagram.TempDataHandle for full documentation of methods. 
 */
public class GradsTempHandle
    implements TempDataHandle {

    public GradsTempHandle(String name, String longName, 
			   GradsDataInfo info, File dataFile, 
			   Set dependencies) 
	throws AnagramException {

	createTime = System.currentTimeMillis();

	this.info = info;
	this.dataFile = dataFile;
	this.dataHandles = new DataHandle[] {
	    new DataHandle(name, info.getTitle(), info, createTime),
	    new DataHandle(longName, info.getTitle(), info, createTime),
	};
	this.dependencies = dependencies;
    }
    
    public GradsTempHandle(String name, GradsDataInfo info, File dataFile) 
	throws AnagramException {

	long createTime = System.currentTimeMillis();

	this.info = info;
	this.dataFile = dataFile;
	this.dataHandles = new DataHandle[] {
	    new DataHandle(name, info.getTitle(), info, createTime),
	};
	this.dependencies = dependencies;
    }
    public Set getDependencies() {
	return dependencies;
    }
    public DataHandle[] getDataHandles() {
	return dataHandles;
    }
    public long getStorageSize() {
	return info.getDescriptorFile().length() + dataFile.length();
    }

    public long getCreateTime() {
	return createTime;
    }

    public void deleteStorage() {
	
	for (int i = 0; i < dataHandles.length; i++) {
	    dataHandles[i].setAvailable(false);
	}
	info.getDescriptorFile().delete();
	dataFile.delete();
    }

    protected Set dependencies;
    protected GradsDataInfo info;
    protected DataHandle[] dataHandles;
    protected long createTime;
    protected File dataFile;
}
