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

import static org.nuxeo.ecm.user.center.profile.UserProfileConstants.USER_PROFILE_AVATAR_FIELD;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.user.center.profile.UserProfileService;
import org.nuxeo.runtime.api.Framework;

/**
 * Default {@link ActivityLinkBuilder} computing URLs with the default id codec for documents and user codec for users.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class DefaultActivityLinkBuilder implements ActivityLinkBuilder {

    @Override
    public String getDocumentLink(String documentActivityObject, String displayValue) {
        documentActivityObject = StringEscapeUtils.escapeHtml(documentActivityObject);
        displayValue = StringEscapeUtils.escapeHtml(displayValue);
        String link = "<a href=\"%s\" target=\"_top\">%s</a>";
        return String.format(
                link,
                getDocumentURL(ActivityHelper.getRepositoryName(documentActivityObject),
                        ActivityHelper.getDocumentId(documentActivityObject)), displayValue);
    }

    protected String getDocumentURL(String repositoryName, String documentId) {
        DocumentLocation docLoc = new DocumentLocationImpl(repositoryName, new IdRef(documentId));
        DocumentView docView = new DocumentViewImpl(docLoc, "view_documents");
        URLPolicyService urlPolicyService = Framework.getLocalService(URLPolicyService.class);
        return urlPolicyService.getUrlFromDocumentView("id", docView, VirtualHostHelper.getContextPathProperty());
    }

    @Override
    public String getUserProfileLink(String userActivityObject, String displayValue) {
        userActivityObject = StringEscapeUtils.escapeHtml(userActivityObject);
        displayValue = StringEscapeUtils.escapeHtml(displayValue);
        String link = "<span class=\"username\"><a href=\"%s\" target=\"_top\" title=\"%s\">%s</a></span>";
        String username = ActivityHelper.getUsername(userActivityObject);
        return String.format(link, getUserProfileURL(username), username, displayValue);
    }

    protected String getUserProfileURL(String username) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        DocumentView docView = new DocumentViewImpl(null, null, params);
        URLPolicyService urlPolicyService = Framework.getLocalService(URLPolicyService.class);
        if (urlPolicyService == null) {
            return "";
        }
        return urlPolicyService.getUrlFromDocumentView("user", docView, VirtualHostHelper.getContextPathProperty());
    }

    @Override
    public String getUserAvatarURL(CoreSession session, String username) {
        UserProfileService userProfileService = Framework.getLocalService(UserProfileService.class);
        DocumentModel profile = userProfileService.getUserProfileDocument(username, session);
        Blob avatar = (Blob) profile.getPropertyValue(USER_PROFILE_AVATAR_FIELD);
        if (avatar != null) {
            DownloadService downloadService = Framework.getService(DownloadService.class);
            String filename = username + "." + FilenameUtils.getExtension(avatar.getFilename());
            return VirtualHostHelper.getContextPathProperty() + "/"
                    + downloadService.getDownloadUrl(profile, USER_PROFILE_AVATAR_FIELD, filename);
        } else {
            return VirtualHostHelper.getContextPathProperty() + "/icons/missing_avatar.png";
        }
    }
}
