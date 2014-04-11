/*
 * Copyright (C) 2013-2014, The Max Planck Institute for
 * Psycholinguistics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License is included in the file
 * LICENSE. If that file is missing, see
 * <http://www.gnu.org/licenses/>.
 */

package nl.mpi.mdmapper.output;

import java.util.HashMap;
import java.util.Map;
import java.io.PrintStream;

/**
 * This is a list of facets (or fields) and their values, created as a
 * result of applying a semantic mapping to a source document.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class FacetList {
    /** A map with key-value pairs in it. Note: package scope. */
    Map<String, String> res;

    /** String describing the source of the record, e.g. filename. */
    private String source;

    public FacetList(String source) {
	this.source = source;
	res = new HashMap<>();
    }

    public String getSourceShort() {
	if (source != null) {
	    int idx = source.lastIndexOf(System.getProperty("file.separator"));
	    if (idx >= 0)
		return source.substring(idx+1);
	}
	return source;
    }
    public String getSource() {
	return source;
    }

    public void add(String field, String value) {
	res.put(field, value);
    }

    public void add(String field, String[] values) {
	StringBuilder sb = new StringBuilder();

	boolean first = true;
	for (String v : values) {
	    if (first) {
		first = false;
	    } else {
		sb.append(", ");
	    }
	    sb.append(v);
	}

	add(field, sb.toString());
    }

    /**
     * Print list of mapped facets. Mainly for debugging purposes.
     */
    public void printFacets(PrintStream out) {
	for (Map.Entry<String, String> e : res.entrySet()) {
	    out.println(e.getKey() + " --> " + e.getValue());
	}
    }
}
