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

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * An {@code ActivityVerb} stores the configuration for a given activity verb:
 * <ul>
 * <li>a label key</li>
 * <li>an icon path</li>
 * </ul>
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@XObject("activityVerb")
public class ActivityVerb {

    @XNode("@verb")
    protected String verb;

    @XNode("@labelKey")
    protected String labelKey;

    @XNode("@icon")
    protected String icon;

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public ActivityVerb clone() {
        ActivityVerb clone = new ActivityVerb();
        clone.setVerb(getVerb());
        clone.setLabelKey(getLabelKey());
        clone.setIcon(getIcon());
        return clone;
    }

}
