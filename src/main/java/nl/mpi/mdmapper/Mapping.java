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
import org.w3c.dom.Document;

/**
 * A mapping rule for an individual field (facet).
 *
 * @author Lari Lampen (MPI-PL)
 */
public abstract class Mapping {
    private static final Logger logger = Logger.getLogger(Mapping.class);

    /**
     * Number of times this mapping has been applied with non-empty
     * result (for debug / statistics purposes).
     */
    protected int numUses;

    protected Mapping() {
	numUses = 0;
    }

    /**
     * Apply this mapping to the specified document tree.
     *
     * @param doc DOM tree representing a metadata record
     * @return result of mapping as a string, or the empty string if
     * there is no other result.
     */
    public abstract String apply(Document doc) throws MappingException;

    /**
     * Apply mapping to the specified document and add result
     * to the given facet list.
     *
     * @param doc DOM tree representing a metadata record
     * @param facetName name of target facet in the facet list
     * @param fl facet list to which the result is to be added
     * @return true if mapping mathced, false otherwise
     * @throws nl.mpi.mdmapper.MappingException on any error
     */
    public boolean mapAndAdd(Document doc, String facetName, FacetList fl)
	    throws MappingException {
	String s  = apply(doc);

	if (!s.isEmpty()) {
	    fl.add(facetName, s);
	    return true;
	}
	return false;
    }

    /**
     * Get number of times this mapping has been applied with
     * non-empty result.
     *
     * @return number of uses of this mapping
     */
    public int getNumUses() {
	return numUses;
    }
}
