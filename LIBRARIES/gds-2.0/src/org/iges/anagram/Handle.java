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
* Last modified: $Date: 2008/07/22 17:22:26 $ 
* Revision for this file: $Revision: 1.4 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/Handle.java,v $
*/
package org.iges.anagram;

import java.io.*;
import java.util.*;
import org.iges.util.*;

/** The base class for all Catalog entries. */
public abstract class Handle 
    implements Serializable{

    protected Handle(String completeName) 
	throws AnagramException {

	validateName(completeName);
	this.completeName = completeName;
	this.synch = new ExclusiveLock();
    }

    /** Returns the portion of the entry's complete name that follows the 
     *  final '/'.
     */
    public String getName() {
	int lastSlash = completeName.lastIndexOf('/');
	if (lastSlash < 0) {
	    lastSlash = 0;
	}
	return completeName.substring(lastSlash + 1);
    }

    /** Returns the full online name of this entry */
    public String getCompleteName() {
	return completeName;
    }

    public String toString() {
	return completeName;
    }

    /** Allows multiple threads to synchronize operations on this handle. 
     *  Before performing operations that depend on the state of the handle,
     *  a non-exclusive lock should always be obtained. 
     *  Before performing operations that alter the state of a handle,
     *  an exclusive lock should always be obtained.
     *  Locks that have been obtained must always be released (even if the synchronized
     *  operation throws an exception), or other requests may become deadlocked.
     */
    public ExclusiveLock getSynch() {
	if (synch == null) {
	    synch = new ExclusiveLock();
	}
	return synch;
    }
    
    protected void validateName(String name) 
	throws AnagramException {
	
    }

    protected String completeName;

    protected transient ExclusiveLock synch;

}
