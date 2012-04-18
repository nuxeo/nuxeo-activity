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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
@RunWith(FeaturesRunner.class)
@Features(ActivityFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
public class TestActivityStreamService {

    @Inject
    protected ActivityStreamService activityStreamService;

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

    @Test
    public void serviceRegistration() {
        assertNotNull(activityStreamService);
    }

    @Test
    public void shouldStoreAnActivity() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());
    }

    @Test
    public void shouldCallRegisteredActivityStreamFilter() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        Map<String, ActivityStreamFilter> filters = ((ActivityStreamServiceImpl) activityStreamService).activityStreamFilters;
        assertEquals(2, filters.size());

        List<Activity> activities = activityStreamService.query(
                DummyActivityStreamFilter.ID, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        activities = filters.get(DummyActivityStreamFilter.ID).query(null,
                null, 0, 0);
        assertEquals(1, activities.size());

        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());
    }

    @Test(expected = ClientRuntimeException.class)
    public void shouldThrowExceptionIfFilterIsNotRegistered() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        activityStreamService.query("nonExistingFilter", null);
    }

    @Test
    public void shouldHandlePagination() {
        addTestActivities(10);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 0, 5);
        assertEquals(5, activities.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("activity" + i, activities.get(i).getObject());
        }

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 5, 5);
        assertEquals(5, activities.size());
        for (int i = 5; i < 10; i++) {
            assertEquals("activity" + i, activities.get(i - 5).getObject());
        }

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 0, 15);
        assertEquals(10, activities.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("activity" + i, activities.get(i).getObject());
        }

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 15, 5);
        assertEquals(0, activities.size());
    }

    protected void addTestActivities(int activitiesCount) {
        for (int i = 0; i < activitiesCount; i++) {
            Activity activity = new ActivityImpl();
            activity.setActor("Administrator");
            activity.setVerb("test");
            activity.setObject("activity" + i);
            activity.setPublishedDate(new Date());
            activityStreamService.addActivity(activity);
        }
    }

    @Test
    public void shouldRemoveActivities() {
        addTestActivities(10);

        List<Activity> allActivities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 0, 0);
        assertEquals(10, allActivities.size());

        Activity firstActivity = allActivities.get(0);
        activityStreamService.removeActivities(Collections.singleton(firstActivity.getId()));

        allActivities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 0, 0);
        assertEquals(9, allActivities.size());
        assertFalse(allActivities.contains(firstActivity));

        List<Activity> activities = allActivities.subList(0, 4);
        activityStreamService.removeActivities(toActivityIds(activities));
        allActivities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 0, 0);
        assertEquals(5, allActivities.size());

        activities = allActivities.subList(0, 5);
        activityStreamService.removeActivities(toActivityIds(activities));
        allActivities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null, 0, 0);
        assertTrue(allActivities.isEmpty());
    }

    private List<Serializable> toActivityIds(List<Activity> activities) {
        List<Serializable> activityIds = new ArrayList<Serializable>();
        for (Activity activity : activities) {
            activityIds.add(activity.getId());
        }
        return activityIds;
    }

    @Test
    public void shouldStoreLabelKeyForActivityVerbs() {
        Map<String, String> activityMessageLabels = ((ActivityStreamServiceImpl) activityStreamService).activityMessageLabels;
        assertNotNull(activityMessageLabels);
        assertEquals(3, activityMessageLabels.size());
        assertTrue(activityMessageLabels.containsKey(DOCUMENT_CREATED));
        assertTrue(activityMessageLabels.containsKey(DOCUMENT_UPDATED));
        assertTrue(activityMessageLabels.containsKey(DOCUMENT_REMOVED));

        assertEquals("label.activity.documentCreated",
                activityMessageLabels.get(DOCUMENT_CREATED));
        assertEquals("label.activity.documentUpdated",
                activityMessageLabels.get(DOCUMENT_UPDATED));
        assertEquals("label.activity.documentRemoved",
                activityMessageLabels.get(DOCUMENT_REMOVED));
    }

    @Test
    public void shouldStoreTweetActivities() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb(TweetActivityStreamFilter.TWEET_VERB);
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("seenBy", "Bob");
        List<Activity> activities = activityStreamService.query(
                TweetActivityStreamFilter.ID, parameters);
        assertEquals(1, activities.size());
        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());

        parameters = new HashMap<String, Serializable>();
        parameters.put("seenBy", "Joe");
        activities = activityStreamService.query(TweetActivityStreamFilter.ID,
                parameters);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());

        parameters = new HashMap<String, Serializable>();
        parameters.put("seenBy", "John");
        activities = activityStreamService.query(TweetActivityStreamFilter.ID,
                parameters);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());
    }

    @Test
    public void shouldRemoveTweets() throws ClientException {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb(TweetActivityStreamFilter.TWEET_VERB);
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("seenBy", "Bob");
        List<Activity> activities = activityStreamService.query(
                TweetActivityStreamFilter.ID, parameters);
        assertEquals(1, activities.size());

        List<TweetActivity> tweets = getAllTweetActivities();
        assertEquals(3, tweets.size());

        activityStreamService.removeActivities(Collections.singleton(activity.getId()));
        activities = activityStreamService.query(TweetActivityStreamFilter.ID,
                parameters);
        assertTrue(activities.isEmpty());

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertTrue(activities.isEmpty());

        tweets = getAllTweetActivities();
        assertTrue(tweets.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private List<TweetActivity> getAllTweetActivities() throws ClientException {
        return ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true,
                new PersistenceProvider.RunCallback<List<TweetActivity>>() {
                    @Override
                    public List<TweetActivity> runWith(EntityManager em) {
                        Query query = em.createQuery("from Tweet");
                        return query.getResultList();
                    }
                });
    }

    @Test
    public void shouldStoreActivityStreams() {
        Map<String, ActivityStream> activityStreams = ((ActivityStreamServiceImpl) activityStreamService).activityStreamRegistry.activityStreams;
        assertNotNull(activityStreams);
        assertEquals(2, activityStreams.size());

        ActivityStream activityStream = activityStreamService.getActivityStream("userActivityStream");
        assertNotNull(activityStream);
        assertEquals("userActivityStream", activityStream.getName());
        List<String> verbs = activityStream.getVerbs();
        assertNotNull(verbs);
        assertEquals(3, verbs.size());
        assertTrue(verbs.contains("documentCreated"));
        assertTrue(verbs.contains("documentModified"));
        assertTrue(verbs.contains("circle"));

        activityStream = activityStreamService.getActivityStream("anotherStream");
        assertNotNull(activityStream);
        assertEquals("anotherStream", activityStream.getName());
        verbs = activityStream.getVerbs();
        assertNotNull(verbs);
        assertEquals(1, verbs.size());
        assertTrue(verbs.contains("documentDeleted"));
    }

    @Test
    public void shouldStoreAnActivityComment() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        Activity storedActivity = activities.get(0);
        assertEquals(activity.getActor(), storedActivity.getActor());
        assertEquals(activity.getVerb(), storedActivity.getVerb());
        assertEquals(activity.getObject(), storedActivity.getObject());

        long commentPublishedDate = new Date().getTime();
        ActivityComment comment = new ActivityComment("bender", "Bender",
                "First comment", commentPublishedDate);
        ActivityComment storedComment = activityStreamService.addActivityComment(activity.getId(), comment);
        assertEquals(storedActivity.getId() + "-comment-1", storedComment.getId());

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertNotNull(storedActivity.getComments());
        List<ActivityComment> comments = storedActivity.getActivityComments();
        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        storedComment = comments.get(0);
        assertEquals(storedActivity.getId() + "-comment-1", storedComment.getId());
        assertEquals("bender", storedComment.getActor());
        assertEquals("Bender", storedComment.getDisplayActor());
        assertEquals(commentPublishedDate, storedComment.getPublishedDate());
        assertEquals("First comment", storedComment.getMessage());
    }

    @Test
    public void shouldStoreMultipleActivityComments() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        Activity storedActivity = activities.get(0);

        long firstCommentPublishedDate = new Date().getTime();
        ActivityComment firstComment = new ActivityComment("bender", "Bender",
                "First comment", firstCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), firstComment);
        long secondCommentPublishedDate = new Date().getTime();
        ActivityComment secondComment = new ActivityComment("bender", "Bender",
                "Second comment", secondCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), secondComment);
        long thirdCommentPublishedDate = new Date().getTime();
        ActivityComment thirdComment = new ActivityComment("fry", "Fry",
                "Third comment", thirdCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), thirdComment);
        long fourthCommentPublishedDate = new Date().getTime();
        ActivityComment fourthComment = new ActivityComment("leela", "Leela",
                "Fourth comment", fourthCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), fourthComment);

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertNotNull(storedActivity.getComments());
        List<ActivityComment> comments = storedActivity.getActivityComments();
        assertFalse(comments.isEmpty());
        assertEquals(4, comments.size());

        ActivityComment storedComment = comments.get(0);
        assertEquals(storedActivity.getId() + "-comment-1", storedComment.getId());
        assertEquals("bender", storedComment.getActor());
        assertEquals("Bender", storedComment.getDisplayActor());
        assertEquals(firstCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("First comment", storedComment.getMessage());
        storedComment = comments.get(1);
        assertEquals(storedActivity.getId() + "-comment-2", storedComment.getId());
        assertEquals("bender", storedComment.getActor());
        assertEquals("Bender", storedComment.getDisplayActor());
        assertEquals(secondCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("Second comment", storedComment.getMessage());
        storedComment = comments.get(2);
        assertEquals(storedActivity.getId() + "-comment-3", storedComment.getId());
        assertEquals("fry", storedComment.getActor());
        assertEquals("Fry", storedComment.getDisplayActor());
        assertEquals(thirdCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("Third comment", storedComment.getMessage());
        storedComment = comments.get(3);
        assertEquals(storedActivity.getId() + "-comment-4", storedComment.getId());
        assertEquals("leela", storedComment.getActor());
        assertEquals("Leela", storedComment.getDisplayActor());
        assertEquals(fourthCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("Fourth comment", storedComment.getMessage());
    }

    @Test
    public void shouldRemoveActivityComment() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activityStreamService.addActivity(activity);

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());

        Activity storedActivity = activities.get(0);

        long firstCommentPublishedDate = new Date().getTime();
        ActivityComment firstComment = new ActivityComment("bender", "Bender",
                "First comment", firstCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), firstComment);
        long secondCommentPublishedDate = new Date().getTime();
        ActivityComment secondComment = new ActivityComment("bender", "Bender",
                "Second comment", secondCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), secondComment);
        long thirdCommentPublishedDate = new Date().getTime();
        ActivityComment thirdComment = new ActivityComment("fry", "Fry",
                "Third comment", thirdCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), thirdComment);
        long fourthCommentPublishedDate = new Date().getTime();
        ActivityComment fourthComment = new ActivityComment("leela", "Leela",
                "Fourth comment", fourthCommentPublishedDate);
        activityStreamService.addActivityComment(storedActivity.getId(), fourthComment);

        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertNotNull(storedActivity.getComments());
        List<ActivityComment> comments = storedActivity.getActivityComments();
        assertFalse(comments.isEmpty());
        assertEquals(4, comments.size());

        ActivityComment storedComment = comments.get(0);
        assertEquals(storedActivity.getId() + "-comment-1", storedComment.getId());
        assertEquals("bender", storedComment.getActor());
        assertEquals("Bender", storedComment.getDisplayActor());
        assertEquals(firstCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("First comment", storedComment.getMessage());
        storedComment = comments.get(1);
        assertEquals(storedActivity.getId() + "-comment-2", storedComment.getId());
        assertEquals("bender", storedComment.getActor());
        assertEquals("Bender", storedComment.getDisplayActor());
        assertEquals(secondCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("Second comment", storedComment.getMessage());
        storedComment = comments.get(2);
        assertEquals(storedActivity.getId() + "-comment-3", storedComment.getId());
        assertEquals("fry", storedComment.getActor());
        assertEquals("Fry", storedComment.getDisplayActor());
        assertEquals(thirdCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("Third comment", storedComment.getMessage());
        storedComment = comments.get(3);
        assertEquals(storedActivity.getId() + "-comment-4", storedComment.getId());
        assertEquals("leela", storedComment.getActor());
        assertEquals("Leela", storedComment.getDisplayActor());
        assertEquals(fourthCommentPublishedDate, storedComment.getPublishedDate());
        assertEquals("Fourth comment", storedComment.getMessage());

        activityStreamService.removeActivityComment(activity.getId(), thirdComment.getId());
        activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        assertEquals(1, activities.size());
        storedActivity = activities.get(0);
        assertNotNull(storedActivity.getComments());
        comments = storedActivity.getActivityComments();
        assertFalse(comments.isEmpty());
        assertEquals(3, comments.size());

        for (ActivityComment comment : comments) {
            assertFalse(comment.getMessage().equals("Third comment"));
        }
    }

}
