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
package org.safehaus.penrose.service;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class ServiceConfig implements Cloneable {

    private String name;
    private boolean enabled = true;
    private String serviceClass;
    private String description;

    private Map parameters = new TreeMap();

    public ServiceConfig() {
    }

    public ServiceConfig(String name, String serviceClass) {
        this.name = name;
        this.serviceClass = serviceClass;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection getParameterNames() {
        return parameters.keySet();
    }

    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    public void removeParameter(String name) {
        parameters.remove(name);
    }

    public String getParameter(String name) {
        return (String)parameters.get(name);
    }

    public int hashCode() {
        return (name == null ? 0 : name.hashCode()) +
                (enabled ? 0 : 1) +
                (serviceClass == null ? 0 : serviceClass.hashCode()) +
                (description == null ? 0 : description.hashCode()) +
                (parameters == null ? 0 : parameters.hashCode());
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || object.getClass() != getClass()) return false;

        ServiceConfig serviceConfig = (ServiceConfig)object;
        if (!equals(name, serviceConfig.name)) return false;
        if (enabled != serviceConfig.enabled) return false;
        if (!equals(serviceClass, serviceConfig.serviceClass)) return false;
        if (!equals(description, serviceConfig.description)) return false;
        if (!equals(parameters, serviceConfig.parameters)) return false;

        return true;
    }

    public void copy(ServiceConfig serviceConfig) {
        name = serviceConfig.name;
        enabled = serviceConfig.enabled;
        serviceClass = serviceConfig.serviceClass;
        description = serviceConfig.description;

        parameters.clear();
        parameters.putAll(serviceConfig.parameters);
    }

    public Object clone() {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.copy(this);
        return serviceConfig;
    }
}
