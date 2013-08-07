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
* Revision for this file: $Revision: 1.8 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/GradsUpdateModule.java,v $
*/
package org.iges.grads.server;

import java.io.*;

import org.iges.anagram.*;

/** Updates a data handle to be in synch with the actual data
 *  source. This provides a quick way for the Catalog to check for
 *  changes to the back-end data store. 
 *
 */
public class GradsUpdateModule
    extends AbstractModule {

    public String getModuleID() {
	return "updater";
    }

    public GradsUpdateModule(GradsTool tool) {
	this.tool = tool;
    }

    protected GradsTool tool;

    public void configure(Setting setting)
	throws ConfigException {
					      
    }

    /** Checks if the DataHandle provided is out of date and updates
     * it if necessary.  A DataHandle is considered out of date iff the
     * descriptor file, source file, or supplemental DAS file have
     * been modified or deleted since the DataHandle was created.
     *  @return null if the Datahandle has not changed, or else, the
     *  new, updated DataHandle object
     *  @throws ModuleException if the data can no longer be accessed.
     */
    public boolean doUpdate(DataHandle data) 
	throws ModuleException {

	GradsDataInfo info = (GradsDataInfo)data.getToolInfo();
	
	File descriptorFile = info.getDescriptorFile();
	File sourceFile = info.getSourceFile();
	File userDAS = info.getUserDAS();
	long createTime = data.getCreateTime();

	boolean modified = false;

	if (!data.isAvailable()) {
	    modified = true;	
	}

	if (sourceFile != null) {
	    if (!sourceFile.exists()) {
		throw new ModuleException(this, 
					  "source file moved or deleted");
	    }
	    if (fileModified(sourceFile, createTime)) {
		if (debug()) debug("source file has changed");
		modified = true;	
	    }
	} 

	if (fileModified(descriptorFile, createTime)) {
	    if (debug()) debug("descriptor file has changed");
	    modified = true;	
	}
	    
	if (userDAS != null && fileModified(userDAS, createTime)) {
	    if (debug()) debug("DAS file has changed");
	    modified = true;	
	}

	if (modified) {
	    reload(data);
	}
	
	return modified;
	
    }
        
    protected boolean fileModified(File file, long createTime) {
	return !file.exists() || file.lastModified() > createTime;
    }

    /** Brings internal structures in a DataHandle up-to-date with
     *  respect to the data files. There is redundancy here with
     *  respect to the code in GradsImportModule, which indicates that
     *  the design could definitely be improved.  
     */
    protected void reload(DataHandle data) 
	throws ModuleException {

	if (debug()) debug(data.getCompleteName() + 
			   " is out of date; regenerating");
	if (debug()) debug("data lock: " + data.getSynch());

	// prevent any access during the update
	data.getSynch().lockExclusive();

	GradsDataInfo oldInfo = (GradsDataInfo)data.getToolInfo();
	File descriptorFile = null;
// 	if (oldInfo.getGradsBinaryType() != GradsDataInfo.CLASSIC) {
	if (oldInfo.getFormat().equals("ctl")) { 
	    descriptorFile = oldInfo.getDescriptorFile();
	} else {
	    if (debug()) debug("regenerating descriptor");
	    descriptorFile = 
		tool.importer.makeDescriptorFile(data.getCompleteName(),
						 oldInfo.getGradsArgument(),
						 oldInfo.getGradsBinaryType());
	}

	if (debug()) debug("regenerating tool info");
	try {
	    GradsDataInfo newInfo = 
		new GradsDataInfo(data.getCompleteName(),
				  oldInfo.getGradsBinaryType(),
				  oldInfo.getGradsArgument(),
				  descriptorFile,
				  oldInfo.getSourceFile(),
				  oldInfo.getUserDAS(),
				  oldInfo.getDocURL(),
				  null,
				  oldInfo.isDirectSubset(),
				  oldInfo.getMetadataFilters(),
				  oldInfo.getMetadata(),
				  oldInfo.hasLevels(),
				  oldInfo.hasEnsemble(),
				  oldInfo.getFormat());
	    data.setDescription(newInfo.getTitle());
	    data.setToolInfo(newInfo);

	    data.setAvailable(true);

	} catch (AnagramException ae) {
	    data.setAvailable(false);
	    throw new ModuleException(this, ae.getMessage());
	} finally {
	    data.getSynch().release();
	}
    }



}
