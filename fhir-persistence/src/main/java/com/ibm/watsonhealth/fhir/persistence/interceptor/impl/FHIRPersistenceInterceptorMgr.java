/**
 * (C) Copyright IBM Corp. 2016,2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.persistence.interceptor.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import com.ibm.watsonhealth.fhir.core.FHIRUtilities;
import com.ibm.watsonhealth.fhir.persistence.interceptor.FHIRPersistenceEvent;
import com.ibm.watsonhealth.fhir.persistence.interceptor.FHIRPersistenceInterceptor;
import com.ibm.watsonhealth.fhir.persistence.interceptor.FHIRPersistenceInterceptorException;

/**
 * This class implements the FHIR persistence interceptor framework. This framework allows users to inject business
 * logic into the REST API request processing code path at various points.
 * 
 * Interceptors are discovered using the jdk's ServiceProvider class.
 * 
 * To register an interceptor implementation, develop a class that implements the FHIRPersistenceInterceptor interface,
 * and then insert your implementation class name into a file called
 * META-INF/services/com.ibm.watsonhealth.fhir.persistence.FHIRPersistenceInterceptor and store that file in your jar.
 * These "interceptor" jars should be stored in a common place defined by the FHIR Server.
 */
public class FHIRPersistenceInterceptorMgr {
    private static final Logger log = Logger.getLogger(FHIRPersistenceInterceptorMgr.class.getName());

    private static FHIRPersistenceInterceptorMgr instance = new FHIRPersistenceInterceptorMgr();

    // Our list of discovered interceptors.
    List<FHIRPersistenceInterceptor> interceptors = new ArrayList<>();

    public static FHIRPersistenceInterceptorMgr getInstance() {
        return instance;
    }

    private FHIRPersistenceInterceptorMgr() {
        // Discover all implementations of our interceptor interface, then add them to our list of interceptors.
        ServiceLoader<FHIRPersistenceInterceptor> slList = ServiceLoader.load(FHIRPersistenceInterceptor.class);
        Iterator<FHIRPersistenceInterceptor> iter = slList.iterator();
        if (iter.hasNext()) {
            log.fine("Discovered the following persistence interceptors:");
            while (iter.hasNext()) {
                FHIRPersistenceInterceptor interceptor = iter.next();
                log.fine(">>> " + interceptor.getClass().getName() + '@' + FHIRUtilities.getObjectHandle(interceptor));
                interceptors.add(interceptor);
            }
        } else {
            log.fine("No persistence interceptors found...");
        }
    }
    
    /**
     * This method can be used to programmatically register an interceptor such that it is added
     * at the end of the list of registered interceptors.
     * @param interceptor persistence interceptor to be registered
     */
    public void addInterceptor(FHIRPersistenceInterceptor interceptor) {
        log.fine("Registering persistence interceptor: " + interceptor.getClass().getName() + '@' + FHIRUtilities.getObjectHandle(interceptor));
        interceptors.add(interceptor);
    }
    
    /**
     * This method can be used to programmatically register an interceptor such that it is added
     * at the beginning of the list of registered interceptors.
     * @param interceptor persistence interceptor to be registered
     */
    public void addPrioritizedInterceptor(FHIRPersistenceInterceptor interceptor) {
        log.fine("Registering persistence interceptor: " + interceptor.getClass().getName() + '@' + FHIRUtilities.getObjectHandle(interceptor));
        interceptors.add(0, interceptor);
    }
    
    /**
     * The following methods will invoke the respective interceptor methods on each registered interceptor.
     */
    public void fireBeforeCreateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeCreate(event);
        }
    }

    public void fireAfterCreateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterCreate(event);
        }
    }

    public void fireBeforeUpdateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.beforeUpdate(event);
        }
    }

    public void fireAfterUpdateEvent(FHIRPersistenceEvent event) throws FHIRPersistenceInterceptorException {
        for (FHIRPersistenceInterceptor interceptor : interceptors) {
            interceptor.afterUpdate(event);
        }
    }
}
