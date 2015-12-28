/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * Descriptor object for registering a mapping between an Activity verb and a label key.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 * @deprecated since 5.6. See {@link ActivityVerb}.
 */
@Deprecated
@XObject("activityMessageLabel")
public class ActivityMessageLabelDescriptor {

    @XNode("@activityVerb")
    protected String activityVerb;

    @XNode("@labelKey")
    protected String labelKey;

    public ActivityMessageLabelDescriptor() {
    }

    public String getActivityVerb() {
        return activityVerb;
    }

    public String getLabelKey() {
        return labelKey;
    }

}
