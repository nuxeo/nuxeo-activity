/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PropertyException;

/**
 * Helper class to deal with activity objects.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivityHelper {

    public static final String SEPARATOR = ":";

    public static final String USER_PREFIX = "user" + SEPARATOR;

    public static final String DOC_PREFIX = "doc" + SEPARATOR;

    public static final String ACTIVITY_PREFIX = "activity" + SEPARATOR;

    private ActivityHelper() {
        // helper class
    }

    public static boolean isUser(String activityObject) {
        return activityObject != null && activityObject.startsWith(USER_PREFIX);
    }

    public static boolean isDocument(String activityObject) {
        return activityObject != null && activityObject.startsWith(DOC_PREFIX);
    }

    public static boolean isActivity(String activityObject) {
        return activityObject != null && activityObject.startsWith(ACTIVITY_PREFIX);
    }

    public static String getUsername(String activityObject) {
        if (!isUser(activityObject)) {
            throw new IllegalArgumentException(activityObject + " is not a user activity object");
        }
        return activityObject.replaceAll(USER_PREFIX, "");
    }

    public static List<String> getUsernames(List<String> activityObjects) {
        List<String> usernames = new ArrayList<String>();
        for (String activityObject : activityObjects) {
            usernames.add(getUsername(activityObject));
        }
        return usernames;
    }

    public static String getDocumentId(String activityObject) {
        if (isDocument(activityObject)) {
            String[] v = activityObject.split(":");
            return v[2];
        }
        return "";
    }

    public static String getRepositoryName(String activityObject) {
        if (isDocument(activityObject)) {
            String[] v = activityObject.split(":");
            return v[1];
        }
        return "";
    }

    public static String getActivityId(String activityObject) {
        if (isActivity(activityObject)) {
            String[] v = activityObject.split(":");
            return v[1];
        }
        return "";
    }

    public static String createDocumentActivityObject(DocumentModel doc) {
        return createDocumentActivityObject(doc.getRepositoryName(), doc.getId());
    }

    public static String createDocumentActivityObject(String repositoryName, String docId) {
        return DOC_PREFIX + repositoryName + SEPARATOR + docId;
    }

    public static String createUserActivityObject(Principal principal) {
        return createUserActivityObject(principal.getName());
    }

    public static String createUserActivityObject(String username) {
        return USER_PREFIX + username;
    }

    public static String createActivityObject(Activity activity) {
        return createActivityObject(activity.getId());
    }

    public static String createActivityObject(Serializable activityId) {
        return ACTIVITY_PREFIX + activityId;
    }

    public static String generateDisplayName(Principal principal) {
        if (principal instanceof NuxeoPrincipal) {
            NuxeoPrincipal nuxeoPrincipal = (NuxeoPrincipal) principal;
            if (!StringUtils.isBlank(nuxeoPrincipal.getFirstName())
                    || !StringUtils.isBlank(nuxeoPrincipal.getLastName())) {
                return nuxeoPrincipal.getFirstName() + " " + nuxeoPrincipal.getLastName();
            }
        }
        return principal.getName();
    }

    public static String getDocumentTitle(DocumentModel doc) {
        try {
            return doc.getTitle();
        } catch (PropertyException e) {
            return doc.getId();
        }
    }

}
