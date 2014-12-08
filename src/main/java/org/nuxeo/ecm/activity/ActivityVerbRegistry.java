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

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.runtime.model.ContributionFragmentRegistry;

/**
 * Registry for activity verbs, handling merge of registered {@link org.nuxeo.ecm.activity.ActivityVerb} elements.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class ActivityVerbRegistry extends ContributionFragmentRegistry<ActivityVerb> {

    protected Map<String, ActivityVerb> activityVerbs = new HashMap<String, ActivityVerb>();

    public ActivityVerb get(String name) {
        return activityVerbs.get(name);
    }

    @Override
    public String getContributionId(ActivityVerb contrib) {
        return contrib.getVerb();
    }

    @Override
    public void contributionUpdated(String id, ActivityVerb contrib, ActivityVerb newOrigContrib) {
        activityVerbs.put(id, contrib);
    }

    @Override
    public void contributionRemoved(String id, ActivityVerb origContrib) {
        activityVerbs.remove(id);
    }

    @Override
    public ActivityVerb clone(ActivityVerb orig) {
        return orig.clone();
    }

    @Override
    public void merge(ActivityVerb src, ActivityVerb dst) {
        String labelKey = src.getLabelKey();
        if (labelKey != null) {
            dst.setLabelKey(labelKey);
        }
        String icon = src.getIcon();
        if (icon != null) {
            dst.setIcon(icon);
        }
    }
}
