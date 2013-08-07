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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/util/Lock.java,v $
*/
package org.iges.util;

import java.util.*;

/** A generalized representation of the thread-lock concept.
 *  This can be used to create other locking systems besides the basic 
 *  one-thread-at-a-time model of Java's <code>synchronized</code> keyword. 
 */
public interface Lock {

    /** @return True if the current thread owns this
     *  lock. */
    public boolean isLocked();
	
    /** Obtains this lock for the current thread, 
     *  blocking until the lock is available. */
    public void lock();
	    
    /** Releases this lock. If the current thread does not own this lock,
     *  does nothing. */
    public void release();
    
    /** Tries to obtain this lock for the current thread. 
     *  This method always returns immediately but does not 
     *  guarantee that the lock will be obtained.
     *  @return True if the lock was succesfully obtained. 
     */
    public boolean tryLock();

}
