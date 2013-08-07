/* 
* Copyright (C) 2000-2008 Institute for Global Environment and Society /
*                        Center for Ocean-Land-Atmosphere Studies
* Author: Joe Wielgosz <joew@cola.iges.org>
* 
* This file is part of the Anagram Server Framework.
* 
* The Anagram Server Framework is free software; you can redistribute
* it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; either version
* 2, or (at your option) any later version.
* 
* The Anagram Server Framework is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with the Anagram Server Framework; see the file COPYRIGHT.  If
* not, write to the Free Software Foundation, Inc., 59 Temple Place -
* Suite 330, Boston, MA 02111-1307, USA.
* 
* You can contact IGES/COLA at 4041 Powder Mill Rd Ste 302, Calverton MD 20705.
* 
* Last modified: $Date: 2008/07/22 17:22:27 $ 
* Revision for this file: $Revision: 1.5 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/Store.java,v $
*/
package org.iges.anagram;

import java.io.*;

import org.iges.util.*;

/** Provides a convenient access mechanism for
 *  temporary disk storage. Each module receives a separate namespace
 *  for its entries, thus minimizing the possibility of namespace
 *  conflicts.
 */
public class Store
    extends AbstractModule {

    public String getModuleID() {
	return "store";
    }

    public void configure(Setting setting) 
	throws ConfigException {
	    
	String workDirName = 
	    setting.getAttribute("work_dir", 
				 System.getProperty("java.io.tmpdir"));
	baseDir = FileResolver.resolve(server.getBase(), workDirName);
	log.verbose(this, "temp dir is " + baseDir.getAbsolutePath());
	if (!baseDir.exists()) {
	    if (!baseDir.mkdirs()) {
		throw new ConfigException(this, "couldn't create temp dir " + 
					  baseDir.getAbsolutePath());
	    }
	}
    }
    
    /** Returns the file handle associated with the given entry name, for 
     *  the given module. If the file is older than the time given,
     *  it is deleted before the handle is returned.<p>
     *  Used in combination with the DataHandle.getCreateTime() method,
     *  this provides a way for modules to keep cached data associated
     *  with a particular data handle up to date.<p>
     *  It is guaranteed that all parent directories 
     *  exist for the handle before it is returned.
     *
     * @param entryName This parameter should uniquely identify the 
     *  resource being stored or accessed, and should not contain
     *  any characters that are illegal in filenames.
     */
    public File get(Module module, String entryName, long staleTime) {
	File entryFile = get(module, entryName);
	if (entryFile.exists()) {
	    if (entryFile.lastModified() < staleTime) {
		entryFile.delete();
	    }
	} 
	return entryFile;
    }
    
    /** Returns the file handle associated with the given entry name, for 
     *  the given module.<p> 
     *  It is guaranteed that all parent directories 
     *  exist for the handle before it is returned.
     */
    public File get(Module module, String entryName) {
	File entryFile = resolve(module, entryName);
	if (!entryFile.exists()) {
	    File entryDir = entryFile.getParentFile();
	    mkdirs(entryDir);
	}
	return entryFile;
    }
    
    /** Returns a unique (to this JVM session) file handle whose name
     *  is constructed using the given prefix and suffix, for the given
     *  module.<p> 
     *  It is guaranteed that all parent directories 
     *  exist for the handle before it is returned.
     */
    public File get(Module module, String prefix, String suffix) 
	throws ModuleException {

	File prefixFile = resolve(module, prefix);
	File entryDir = prefixFile.getParentFile();
	mkdirs(entryDir);
	try {
	    File entryFile = File.createTempFile(prefixFile.getName(), 
						 suffix, 
						 entryDir);
	    return entryFile;
	} catch (IOException ioe) {
	    throw new RuntimeException("couldn't create temp file for " + 
				      module + " using pattern " + 
				       prefix + " ... " + suffix + "; " + 
				      ioe.getClass());
	}
    }

    protected void mkdirs(File entryDir) {

	if (!entryDir.exists()) {
	    if (!entryDir.mkdirs()) {
		log.critical(this, "couldn't create directory " + 
			     entryDir.getAbsolutePath());
	    } else {
		if (verbose()) log.verbose(this, "created dir " + 
				       entryDir.getAbsolutePath());
	    }
	}
    }	

    protected File resolve(Module module, String entryName) {
	File entryFile = 
	    new File(baseDir, module.getModuleName() + "/" + entryName);
	return entryFile;
    }

    protected File baseDir;

}
