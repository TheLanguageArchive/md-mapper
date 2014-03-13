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

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.PrintStream;
import java.io.File;
import java.nio.file.Path;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;


/**
 * JSON output consistent with CKAN API version 3.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class Ckan3JsonOutput implements Output {
    private static final Logger logger = Logger.getLogger(Ckan3JsonOutput.class);

    private Path outputDir;
    private Gson gson;

    public Ckan3JsonOutput(boolean prettyPrint, Path outputDir) {
	this.outputDir = outputDir;
	if (prettyPrint) {
	    gson = new GsonBuilder().setPrettyPrinting().create();
	} else {
	    gson = new Gson();
	}
    }

    /**
     * Set of CKAN's internal / "native" fields. That means these
     * fields can be put on the base level of the JSON record, while
     * any other fields must be stored inside the 'extras' structure.
     */
    private static final Set<String> internal = new HashSet<String>
	(Arrays.asList(new String[] {"author", "maintainer", "title", "name", "version",
				     "url", "notes", "tags", "status", "id", "group"} ));

    private void addExtraField(Map rec, String key, String value) {
	List extras;
	if (!rec.containsKey("extras")) {
	    extras = new ArrayList();
	    rec.put("extras", extras);
	} else {
	    extras = (List)rec.get("extras");
	}
	Map tt = new HashMap<String, String>();
	tt.put("key", key);
	tt.put("value", value);
	extras.add(tt);
    }

    public void save(FacetList fl) {
	Map rec = new HashMap();
	for (Map.Entry<String, String> e : fl.res.entrySet()) {
	    String key = e.getKey();
	    String value = e.getValue();
	    if ("tags".equals(key)) {
		// The "tags" facet has a special syntax in this JSON
		// format. It is assumed to be comma-separated after
		// the intial mapping.
		List tags;
		if (!rec.containsKey("tags")) {
		    tags = new ArrayList();
		    rec.put("tags", tags);
		} else {
		    tags = (List)rec.get("tags");
		}
		String[] parts = value.split(",");
		for (String part : parts) {
		    Map tt = new HashMap<String, String>();
		    tt.put("name", part);
		    tags.add(tt);
		}
	    } else if ("spatial".equals(key)) {
		// The value should be a bounding box of type "minx,
		// miny, maxx, maxy". If the value is invalid, skip
		// it. Otherwise detect whether it is a bounding box
		// or a simple point and handle accordingly.
		String[] parts = value.split(",");
		if (parts.length < 4) {
		    logger.info("Spatial value " + value + " is malformed, skipping.");
		    continue;
		}
		String minx = parts[0];
		String miny = parts[1];
		String maxx = parts[2];
		String maxy = parts[3];

		StringBuilder val = new StringBuilder();
/*		if (minx.equals(maxx) && miny.equals(maxy)) {
 *		    val.append("{\"type\":\"Point\",\"coordinates\":");
 *		    val.append("[").append(minx).append(",").append(miny).append("]");
 *		} else {
 */		
                val.append("{\"type\":\"Polygon\",\"coordinates\":");
	        val.append("[[");
	        val.append("[").append(minx).append(",").append(miny).append("]");
	        val.append(",");
	        val.append("[").append(minx).append(",").append(maxy).append("]");
	        val.append(",");
	        val.append("[").append(maxx).append(",").append(maxy).append("]");
	        val.append(",");
	        val.append("[").append(maxx).append(",").append(miny).append("]");
	        val.append(",");
	        val.append("[").append(minx).append(",").append(miny).append("]");
	        val.append("]]");
/*		}
 */		
		val.append("}");
		addExtraField(rec, key, val.toString());
	    } else if (internal.contains(key)) {
		rec.put(key, value);
	    } else {
		addExtraField(rec, key, value);
	    }
	}
	
	PrintStream out = System.out;
	boolean close;

	if (outputDir == null) {
	    close = false;
	    out = System.out;
	} else {
	    close = true;
	    try {
		out = new PrintStream(outputDir.resolve(fl.getSource().replaceAll("xml$", "") + "json").toFile());
	    } catch (FileNotFoundException e) {
		logger.error(e.getMessage(), e);
		return;
	    }
	}

	gson.toJson(rec, out);
	out.println();
	if (close) out.close();
    }
}
