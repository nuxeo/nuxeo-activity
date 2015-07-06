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

import static org.nuxeo.ecm.activity.ActivityHelper.getUsername;
import static org.nuxeo.ecm.activity.ActivityHelper.isUser;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class ActivityReplyMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String activityReplyId;

    private final String actor;

    private final String displayActor;

    private final String displayActorLink;

    private final String message;

    private final long publishedDate;

    public ActivityReplyMessage(String activityReplyId, String actor, String displayActor, String displayActorLink,
            String message, long publishedDate) {
        this.activityReplyId = activityReplyId;
        this.actor = actor;
        this.displayActor = displayActor;
        this.displayActorLink = displayActorLink;
        this.message = message;
        this.publishedDate = publishedDate;
    }

    public String getActivityReplyId() {
        return activityReplyId;
    }

    public String getActor() {
        return actor;
    }

    public String getDisplayActor() {
        return displayActor;
    }

    public String getDisplayActorLink() {
        return displayActorLink;
    }

    public String getMessage() {
        return message;
    }

    public long getPublishedDate() {
        return publishedDate;
    }

    public Map<String, Object> toMap(CoreSession session, Locale locale) {
        return toMap(session, locale, null);
    }

    public Map<String, Object> toMap(CoreSession session, Locale locale, String activityLinkBuilderName)
            {
        ActivityLinkBuilder activityLinkBuilder = Framework.getLocalService(ActivityStreamService.class).getActivityLinkBuilder(
                activityLinkBuilderName);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        Map<String, Object> o = new HashMap<String, Object>();
        o.put("id", getActivityReplyId());
        o.put("actor", getActor());
        o.put("displayActor", getDisplayActor());
        o.put("displayActorLink", getDisplayActorLink());
        if (isUser(getActor())) {
            String actorUsername = getUsername(getActor());
            o.put("actorAvatarURL", activityLinkBuilder.getUserAvatarURL(session, actorUsername));
        }
        o.put("message", getMessage());
        o.put("publishedDate", dateFormat.format(new Date(getPublishedDate())));
        return o;
    }
}
