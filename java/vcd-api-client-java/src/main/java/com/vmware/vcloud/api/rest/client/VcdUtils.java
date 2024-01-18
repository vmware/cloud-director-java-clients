/* ***************************************************************************
 * Copyright 2013-2018 VMware, Inc.  All rights reserved. VMware Confidential
 * **************************************************************************/

package com.vmware.vcloud.api.rest.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import com.vmware.vcloud.api.rest.constants.RestConstants;
import com.vmware.vcloud.api.rest.links.LinkRelation;
import com.vmware.vcloud.api.rest.schema_v1_5.EntityType;
import com.vmware.vcloud.api.rest.schema_v1_5.IdentifiableResourceType;
import com.vmware.vcloud.api.rest.schema_v1_5.LinkType;
import com.vmware.vcloud.api.rest.schema_v1_5.QueryResultRecordType;
import com.vmware.vcloud.api.rest.schema_v1_5.ReferenceType;
import com.vmware.vcloud.api.rest.schema_v1_5.ResourceType;
import com.vmware.vcloud.api.rest.schema_v1_5.TaskType;
import com.vmware.vcloud.api.rest.schema_v1_5.TasksInProgressType;

/**
 * Miscellaneous utility methods for use when working with {@link VcdClient}s.
 */
public class VcdUtils {

    /**
     * Constructs a {@code URI} which has the correct delete parameters set as {@code URL}
     * parameters if applicable
     *
     * @param baseHref
     * @param force
     *            {@code true}, {@code false} or {@code null} which indicates the parameter should
     *            be omitted
     * @param recursive
     *            {@code true}, {@code false} or {@code null} which indicates the parameter should
     *            be omitted
     * @return a {@code URI} which can be further altered by the caller if necessary.
     */
    public static URI buildDeleteUri(final URI baseHref, final Boolean force, final Boolean recursive) {

        final UriBuilder builder = UriBuilder.fromUri(baseHref);

        if (force != null) {
            builder.queryParam(RestConstants.DeleteParameters.FORCE, force);
        }

        if (recursive != null) {
            builder.queryParam(RestConstants.DeleteParameters.RECURSIVE, recursive);
        }

        return builder.build();
    }

    /**
     * Gets an entity's ID in a form that can be used as an input parameter to a service method that
     * expects an ID.
     *
     * TODO: The existence of this method is an indication of a flaw in the nature of
     * {@link VcdClient} and/or CXF's REST client support. The vCloud API, being a "proper" sort of
     * REST, returns links to resources in its responses with the idea that clients traverse the
     * graph of vCloud objects by following those links. However, the vCloud JAX-RS
     * interfaces/handlers are "service-oriented" in a way that makes it hard to usefully represent
     * those links in a CXF REST client. Instead, clients end up doing the bad thing of using entity
     * IDs to compose URLs (CXF composes them on the clients behalf, using JAX-RS annotations in the
     * vCloud JAX-RS interfaces, which is just as bad), rather than simply following returned links.
     * We should figure out if there's a way to do better.
     */
    public static String getEntityId(EntityType entityType) {
        // TODO: Do better?
        return entityType.getId().split(":")[3];
    }

    /**
     * Returns the link of the specified rel and type in the specified resource
     * @param resource the resource with the link
     * @param rel the rel of the desired link
     * @param mediaType media type of content
     * @return the link
     * @throws MissingLinkException if no link of the specified rel and media type is found
     * @throws MultipleLinksException if multiple links of the specified rel and media type are found
     */
    public static LinkType findLink(ResourceType resource, LinkRelation rel, String mediaType) throws MissingLinkException, MultipleLinksException {
        return findLink(resource, rel, mediaType, true);
    }

    /**
     * Returns the link of the specified rel and type in the specified resource
     * @param resource the resource with the link
     * @param rel the rel of the desired link
     * @param mediaType media type of content
     * @param failIfAbsent controls whether an exception is thrown if there's not exactly one link of the specified rel and media type
     * @return the link, or null if no such link is present and failIfAbsent is false
     * @throws MissingLinkException if no link of the specified rel and media type is found
     * @throws MultipleLinksException if multiple links of the specified rel and media type are found
     */
    public static LinkType findLink(final ResourceType resource,
            final LinkRelation rel,
            final String mediaType,
            final boolean failIfAbsent) throws MissingLinkException, MultipleLinksException {
        final List<LinkType> links = getLinks(resource, rel, mediaType);
        return findLink(links, rel, mediaType, resource.getHref(), failIfAbsent);
    }

    /**
     * Returns the link of the specified rel and type in the specified {@link QueryResultRecordType}
     * @param query record resourcee with the link
     * @param rel the rel of the desired link
     * @param mediaType media type of content
     * @param failIfAbsent controls whether an exception is thrown if there's not exactly one link of the specified rel and media type
     * @return the link, or null if no such link is present and failIfAbsent is false
     * @throws MissingLinkException if no link of the specified rel and media type is found
     * @throws MultipleLinksException if multiple links of the specified rel and media type are found
     */
    public static LinkType findLink(final QueryResultRecordType resource,
            final LinkRelation rel,
            final String mediaType,
            final boolean failIfAbsent) throws MissingLinkException, MultipleLinksException {
        final List<LinkType> links = getLinks(resource, rel, mediaType);
        return findLink(links, rel, mediaType, resource.getHref(), failIfAbsent);
    }

