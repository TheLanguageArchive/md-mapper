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

import nl.mpi.mdmapper.output.Output;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * This class stores configuration parameters and global variables of
 * the converter.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class Configuration {
    private static final Logger logger = Logger.getLogger(Configuration.class);

    private Map<String, String> params;
    private List<Output> outputs;

    public Configuration() {
	params = new HashMap<>();
	outputs = new ArrayList<>();
    }

    /**
     * Read configuration from config file. Parameters that are
     * already set are not overwritten.
     */
    public void readConfigurationFile() throws UnknownParameterException {
	String file = params.get("config");
	// Default configuration file name.
	if (file == null)
	    file = "config.xml";
	XPathFactory factory = XPathFactory.newInstance();
	XPath xpath = factory.newXPath();
	DocumentBuilder db = null;
	try {
	    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	    db = fac.newDocumentBuilder();
	} catch (ParserConfigurationException eE) {
	    logger.error("Cannot create parser", eE);
	}
	Document doc = null;
	NodeList nl = null;

	// Read and store parameters.
	try {
	    doc = db.parse(file);
	    nl = (NodeList)xpath.evaluate("/config/params/*", doc, XPathConstants.NODESET);
	} catch (SAXException | XPathExpressionException | IOException e) {
	    logger.error(e.getMessage(), e);
	    return;
	}
	for (int i=0; i<nl.getLength(); i++) {
	    Node node = nl.item(i);
	    if (node.getNodeType() == Node.TEXT_NODE
		|| node.getNodeType() == Node.COMMENT_NODE)
		continue;
	    String key = node.getNodeName();
	    if (!params.containsKey(key))
		params.put(key, node.getTextContent());
	}

	// Read and store output instructions.
	try {
	    nl = (NodeList)xpath.evaluate("/config/outputs/output", doc, XPathConstants.NODESET);
	} catch (XPathExpressionException e) {
	    logger.error(e.getMessage(), e);
	    return;
	}
	for (int i=0; i<nl.getLength(); i++) {
	    Node node = nl.item(i);
	    NamedNodeMap attr = node.getAttributes();
	    Node t = attr.getNamedItem("type");
	    if (t == null) continue;
	    String type = t.getNodeValue();

	    if (type.equals("simplejson") || type.equals("ckan3json") || type.equals("xml")) {
		boolean pp = false;
		t = attr.getNamedItem("prettyPrint");
		if (t != null)
		    pp = Boolean.valueOf(t.getNodeValue());
		String dirName = node.getTextContent();

		Path dir = null;
		if (!dirName.isEmpty()) {
		    if (getOutputdir() != null) {
			// Resolve in context of output directory, if specified.
			Path p = Paths.get(getOutputdir());
			dir = p.resolve(dirName);
		    } else {
			dir = Paths.get(dirName);
		    }
		    File ff = dir.toFile();
		    if (!ff.exists() && !ff.mkdirs()) {
			logger.error("FATAL: directory "+dirName+" does not exist and cannot be created.");
			System.exit(1);
		    }
		}
		Output o;
		if (type.equals("simplejson"))
		    o = new nl.mpi.mdmapper.output.PlainJsonOutput(pp, dir);
		else if (type.equals("ckan3json"))
		    o = new nl.mpi.mdmapper.output.Ckan3JsonOutput(pp, dir);
		else
		    o = new nl.mpi.mdmapper.output.PlainXmlOutput(pp, dir);
		outputs.add(o);
	    } else {
		logger.error("Unknown output type "+type);
	    }
	}
    }

    /**
     * Set a parameter to a specified value, overriding previous value.
     */
    public void setParam(String key, String value) {
	if (key == null) return;
	params.put(key, value);
    }

    /**
     * Get the value of a parameter, or null if no value has been set.
     */
    public String getParam(String key) {
	return params.get(key);
    }

    public Output[] getOutputs() {
	return outputs.toArray(new Output[outputs.size()]);
    }

    // These are helpers for specific configuration parameters, used
    // so that the compiler can check calls. (Using getParam
    // everywhere would risk typos in the parameter names.)
    public String getMapfile() {
	return getParam("mapfile");
    }
    public String getInputdir() {
	return getParam("inputdir");
    }
    public String getOutputdir() {
	return getParam("outputdir");
    }
    public String getSavestats() {
	return getParam("savestats");
    }
}
