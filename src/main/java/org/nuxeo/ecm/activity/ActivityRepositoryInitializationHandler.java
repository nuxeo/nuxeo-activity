/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Thomas Roger (troger@nuxeo.com)
 */

package org.nuxeo.ecm.activity;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.repository.RepositoryInitializationHandler;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.7
 */
public class ActivityRepositoryInitializationHandler extends RepositoryInitializationHandler {

    @Override
    public void doInitializeRepository(CoreSession session) throws ClientException {
        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        if (activityStreamService != null) {
            ((ActivityStreamServiceImpl) activityStreamService).upgradeActivities();
        }
    }

}
