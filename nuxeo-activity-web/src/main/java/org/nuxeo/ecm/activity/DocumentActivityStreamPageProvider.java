package org.nuxeo.ecm.activity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.nuxeo.ecm.activity.DocumentActivityStreamFilter.ACTIVITY_STREAM_PARAMETER;
import static org.nuxeo.ecm.activity.DocumentActivityStreamFilter.DOCUMENT_PARAMETER;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.7
 */
public class DocumentActivityStreamPageProvider extends AbstractActivityPageProvider<ActivityMessage> {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(DocumentActivityStreamPageProvider.class);

    public static final String ACTIVITY_STREAM_NAME_PROPERTY = "activityStreamName";

    public static final String ACTIVITY_LINK_BUILDER_NAME_PROPERTY = "activityLinkBuilderName";

    public static final String DOCUMENT_PROPERTY = "document";

    public static final String LOCALE_PROPERTY = "locale";

    public static final String CORE_SESSION_PROPERTY = "coreSession";

    protected List<ActivityMessage> pageActivityMessages;

    @Override
    public List<ActivityMessage> getCurrentPage() {
        if (pageActivityMessages == null) {
            pageActivityMessages = new ArrayList<ActivityMessage>();
            long pageSize = getMinMaxPageSize();

            ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
            Map<String, Serializable> parameters = new HashMap<String, Serializable>();
            parameters.put(ACTIVITY_STREAM_PARAMETER, getActivityStreamName());
            parameters.put(DOCUMENT_PARAMETER, getDocument());
            ActivitiesList activities = activityStreamService.query(
                    DocumentActivityStreamFilter.ID, parameters,
                    getCurrentPageOffset(), pageSize);
            nextOffset = offset + activities.size();
            activities = activities.filterActivities(getCoreSession());
            pageActivityMessages.addAll(activities.toActivityMessages(
                    getLocale(), getActivityLinkBuilderName()));
            setResultsCount(UNKNOWN_SIZE_AFTER_QUERY);
        }
        return pageActivityMessages;
    }

    protected String getActivityStreamName() {
        Map<String, Serializable> props = getProperties();
        String activityStreamName = (String) props.get(ACTIVITY_STREAM_NAME_PROPERTY);
        if (activityStreamName == null) {
            throw new ClientRuntimeException("Cannot find "
                    + ACTIVITY_STREAM_NAME_PROPERTY + " property.");
        }
        return activityStreamName;
    }

    protected DocumentModel getDocument() {
        Map<String, Serializable> props = getProperties();
        DocumentModel contextDocument = (DocumentModel) props.get(DOCUMENT_PROPERTY);
        if (contextDocument == null) {
            throw new ClientRuntimeException("Cannot find "
                    + DOCUMENT_PROPERTY + " property.");
        }
        return contextDocument;
    }

    protected Locale getLocale() {
        Map<String, Serializable> props = getProperties();
        Locale locale = (Locale) props.get(LOCALE_PROPERTY);
        if (locale == null) {
            throw new ClientRuntimeException("Cannot find " + LOCALE_PROPERTY
                    + " property.");
        }
        return locale;
    }

    protected String getActivityLinkBuilderName() {
        Map<String, Serializable> props = getProperties();
        return (String) props.get(ACTIVITY_LINK_BUILDER_NAME_PROPERTY);
    }

    protected CoreSession getCoreSession() {
        Map<String, Serializable> props = getProperties();
        CoreSession session = (CoreSession) props.get(CORE_SESSION_PROPERTY);
        if (session == null) {
            throw new ClientRuntimeException("Cannot find "
                    + CORE_SESSION_PROPERTY + " property.");
        }
        return session;
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    protected void pageChanged() {
        super.pageChanged();
        pageActivityMessages = null;
    }

    @Override
    public void refresh() {
        super.refresh();
        pageActivityMessages = null;
    }
}
