/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.api.ClientRuntimeException;

/**
 * Descriptor object for registering {@link ActivityLinkBuilder}s.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@XObject("activityLinkBuilder")
public class ActivityLinkBuilderDescriptor {

    @XNode("@name")
    protected String name;

    @XNode("@class")
    protected Class<? extends ActivityLinkBuilder> activityLinkBuilderClass;

    @XNode("@default")
    protected boolean isDefault = false;

    @XNode("@enabled")
    protected boolean enabled = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends ActivityLinkBuilder> getActivityLinkBuilderClass() {
        return activityLinkBuilderClass;
    }

    public void setActivityLinkBuilderClass(Class<? extends ActivityLinkBuilder> activityLinkBuilderClass) {
        this.activityLinkBuilderClass = activityLinkBuilderClass;
    }

    public ActivityLinkBuilder getActivityLinkBuilder() {
        try {
            return activityLinkBuilderClass.newInstance();
        } catch (Exception e) {
            throw new ClientRuntimeException(e);
        }
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ActivityLinkBuilderDescriptor clone() {
        ActivityLinkBuilderDescriptor clone = new ActivityLinkBuilderDescriptor();
        clone.setName(name);
        clone.setActivityLinkBuilderClass(activityLinkBuilderClass);
        clone.setDefault(isDefault);
        clone.setEnabled(enabled);
        return clone;
    }

}
