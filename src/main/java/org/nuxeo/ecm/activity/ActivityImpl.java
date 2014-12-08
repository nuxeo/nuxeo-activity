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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * Default implementation of {@link Activity}.
 * 
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@Entity(name = "Activity")
@Table(name = "nxp_activities")
public class ActivityImpl implements Activity {

    private static final Log log = LogFactory.getLog(ActivityImpl.class);

    private Long id;

    private String actor;

    private String displayActor;

    private String verb;

    private String object;

    private String displayObject;

    private String target;

    private String displayTarget;

    private String context;

    private Date publishedDate;

    private Date lastUpdatedDate;

    private String replies;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, columnDefinition = "integer")
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column
    @Override
    public String getActor() {
        return actor;
    }

    @Override
    public void setActor(String actor) {
        this.actor = actor;
    }

    @Column
    @Override
    public String getDisplayActor() {
        return displayActor;
    }

    @Override
    public void setDisplayActor(String displayActor) {
        this.displayActor = displayActor;
    }

    @Column
    @Override
    public String getVerb() {
        return verb;
    }

    @Override
    public void setVerb(String verb) {
        this.verb = verb;
    }

    @Column
    @Override
    public String getObject() {
        return object;
    }

    @Override
    public void setObject(String object) {
        this.object = object;
    }

    @Column
    @Override
    public String getDisplayObject() {
        return displayObject;
    }

    @Override
    public void setDisplayObject(String displayObject) {
        this.displayObject = displayObject;
    }

    @Column
    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setTarget(String target) {
        this.target = target;
    }

    @Column
    @Override
    public String getDisplayTarget() {
        return displayTarget;
    }

    @Override
    public void setDisplayTarget(String displayTarget) {
        this.displayTarget = displayTarget;
    }

    @Column
    @Override
    public String getContext() {
        return context;
    }

    @Override
    public void setContext(String context) {
        this.context = context;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @Override
    public Date getPublishedDate() {
        return publishedDate;
    }

    @Override
    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    @Override
    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    @Override
    public void setLastUpdatedDate(Date lastUpdated) {
        this.lastUpdatedDate = lastUpdated;
    }

    @Column
    @Lob
    @Override
    public String getReplies() {
        return replies;
    }

    @Override
    public void setReplies(String replies) {
        this.replies = replies;
    }

    @Transient
    @Override
    public List<ActivityReply> getActivityReplies() {
        if (replies == null) {
            return new ArrayList<ActivityReply>();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(replies, new TypeReference<List<ActivityReply>>() {
            });
        } catch (Exception e) {
            log.warn(String.format("Unable to convert replies to ActivityReply: %s", e.getMessage()));
            log.debug(e, e);
            return new ArrayList<ActivityReply>();
        }
    }

    @Override
    public void setActivityReplies(List<ActivityReply> activityReplies) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, activityReplies);
            replies = writer.toString();
        } catch (Exception e) {
            log.warn(String.format("Unable to convert replies to ActivityReply: %s", e.getMessage()));
            log.debug(e, e);
        }
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("id", String.valueOf(id));
        m.put("actor", actor);
        m.put("displayActor", displayActor);
        m.put("object", object);
        m.put("displayObject", displayObject);
        m.put("target", target);
        m.put("displayTarget", displayTarget);
        m.put("verb", verb);
        m.put("context", context);
        m.put("publishedDate", publishedDate.toString());
        m.put("lastUpdatedDate", lastUpdatedDate != null ? lastUpdatedDate.toString() : null);
        m.put("replies", replies);
        return Collections.unmodifiableMap(m);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
