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


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.nio.file.Path;

import java.io.FileNotFoundException;

/**
 * Plain XML output (i.e. key-value pairs on the top level with no further structure).
 *
 * @author Lari Lampen (MPI-PL)
 */
public class PlainXmlOutput implements Output {
    private static final Logger logger = Logger.getLogger(PlainXmlOutput.class);

    private Path outputDir;
    private boolean indent;

    /**
     * Create new output object for plain XML.
     *
     * @param prettyPrint whether to indent output for readability
     * @param outputDir  output directory
     */
    public PlainXmlOutput(boolean prettyPrint, Path outputDir) {
	this.outputDir = outputDir;
	indent = prettyPrint;
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
		out = new PrintStream(outputDir.resolve(fl.getSourceShort()).toFile());
	    } catch (FileNotFoundException e) {
		logger.error(e.getMessage(), e);
		return;
	    }
	}

	try {
	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

	    Document doc = docBuilder.newDocument();
	    Element rootElement = doc.createElement("fieldList");
	    doc.appendChild(rootElement);

	    for (Map.Entry<String, String> e : fl.res.entrySet()) {
		Element field = doc.createElement("field");
		rootElement.appendChild(field);
		field.setAttribute("name", e.getKey());
		field.appendChild(doc.createTextNode(e.getValue()));
	    }

	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

	    DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(out);

	    transformer.transform(source, result);

	} catch (ParserConfigurationException | TransformerException e) {
	    logger.error("Error saving XML", e);
	}

	out.println();
	if (close) out.close();
    }
}
