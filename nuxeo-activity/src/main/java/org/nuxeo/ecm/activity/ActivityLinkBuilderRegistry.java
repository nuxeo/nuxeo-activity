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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.runtime.model.ContributionFragmentRegistry;

/**
 * Registry for activity link builders, handling merge of registered
 * {@link ActivityLinkBuilderDescriptor} elements.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class ActivityLinkBuilderRegistry extends
        ContributionFragmentRegistry<ActivityLinkBuilderDescriptor> {

    protected Map<String, ActivityLinkBuilder> activityLinkBuilders = new HashMap<String, ActivityLinkBuilder>();

    protected List<String> activityLinkBuildersIds = new ArrayList<String>();

    public ActivityLinkBuilder getDefaultActivityLinkBuilder() {
        if (activityLinkBuildersIds.isEmpty()) {
            throw new IllegalStateException(
                    "No default ActivityLinkBuilder configured");
        }
        return activityLinkBuilders.get(activityLinkBuildersIds.get(0));
    }

    public ActivityLinkBuilder get(String name) {
        return activityLinkBuilders.get(name);
    }

    @Override
    public String getContributionId(ActivityLinkBuilderDescriptor contrib) {
        return contrib.getName();
    }

    @Override
    public void contributionUpdated(String id,
            ActivityLinkBuilderDescriptor contrib,
            ActivityLinkBuilderDescriptor newOrigContrib) {
        activityLinkBuilders.put(id, contrib.getActivityLinkBuilder());
        if (contrib.isDefault()) {
            activityLinkBuildersIds.add(0, id);
        }
    }

    @Override
    public void contributionRemoved(String id,
            ActivityLinkBuilderDescriptor origContrib) {
        activityLinkBuilders.remove(id);
        activityLinkBuildersIds.remove(id);
    }

    @Override
    public ActivityLinkBuilderDescriptor clone(
            ActivityLinkBuilderDescriptor orig) {
        return orig.clone();
    }

    @Override
    public void merge(ActivityLinkBuilderDescriptor src,
            ActivityLinkBuilderDescriptor dst) {
        Class<? extends ActivityLinkBuilder> clazz = src.getActivityLinkBuilderClass();
        if (clazz != null) {
            dst.setActivityLinkBuilderClass(clazz);
        }
        boolean isDefault = src.isDefault();
        if (isDefault != dst.isDefault()) {
            dst.setDefault(isDefault);
        }
        boolean enabled = src.isEnabled();
        if (enabled != dst.isEnabled()) {
            dst.setEnabled(enabled);
        }
    }

}
