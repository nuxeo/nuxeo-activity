package org.nuxeo.ecm.activity;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;


/**
 * Activity Stream filter handling document activity stream.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.7
 */
public class DocumentActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "DocumentActivityStreamFilter";

    public static final String DOCUMENT_PARAMETER = "documentParameter";

    public static final String DEFAULT_DOCUMENT_ACTIVITY_STREAM_NAME = "defaultDocumentActivityStream";

    public static final String ACTIVITY_STREAM_PARAMETER = "activityStreamParameter";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInterestedIn(Activity activity) {
        return false;
    }

    @Override
    public void handleNewActivity(ActivityStreamService activityStreamService,
            Activity activity) {
        // do nothing
    }

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // do nothing
    }

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            ActivitiesList activities) {
        // do nothing
    }

    @Override
    public void handleRemovedActivityReply(
            ActivityStreamService activityStreamService, Activity activity,
            ActivityReply activityReply) {
        // do nothing
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {
        DocumentModel doc = (DocumentModel) parameters.get(DOCUMENT_PARAMETER);
        if (doc == null) {
            throw new IllegalArgumentException(DOCUMENT_PARAMETER
                    + " is required");
        }
        String docActivityObject = ActivityHelper.createDocumentActivityObject(doc);

        String activityStreamName = (String) parameters.get(ACTIVITY_STREAM_PARAMETER);
        if (StringUtils.isBlank(activityStreamName)) {
            activityStreamName = DEFAULT_DOCUMENT_ACTIVITY_STREAM_NAME;
        }
        ActivityStream documentActivityStream = activityStreamService.getActivityStream(activityStreamName);
        List<String> verbs = documentActivityStream.getVerbs();

        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query = em.createQuery("select activity from Activity activity "
                + "where (activity.object = :document or activity.target = :document) "
                + "and activity.verb in (:verbs) "
                + "and activity.actor like :actor "
                + "and activity.context is null "
                + "order by activity.lastUpdatedDate desc");
        query.setParameter("document", docActivityObject);
        query.setParameter("verbs", verbs);
        query.setParameter("actor", "user:%");

        if (limit > 0) {
            query.setMaxResults((int) limit);
            if (offset > 0) {
                query.setFirstResult((int) offset);
            }
        }
        return new ActivitiesListImpl(query.getResultList());
    }

}