    private static LinkType findLink(final List<LinkType> links,
            final LinkRelation rel,
            final String mediaType,
            final String href,
            final boolean failIfAbsent) throws MissingLinkException, MultipleLinksException {
        switch (links.size()) {
        case 0:
            if (failIfAbsent) {
                throw new MissingLinkException(href, rel, mediaType);
            } else {
                return null;
            }
        case 1:
            return links.get(0);
        default:
            throw new MultipleLinksException(href, rel, mediaType);
        }
    }

    /**
     * Returns the link of the specified type in the specified list of links
     *
     * @param link List of links
     * @param mediaType The media type to look for
     * @return the link, or null if no such link
     */
    public static LinkType findLink(List<LinkType> links, String mediaType) {
        for (LinkType link : links) {
            if (mediaType.equals(link.getType())) {
                return link;
            }
        }
        return null;
    }

    /**
     * Returns all the links of the specified rel and type in the specified resource
     * @param resource the resource with the link
     * @param rel the rel of the desired link
     * @param mediaType media type of content
     * @return the links (could be an empty list)
     */
    public static List<LinkType> getLinks(final ResourceType resource,
            final LinkRelation rel,
            final String mediaType) {
        return getLinksForRelAndMediaType(resource.getLink(), rel, mediaType);
    }

    /**
     * Returns all the links of the specified rel and type in the specified {@link QueryResultRecordType}
     * @param resource the resource with the link
     * @param rel the rel of the desired link
     * @param mediaType media type of content
     * @return the links (could be an empty list)
     */
    public static List<LinkType> getLinks(final QueryResultRecordType resource,
            final LinkRelation rel,
            final String mediaType) {
        return getLinksForRelAndMediaType(resource.getLink(), rel, mediaType);
    }

    private static List<LinkType> getLinksForRelAndMediaType(final List<LinkType> links,
            final LinkRelation rel,
            final String mediaType) {
        final List<LinkType> filteredLinks = new ArrayList<>();
        for (LinkType link : links) {
            try {
                if (link.getRel().equals(rel.value())) {
                    if (mediaType == null && link.getType() == null) {
                        filteredLinks.add(link);
                    } else if (mediaType != null && link.getType().equals(mediaType)) {
                        filteredLinks.add(link);
                    }
                }
            } catch (IllegalArgumentException e) {
                // See comment in corresponding catch in findLink().
            }
        }
        return filteredLinks;
    }

    /**
     * Convenience method to turn a {@link ResourceType} into a {@link ReferenceType} to that resource.
     */
    public static ReferenceType makeRef(ResourceType resource) {
        if (resource == null) {
            return null;
        }
        final ReferenceType ref = new ReferenceType();
        ref.setHref(resource.getHref());
        ref.setType(resource.getType());
        return ref;
    }

    /**
     * Convenience method to turn an {@link IdentifiableResourceType} into a {@link ReferenceType} to that resource.
     */
    public static ReferenceType makeRef(IdentifiableResourceType identifiableResource) {
        final ReferenceType ref = makeRef((ResourceType) identifiableResource);
        if (ref == null) {
            return null;
        }
        ref.setId(identifiableResource.getId());
        return ref;
    }

    /**
     * Convenience method to turn an {@link EntityType} into a {@link ReferenceType} to that entity.
     */
    public static ReferenceType makeRef(EntityType entity) {
        final ReferenceType ref = makeRef((IdentifiableResourceType) entity);
        if (ref == null) {
            return null;
        }
        ref.setName(entity.getName());
        return ref;
    }

    /**
     * Convenience method to turn a {@link LinkType} into a {@link ReferenceType} to the linked-to resource.
     */
    public static ReferenceType makeRef(LinkType link) {
        ReferenceType ref = new ReferenceType();
        ref.setHref(link.getHref());
        return ref;
    }

    /**
     * Convenience method to turn a {@link String} of an URL into a {@link ReferenceType} with that URL.
     */
    public static ReferenceType makeRef(String href) {
        ReferenceType ref = new ReferenceType();
        ref.setHref(href);
        return ref;
    }

    /**
     *
     * Convenience method to get {@link TaskType} from given resource.
     *
     * @throws RuntimeException
     *             if more than one {@link TaskType} found in given resource.
     */
    public static <T extends EntityType> TaskType getTask(T entityType) {
        final TasksInProgressType tasks = entityType.getTasks();
        if (tasks == null) {
            throw new RuntimeException("No tasks found. Expected one task");
        }

        final List<TaskType> taskList = tasks.getTask();
        if (taskList.size() != 1) {
            throw new RuntimeException("Expected one task, got " + taskList.size());
        }
        return taskList.get(0);
    }
}
