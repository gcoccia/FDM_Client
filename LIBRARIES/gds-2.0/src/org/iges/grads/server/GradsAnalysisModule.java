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
* Revision for this file: $Revision: 1.11 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/GradsAnalysisModule.java,v $
*/
package org.iges.grads.server;

import java.io.*;
import java.util.*;
import org.iges.util.Bounds;
import org.iges.anagram.*;

/** Runs a GrADS analysis task and returns a handle to the results. 
 */
public class GradsAnalysisModule 
    extends AbstractModule {

    public String getModuleID() {
	return "analyzer";
    }

    public GradsAnalysisModule(GradsTool tool) {
	this.tool = tool;
    }

    protected GradsTool tool;

    public void configure(Setting setting) {
	this.defaultStorageLimit = setting.getNumAttribute("storage", 0);

	this.defaultTimeLimit = setting.getNumAttribute("time", 0);

    }

    public TempDataHandle doAnalysis(String handleName, 
				   String ae, 
				   Privilege privilege) 
	throws ModuleException {

	String allowed = privilege.getAttribute("analyze_allowed", "true");
	if (!allowed.equals("true")) {
	    throw new ModuleException(this, 
				      "upload service not available");
	}

	ParsedExpression parsed = new ParsedExpression(ae, privilege);
	
	String shortName = "/_exprcache_" + System.currentTimeMillis() + counter;
	File baseFile = server.getStore().get(this, shortName);	
	File descriptorFile = new File(baseFile.getAbsolutePath() + ".ctl");
	File dataFile = new File(baseFile.getAbsolutePath() + ".dat");

	long storageLimit = 
	    privilege.getNumAttribute("analyze_storage", defaultStorageLimit);
	long timeLimit = 
	    privilege.getNumAttribute("analyze_time", defaultTimeLimit);


	List handles = new ArrayList();
	try {	    
	    
	    Catalog catalog = server.getCatalog();
	    Iterator it = parsed.dataNames.iterator();
	    while (it.hasNext()) {
		String name = (String)it.next();
		if (!name.startsWith("/")) {
		    name = "/" + name;
		}
		Handle handle = catalog.getLocked(name);
		if (handle == null || 
		    !(handle instanceof DataHandle) || 
		    !privilege.allows(handle.getCompleteName())) {

		    throw new ModuleException(this, "data " + name + 
					      " not found");
		}
		handles.add(handle);
	    }

	    Task task = tool.getTask().task(chooseGradsBinary(handles),
					    "expression", 
					    new String[] {
		baseFile.getAbsolutePath(),
		parsed.bounds.toString(),
		parsed.expression,
		String.valueOf((int)(storageLimit * 1000)),
		String.valueOf(parsed.dataNames.size()),
		buildDatasetNameList(handles),
	    });
	    
	    if (timeLimit > 0) {
		task.run(timeLimit * 1000);
	    } else {
		task.run();
	    }

	    if (!descriptorFile.exists() || !dataFile.exists()) {
		throw new ModuleException
		    (this, "expression evaluation script produced no " + 
		     "output for " + ae);
	    }

	    counter++;

	    StringBuffer title = new StringBuffer();
	    title.append("shorthand: ");
	    title.append(shortName);
	    title.append("  expression: ");
	    title.append(parsed.expression);
	    title.append("  source datasets: ");
	    it = parsed.dataNames.iterator(); 
	    title.append((String)it.next());
	    while(it.hasNext()) {
		title.append(" ");
		title.append((String)it.next());
	    }

	    GradsDataInfo info = 
		new GradsDataInfo(shortName,
				  GradsDataInfo.CLASSIC,
				  descriptorFile.getAbsolutePath(), 
				  descriptorFile, 
				  dataFile, 
				  null,
				  null,
				  title.toString(),
				  true,
				  new ArrayList(),
				  new ArrayList(),
				  false, 
				  false,
				  "ctl");

	    return new GradsTempHandle(shortName,
				       handleName,
				       info,
				       dataFile,
				       new HashSet(parsed.dataNames));		

	} catch (ModuleException me) {
	    descriptorFile.delete();
	    dataFile.delete();
	    throw me;
	} catch (AnagramException e) {
	    descriptorFile.delete();
	    dataFile.delete();
	    throw new ModuleException(this, e.getMessage());
	} finally {
	    releaseSources(handles);
	}		    

    }

    protected int chooseGradsBinary(List handles)
	throws AnagramException {

	int returnType = GradsDataInfo.CLASSIC;
	Iterator it = handles.iterator();
	while (it.hasNext()) {
	    GradsDataInfo info = 
		(GradsDataInfo)((DataHandle)it.next()).getToolInfo();
	    int currentType = info.getGradsBinaryType();
	    returnType = Math.max(currentType, returnType);
	}
	return returnType;
    }	

    protected String buildDatasetNameList(List datasets) {
	StringBuffer returnVal = new StringBuffer();
	Iterator it = datasets.iterator();
	while (it.hasNext()) {
	    if (returnVal.length() > 0) {
		returnVal.append(" ");
	    }
	    GradsDataInfo info = 
		(GradsDataInfo)((DataHandle)it.next()).getToolInfo();
	    returnVal.append(info.getGradsArgument());
	}
	return returnVal.toString();
    }

    protected void releaseSources(List handles) {
	Iterator it = handles.iterator();
	while (it.hasNext()) {
	    ((Handle)it.next()).getSynch().release();
	}
    }

    protected long counter;
    protected long defaultStorageLimit;
    protected long defaultTimeLimit;

    protected class ParsedExpression {

	protected ParsedExpression(String expressionString, 
				      Privilege privilege) 
	    throws ModuleException {

	    fullString = expressionString;

	    try {
		// Break up the string by occurences of "{" or "}" 
		StringTokenizer st = 
		    new StringTokenizer(expressionString, "{}", true);
		String current;
		
		consume(st, "{");
		
		parseDatasetNames(st);

		consume(st, "}");
		consume(st, "{");
		
		parseExpression(st);

		consume(st, "}");
		consume(st, "{");

		parseBounds(st);
		
		consume(st, "}");
	    } catch (AnagramException ae) {
		throw new ModuleException(GradsAnalysisModule.this,
					  "invalid expression syntax",
					  ae);
	    }	
	}

	protected void consume(StringTokenizer st, String token) 
	    throws AnagramException {

	    if (!st.hasMoreTokens()) {
		throw new AnagramException("unexpected end of expression; " + 
					   "expected '" + token + "'");
	    }
	    String current = st.nextToken();
	    if (!current.equals(token)) {
		throw new AnagramException("read '" + current + 
					   "'; expected '" + token + "'");
	    }
	}
		
	protected void parseDatasetNames(StringTokenizer st) 
	    throws AnagramException {
	    if (!st.hasMoreElements()) {
		throw new AnagramException("unexpected end of expression; " + 
					   "expected dataset names");
	    }
	    
	    String datasetNames = st.nextToken().replace(',', ' ');

	    StringTokenizer datasetList = new StringTokenizer(datasetNames);
	    dataNames = new ArrayList();
	    while (datasetList.hasMoreTokens()) {
		String name = datasetList.nextToken();
		if (!name.startsWith("/")) {
		    name = "/" + name;
		}
		dataNames.add(name);
	    }
	}
	
	protected void parseExpression(StringTokenizer st) 
	    throws AnagramException {
	    if (!st.hasMoreElements()) {
		throw new AnagramException("unexpected end of expression; " + 
					   "expected analysis expression");
	    }
	    expression = st.nextToken().replace(' ', '+');
	}

	protected void parseBounds(StringTokenizer st) 
	    throws AnagramException {
	    if (!st.hasMoreElements()) {
		throw new AnagramException("unexpected end of expression; " + 
					   "expected bounds");
	    }

	    String boundsString = 
		st.nextToken().replace(',', ' ').replace(':', ' ');
	    try {
		bounds = new Bounds.World(boundsString);
	    } catch (IllegalArgumentException iae) {
		throw new AnagramException(iae.getMessage());
	    }
	}

	protected String fullString;
	protected String expression;
	protected List dataNames;
	protected Bounds.World bounds;
	
    }

}
