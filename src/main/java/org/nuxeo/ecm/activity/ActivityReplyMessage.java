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

import java.io.Serializable;


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

    private final String publishedDate;

    public ActivityReplyMessage(String activityReplyId, String actor,
            String displayActor, String displayActorLink, String message,
            String publishedDate) {
        this.activityReplyId = activityReplyId;
        this.actor = actor;
        this.displayActor = displayActor;
        this.displayActorLink = displayActorLink;
        this.message = message;
        this.publishedDate = publishedDate;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public String getPublishedDate() {
        return publishedDate;
    }
}
