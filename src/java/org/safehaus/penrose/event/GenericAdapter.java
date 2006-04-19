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
package org.safehaus.penrose.event;

import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class GenericAdapter implements BindListener, AddListener, ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public GenericAdapter() {
    	log.debug("GenericAdapter.<init>()");
    }

    public void beforeBind(BindEvent e) throws Exception {
    	log.debug("GenericAdapter.beforeBind()");
    }

    public void afterBind(BindEvent e) throws Exception {
    	log.debug("GenericAdapter.afterBind()");
    }

    public void beforeUnbind(BindEvent e) throws Exception {
    	log.debug("GenericAdapter.beforeUnbind()");
    }

    public void afterUnbind(BindEvent e) throws Exception {
    	log.debug("GenericAdapter.afterUnbind()");
    }

    public void beforeAdd(AddEvent event) throws Exception {
    	log.debug("GenericAdapter.beforeAdd()");
    }

    public void afterAdd(AddEvent event) throws Exception {
    	log.debug("GenericAdapter.afterAdd()");
    }

    public void beforeModify(ModifyEvent event) throws Exception {
    	log.debug("GenericAdapter.beforeModify()");
    }

    public void afterModify(ModifyEvent event) throws Exception {
    	log.debug("GenericAdapter.afterModify()");
    }
}