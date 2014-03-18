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
import java.io.PrintStream;
import java.text.DecimalFormat;

import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * An instance of this class represents a set of mappings intended for
 * a specific metadata input format (schema).
 *
 * @author Lari Lampen (MPI-PL)
 */
public class MappingTable {
    private static final Logger logger = Logger.getLogger(MappingTable.class);

    private Map<String, List<Mapping>> mappings;
    private XPath xpath;
    private DocumentBuilder db;
    private Configuration config;

    /** How many documents have been processed (for statistics). */
    private int numUses;

    public MappingTable(String mapFile, Configuration config) {
	this.config = config;

	numUses = 0;

	// Initialise the XPath processing paraphernalia.
	XPathFactory factory = XPathFactory.newInstance();
	xpath = factory.newXPath();
	try {
	    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	    db = fac.newDocumentBuilder();
	} catch (ParserConfigurationException eE) {
	    logger.error("Cannot create parser", eE);
	}

	mappings = new HashMap<>();
	readMappings(mapFile);
    }

    /**
     * Read the mappings from an XML file. Since we have all the XPath
     * stuff ready, let's use it here too.
     */
    private void readMappings(String mapFile) {
	// Note: not being able to read the mapping file terminates
	// the converter.
	Document mapping = null;
	try {
	    mapping = db.parse(mapFile);
	} catch (SAXException e) {
	    logger.error(e.getMessage(), e);
	    System.exit(1);
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	    System.exit(1);
	}

	NSContext nsContext = null;
	try {
	    nsContext = parseNamespaces((NodeList)xpath.evaluate("/mapping-table/namespaces/namespace",
								 mapping, XPathConstants.NODESET));

	    parseFieldMappings((NodeList)xpath.evaluate("/mapping-table/mappings/field", mapping,
							XPathConstants.NODESET));
	} catch (XPathExpressionException e) {
	    logger.error("XPath error", e);
	    System.exit(1);
	}

	xpath.setNamespaceContext(nsContext);
    }

    /**
     * Parse namespace definitions from the mapping configuration
     * file.
     *
     * @return a namespace context with the specified namespace
     * bindings set
     */
    private NSContext parseNamespaces(NodeList nl) {
	NSContext nsContext = new NSContext();
	for (int i=0; i<nl.getLength(); i++) {
	    Node node = nl.item(i);
	    NamedNodeMap attr = node.getAttributes();
	    nsContext.add(attr.getNamedItem("ns").getNodeValue(), attr.getNamedItem("uri").getNodeValue());
	}
	return nsContext;
    }

    /**
     * Parse mapping definitions from configuration file and add them
     * directly into 'mappings'.
     */
    private void parseFieldMappings(NodeList nl) {
	for (int i=0; i<nl.getLength(); i++) {
	    Node node = nl.item(i);
	    NamedNodeMap attr = node.getAttributes();
	    String fieldName = attr.getNamedItem("name").getNodeValue();
	    NodeList children = node.getChildNodes();
	    for (int j=0; j<children.getLength(); j++) {
		Node t = children.item(j);
		if (t.getNodeType() == Node.TEXT_NODE
		    || t.getNodeType() == Node.COMMENT_NODE)
		    continue;
		Mapping newMapping;
		if ("xpath".equals(t.getNodeName())) {
		    newMapping = new XpathMapping(xpath, t.getTextContent());
		} else if ("string".equals(t.getNodeName())) {
		    NamedNodeMap attr2 = t.getAttributes();
		    Node expNode = attr2.getNamedItem("expand");
		    boolean expand;
		    if (expNode == null) {
			expand = false;
		    } else {
			expand = Boolean.valueOf(expNode.getNodeValue());
		    }
		    newMapping = new StringMapping(t.getTextContent(),
			    expand ? config : null);
		} else {
		    logger.info("Unsure how to handle element '" + t.getNodeName() + "', skipping.");
		    continue;
		}

		logger.debug("Adding "+fieldName);
		List<Mapping> mapList = mappings.get(fieldName);
		if (mapList == null) {
		    mapList = new ArrayList<>();
		    mappings.put(fieldName, mapList);
		}
		mapList.add(newMapping);
	    }
	}
    }

    public FacetList applyMappings(Document doc, String filename) {
	FacetList result = new FacetList(filename);
	numUses++;

	for (Map.Entry<String, List<Mapping>> me : mappings.entrySet()) {
	    List<Mapping> mapList = me.getValue();
	    for (Mapping m : mapList) {
		String s = m.apply(doc);
		if (!s.isEmpty()) {
		    result.add(me.getKey(), s);
		    break;
		}
	    }
	}
	return result;
    }

    public FacetList applyMappings(File inFile) throws IOException, SAXException {
	return applyMappings(db.parse(inFile), inFile.getName());
    }

    public int getNumUses() {
	return numUses;
    }

    /**
     * Print statistics of coverage of mappings. Mainly for debugging
     * purposes.
     */
    public void printStatistics(PrintStream out) {
	DecimalFormat df = new DecimalFormat("0.#");

	out.println("----- Statistics of coverage of mappings -----");
	for (Map.Entry<String, List<Mapping>> me : mappings.entrySet()) {
	    List<Mapping> mapList = me.getValue();
	    out.println("  --- Facet: " + me.getKey() + " ---");
	    int unmapped = numUses;
	    for (Mapping m : mapList) {
		int n = m.getNumUses();
		unmapped -= n;
		out.println("\t" + n + " (" + df.format(100*(double)n/numUses) + " %) \t" + m);
	    }
	    if (unmapped > 0)
		out.println("\t" + unmapped + " (" + df.format(100*(double)unmapped/numUses) + " %) \tunmapped");
	}
    }
}
