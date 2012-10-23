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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@RunWith(FeaturesRunner.class)
@Features(ActivityFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.platform.userworkspace.types",
        "org.nuxeo.ecm.platform.userworkspace.api",
        "org.nuxeo.ecm.platform.userworkspace.core",
        "org.nuxeo.ecm.user.center.profile" })
public class TestActivityLinkBuilder {

    @Inject
    protected ActivityStreamService activityStreamService;

    @Test
    public void shouldHaveADefaultActivityLinkBuilder() {
        ActivityLinkBuilder activityLinkBuilder = activityStreamService.getActivityLinkBuilder(null);
        assertNotNull(activityLinkBuilder);
    }

    @Test
    public void shouldRewriteLinkInActivityMessage() throws ClientException {
        Activity activity = new ActivityImpl();
        activity.setActor(ActivityHelper.createUserActivityObject("bender"));
        activity.setVerb("test");
        activity.setObject(ActivityHelper.createDocumentActivityObject(
                "server", "docId"));
        activity.setPublishedDate(new Date());

        ActivityMessage activityMessage = activityStreamService.toActivityMessage(
                activity, Locale.ENGLISH, "dummy");
        assertEquals("userProfileLink", activityMessage.getDisplayActorLink());

        ActivityLinkBuilder dummyActivityLinkBuilder = activityStreamService.getActivityLinkBuilder("dummy");
        assertEquals("documentLink",
                dummyActivityLinkBuilder.getDocumentLink("server", "docId"));
        assertEquals("userAvatarURL",
                dummyActivityLinkBuilder.getUserAvatarURL(null, "bender"));
    }

}
