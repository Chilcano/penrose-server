/**
 * Copyright (c) 2000-2005, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.schema;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ObjectClass implements Cloneable, Comparable {

    Logger log = LoggerFactory.getLogger(getClass());

	public final static String ABSTRACT   = "ABSTRACT";
	public final static String STRUCTURAL = "STRUCTURAL";
	public final static String AUXILIARY  = "AUXILIARY";
	
    /**
     * Identifier.
     */
	public String oid;
    
	/**
	 * Name.
	 */
	public Collection names = new ArrayList();
    
    /**
     * Description.
     */
	public String description;
    
    /**
     * Obsolete.
     */
	public boolean obsolete;
    
    /**
     * Super class.
     */
	public Collection superClasses = new ArrayList();
    
    /**
     * Type (ABSTRACT, STRUCTURAL, AUXILIARY). Default: STRUCTURAL.
     */
	public String type = STRUCTURAL;

    /**
     * Required attribute types. Each element is of type String.
     */
	public Collection requiredAttributes = new ArrayList();
    
    /**
     * Optional attribute types. Each element is of type String.
     */
	public Collection optionalAttributes = new ArrayList();
	
	public ObjectClass() {
	}

    public ObjectClass(String name, String superClass, String description) {
        this.names.add(name);
        this.superClasses.add(superClass);
        this.description = description;
    }

    public ObjectClass(Collection names, Collection superClasses, String description) {
        this.names.addAll(names);
        this.superClasses.addAll(superClasses);
        this.description = description;
    }
    
    public String getName() {
    	if (names != null && names.size() >= 1) return names.iterator().next().toString();
    	return null;
    }
    
    public void setName(String name) {
    	names.clear();
    	names.add(name);
    }

    public void addName(String name) {
        names.add(name);
    }

    public Collection getNames() {
        return names;
    }

    public void setNames(Collection names) {
        this.names = names;
    }

    public void removeNames() {
        names.clear();
    }

    public void addSuperClass(String superClass) {
        superClasses.add(superClass);
    }

    public Collection getSuperClasses() {
        return superClasses;
    }

    public void setSuperClasses(Collection superClasses) {
        this.superClasses.addAll(superClasses);
    }

    public void removeSuperClasses() {
        superClasses.clear();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection getRequiredAttributes() {
        return requiredAttributes;
    }

    public void addRequiredAttribute(String requiredAttribute) {
        requiredAttributes.add(requiredAttribute);
    }
    
    public void addRequiredAttributes(Collection requiredAttributes) {
        if (requiredAttributes == null) return;
        this.requiredAttributes.addAll(requiredAttributes);
    }

    public void setRequiredAttributes(Collection requiredAttributes) {
        this.requiredAttributes.clear();
        if (requiredAttributes == null) return;
        this.requiredAttributes.addAll(requiredAttributes);
    }

    public void removeRequiredAttributes() {
        requiredAttributes.clear();
    }
    
    public Collection getOptionalAttributes() {
        return optionalAttributes;
    }

    public void addOptionalAttribute(String optionalAttribute) {
        optionalAttributes.add(optionalAttribute);
    }

    public void addOptionalAttributes(Collection optionalAttributes) {
        if (optionalAttributes == null) return;
        this.optionalAttributes.addAll(optionalAttributes);
    }

    public void setOptionalAttributes(Collection optionalAttributes) {
        this.optionalAttributes.clear();
        if (optionalAttributes == null) return;
        this.optionalAttributes.addAll(optionalAttributes);
    }

    public void removeOptionalAttributes() {
        optionalAttributes.clear();
    }

	public boolean isObsolete() {
		return obsolete;
	}
	/**
	 * @param obsolete The obsolete to set.
	 */
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}
	/**
	 * @return Returns the oid.
	 */
	public String getOid() {
		return oid;
	}
	/**
	 * @param oid The oid to set.
	 */
	public void setOid(String oid) {
		this.oid = oid;
	}
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
    public boolean containsRequiredAttribute(String name) {
        name = name.toLowerCase();

        for (Iterator i=requiredAttributes.iterator(); i.hasNext(); ) {
            String attrName = (String)i.next();
            if (name.equals(attrName.toLowerCase())) return true;
        }

        return false;
    }

    public boolean containsOptionalAttribute(String name) {
        name = name.toLowerCase();

        for (Iterator i=optionalAttributes.iterator(); i.hasNext(); ) {
            String attrName = (String)i.next();
            if (name.equals(attrName.toLowerCase())) return true;
        }

        return false;
    }

    public int hashCode() {
        return (oid == null ? 0 : oid.hashCode()) +
                (names == null ? 0 : names.hashCode()) +
                (description == null ? 0 : description.hashCode()) +
                (obsolete ? 0 : 1) +
                (superClasses == null ? 0 : superClasses.hashCode()) +
                (type == null ? 0 : type.hashCode()) +
                (requiredAttributes == null ? 0 : requiredAttributes.hashCode()) +
                (optionalAttributes == null ? 0 : optionalAttributes.hashCode());
    }

    boolean compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if((object == null) || (object.getClass() != this.getClass())) return false;

        ObjectClass oc = (ObjectClass)object;
        if (!compare(oid, oc.oid)) return false;
        if (!compare(names, oc.names)) return false;
        if (!compare(description, oc.description)) return false;
        if (obsolete != oc.obsolete) return false;
        if (!compare(superClasses, oc.superClasses)) return false;
        if (!compare(type, oc.type)) return false;
        if (!compare(requiredAttributes, oc.requiredAttributes)) return false;
        if (!compare(optionalAttributes, oc.optionalAttributes)) return false;

        return true;
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public void copy(ObjectClass oc) {
        oid = oc.oid;

        names.clear();
        names.addAll(oc.names);

        description = oc.description;
        obsolete = oc.obsolete;

        superClasses.clear();
        superClasses.addAll(oc.superClasses);

        type = oc.type;

        requiredAttributes.clear();
        requiredAttributes.addAll(oc.requiredAttributes);

        optionalAttributes.clear();
        optionalAttributes.addAll(oc.optionalAttributes);
    }

    public Object clone() {
        ObjectClass oc = new ObjectClass();
        oc.copy(this);
        return oc;
    }

    public int compareTo(Object object) {
        if (!(object instanceof ObjectClass)) return 0;

        ObjectClass oc = (ObjectClass)object;
        return oid.compareTo(oc.getOid());
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean multiLine) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

        out.print(oid);
        if (multiLine) out.println();

        if (names.size() == 1) {
            if (multiLine) out.print("   ");
            out.print(" NAME '"+names.iterator().next()+"'");
            if (multiLine) out.println();

        } else if (names.size() > 1) {
            if (multiLine) out.print("   ");
            out.print(" NAME ( ");
            for (Iterator i=names.iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                out.print("'"+name+"' ");
            }
            out.print(")");
            if (multiLine) out.println();
        }

        if (description != null) {
            if (multiLine) out.print("   ");
            out.print(" DESC '"+escape(description)+"'");
            if (multiLine) out.println();
        }

        if (obsolete) {
            if (multiLine) out.print("   ");
            out.print(" OBSOLETE");
            if (multiLine) out.println();
        }

        if (superClasses.size() == 1) {
            if (multiLine) out.print("   ");
            out.print(" SUP "+superClasses.iterator().next());
            if (multiLine) out.println();

        } else if (superClasses.size() > 1) {
            if (multiLine) out.print("   ");
            out.print(" SUP ( ");
            for (Iterator i=superClasses.iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                out.print(name);
                if (i.hasNext()) out.print(" $ ");
            }
            out.print(" )");
            if (multiLine) out.println();
        }

        if (!STRUCTURAL.equals(type)) {
            if (multiLine) out.print("   ");
            out.print(" "+type);
            if (multiLine) out.println();
        }

        if (requiredAttributes.size() == 1) {
            if (multiLine) out.print("   ");
            out.print(" MUST "+requiredAttributes.iterator().next());
            if (multiLine) out.println();

        } else if (requiredAttributes.size() > 1) {
            if (multiLine) out.print("   ");
            out.print(" MUST ( ");
            for (Iterator i=requiredAttributes.iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                out.print(name);
                if (i.hasNext()) out.print(" $ ");
            }
            out.print(" )");
            if (multiLine) out.println();
        }

        if (optionalAttributes.size() == 1) {
            if (multiLine) out.print("   ");
            out.print(" MAY "+optionalAttributes.iterator().next());
            if (multiLine) out.println();

        } else if (optionalAttributes.size() > 1) {
            if (multiLine) out.print("   ");
            out.print(" MAY ( ");
            for (Iterator i=optionalAttributes.iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                out.print(name);
                if (i.hasNext()) out.print(" $ ");
            }
            out.print(" )");
            if (multiLine) out.println();
        }

        return sw.toString();
    }

    public static String escape(String s) {
        StringBuffer sb = new StringBuffer();

        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' || c == '\\') {
                sb.append("\\");
                sb.append(toHex(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String toHex(char c) {
        String s = Integer.toHexString(c);
        return s.length() == 1 ? "0"+s : s;
    }
}