/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.activity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Representation of an Activity.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface Activity {

    Serializable getId();

    String getActor();

    void setActor(String actor);

    String getDisplayActor();

    void setDisplayActor(String displayActor);

    String getVerb();

    void setVerb(String verb);

    String getObject();

    void setObject(String object);

    String getDisplayObject();

    void setDisplayObject(String displayObject);

    String getTarget();

    void setTarget(String target);

    String getDisplayTarget();

    void setDisplayTarget(String displayTarget);

    /**
     * Returns the context of this {@code Activity}.
     *
     * @since 5.6
     */
    String getContext();

    /**
     * Sets the context of this {@code Activity}.
     *
     * @since 5.6
     */
    void setContext(String context);

    Date getPublishedDate();

    void setPublishedDate(Date publishedDate);

    /**
     * Returns the last updated date of this {@code Activity}.
     *
     * @since 5.6
     */
    Date getLastUpdatedDate();

    /**
     * Sets the last updated date of this {@code Activity}.
     *
     * @since 5.6
     */
    void setLastUpdatedDate(Date lastUpdatedDate);

    /**
     * Returns the comments of this {@code Activity}.
     * <p>
     * The comments are stored as a JSON string.
     *
     * @since 5.6
     */
    String getComments();

    /**
     * Sets the comments of this {@code Activity}.
     * <p>
     * The comments are stored as a JSON string.
     *
     * @since 5.6
     */
    void setComments(String comments);

    /**
     * Returns the list of {@link ActivityComment} of this {@code Activity}.
     *
     * @since 5.6
     */
    List<ActivityComment> getActivityComments();

    /**
     * Sets the comments of this {@code Activity}.
     *
     * @since 5.6
     */
    void setActivityComments(List<ActivityComment> activityComments);

    Map<String, String> toMap();

}
