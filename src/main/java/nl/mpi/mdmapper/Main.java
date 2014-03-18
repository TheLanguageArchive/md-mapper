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

package nl.mpi.mdmapper;

import org.apache.log4j.Logger;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[]) {
	// Select Saxon XPath implementation (necessary in case there
	// are other XPath libraries in classpath).
	System.setProperty("javax.xml.xpath.XPathFactory", "net.sf.saxon.xpath.XPathFactoryImpl");

	Configuration config = new Configuration();

	// Configuration parameters can be given with 'key=value'
	// strings. Parse them first.
	try {
	    for (String arg : args) {
		if (arg.contains("=")) {
		    String[] tmp=arg.split("=");
		    if (tmp.length == 1) {
			config.setParam(tmp[0], "");
		    } else if (tmp.length >= 2) {
			config.setParam(tmp[0], tmp[1]);
		    }
		}
	    }
	} catch (UnknownParameterException e) {
	    logger.error("Error in command line parameter", e);
	    System.exit(1);
	}

	// Read the configuration file (which is either config.xml, or
	// was set above via the 'config' parameter on the command
	// line).
	try {
	    config.readConfigurationFile();
	} catch (UnknownParameterException e) {
	    logger.error("Error in configuration file", e);
	    System.exit(1);
	}
	process(config);
    }

    public static void process(Configuration config) {
	String dirName = config.getInputdir();
	MappingTable mt = new MappingTable(config.getMapfile());

	logger.info("Processing files in " + dirName + ".");
	// Actually process all the files.
	Path path = Paths.get(dirName);
	FileProcessor fp = new FileProcessor(mt, config.getOutputs());
	try {
	    Files.walkFileTree(path, fp);
	} catch (IOException e) {
	    logger.error("Error processing file " + path, e);
	}
	logger.info("" + mt.getNumUses() + " records mapped.");

	// Processing completed; save statistics, if required.
	String stat = config.getSavestats();
	if (stat != null) {
	    PrintStream out;
	    boolean close;
	    if (stat.isEmpty()) {
		close = false;
		out = System.out;
	    } else {
		close = true;
		try {
		    out = new PrintStream(stat);
		} catch (FileNotFoundException e) {
		    logger.error(e.getMessage(), e);
		    return;
		}
	    }
	    mt.printStatistics(out);
	    if (close) out.close();
	}
    }
}
