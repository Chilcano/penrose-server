package org.safehaus.penrose.test.mapping;

import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.entry.Entry;
import org.safehaus.penrose.entry.AttributeValues;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class SearchSubtreeTest extends StaticTestCase {

    public SearchSubtreeTest() throws Exception {
    }

    public void testSearchingOneLevelOnGroup() throws Exception {

        PenroseSession session = penrose.newSession();
        session.bind(penroseConfig.getRootDn(), penroseConfig.getRootPassword());

        PenroseSearchControls sc = new PenroseSearchControls();
        PenroseSearchResults results = new PenroseSearchResults();
        session.search("cn=group,"+baseDn, "(objectClass=*)", sc, results);

        assertTrue(results.hasNext());

        Entry entry = (Entry)results.next();
        String dn = entry.getDn().toString();
        assertEquals("cn=group,"+baseDn, dn);

        AttributeValues attributes = entry.getAttributeValues();

        Object value = attributes.getOne("cn");
        assertEquals("group", value);

        value = attributes.getOne("description");
        assertEquals("description", value);

        Collection values = attributes.get("uniqueMember");

        for (Iterator i = values.iterator(); i.hasNext(); ) {
            value = i.next();
            if (!value.equals("member1") && !value.equals("member2")) {
                fail();
            }
        }

        assertTrue(results.hasNext());

        entry = (Entry)results.next();
        dn = entry.getDn().toString();
        assertEquals("uid=member1,cn=group,"+baseDn, dn);

        attributes = entry.getAttributeValues();

        value = attributes.getOne("uid");
        assertEquals("member1", value);

        value = attributes.getOne("memberOf");
        assertEquals("group", value);

        assertTrue(results.hasNext());

        entry = (Entry)results.next();
        dn = entry.getDn().toString();
        assertEquals("uid=member2,cn=group,"+baseDn, dn);

        attributes = entry.getAttributeValues();

        value = attributes.getOne("uid");
        assertEquals("member2", value);

        value = attributes.getOne("memberOf");
        assertEquals("group", value);

        assertFalse(results.hasNext());

        session.close();
    }
}
