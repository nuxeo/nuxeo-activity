/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger (troger@nuxeo.com)
 */

package org.nuxeo.ecm.activity;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * Descriptor object for registering {@link org.nuxeo.ecm.activity.ActivityUpgrader}s.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.7
 */
@XObject("activityUpgrader")
public class ActivityUpgraderDescriptor {

    @XNode("@enabled")
    protected boolean enabled = true;

    @XNode("@name")
    protected String name;

    @XNode("@order")
    protected int order = 0;

    @XNode("@class")
    protected Class<? extends ActivityUpgrader> activityUpgraderClass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ActivityUpgrader getActivityUpgrader() {
        try {
            ActivityUpgrader upgrader = activityUpgraderClass.newInstance();
            upgrader.setName(name);
            upgrader.setOrder(order);
            return upgrader;
        } catch (ReflectiveOperationException e) {
            throw new NuxeoException(e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Class<? extends ActivityUpgrader> getActivityUpgraderClass() {
        return activityUpgraderClass;
    }

    public void setActivityUpgraderClass(Class<? extends ActivityUpgrader> activityUpgraderClass) {
        this.activityUpgraderClass = activityUpgraderClass;
    }

    @Override
    public ActivityUpgraderDescriptor clone() {
        ActivityUpgraderDescriptor clone = new ActivityUpgraderDescriptor();
        clone.setName(name);
        clone.setOrder(order);
        clone.setActivityUpgraderClass(activityUpgraderClass);
        clone.setEnabled(enabled);
        return clone;
    }

}
