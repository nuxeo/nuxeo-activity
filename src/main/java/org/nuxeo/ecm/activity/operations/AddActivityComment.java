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

import static org.nuxeo.ecm.activity.ActivityHelper.getUserProfileLink;
import static org.nuxeo.ecm.user.center.profile.UserProfileConstants.USER_PROFILE_AVATAR_FIELD;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.activity.ActivityComment;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.user.center.profile.UserProfileConstants;
import org.nuxeo.ecm.user.center.profile.UserProfileService;
import org.nuxeo.runtime.api.Framework;

/**
 * Operation to add an activity comment.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Operation(id = AddActivityComment.ID, category = Constants.CAT_SERVICES, label = "Add a comment to an existing activity", description = "Add a comment to an existing activity.")
public class AddActivityComment {

    public static final String ID = "Services.AddActivityComment";

    private static final Log log = LogFactory.getLog(AddActivityComment.class);

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

    @OperationMethod
    public Blob run() throws Exception {
        String actor = ActivityHelper.createUserActivityObject(session.getPrincipal());
        String displayActor = ActivityHelper.generateDisplayName(session.getPrincipal());
        ActivityComment comment = new ActivityComment(actor, displayActor,
                message, new Date().getTime());
        comment = activityStreamService.addActivityComment(
                Long.valueOf(activityId), comment);

        Locale locale = language != null && !language.isEmpty() ? new Locale(
                language) : Locale.ENGLISH;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("id", comment.getId());
        m.put("actor", comment.getActor());
        m.put("displayActor", comment.getDisplayActor());
        m.put("displayActorLink",
                getDisplayActorLink(comment.getActor(),
                        comment.getDisplayActor()));
        m.put("actorAvatarURL",
                getUserAvatarURL(session,
                        ActivityHelper.getUsername(comment.getActor())));
        m.put("message",
                ActivityHelper.replaceURLsByLinks(comment.getMessage()));
        m.put("publishedDate",
                dateFormat.format(new Date(comment.getPublishedDate())));
        m.put("allowDeletion",
                session.getPrincipal().getName().equals(comment.getActor()));

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(
                writer.toString().getBytes("UTF-8")), "application/json");
    }

    protected String getDisplayActorLink(String actor, String displayActor) {
        try {
            return getUserProfileLink(actor, displayActor);
        } catch (Exception e) {
            log.warn(String.format(
                    "Unable to get user profile link for '%s': %s", actor,
                    e.getMessage()));
            log.debug(e, e);
        }
        return "";
    }

    public static String getUserAvatarURL(CoreSession session, String username)
            throws ClientException {
        UserProfileService userProfileService = Framework.getLocalService(UserProfileService.class);
        DocumentModel profile = userProfileService.getUserProfileDocument(
                username, session);
        Blob avatar = (Blob) profile.getPropertyValue(USER_PROFILE_AVATAR_FIELD);
        if (avatar != null) {
            String bigDownloadURL = VirtualHostHelper.getContextPathProperty()
                    + "/";
            bigDownloadURL += "nxbigfile" + "/";
            bigDownloadURL += profile.getRepositoryName() + "/";
            bigDownloadURL += profile.getRef().toString() + "/";
            bigDownloadURL += USER_PROFILE_AVATAR_FIELD + "/";
            String filename = username + "."
                    + FilenameUtils.getExtension(avatar.getFilename());
            bigDownloadURL += URIUtils.quoteURIPathComponent(filename, true);
            return bigDownloadURL;
        } else {
            return VirtualHostHelper.getContextPathProperty()
                    + "/icons/missing_avatar.png";
        }
    }

}
