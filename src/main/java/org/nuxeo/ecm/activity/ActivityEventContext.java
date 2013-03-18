package org.nuxeo.ecm.activity;

import org.nuxeo.ecm.core.event.impl.InlineEventContext;

public class ActivityEventContext extends InlineEventContext {
	
	public ActivityEventContext(Activity activity) {
		super(null, null);
		setArgs(new Object[]{activity});
	}
	
	public Activity getActivity() {
		return (Activity) args[0];
	}

	  
}
