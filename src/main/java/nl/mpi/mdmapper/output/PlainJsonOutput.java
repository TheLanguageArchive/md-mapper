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
import java.nio.file.Path;

import java.io.FileNotFoundException;

/**
 * Plain JSON output (i.e. key-value pairs on the top level with no further structure).
 *
 * @author Lari Lampen (MPI-PL)
 */
public class PlainJsonOutput implements Output {
    private static final Logger logger = Logger.getLogger(PlainJsonOutput.class);

    private Path outputDir;
    private Gson gson;

    /**
     * Create new output object for plain JSON.
     *
     * @param prettyPrint whether to indent output for readability
     * @param outputDir  output directory
     */
    public PlainJsonOutput(boolean prettyPrint, Path outputDir) {
	this.outputDir = outputDir;
	if (prettyPrint) {
	    gson = new GsonBuilder().setPrettyPrinting().create();
	} else {
	    gson = new Gson();
	}
    }

    @Override
    public void save(FacetList fl) {
	PrintStream out;
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

	gson.toJson(fl.res, out);
	out.println();
	if (close) out.close();
    }
}
