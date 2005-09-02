/**
 * Copyright (c) 2000-2005, Identyx Corporation.
 * All rights reserved.
 */
package org.safehaus.penrose.mapping;

/**
 * @author Endi S. Dewata
 */
public class Field {

    private FieldDefinition fieldDefinition;

	/**
	 * Name.
	 */
	private String name;

    /**
     * Script.
     */
    private String script;

	/**
	 * Expression.
	 */
	private Expression expression;


    public Field() {
    }
    
    public Field(String name) {
        this.name = name;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression.setScript(expression);
	}

    public void addExpression(Expression expression) {
        this.expression = expression;
    }

    public boolean isPrimaryKey() {
        return fieldDefinition.isPrimaryKey();
    }

    public String getOriginalName() {
        return fieldDefinition.getOriginalName();
    }

    public String getEncryption() {
        return fieldDefinition.getEncryption();
    }

    public String getEncoding() {
        return fieldDefinition.getEncoding();
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public void setFieldDefinition(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    public String getType() {
        return fieldDefinition.getType();
    }

    public String toString() {
        return name;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}