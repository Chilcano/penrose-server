package org.safehaus.penrose.test.mapping.nested;

import org.apache.log4j.Logger;
import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.entry.Entry;
import org.safehaus.penrose.entry.AttributeValues;

/**
 * @author Endi S. Dewata
 */
public class SearchFilterTest extends NestedTestCase {

    Logger log = Logger.getLogger(getClass());

    public SearchFilterTest() throws Exception {
    }

    public void testSearchingGroupsWithFilter() throws Exception {

        executeUpdate("insert into groups values ('group1', 'desc1')");
        executeUpdate("insert into groups values ('group2', 'desc2')");
        executeUpdate("insert into groups values ('group3', 'desc3')");

        executeUpdate("insert into members values ('member1', 'group1', 'Member1')");
        executeUpdate("insert into members values ('member2', 'group1', 'Member2')");
        executeUpdate("insert into members values ('member3', 'group2', 'Member3')");
        executeUpdate("insert into members values ('member4', 'group2', 'Member4')");

        PenroseSession session = penrose.newSession();
        session.bind(penroseConfig.getRootDn(), penroseConfig.getRootPassword());

        PenroseSearchControls sc = new PenroseSearchControls();
        PenroseSearchResults results = new PenroseSearchResults();
        session.search(baseDn, "(description=desc2)", sc, results);

        boolean hasNext = results.hasNext();
        log.debug("hasNext: "+hasNext);
        assertTrue(hasNext);

        Entry entry = (Entry)results.next();
        String dn = entry.getDn().toString();
        log.debug("DN: "+dn);
        assertEquals("cn=group2,"+baseDn, dn);

        hasNext = results.hasNext();
        log.debug("hasNext: "+hasNext);
        assertFalse(hasNext);

        session.close();
    }

    public void testSearchingMembersWithFilter() throws Exception {

        executeUpdate("insert into groups values ('group1', 'desc1')");
        executeUpdate("insert into groups values ('group2', 'desc2')");
        executeUpdate("insert into groups values ('group3', 'desc3')");

        executeUpdate("insert into members values ('member1', 'group1', 'Member1')");
        executeUpdate("insert into members values ('member2', 'group1', 'Member2')");
        executeUpdate("insert into members values ('member3', 'group2', 'Member3')");
        executeUpdate("insert into members values ('member4', 'group2', 'Member4')");

        PenroseSession session = penrose.newSession();
        session.bind(penroseConfig.getRootDn(), penroseConfig.getRootPassword());

        PenroseSearchControls sc = new PenroseSearchControls();
        PenroseSearchResults results = new PenroseSearchResults();
        session.search(baseDn, "(memberOf=group2)", sc, results);

        while (results.hasNext()) {
            Entry entry = (Entry)results.next();
            String dn = entry.getDn().toString();
            log.info("Checking "+dn+":");

            AttributeValues attributes = entry.getAttributeValues();
            attributes.print();

            if (dn.equals("uid=member3,cn=group2,"+baseDn)) {
                Object value = attributes.getOne("memberOf");
                assertEquals("group2", value);

            } else if (dn.equals("uid=member4,cn=group2,"+baseDn)) {
                Object value = attributes.getOne("memberOf");
                assertEquals("group2", value);

            } else {
                fail("Unexpected DN: "+dn);
            }
        }

        log.debug("Total count: "+results.getTotalCount());
        assertEquals(2, results.getTotalCount());

        session.close();
    }

}
