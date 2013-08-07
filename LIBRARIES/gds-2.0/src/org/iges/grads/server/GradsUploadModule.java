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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/GradsUploadModule.java,v $
*/
package org.iges.grads.server;

import java.io.*;
import java.util.*;

import org.iges.util.Spooler;

import org.iges.util.FileResolver;

import org.iges.anagram.*;

/** Handles an incoming dataset upload stream. The contents of the
 * stream must be binary data in the format used for passing data to
 * GrADS UDF functions. This module requires a utility called
 * "udfread" (which is not currently a standard part of the GrADS
 * package). The GDS upload interface is somewhat limited and has not
 * fully tested or documented, so it is not yet being publicly
 * advertised as an operational capability.
 */
public class GradsUploadModule 
    extends AbstractModule {

    public String getModuleID() {
	return "uploader";
    }

    public GradsUploadModule(GradsTool tool) {
	this.tool = tool;
    }

    protected GradsTool tool;

    public void configure(Setting setting)
	throws ConfigException {
	String udfPath = setting.getAttribute("udfread");
	if (udfPath == "") {
	    if (verbose()) verbose("udfread not specified; uploads will not be available");
	    this.udfBinary = null;
	}
	this.udfBinary = FileResolver.resolve(server.getHome(), udfPath);
	if (!udfBinary.exists()) {
	    if (verbose()) verbose("udfread not found at " + 
				 udfBinary.getAbsolutePath() + 
				 "; uploads will not be available");
	    this.udfBinary = null;
	}

	this.defaultStorage = setting.getNumAttribute("storage", 0);
					      
    }

    /** Accepts an input stream, writes it to disk, and invokes the
     *  'udfread' utility which generates a .dat and a .ctl file from a 
     *  UDF input file, then creates a handle to the temporary data.
     */
    public TempDataHandle doUpload(String name,
				InputStream input,
				long size,
				Privilege privilege)
	throws ModuleException {

	// decide whether to accept upload
	String allowed = privilege.getAttribute("upload_allowed", "true");
	if (udfBinary == null || !allowed.equals("true")) {
	    throw new ModuleException(this, 
				      "upload service not available");
	}

	// check if upload is within max size
	long storage = 
	    privilege.getNumAttribute("upload_storage", defaultStorage);
	if (storage > 0 && size > storage) {
	    throw new ModuleException(this, 
				      "upload exceeds maximum size of " + 
				      storage);
	}
	
	// create files
	File baseFile = server.getStore().get(this, name);	
	File descriptorFile = new File(baseFile.getAbsolutePath() + ".ctl");
	File dataFile = new File(baseFile.getAbsolutePath() + ".dat");
	File packedFile = new File(baseFile.getAbsolutePath() + ".udf");

	writeToDisk(name, input, size, packedFile);

	try {

	    // unpack

	    long timeLimit = ((GradsTaskModule)tool.getTask()).getTimeLimit();

	    Task task = new Task(new String[] {
		"nice",
		udfBinary.getAbsolutePath(), 
		packedFile.getAbsolutePath(),
		descriptorFile.getAbsolutePath(),
		dataFile.getAbsolutePath()
	    },
				 null,
				 null,
				 timeLimit);
		
	    task.run();

	    // add to catalog

	    GradsDataInfo info = 
		new GradsDataInfo(name,
				  GradsDataInfo.CLASSIC,
				  descriptorFile.getAbsolutePath(), 
				  descriptorFile, 
				  descriptorFile,
				  null,
				  null,
				  "uploaded data",
				  false,
				  new ArrayList(),
				  new ArrayList(),
		                  false,
				  false,
				  "ctl");

	    return new GradsTempHandle(name,
				       name,
				       info,
				       dataFile,
				       null);		

	} catch (ModuleException me) {
	    descriptorFile.delete();
	    dataFile.delete();
	    throw me;

	} catch (AnagramException ae) {
	    descriptorFile.delete();
	    dataFile.delete();
	    throw new ModuleException(this, "upload failed", ae);

	} finally {
	    packedFile.delete();
	}

    }

    /** Writes the input stream given to the file given
     * @throws ModuleException if stream contains too much data, or
     * there is an IO problem. The file is not deleted however. */
    private void writeToDisk(String name, 
			     InputStream in, 
			     long size, 
			     File diskFile) 
	throws ModuleException {

	try { 
	    OutputStream out =
		new BufferedOutputStream
		    (new FileOutputStream
			(diskFile));
	    
	    long bytesWritten = Spooler.spool(in, out);

	    out.close();

	    if (bytesWritten > size) {
		throw new ModuleException(this, "uploaded data exceeds " + 
					  "size given in Content-Length");
	    }

	} catch (IOException ioe) {
	    throw new ModuleException(this, 
				      "can't write uploaded data to disk");
	}
	
    }

    protected File udfBinary;
    protected long defaultStorage;
    
}
