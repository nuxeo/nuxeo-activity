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

import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;

/**
 * Operation to remove an activity reply.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Operation(id = RemoveActivityReply.ID, category = Constants.CAT_SERVICES, label = "Remove a reply to an existing activity", description = "Remove a reply to an existing activity.")
public class RemoveActivityReply {

    public static final String ID = "Services.RemoveActivityReply";

    @Context
    protected ActivityStreamService activityStreamService;

    @Param(name = "activityId", required = true)
    protected String activityId;

    @Param(name = "replyId", required = true)
    protected String replyId;

    @OperationMethod
    public void run() throws NumberFormatException {
        activityStreamService.removeActivityReply(Long.valueOf(activityId), replyId);
    }

}
