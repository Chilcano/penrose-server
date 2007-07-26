package org.safehaus.penrose.service;

import org.safehaus.penrose.Penrose;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import java.io.Writer;
import java.io.FileWriter;
import java.io.File;

/**
 * @author Endi Sukma Dewata
 */
public class ServiceWriter {

    public void write(String filename, ServiceConfig serviceConfig) throws Exception {

        File file = new File(filename);
        file.getParentFile().mkdirs();

        Writer writer = new FileWriter(file);

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);

        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.startDocument();

        xmlWriter.startDTD(
                "service",
                "-//Penrose/DTD Service "+ Penrose.SPECIFICATION_VERSION+"//EN",
                "http://penrose.safehaus.org/dtd/service.dtd"
        );

        xmlWriter.write(createElement(serviceConfig));
        xmlWriter.close();

        writer.close();
    }

    public Element createElement(ServiceConfig serviceConfig) {

        Element element = new DefaultElement("service");
        if (!serviceConfig.isEnabled()) element.addAttribute("enabled", "false");

        Element serviceName = new DefaultElement("service-name");
        serviceName.add(new DefaultText(serviceConfig.getName()));
        element.add(serviceName);

        Element serviceClass = new DefaultElement("service-class");
        serviceClass.add(new DefaultText(serviceConfig.getServiceClass()));
        element.add(serviceClass);

        if (serviceConfig.getDescription() != null && !"".equals(serviceConfig.getDescription())) {
            Element description = new DefaultElement("description");
            description.add(new DefaultText(serviceConfig.getDescription()));
            element.add(description);
        }

        for (String name : serviceConfig.getParameterNames()) {
            String value = serviceConfig.getParameter(name);

            Element parameter = new DefaultElement("parameter");

            Element paramName = new DefaultElement("param-name");
            paramName.add(new DefaultText(name));
            parameter.add(paramName);

            Element paramValue = new DefaultElement("param-value");
            paramValue.add(new DefaultText(value));
            parameter.add(paramValue);

            element.add(parameter);
        }

        return element;
    }
}
