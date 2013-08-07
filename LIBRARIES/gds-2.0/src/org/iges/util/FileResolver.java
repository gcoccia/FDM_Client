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
* Last modified: $Date: 2008/07/22 17:22:28 $ 
* Revision for this file: $Revision: 1.4 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/util/FileResolver.java,v $
*/
package org.iges.util;

import java.io.*;

/** Provides slightly more flexible handling of paths than the
 *  java.io.File class. 
 */
public class FileResolver {

    /** Resolves a filename relative to a base path, 
     *  similar to the way a URL is resolved relative to the server's
     *  base URL. If the filename is absolute, then it is returned as is.
     *   If it is relative, then it is resolved with respect to the 
     *   <code>path</code> parameter.
     *  @return The File object that the filename resolves to.
     *  @param filename The filename to be resolved.
     *  @param path The path to resolve relative filenames by.
     */
    public static File resolve(String path, String filename) {
	File file = new File(filename);
	if (file.isAbsolute()) {
	    return file;
	} else {
	    return new File(path, filename);
	}
    }

}
