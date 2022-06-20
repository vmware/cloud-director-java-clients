/* **********************************************************************
 * Copyright 2014 VMware, Inc.  All rights reserved. VMware Confidential
 * *********************************************************************/
package com.vmware.vcloud.api.rest.client;

/**
 * An exception to indicate that a query returned more than one record when it was
 * expected to return exactly one record.
 */
public class MultipleRecordsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MultipleRecordsException() {
    }

    public MultipleRecordsException(String message) {
        super(message);
    }

    public MultipleRecordsException(String message, Throwable t) {
        super(message, t);
    }

    public MultipleRecordsException(Throwable t) {
        super(t);
    }
}
