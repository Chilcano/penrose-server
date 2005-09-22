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
package org.safehaus.penrose.mapping;

/**
 * @author Endi S. Dewata
 */
public class FieldDefinition implements Comparable, Cloneable {

	/**
	 * Name.
	 */
	private String name;

    private String originalName;

    private String type = "VARCHAR";

    private int length = 50;
    private int precision = 0;

	/**
	 * This is a primary key.
	 */
	private boolean primaryKey;

    /**
     * Encryption method used to encrypt the value
     */
    private String encryption;

    /**
     * Encoding method used to encode the value
     */
    private String encoding;

	public FieldDefinition() {
	}

    public FieldDefinition(String name) {
        this.name = name;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}
	
    public String getOriginalName() {
        return originalName == null ? name : originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int hashCode() {
        return (name == null ? 0 : name.hashCode()) +
                (originalName == null ? 0 : originalName.hashCode()) +
                (type == null ? 0 : type.hashCode()) +
                (primaryKey ? 0 : 1) +
                (encryption == null ? 0 : encryption.hashCode()) +
                (encoding == null ? 0 : encoding.hashCode()) +
                (length) +
                (precision);
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof FieldDefinition)) return false;

        FieldDefinition fieldDefinition = (FieldDefinition)object;
        if (!equals(name, fieldDefinition.name)) return false;
        if (!equals(originalName, fieldDefinition.originalName)) return false;
        if (!equals(type, fieldDefinition.type)) return false;
        if (primaryKey != fieldDefinition.primaryKey) return false;
        if (!equals(encryption, fieldDefinition.encryption)) return false;
        if (!equals(encoding, fieldDefinition.encoding)) return false;
        if (length != fieldDefinition.length) return false;
        if (precision != fieldDefinition.precision) return false;

        return true;
    }

    public int compareTo(Object object) {
        if (object == null) return 0;
        if (!(object instanceof FieldDefinition)) return 0;

        FieldDefinition fd = (FieldDefinition)object;
        return name.compareTo(fd.name);
    }

    public void copy(FieldDefinition fieldDefinition) {
        name = fieldDefinition.name;
        originalName = fieldDefinition.originalName;
        type = fieldDefinition.type;
        primaryKey = fieldDefinition.primaryKey;
        encryption = fieldDefinition.encryption;
        encoding = fieldDefinition.encoding;
        length = fieldDefinition.length;
        precision = fieldDefinition.precision;
    }

    public Object clone() {
        FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.copy(this);
        return fieldDefinition;
    }
}