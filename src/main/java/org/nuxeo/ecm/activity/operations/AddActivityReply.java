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

package org.nuxeo.ecm.activity.operations;

import static org.nuxeo.ecm.activity.ActivityHelper.getUsername;
import static org.nuxeo.ecm.activity.ActivityMessageHelper.replaceURLsByLinks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityLinkBuilder;
import org.nuxeo.ecm.activity.ActivityReply;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.runtime.api.Framework;

/**
 * Operation to add an activity reply.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Operation(id = AddActivityReply.ID, category = Constants.CAT_SERVICES, label = "Add a reply to an existing activity", description = "Add a reply to an existing activity.")
public class AddActivityReply {

    public static final String ID = "Services.AddActivityReply";

    private static final Log log = LogFactory.getLog(AddActivityReply.class);

    @Context
    protected CoreSession session;

    @Context
    protected ActivityStreamService activityStreamService;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "activityId", required = true)
    protected String activityId;

    @Param(name = "message", required = true)
    protected String message;

    @Param(name = "activityLinkBuilderName", required = true)
    protected String activityLinkBuilderName;

    @OperationMethod
    public Blob run() throws IOException {
        String actor = ActivityHelper.createUserActivityObject(session.getPrincipal());
        String displayActor = ActivityHelper.generateDisplayName(session.getPrincipal());
        ActivityReply reply = new ActivityReply(actor, displayActor, message, new Date().getTime());
        reply = activityStreamService.addActivityReply(Long.valueOf(activityId), reply);

        Locale locale = language != null && !language.isEmpty() ? new Locale(language) : Locale.ENGLISH;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        ActivityLinkBuilder activityLinkBuilder = Framework.getLocalService(ActivityStreamService.class).getActivityLinkBuilder(
                activityLinkBuilderName);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("id", reply.getId());
        m.put("actor", reply.getActor());
        m.put("displayActor", reply.getDisplayActor());
        m.put("displayActorLink", getDisplayActorLink(reply.getActor(), reply.getDisplayActor(), activityLinkBuilder));
        m.put("actorAvatarURL", activityLinkBuilder.getUserAvatarURL(session, getUsername(reply.getActor())));
        m.put("message", replaceURLsByLinks(reply.getMessage()));
        m.put("publishedDate", dateFormat.format(new Date(reply.getPublishedDate())));
        String username = ActivityHelper.getUsername(reply.getActor());
        m.put("allowDeletion", session.getPrincipal().getName().equals(username));

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), "application/json");
    }

    protected String getDisplayActorLink(String actor, String displayActor, ActivityLinkBuilder activityLinkBuilder) {
        return activityLinkBuilder.getUserProfileLink(actor, displayActor);
    }

}
