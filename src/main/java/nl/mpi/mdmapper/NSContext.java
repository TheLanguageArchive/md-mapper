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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;

/**
 * A simple implementation of an XML namespace context.
 *
 * @author Lari Lampen (MPI-PL)
 */

public class NSContext implements NamespaceContext {
    private static final Logger logger = Logger.getLogger(NSContext.class);

    // Two maps are used to make it simple to map from prefixes to
    // URIs and vice versa.
    private Map<String, String> pref2ns;
    private Map<String, String> ns2pref;

    public NSContext() {
	pref2ns = new HashMap<String, String>();
	ns2pref = new HashMap<String, String>();
    }

    public void add(String prefix, String ns) {
	logger.debug("add "+prefix+", "+ns);
	ns2pref.put(ns, prefix);
	pref2ns.put(prefix, ns);
    }

    /**
     * Look up namespace URI based on prefix. Some of the return
     * values are fixed by the XML standard.
     */
    @Override
    public String getNamespaceURI(String prefix) {
	//	logger.debug("received request for prefix: " + prefix);

	if (prefix == null)
	    throw new IllegalArgumentException("Illegal check of null namespace prefix");

	if (pref2ns.containsKey(prefix))
	    return pref2ns.get(prefix);

	if (prefix.equals(XMLConstants.XML_NS_PREFIX))
	    return XMLConstants.XML_NS_URI;
	if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
	    return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

	return "";
    }

    /**
     * Look up prefix based on namespace URI. Some of the return
     * values are fixed by the XML standard.
     */
    @Override
    public String getPrefix(String uri) {
	//	logger.debug("received request for uri: " + uri);

	if (uri == null)
	    throw new IllegalArgumentException("Illegal check of null namespace URI");

	if (pref2ns.containsKey(uri))
	    return pref2ns.get(uri);

	if (uri.equals(XMLConstants.XML_NS_URI))
	    return XMLConstants.XML_NS_PREFIX;
	if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
	    return XMLConstants.XMLNS_ATTRIBUTE;

	return null;
    }

    /**
     * NOTE. Many prefixes can be bound to the same URI, e.g.
     *
     * <pre>
     * <parent xmlns:prefix1="http://Namespace-name-URI">
     *   <child xmlns:prefix2="http://Namespace-name-URI">
     *     ...
     *   </child>
     * </parent>
     * </pre>
     *
     * However, for simplicity this class does not implement multiple
     * prefixes. Hence this method always returns an iterator into a
     * list of one.
     */
    @Override
    public Iterator getPrefixes(String uri) {
	List<String> dummy = new ArrayList<>(1);
	dummy.add(getPrefix(uri));
	return dummy.iterator();
    }
}
