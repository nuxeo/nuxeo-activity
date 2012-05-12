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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.user.center.profile.UserProfileService;
import org.nuxeo.runtime.api.Framework;

/**
 * Helper class to compute links for {@link ActivityMessage} content from an
 * activity attributes.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class ActivityMessageHelper {

    private ActivityMessageHelper() {
        // helper class
    }

    public static String getDocumentLink(String documentActivityObject,
            String displayValue) {
        documentActivityObject = StringEscapeUtils.escapeHtml(documentActivityObject);
        displayValue = StringEscapeUtils.escapeHtml(displayValue);
        String link = "<a href=\"%s\" target=\"_top\">%s</a>";
        return String.format(
                link,
                getDocumentURL(
                        ActivityHelper.getRepositoryName(documentActivityObject),
                        ActivityHelper.getDocumentId(documentActivityObject)),
                displayValue);
    }

    public static String getDocumentURL(String repositoryName, String documentId) {
        DocumentLocation docLoc = new DocumentLocationImpl(repositoryName,
                new IdRef(documentId));
        DocumentView docView = new DocumentViewImpl(docLoc, "view_documents");
        URLPolicyService urlPolicyService = Framework.getLocalService(URLPolicyService.class);
        return VirtualHostHelper.getContextPathProperty() + "/"
                + urlPolicyService.getUrlFromDocumentView("id", docView, null);
    }

    public static String getUserProfileLink(String userActivityObject,
            String displayValue) {
        userActivityObject = StringEscapeUtils.escapeHtml(userActivityObject);
        displayValue = StringEscapeUtils.escapeHtml(displayValue);
        String link = "<a href=\"%s\" target=\"_top\" title=\"%s\">%s</a>";
        String username = ActivityHelper.getUsername(userActivityObject);
        return String.format(link, getUserProfileURL(username), username,
                displayValue);
    }

    public static String getUserProfileURL(String username) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        DocumentView docView = new DocumentViewImpl(null, null, params);
        URLPolicyService urlPolicyService = Framework.getLocalService(URLPolicyService.class);
        return VirtualHostHelper.getContextPathProperty()
                + "/"
                + urlPolicyService.getUrlFromDocumentView("user", docView, null);
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

    public static Pattern HTTP_URL_PATTERN = Pattern.compile("\\b(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");

    public static String replaceURLsByLinks(String message) {
        String escapedMessage = StringEscapeUtils.escapeHtml(message);
        Matcher m = HTTP_URL_PATTERN.matcher(escapedMessage);
        StringBuffer sb = new StringBuffer(escapedMessage.length());
        while (m.find()) {
            String url = m.group(1);
            m.appendReplacement(sb, computeLinkFor(url));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String computeLinkFor(String url) {
        return "<a href=\"" + url + "\" target=\"_top\">" + url + "</a>";
    }
}
