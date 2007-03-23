package org.safehaus.penrose.test.mapping.nested;

import org.safehaus.penrose.session.Session;
import org.safehaus.penrose.session.SearchRequest;
import org.safehaus.penrose.session.SearchResponse;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class SearchNestedTest extends NestedTestCase {

    Logger log = Logger.getLogger(getClass());

    public SearchNestedTest() throws Exception {
    }

    public void testSearchingEmptyDatabase() throws Exception {

        Session session = penrose.newSession();
        session.bind(penroseConfig.getRootDn(), penroseConfig.getRootPassword());

        SearchResponse response = session.search(
                baseDn,
                "(objectClass=*)",
                SearchRequest.SCOPE_ONE
        );

        assertFalse(response.hasNext());

        session.close();
    }
}
