package org.nuxeo.ecm.activity.operations;

import static org.nuxeo.ecm.activity.DocumentActivityStreamPageProvider.ACTIVITY_LINK_BUILDER_NAME_PROPERTY;
import static org.nuxeo.ecm.activity.DocumentActivityStreamPageProvider.ACTIVITY_STREAM_NAME_PROPERTY;
import static org.nuxeo.ecm.activity.DocumentActivityStreamPageProvider.CORE_SESSION_PROPERTY;
import static org.nuxeo.ecm.activity.DocumentActivityStreamPageProvider.DOCUMENT_PROPERTY;
import static org.nuxeo.ecm.activity.DocumentActivityStreamPageProvider.LOCALE_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.activity.ActivityMessage;
import org.nuxeo.ecm.activity.DocumentActivityStreamPageProvider;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.7
 */
@Operation(id = GetDocumentActivityStream.ID, category = Constants.CAT_SERVICES, label = "Get a document activity stream", description = "Get a document activity stream for the given document.")
public class GetDocumentActivityStream {

    public static final String ID = "Services.GetDocumentActivityStream";

    public static final String PROVIDER_NAME = "document_activity_stream";

    @Context
    protected CoreSession session;

    @Context
    protected PageProviderService pageProviderService;

    @Param(name = "activityStreamName", required = false)
    protected String activityStreamName;

    @Param(name = "activityLinkBuilder", required = false)
    protected String activityLinkBuilder;

    @Param(name = "contextPath", required = true)
    protected String contextPath;

    @Param(name = "language", required = false)
    protected String language;

    @Param(name = "offset", required = false)
    protected Integer offset;

    @Param(name = "limit", required = false)
    protected Integer limit;

    @OperationMethod
    public Blob run() throws Exception {
        Long targetOffset = 0L;
        if (offset != null) {
            targetOffset = offset.longValue();
        }
        Long targetLimit = null;
        if (limit != null) {
            targetLimit = limit.longValue();
        }

        DocumentModel doc = session.getDocument(new PathRef(contextPath));

        Locale locale = language != null && !language.isEmpty() ? new Locale(
                language) : Locale.ENGLISH;

        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(ACTIVITY_STREAM_NAME_PROPERTY, activityStreamName);
        props.put(ACTIVITY_LINK_BUILDER_NAME_PROPERTY, activityLinkBuilder);
        props.put(DOCUMENT_PROPERTY, doc);
        props.put(LOCALE_PROPERTY, locale);
        props.put(CORE_SESSION_PROPERTY, (Serializable) session);
        @SuppressWarnings("unchecked")
        PageProvider<ActivityMessage> pageProvider = (PageProvider<ActivityMessage>) pageProviderService.getPageProvider(
                PROVIDER_NAME, null, targetLimit, 0L, props);
        pageProvider.setCurrentPageOffset(targetOffset);

        List<ActivityMessage> activityMessages = pageProvider.getCurrentPage();
        List<Map<String, Object>> activitiesJSON = new ArrayList<Map<String, Object>>();
        for (ActivityMessage activityMessage : activityMessages) {
            Map<String, Object> o = activityMessage.toMap(session, locale,
                    activityLinkBuilder);
            activitiesJSON.add(o);
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("offset",
                ((DocumentActivityStreamPageProvider) pageProvider).getNextOffset());
        m.put("limit", pageProvider.getPageSize());
        m.put("activities", activitiesJSON);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, m);

        return new InputStreamBlob(new ByteArrayInputStream(
                writer.toString().getBytes("UTF-8")), "application/json");
    }

}
