/*
 * Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.javadoc.internal.doclets.toolkit.builders;

import java.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import jdk.javadoc.internal.doclets.toolkit.Configuration;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.DocletException;
import jdk.javadoc.internal.doclets.toolkit.PropertyWriter;
import jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberMap;


/**
 * Builds documentation for a property.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @author Jamie Ho
 * @author Bhavesh Patel (Modified)
 */
public class PropertyBuilder extends AbstractMemberBuilder {

    /**
     * The class whose properties are being documented.
     */
    private final TypeElement typeElement;

    /**
     * The visible properties for the given class.
     */
    private final VisibleMemberMap visibleMemberMap;

    /**
     * The writer to output the property documentation.
     */
    private final PropertyWriter writer;

    /**
     * The list of properties being documented.
     */
    private final List<Element> properties;

    /**
     * The index of the current property that is being documented at this point
     * in time.
     */
    private ExecutableElement currentProperty;

    /**
     * Construct a new PropertyBuilder.
     *
     * @param context  the build context.
     * @param typeElement the class whoses members are being documented.
     * @param writer the doclet specific writer.
     */
    private PropertyBuilder(Context context,
            TypeElement typeElement,
            PropertyWriter writer) {
        super(context);
        this.typeElement = typeElement;
        this.writer = writer;
        visibleMemberMap = configuration.getVisibleMemberMap(typeElement,
                VisibleMemberMap.Kind.PROPERTIES);
        properties = visibleMemberMap.getMembers(typeElement);
    }

    /**
     * Construct a new PropertyBuilder.
     *
     * @param context  the build context.
     * @param typeElement the class whoses members are being documented.
     * @param writer the doclet specific writer.
     * @return the new PropertyBuilder
     */
    public static PropertyBuilder getInstance(Context context,
            TypeElement typeElement,
            PropertyWriter writer) {
        return new PropertyBuilder(context, typeElement, writer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "PropertyDetails";
    }

    /**
     * Returns whether or not there are members to document.
     *
     * @return whether or not there are members to document
     */
    @Override
    public boolean hasMembersToDocument() {
        return !properties.isEmpty();
    }

    /**
     * Build the property documentation.
     *
     * @param node the XML element that specifies which components to document
     * @param memberDetailsTree the content tree to which the documentation will be added
     * @throws DocletException if there is a problem while building the documentation
     */
    public void buildPropertyDoc(XMLNode node, Content memberDetailsTree) throws DocletException {
        if (writer == null) {
            return;
        }
        if (hasMembersToDocument()) {
            Content propertyDetailsTree = writer.getPropertyDetailsTreeHeader(typeElement,
                    memberDetailsTree);
            Element lastElement = properties.get(properties.size() - 1);
            for (Element property : properties) {
                currentProperty = (ExecutableElement)property;
                Content propertyDocTree = writer.getPropertyDocTreeHeader(currentProperty,
                        propertyDetailsTree);
                buildChildren(node, propertyDocTree);
                propertyDetailsTree.addContent(writer.getPropertyDoc(
                        propertyDocTree, currentProperty == lastElement));
            }
            memberDetailsTree.addContent(
                    writer.getPropertyDetails(propertyDetailsTree));
        }
    }

    /**
     * Build the signature.
     *
     * @param node the XML element that specifies which components to document
     * @param propertyDocTree the content tree to which the documentation will be added
     */
    public void buildSignature(XMLNode node, Content propertyDocTree) {
        propertyDocTree.addContent(writer.getSignature(currentProperty));
    }

    /**
     * Build the deprecation information.
     *
     * @param node the XML element that specifies which components to document
     * @param propertyDocTree the content tree to which the documentation will be added
     */
    public void buildDeprecationInfo(XMLNode node, Content propertyDocTree) {
        writer.addDeprecated(currentProperty, propertyDocTree);
    }

    /**
     * Build the comments for the property.  Do nothing if
     * {@link Configuration#nocomment} is set to true.
     *
     * @param node the XML element that specifies which components to document
     * @param propertyDocTree the content tree to which the documentation will be added
     */
    public void buildPropertyComments(XMLNode node, Content propertyDocTree) {
        if (!configuration.nocomment) {
            writer.addComments(currentProperty, propertyDocTree);
        }
    }

    /**
     * Build the tag information.
     *
     * @param node the XML element that specifies which components to document
     * @param propertyDocTree the content tree to which the documentation will be added
     */
    public void buildTagInfo(XMLNode node, Content propertyDocTree) {
        writer.addTags(currentProperty, propertyDocTree);
    }

    /**
     * Return the property writer for this builder.
     *
     * @return the property writer for this builder.
     */
    public PropertyWriter getWriter() {
        return writer;
    }
}
