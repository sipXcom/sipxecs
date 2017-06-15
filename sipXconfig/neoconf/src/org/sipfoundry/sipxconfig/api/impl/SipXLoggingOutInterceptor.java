/**
 * Copyright (c) 2017 eZuce, Inc. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.api.impl;

import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.sipfoundry.sipxconfig.api.IContent;

public class SipXLoggingOutInterceptor extends LoggingOutInterceptor implements IContent {

    @Override
    public boolean isBinaryContent(String contentType) {
        return contentType != null && (super.isBinaryContent(contentType) || isAudioBinary(contentType));
    }

    @Override
    public boolean isAudioBinary(String contentType) {
        return AUDIO_BINARY.contains(contentType);
    }

    @Override
    public boolean isMultiPart(String contentType) {
        return false;
    }
}
