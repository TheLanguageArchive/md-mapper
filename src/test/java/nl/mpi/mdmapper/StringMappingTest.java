/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.mpi.mdmapper;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for StringMapping class.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class StringMappingTest {
    /**
     * Test of the expand method, of class StringMapping.
     */
    @Test
    public void testExpand() {
	Configuration conf = new Configuration();
	conf.setParam("test1", "xyz");
	String str = "Strings: $1 ${test1}, ${test1} again, and nonexistent ${test2}.";
	String expected = "Strings: $1 xyz, xyz again, and nonexistent .";
	String result = StringMapping.expand(str, conf, null);
	assertEquals(expected, result);
    }
}
