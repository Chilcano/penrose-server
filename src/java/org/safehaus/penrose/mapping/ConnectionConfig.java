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

import org.safehaus.penrose.mapping.SourceDefinition;

import java.util.*;
import java.io.Serializable;


/**
 * @author Endi S. Dewata
 */
public class ConnectionConfig implements Serializable, Cloneable {

	/**
	 * Name.
	 */
	public String connectionName;

	/**
	 * Type.
	 */
	public String adapterName;

	/**
	 * Connection pool size.
	 */
	public int poolSize;

	/**
	 * Connection pool test query.
	 */
	public String testQuery;

	/**
	 * Description
	 */
	public String description;

	/**
	 * Parameters.
	 */
	public Map parameters = new TreeMap();

    private List listenerClasses = new ArrayList();
    private List listeners = new ArrayList();

    /**
     * Sources.
     */
    private Map sourceDefinitions = new TreeMap();

    public Object clone() {
        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.connectionName = connectionName;
        connectionConfig.adapterName = adapterName;
        connectionConfig.poolSize = poolSize;
        connectionConfig.testQuery = testQuery;
        connectionConfig.description = description;
        connectionConfig.parameters.putAll(parameters);
        connectionConfig.listenerClasses.addAll(listenerClasses);
        connectionConfig.listeners.addAll(listeners);

        for (Iterator i=sourceDefinitions.values().iterator(); i.hasNext(); ) {
            SourceDefinition sourceDefinition = (SourceDefinition)((SourceDefinition)i.next()).clone();
            connectionConfig.addSourceDefinition(sourceDefinition);
        }
        
        return connectionConfig;
    }

	public ConnectionConfig() {
	}

	/**
	 * Constructor w/ name and type
	 * 
	 * @param name
	 *            the name of the connection
	 * @param type
	 *            the type of the connection, whether JNDI or LDAP
	 */
	public ConnectionConfig(String name, String type) {
		this.connectionName = name;
		this.adapterName = type;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getAdapterName() {
		return adapterName;
	}

	public void setAdapterName(String adapterName) {
		this.adapterName = adapterName;
	}

    public Collection getParameterNames() {
        return parameters.keySet();
    }

    public String getParameter(String name) {
        return (String)parameters.get(name);
    }

    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    public void removeParameter(String name) {
        parameters.remove(name);
    }

	/**
	 * @return Returns the poolSize.
	 */
	public int getPoolSize() {
		return poolSize;
	}
	/**
	 * @param poolSize
	 *            The poolSize to set.
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public void setPoolSize(String poolSize) {
		this.poolSize = Integer.parseInt(poolSize);
	}
	/**
	 * @return Returns the testQuery.
	 */
	public String getTestQuery() {
		return testQuery;
	}
	/**
	 * @param testQuery
	 *            The testQuery to set.
	 */
	public void setTestQuery(String testQuery) {
		this.testQuery = testQuery;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the descripiton to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

    public List getListenerClasses() {
        return listenerClasses;
    }

    public void setListenerClasses(List listenerClasses) {
        this.listenerClasses = listenerClasses;
    }

    public List getListeners() {
        return listeners;
    }

    public void setListeners(List listeners) {
        this.listeners = listeners;
    }

    public void addListenerClass(String listener) {
        listenerClasses.add(listener);
    }

    public void removeListenerClass(String listener) {
        listenerClasses.remove(listener);
    }

    public void addListener(Object listener) {
        listeners.add(listener);
    }

    public void removeListener(Object listener) {
        listeners.remove(listener);
    }

    public int hashCode() {
        int value = connectionName.hashCode();
        //System.out.println("[ConnectionConfig("+connectionName+")] hashCode() => "+value);
        return value;
    }

    public boolean equals(Object object) {
        boolean value = false;
        try {
            if (object == null) {
                value = false;
                return value;
            }

            if (!(object instanceof ConnectionConfig)) {
                value = false;
                return value;
            }

            ConnectionConfig connectionConfig = (ConnectionConfig)object;
            if (!connectionName.equals(connectionConfig.connectionName)) {
                value = false;
                return value;
            }

            value = true;
            return value;

        } finally {
            //System.out.println("["+this+"] equals("+object+") => "+value);
        }
    }

    public Collection getSourceDefinitions() {
        return sourceDefinitions.values();
    }

    public SourceDefinition getSourceDefinition(String sourceName) {
        return (SourceDefinition)sourceDefinitions.get(sourceName);
    }

    public void addSourceDefinition(SourceDefinition sourceDefinition) {
        sourceDefinitions.put(sourceDefinition.getName(), sourceDefinition);
        sourceDefinition.setConnectionName(connectionName);
    }

    public SourceDefinition removeSourceDefinition(String sourceName) {
        return (SourceDefinition)sourceDefinitions.remove(sourceName);
    }

    public void renameSourceDefinition(SourceDefinition sourceDefinition, String newName) {
        if (sourceDefinition == null) return;
        if (sourceDefinition.getName().equals(newName)) return;

        sourceDefinitions.remove(sourceDefinition.getName());
        sourceDefinitions.put(newName, sourceDefinition);
    }

    public void modifySourceDefinition(String name, SourceDefinition newSourceDefinition) {
        SourceDefinition sourceDefinition = (SourceDefinition)sourceDefinitions.get(name);
        sourceDefinition.copy(newSourceDefinition);
    }

    public String toString() {
        return "ConnectionConfig("+connectionName+")";
    }
}