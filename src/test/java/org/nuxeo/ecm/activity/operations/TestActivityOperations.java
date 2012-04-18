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
import org.nuxeo.ecm.activity.ActivityComment;
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
import org.nuxeo.ecm.core.test.annotations.BackendType;
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
@Deploy({ "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.platform.userworkspace.types",
        "org.nuxeo.ecm.platform.userworkspace.api",
        "org.nuxeo.ecm.platform.userworkspace.core",
        "org.nuxeo.ecm.user.center.profile" })
public class TestActivityOperations {

    @Inject
    protected CoreSession session;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected AutomationService automationService;

    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    @Override
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                        query = em.createQuery("delete from Tweet");
                        query.executeUpdate();
                    }
                });
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();
    }

    @Ignore
    @Test
    public void shouldAddAnActivityComment() throws Exception {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        String commentMessage = "First comment";

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testActivityOperation");
        chain.add(AddActivityComment.ID).set("activityId",
                String.valueOf(activity.getId())).set("message", commentMessage);
        Blob result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        activity = activities.get(0);
        List<ActivityComment> comments = activity.getActivityComments();
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        ActivityComment comment = comments.get(0);
        assertEquals(activity.getId() + "-comment-1", comment.getId());
        assertEquals("user:Administrator", comment.getActor());
        assertEquals("Administrator", comment.getDisplayActor());
        assertNotNull(comment.getPublishedDate());
        assertEquals("First comment", comment.getMessage());
    }

    @Ignore
    @Test
    public void shouldRemoveAnActivityComment() throws Exception {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        long firstCommentPublishedDate = new Date().getTime();
        ActivityComment firstComment = new ActivityComment("bender", "Bender",
                "First comment", firstCommentPublishedDate);
        firstComment = activityStreamService.addActivityComment(
                activity.getId(), firstComment);
        long secondCommentPublishedDate = new Date().getTime();
        ActivityComment secondComment = new ActivityComment("bender", "Bender",
                "Second comment", secondCommentPublishedDate);
        secondComment = activityStreamService.addActivityComment(
                activity.getId(), secondComment);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        activity = activities.get(0);
        List<ActivityComment> comments = activity.getActivityComments();
        assertFalse(comments.isEmpty());
        assertEquals(2, comments.size());

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testActivityOperation");
        chain.add(RemoveActivityComment.ID).set("activityId",
                String.valueOf(activity.getId())).set("commentId",
                secondComment.getId());
        automationService.run(ctx, chain);

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        activity = activities.get(0);
        comments = activity.getActivityComments();
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());

        ActivityComment comment = comments.get(0);
        assertEquals(activity.getId() + "-comment-1", comment.getId());
        assertEquals("bender", comment.getActor());
        assertEquals("Bender", comment.getDisplayActor());
        assertNotNull(comment.getPublishedDate());
        assertEquals("First comment", comment.getMessage());
    }

}
