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
* Revision for this file: $Revision: 1.12 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/GradsStnSubsetter.java,v $
*/
package org.iges.grads.server;

import java.io.*;
import java.util.*;

import org.iges.util.*;

import dods.dap.*;
import dods.dap.Server.*;

import org.iges.anagram.*;

import org.iges.grads.server.dap.*;

/** Handles subsetting of GrADS station datasets. */
public class GradsStnSubsetter 
    extends GradsSubsetter {

    /** @see org.iges.grads.server.dap.GradsSequence */
    public void subset(DataHandle data, 
		       CEEvaluator ce,
		       long subsetLimit,
		       boolean useASCII,
		       OutputStream out)
	throws ModuleException {

	SubsetParser parser = new SubsetParser(ce);
	File subsetFile = server.getStore().get(this, 
						data.getName(),
						".subset");
	GradsDataInfo info = (GradsDataInfo)data.getToolInfo();

	// create arguments for station data script
	List args = new ArrayList();
	args.add(subsetFile.getAbsolutePath());
	args.add(info.getGradsArgument());
	args.add(parser.options);
	if (parser.stationID != null) {
	    args.add("stid");
	    args.add(parser.stationID);
	}
	if (parser.bounds != null) {
	    args.add("bounds");
	    args.add(parser.bounds.toString());
	}
	args.add(String.valueOf(parser.variableNames.size()));
	args.addAll(parser.variableNames);

	String[] argArray = (String[])args.toArray(new String[0]);

	// run station data script
	Task task = tool.getTask().task(info.getGradsBinaryType(),
					"station", argArray);	
	try {
	    if (debug()) debug("starting GrADS task");
	    try {
		task.run();
	    } catch (AnagramException ae) {
		throw new ModuleException(this, 
					  "GrADS task for subset failed", ae);
	    }
	    if (debug()) debug("finished GrADS task");

	    if (!subsetFile.exists() || subsetFile.length() == 0) {
		throw new ModuleException
		    (this, "subset script produced no output for " + data);
	    }

	    if (subsetLimit > 0 && subsetFile.length() > subsetLimit) {
		throw new ModuleException
		    (this, "subset exceeds limit of " + subsetLimit + "bytes");
	    }

	    if (!parser.extraClauses && !useASCII) {

		// write subset directly to stream
	    
		if (debug()) debug("streaming directly to client");
		writeBinaryData(out, subsetFile, (int)bufferSize);
	    } else {

		// need to parse one row of subset at a time, and
		// test if it matches the constraints in the request

		if (debug()) debug("filtering GrADS output through CE...");
		GradsServerMethods reportVar = 
		    (GradsServerMethods)ce.getDDS().getVariable("reports");
		try {
		    reportVar.serialize(ce.getDDS().getName(), 
					new DataOutputStream(out), 
					ce, 
					subsetFile,
					useASCII);
		} catch (AnagramException ae) {
		    throw new ModuleException(this, 
					      "serialization failed for " +
					      ((BaseType)reportVar).getName(), 
					      ae);
		}
	    }
	} catch (NoSuchVariableException nsve) {
	    throw new ModuleException
		(this, "missing report variable in station data DDS");
	} finally {
	    subsetFile.delete();
	}
    }


    /** Writes data from a temporary file to the output stream given,
     *  in binary format. GrADS contains a special output mode,
     *  'writegds', which writes station data in network-ready
     *  DODS/3.2 binary format. Thus unless there are non-standard
     *  selection constraints to apply, data can be streamed directly
     *  to the network without any parsing.
     */
    private void writeBinaryData(OutputStream out, 
				 File generated, 
				 int bufferSize) 
	throws ModuleException {

	// Open the temporary file 
	InputStream in;
	try {
	    in = new FileInputStream(generated); 
	} catch (FileNotFoundException fnfe) {
	    throw new ModuleException
		(GradsStnSubsetter.this,
		 "temporary file " + generated + " disappeared!");
	}

	try {
	    byte[] buf = new byte[bufferSize];
	    Spooler.spool(in, out, buf);
	} catch (IOException ioe) {
	    // Swallow exception if it was just the client disconnecting;
	    // otherwise, rethrow it
	    if (!ioe.getMessage().equals("Broken pipe")) {
		throw new ModuleException
		    (GradsStnSubsetter.this,
		     "io error during subsetting---" + ioe.getMessage());
	    } 
	} finally {
	    try {
		in.close();
	    } catch (Exception e) {}
	}

    }

    public String readTimeDimension(int index) {
	return String.valueOf(index);
    }

    /** Extracts relevant objects from a constraint expression on a
     * station dataset */
    private class SubsetParser {
	
	private SubsetParser(CEEvaluator ce) 
	    throws ModuleException {
	    parseConstraint(ce);
	    parseDDS(ce);
	}


	/** Loops through constraint clauses, looking for stid() and
	 * bounds() functions 
	 */
	private void parseConstraint(CEEvaluator ce)
	    throws ModuleException {

	    // indicates whether there are non-function clauses
	    // (which will determine whether the subset needs to be parsed
	    // after it is generated by GrADS)
	    extraClauses = false;

	    Enumeration ensnum = ce.getClauses();
	    while (ensnum.hasMoreElements()) {
		Clause clause = (Clause)ensnum.nextElement();
		if (clause instanceof BoolFunctionClause) {
		    if (debug()) debug("got a function clause");
		    try {
			BoolFunction function = 
			    ((BoolFunctionClause)clause).getFunction();

			if (!(function instanceof DummyFunction)) {
			    if (debug()) debug("function is not dummy");
			    
			    continue;
			}


			// this is really ugly. sorry.
			// we just get a string
			// containing the name of the function
			// followed by a ":" and its arguments. so we
			// "evaluate" each Function clause, see if it
			// is a String, and if so, save the argument
			// list to pass to GrADS
			String args = ((DummyFunction)function).printArgs
			    (clause.getChildren());

			StringTokenizer tok = 
			    new StringTokenizer(args, ":");
			String functionName = tok.nextToken();

			if (debug()) 
			    debug("args: " + args);

			// look for stid and bounds, the two functions
			// that we handle, and extract the arguments
			// as a string
			if (functionName.equals("stid")) {

			    if (stationID != null) {
				throw new ModuleException
				    (GradsStnSubsetter.this,
				     "multiple invocations of stid()");
			    }
			    stationID = tok.nextToken().trim();
			} else if (functionName.equals("bounds")) {
			    if (bounds != null) {
				throw new ModuleException
				    (GradsStnSubsetter.this,
				     "multiple invocations of bounds()");
			    }
			    bounds = new Bounds.World(tok.nextToken());
			}

			// other functions are simply ignored
			// might be better to throw an error

		    } catch (NoSuchElementException nsee) {
			throw new ModuleException
			    (GradsStnSubsetter.this,
			     "invalid parameters to server function");
		    } catch (Exception e) {
			throw new ModuleException
			    (GradsStnSubsetter.this,
			     e.getMessage());
		    }
		} else {
		    // not a function clause, will need to be evaluated 
		    // for each row of the subset after it is generated
		    // by GrADS
		    extraClauses = true;
		}
	    }
	    if (debug()) debug("bounds: " + bounds);
	    if (debug()) debug("stid: " + stationID);
	}



	/** Translates a subset request (i.e. a constrained DDS) into
	 * script arguments for station.gs, indicating which variables
	 * and coordinates should be included in the binary stream
	 */
	private void parseDDS(CEEvaluator ce) 
	    throws ModuleException {

	    StringBuffer optionBuffer = new StringBuffer("-");
	    variableNames = new ArrayList();

	    // GrADS always needs to think it is displaying a data variable,
	    // even if the only thing we are interested in is
	    // coordinates, so we need a "dummy" name to give it in
	    // this case
	    String dependentDummy = null;
	    String independentDummy = null;

	    boolean gotDependent = false;
	    boolean gotIndependent = false;

	    // whether the inner sequence will appear
	    boolean needLevels = false;

	    Enumeration ensnum = null;
	    try {
		DSequence sequence = 
		    (DSequence)ce.getDDS().getVariable("reports");
		ensnum = sequence.getVariables();
	    } catch (ClassCastException cce) {
		throw new ModuleException
		    (GradsStnSubsetter.this,
		    "illegal top-level variable in station data DDS");
	    } catch (NoSuchVariableException nsve) {
	    throw new ModuleException
		(GradsStnSubsetter.this,
		 "missing top-level sequence in station data DDS");
	    }
	    while (ensnum.hasMoreElements()) { 
		BaseType var = (BaseType)ensnum.nextElement();
		String name = var.getName();

		// projected means part of the subset request
		boolean projected = ((ServerMethods)var).isProject();

		// first check for coordinate variables;
		// each one has a special letter option for the 
		// GrADS stn data output command
		if (name.equals("stid")) {
		    if (projected) {
			optionBuffer.append('s');
		    }
		} else if (name.equals("lon")) {
		    if (projected) {
			optionBuffer.append('x');
		    }
		} else if (name.equals("lat")) {
		    if (projected) {
			optionBuffer.append('y');
		    }
		} else if (name.equals("time")) {
		    if (projected) {
		    optionBuffer.append('t');
		    }

		    // now check for the inner sequence

		} else if (name.equals("levels")) {
		    if (projected) {
			needLevels = true;
			dependentDummy = parseLevels(var, optionBuffer);
			gotDependent = (dependentDummy == null);
		    }

		    // now check for level-independent variables

		} else {
		    // this is our fall-back dummy name to use if only
		    // coordinate variables are projected
		    independentDummy = name;

		    if (projected) {
			if (!gotIndependent) {
			    optionBuffer.append('i');
			    gotIndependent = true;
			}
			variableNames.add(name);
		    }
		}
	    }

	    // add dummy names if necessary
	    if (needLevels && !gotDependent) {
		variableNames.add(dependentDummy);
	    }
	    if (variableNames.size() == 0) {
		variableNames.add(independentDummy);
	    }
	    
	    options = optionBuffer.toString();

	    if (debug()) debug("options: " + options);
	    if (debug()) {
		Iterator it = variableNames.iterator();
		debug("variables:");
		while (it.hasNext()) {
		    debug((String)it.next());
		}
	    }
	}

	/** Helper for parseDDS(), to handle the variables with
	 * vertical levels.
	 */ 
	private String parseLevels(BaseType mainVar, 
				   StringBuffer optionBuffer) {
	    boolean gotDependent = false;
	    String dependentDummy = null;
	    Enumeration ensnum = 
		((DSequence)mainVar).getVariables();
	    while (ensnum.hasMoreElements()) {
		BaseType var = 
		    (BaseType)ensnum.nextElement();
		String name = var.getName();
		boolean levelProjected = 
		    ((ServerMethods)var).isProject();

		// check for coordinate var
		if (name.equals("lev") && levelProjected) {
		    optionBuffer.append('z');
		} else {
		    //  data variable

		    // this is our fall-back dummy name to use if
		    // the lev variable is needed but no level-dependent
		    // data variables are requested
		    dependentDummy = name;

		    if (levelProjected) {
			if (!gotDependent) {
			    optionBuffer.append('d');
			    gotDependent = true;
			}
			variableNames.add(name);
		    }
		}
	    }
	    if (gotDependent) {
		return null;
	    } else {
		return dependentDummy;
	    }
	}

	boolean extraClauses;
	String stationID;
	Bounds.World bounds;
	String options;
	List variableNames;
	
    }
	
}
