package org.safehaus.penrose.test.mapping.basic;

import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.entry.Entry;
import org.safehaus.penrose.entry.AttributeValues;
import org.ietf.ldap.LDAPException;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class SearchBaseTest extends BasicTestCase {

    public SearchBaseTest() throws Exception {
    }

    public void testSearchingBase() throws Exception {

        String groupnames[] = new String[] { "abc", "def", "ghi" };
        String descriptions[] = new String[] { "ABC", "DEF", "GHI" };
        for (int i=0; i<groupnames.length; i++) {
            Collection params = new ArrayList();
            params.add(groupnames[i]);
            params.add(descriptions[i]);
            executeUpdate("insert into groups values (?, ?)", params);
        }

        PenroseSession session = penrose.newSession();
        session.bind(penroseConfig.getRootDn(), penroseConfig.getRootPassword());

        PenroseSearchControls sc = new PenroseSearchControls();
        sc.setScope(PenroseSearchControls.SCOPE_BASE);
        PenroseSearchResults results = new PenroseSearchResults();
        session.search("cn=def,"+baseDn, "(objectClass=*)", sc, results);

        boolean hasNext = results.hasNext();
        log.debug("hasNext: "+hasNext);
        assertTrue(hasNext);

        Entry entry = (Entry)results.next();
        String dn = entry.getDn().toString();
        log.debug("dn: "+dn);
        assertEquals("cn=def,"+baseDn, dn);

        AttributeValues attributes = entry.getAttributeValues();

        Object value = attributes.getOne("cn");
        log.debug("cn: "+value);
        assertEquals("def", value);

        value = attributes.getOne("description");
        log.debug("description: "+value);
        assertEquals("DEF", value);

        hasNext = results.hasNext();
        log.debug("hasNext: "+hasNext);
        assertFalse(hasNext);

        session.close();
    }

    public void testSearchingNonExistentBase() throws Exception {

        String groupnames[] = new String[] { "abc", "def", "ghi" };
        String descriptions[] = new String[] { "ABC", "DEF", "GHI" };
        for (int i=0; i<groupnames.length; i++) {
            Collection params = new ArrayList();
            params.add(groupnames[i]);
            params.add(descriptions[i]);
            executeUpdate("insert into groups values (?, ?)", params);
        }

        PenroseSession session = penrose.newSession();
        session.bind(penroseConfig.getRootDn(), penroseConfig.getRootPassword());

        PenroseSearchControls sc = new PenroseSearchControls();
        sc.setScope(PenroseSearchControls.SCOPE_BASE);
        PenroseSearchResults results = new PenroseSearchResults();
        session.search("cn=jkl,"+baseDn, "(objectClass=*)", sc, results);

        try {
            boolean hasNext = results.hasNext();
            log.debug("hasNext: "+hasNext);
            fail();
        } catch (LDAPException e) {
            log.debug(e.getMessage(), e);
            assertEquals(LDAPException.NO_SUCH_OBJECT, e.getResultCode());
        }

        session.close();
    }
}
