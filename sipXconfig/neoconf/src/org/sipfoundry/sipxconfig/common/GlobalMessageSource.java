/**
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
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
package org.sipfoundry.sipxconfig.common;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

import org.sipfoundry.sipxconfig.Pluggable0ResourceBundleMessageSource;
import org.sipfoundry.sipxconfig.PluggableResourceBundleMessageSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Read resource bundles from all beans that implement MessageSource, including plugins.
 *
 * To register your resource bundle into the global pool, just add bean
 *
 *   <bean id="alarmMessages" class="org.springframework.context.support.ResourceBundleMessageSource">
 *     <property name="basename" value="my.package.xyx"/>
 *   < /bean>
 *
 *   And create file with english translations
 *     src/my/package/xyz.properties
 *   and optionally other files with other language translations following the java resource rules
 *
 *   Code that wished to load strings from global pool can just inject this bean and call one
 *   of the getMessage methods.
 */
public class GlobalMessageSource implements MessageSource, BeanFactoryAware {
    private ListableBeanFactory m_beanFactory;
    private Collection<MessageSource> m_delegates;
    private int m_avoidCheckstyleError;

    @Override
    public String getMessage(MessageSourceResolvable r, Locale l) {
        for (MessageSource d : getDelegates()) {
            try {
                return d.getMessage(r, l);
            } catch (NoSuchMessageException ignore) {
                m_avoidCheckstyleError++;
            }
        }
        throw new NoSuchMessageException(r.toString(), l);
    }

    @Override
    public String getMessage(String s, Object[] p, Locale l) {
        for (MessageSource d : getDelegates()) {
            try {
                return d.getMessage(s, p, l);
            } catch (NoSuchMessageException ignore) {
                m_avoidCheckstyleError++;
            }
        }
        throw new NoSuchMessageException(s, l);
    }

    @Override
    public String getMessage(String s, Object[] p, String s2, Locale l) {
        for (MessageSource d : getDelegates()) {
            try {
                return d.getMessage(s, p, s2, l);
            } catch (NoSuchMessageException ignore) {
                m_avoidCheckstyleError++;
            }
        }
        throw new NoSuchMessageException(s, l);
    }

    @Override
    public void setBeanFactory(BeanFactory bf) {
        m_beanFactory = (ListableBeanFactory) bf;
    }

    Collection<MessageSource> getDelegates() {
        if (m_delegates == null) {
            //using LinkedHashSet to keep insertion order
            LinkedHashSet<MessageSource> copy = new LinkedHashSet<MessageSource>();
            //pluggable 0 beans will be added first so plugin resource labels to come last
            Map<String, Pluggable0ResourceBundleMessageSource> pluggable0Beans = m_beanFactory.
                getBeansOfType(Pluggable0ResourceBundleMessageSource.class);
            Collection<Pluggable0ResourceBundleMessageSource> pluggable0Values = pluggable0Beans.values();
            //Add pluggable 0 values first - make sure they are not getting overwritten
            copy.removeAll(pluggable0Values);
            copy.addAll(pluggable0Values);
            Map<String, MessageSource> beans = m_beanFactory.getBeansOfType(MessageSource.class);
            copy.addAll(beans.values());
            //pluggable beans will be added last so plugin resource labels to come first
            Map<String, PluggableResourceBundleMessageSource> pluggableBeans = m_beanFactory.
                getBeansOfType(PluggableResourceBundleMessageSource.class);
            Collection<PluggableResourceBundleMessageSource> pluggableValues = pluggableBeans.values();
            //Add pluggable values last - might be needed to get overwritten
            copy.removeAll(pluggableValues);
            copy.addAll(pluggableValues);

            // otherwise recursive!
            copy.remove(this);
            m_delegates = copy;
        }
        return m_delegates;
    }
}
