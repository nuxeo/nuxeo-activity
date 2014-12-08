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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityReply;
import org.nuxeo.ecm.activity.ActivityFeature;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@RunWith(FeaturesRunner.class)
@Features(ActivityFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.platform.userworkspace.types",
        "org.nuxeo.ecm.platform.userworkspace.api", "org.nuxeo.ecm.platform.userworkspace.core",
        "org.nuxeo.ecm.user.center.profile" })
public class TestActivityOperations {

    @Inject
    protected CoreSession session;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected AutomationService automationService;

    protected int getOffset() {
        return activityStreamService.query(ActivityStreamService.ALL_ACTIVITIES, null).size();
    }

    @Test
    @Ignore
    public void shouldAddAnActivityReply() throws Exception {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        String replyMessage = "First reply";

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testActivityOperation");
        chain.add(AddActivityReply.ID).set("activityId", String.valueOf(activity.getId())).set("message", replyMessage);
        Blob result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        List<Activity> activities = activityStreamService.query(ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        activity = activities.get(0);
        List<ActivityReply> replies = activity.getActivityReplies();
        assertFalse(replies.isEmpty());
        assertEquals(1, replies.size());
        ActivityReply reply = replies.get(0);
        assertEquals(activity.getId() + "-reply-1", reply.getId());
        assertEquals("user:Administrator", reply.getActor());
        assertEquals("Administrator", reply.getDisplayActor());
        assertNotNull(reply.getPublishedDate());
        assertEquals("First reply", reply.getMessage());
    }

    @Test
    public void shouldRemoveAnActivityReply() throws Exception {
        int offset = getOffset();

        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        long firstReplyPublishedDate = new Date().getTime();
        ActivityReply firstReply = new ActivityReply("bender", "Bender", "First reply", firstReplyPublishedDate);
        firstReply = activityStreamService.addActivityReply(activity.getId(), firstReply);
        long secondReplyPublishedDate = new Date().getTime();
        ActivityReply secondReply = new ActivityReply("bender", "Bender", "Second reply", secondReplyPublishedDate);
        secondReply = activityStreamService.addActivityReply(activity.getId(), secondReply);

        List<Activity> activities = activityStreamService.query(ActivityStreamService.ALL_ACTIVITIES, null, offset, 999);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        activity = activities.get(0);
        List<ActivityReply> replies = activity.getActivityReplies();
        assertFalse(replies.isEmpty());
        assertEquals(2, replies.size());

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testActivityOperation");
        chain.add(RemoveActivityReply.ID).set("activityId", String.valueOf(activity.getId())).set("replyId",
                secondReply.getId());
        automationService.run(ctx, chain);

        activities = activityStreamService.query(ActivityStreamService.ALL_ACTIVITIES, null, offset, 999);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        activity = activities.get(0);
        replies = activity.getActivityReplies();
        assertFalse(replies.isEmpty());
        assertEquals(1, replies.size());

        ActivityReply reply = replies.get(0);
        assertEquals(activity.getId() + "-reply-1", reply.getId());
        assertEquals("bender", reply.getActor());
        assertEquals("Bender", reply.getDisplayActor());
        assertNotNull(reply.getPublishedDate());
        assertEquals("First reply", reply.getMessage());
    }

}
