package org.safehaus.penrose.ldap;

import org.safehaus.penrose.control.Control;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.util.Formatter;
import org.safehaus.penrose.entry.SourceValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class SearchResult {

    public Logger log = LoggerFactory.getLogger(getClass());

    private EntryMapping entryMapping;

    private DN dn;
    private Attributes attributes = new Attributes();

    private SourceValues sourceValues = new SourceValues();

    private Collection<Control> controls = new ArrayList<Control>();

    public SearchResult() {
    }

    public SearchResult(String dn, Attributes attributes) {
        this.dn = new DN(dn);
        this.attributes.add(attributes);
    }

    public SearchResult(DN dn, Attributes attributes) {
        this.dn = dn;
        this.attributes.add(attributes);
    }

    public SearchResult(DN dn, Attributes attributes, Collection<Control> controls) {
        this.dn = dn;
        this.attributes.add(attributes);
        this.controls.addAll(controls);
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        if (this.attributes == attributes) return;
        this.attributes.clear();
        this.attributes.add(attributes);
    }

    public Collection<Control> getControls() {
        return controls;
    }

    public void setControls(Collection<Control> controls) {
        if (this.controls == controls) return;
        this.controls.clear();
        if (controls != null) this.controls.addAll(controls);
    }

    public EntryMapping getEntryMapping() {
        return entryMapping;
    }

    public void setEntryMapping(EntryMapping entryMapping) {
        this.entryMapping = entryMapping;
    }

    public SourceValues getSourceValues() {
        return sourceValues;
    }
    
    public void addSourceValues(SourceValues sourceValues) {
        this.sourceValues.add(sourceValues);
    }

    public void setSourceValues(SourceValues sourceValues) {
        this.sourceValues.set(sourceValues);
    }

    public void setSourceValues(String sourceName, Attributes attributes) {
        sourceValues.set(sourceName, attributes);
    }

    public void print() throws Exception {

        log.debug(Formatter.displaySeparator(80));
        log.debug(Formatter.displayLine("Search Result: "+dn, 80));

        for (Attribute attribute : attributes.getAll()) {
            String name = attribute.getName();

            for (Object value : attribute.getValues()) {
                String className = value.getClass().getName();
                className = className.substring(className.lastIndexOf(".") + 1);

                log.debug(Formatter.displayLine(" - " + name + ": " + value + " (" + className + ")", 80));
            }
        }

        for (String sourceName : sourceValues.getNames()) {
            Attributes attrs = sourceValues.get(sourceName);

            for (Attribute attribute : attrs.getAll()) {
                String fieldName = sourceName + "." + attribute.getName();

                for (Object value : attribute.getValues()) {
                    String className = value.getClass().getName();
                    className = className.substring(className.lastIndexOf(".") + 1);

                    log.debug(Formatter.displayLine(" - " + fieldName + ": " + value + " (" + className + ")", 80));
                }
            }
        }

        log.debug(Formatter.displaySeparator(80));
    }
}