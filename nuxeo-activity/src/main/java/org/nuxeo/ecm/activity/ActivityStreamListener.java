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

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_RESTORED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.core.schema.FacetNames.HIDDEN_IN_NAVIGATION;
import static org.nuxeo.ecm.core.schema.FacetNames.SUPER_SPACE;
import static org.nuxeo.ecm.core.schema.FacetNames.SYSTEM_DOCUMENT;
import static org.nuxeo.ecm.platform.comment.api.CommentEvents.COMMENT_ADDED;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.SystemPrincipal;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.ShallowDocumentModel;
import org.nuxeo.ecm.platform.publisher.api.PublishingEvent;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener called asynchronously to save events as activities through the
 * {@link ActivityStreamService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public class ActivityStreamListener implements PostCommitEventListener {
    private static final Log log = LogFactory.getLog(ActivityStreamListener.class);

    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        if (events.containsEventName(DOCUMENT_CREATED)
                || events.containsEventName(DOCUMENT_UPDATED)
                || events.containsEventName(DOCUMENT_REMOVED)
                || events.containsEventName(COMMENT_ADDED)
                || events.containsEventName(DOCUMENT_RESTORED)
                || events.containsEventName(DocumentRoutingConstants.Events.beforeRouteStart.name())
                || events.containsEventName(PublishingEvent.documentPublished.name())) {
            List<Event> filteredEvents = filterDuplicateEvents(events);
            for (Event event : filteredEvents) {
                handleEvent(event);
            }
        }
    }

    private List<Event> filterDuplicateEvents(EventBundle events) {
        List<Event> filteredEvents = new ArrayList<Event>();

        for (Event event : events) {
            filteredEvents = removeEventIfExist(filteredEvents, event);
            filteredEvents.add(event);
        }

        return filteredEvents;
    }

    private List<Event> removeEventIfExist(List<Event> events, Event event) {
        EventContext eventContext = event.getContext();
        if (eventContext instanceof DocumentEventContext) {
            DocumentModel doc = ((DocumentEventContext) eventContext).getSourceDocument();
            for (Iterator<Event> it = events.iterator(); it.hasNext();) {
                Event filteredEvent = it.next();
                EventContext filteredEventContext = filteredEvent.getContext();
                if (filteredEventContext instanceof DocumentEventContext) {
                    DocumentModel filteredEventDoc = ((DocumentEventContext) filteredEventContext).getSourceDocument();
                    if (event.getName().equals(filteredEvent.getName())
                            && doc.getRef().equals(filteredEventDoc.getRef())) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        return events;
    }

    private void handleEvent(Event event) throws ClientException {
        EventContext eventContext = event.getContext();
        if (eventContext instanceof DocumentEventContext) {
            if (DOCUMENT_CREATED.equals(event.getName())
                    || DOCUMENT_REMOVED.equals(event.getName())
                    || DOCUMENT_UPDATED.equals(event.getName())
                    || COMMENT_ADDED.equals(event.getName())) {
                DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
                DocumentModel doc = docEventContext.getSourceDocument();
                if (doc instanceof ShallowDocumentModel
                        || doc.hasFacet(HIDDEN_IN_NAVIGATION)
                        || doc.hasFacet(SYSTEM_DOCUMENT) || doc.isProxy()
                        || doc.isVersion()) {
                    // Not really interested in non live document or if document
                    // cannot be reconnected
                    // or if not visible
                    return;
                }

                if (docEventContext.getPrincipal() instanceof SystemPrincipal) {
                    // do not log activity for system principal
                    return;
                }

                // add activity without context
                ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
                Activity activity = toActivity(docEventContext, event);
                activityStreamService.addActivity(activity);

                CoreSession session = docEventContext.getCoreSession();
                for (DocumentRef ref : getParentSuperSpaceRefs(session, doc)) {
                    String context = ActivityHelper.createDocumentActivityObject(
                            session.getRepositoryName(), ref.toString());
                    activity = toActivity(docEventContext, event, context);
                    activityStreamService.addActivity(activity);
                }
            } else if (DOCUMENT_RESTORED.equals(event.getName())) {
                DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
                DocumentModel doc = docEventContext.getSourceDocument();
                if (doc instanceof ShallowDocumentModel
                        || doc.hasFacet(HIDDEN_IN_NAVIGATION)
                        || doc.hasFacet(SYSTEM_DOCUMENT) || doc.isProxy()
                        || doc.isVersion()) {
                    // Not really interested in non live document or if document
                    // cannot be reconnected
                    // or if not visible
                    return;
                }

                if (docEventContext.getPrincipal() instanceof SystemPrincipal) {
                    // do not log activity for system principal
                    return;
                }

                // add activity without context
                ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
                Principal principal = docEventContext.getPrincipal();
                Activity activity = new ActivityBuilder().actor(
                        ActivityHelper.createUserActivityObject(principal)).displayActor(
                        ActivityHelper.generateDisplayName(principal)).verb(
                        event.getName()).object(doc.getVersionLabel()).target(
                        ActivityHelper.createDocumentActivityObject(doc)).displayTarget(
                        ActivityHelper.getDocumentTitle(doc)).build();
                activityStreamService.addActivity(activity);

                CoreSession session = docEventContext.getCoreSession();
                for (DocumentRef ref : getParentSuperSpaceRefs(session, doc)) {
                    String context = ActivityHelper.createDocumentActivityObject(
                            session.getRepositoryName(), ref.toString());
                    activityStreamService.addActivity(new ActivityBuilder(
                            activity).context(context).build());
                }
            } else if (DocumentRoutingConstants.Events.beforeRouteStart.name().equals(
                    event.getName())) {
                DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
                DocumentRoute route = (DocumentRoute) eventContext.getProperty(DocumentRoutingConstants.DOCUMENT_ELEMENT_EVENT_CONTEXT_KEY);
                ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
                if (route != null) {
                    List<String> docIds = route.getAttachedDocuments();
                    for (String docId : docIds) {
                        CoreSession session = docEventContext.getCoreSession();
                        DocumentModel doc = session.getDocument(new IdRef(docId));
                        Principal principal = docEventContext.getPrincipal();
                        Activity activity = new ActivityBuilder().actor(
                                ActivityHelper.createUserActivityObject(principal)).displayActor(
                                ActivityHelper.generateDisplayName(principal)).verb(
                                "workflowStarted").object(route.getName()).target(
                                ActivityHelper.createDocumentActivityObject(doc)).displayTarget(
                                ActivityHelper.getDocumentTitle(doc)).build();
                        activityStreamService.addActivity(activity);

                        for (DocumentRef ref : getParentSuperSpaceRefs(session,
                                doc)) {
                            String context = ActivityHelper.createDocumentActivityObject(
                                    session.getRepositoryName(), ref.toString());
                            activityStreamService.addActivity(new ActivityBuilder(
                                    activity).context(context).build());
                        }
                    }
                }
            } else if (PublishingEvent.documentPublished.name().equals(
                    event.getName())) {
                DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
                if (docEventContext.getPrincipal() instanceof SystemPrincipal) {
                    // do not log activity for system principal
                    return;
                }

                DocumentModel proxy = docEventContext.getSourceDocument();
                CoreSession session = docEventContext.getCoreSession();
                if (proxy.isProxy()) {
                    DocumentModel version = session.getSourceDocument(proxy.getRef());
                    DocumentModel liveDoc = session.getSourceDocument(version.getRef());

                    // Build an activity for this live doc publishing
                    Principal principal = docEventContext.getPrincipal();
                    Activity activity = newPublishedInSectionActivity(event,
                            proxy, session, liveDoc, principal);

                    ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
                    activityStreamService.addActivity(activity);

                    for (DocumentRef ref : getParentSuperSpaceRefs(session,
                            liveDoc)) {
                        String context = ActivityHelper.createDocumentActivityObject(
                                session.getRepositoryName(), ref.toString());
                        activity = toActivity(docEventContext, event, context);
                        activityStreamService.addActivity(activity);
                    }
                }
            }
        }
    }

    private Activity toActivity(DocumentEventContext docEventContext,
            Event event) {
        return toActivity(docEventContext, event, null);
    }

    private Activity toActivity(DocumentEventContext docEventContext,
            Event event, String context) {
        Principal principal = docEventContext.getPrincipal();
        DocumentModel doc = docEventContext.getSourceDocument();
        return new ActivityBuilder().actor(
                ActivityHelper.createUserActivityObject(principal)).displayActor(
                ActivityHelper.generateDisplayName(principal)).verb(
                event.getName()).object(
                ActivityHelper.createDocumentActivityObject(doc)).displayObject(
                ActivityHelper.getDocumentTitle(doc)).target(
                ActivityHelper.createDocumentActivityObject(
                        doc.getRepositoryName(), doc.getParentRef().toString())).displayTarget(
                getDocumentTitle(docEventContext.getCoreSession(),
                        doc.getParentRef())).context(context).build();
    }

    protected Activity newPublishedInSectionActivity(Event event,
            DocumentModel proxy, CoreSession session, DocumentModel liveDoc,
            Principal principal) {
        return newPublishedInSectionActivity(event, proxy, session, liveDoc,
                principal, null);
    }

    protected Activity newPublishedInSectionActivity(Event event,
            DocumentModel proxy, CoreSession session, DocumentModel liveDoc,
            Principal principal, String context) {
        return new ActivityBuilder().actor(
                ActivityHelper.createUserActivityObject(principal)).displayActor(
                ActivityHelper.generateDisplayName(principal)).verb(
                event.getName()).object(
                ActivityHelper.createDocumentActivityObject(liveDoc)).displayObject(
                ActivityHelper.getDocumentTitle(liveDoc)).target(
                ActivityHelper.createDocumentActivityObject(
                        proxy.getRepositoryName(),
                        proxy.getParentRef().toString())).displayTarget(
                getDocumentTitle(session, proxy.getParentRef())).context(
                context).build();
    }

    private String getDocumentTitle(CoreSession session, DocumentRef docRef) {
        try {
            DocumentModel doc = session.getDocument(docRef);
            return ActivityHelper.getDocumentTitle(doc);
        } catch (ClientException e) {
            return docRef.toString();
        }
    }

    private List<DocumentRef> getParentSuperSpaceRefs(CoreSession session,
            final DocumentModel doc) throws ClientException {
        final List<DocumentRef> parents = new ArrayList<DocumentRef>();
        new UnrestrictedSessionRunner(session) {
            @Override
            public void run() throws ClientException {
                List<DocumentModel> parentDocuments = session.getParentDocuments(doc.getRef());
                for (DocumentModel parent : parentDocuments) {
                    if (parent.hasFacet(SUPER_SPACE)) {
                        parents.add(parent.getRef());
                    }
                }
            }
        }.runUnrestricted();
        return parents;
    }

}
