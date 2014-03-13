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

import nl.mpi.mdmapper.output.FacetList;
import nl.mpi.mdmapper.output.Output;

import org.apache.log4j.Logger;

import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * Process a single input file.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class FileProcessor implements FileVisitor<Path> {
    private static final Logger logger = Logger.getLogger(FileProcessor.class);

    private MappingTable mappings;
    private Output[] outputs;

    public FileProcessor(MappingTable mappings, Output[] outputs) {
	this.mappings = mappings;
	this.outputs = outputs;
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	String fileName = file.toString();
	if (fileName.endsWith(".xml")) {
	    logger.info("Processing: " + file);
	    try {
		FacetList fl = mappings.applyMappings(file.toFile());
		for (Output o : outputs) {
		    o.save(fl);
		}
	    } catch (SAXException e) {
		logger.error(e.getMessage(), e);
	    }
	} else {
	    logger.debug("Skip: "+file);
	}

	return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
	e.printStackTrace();
	return FileVisitResult.CONTINUE;
    }

    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	return FileVisitResult.CONTINUE;
    }
    public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
	return FileVisitResult.CONTINUE;
    }
}
