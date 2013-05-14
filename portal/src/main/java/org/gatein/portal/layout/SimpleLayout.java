/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.gatein.portal.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import juzu.PropertyMap;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.page.PageState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleLayout extends Layout {

    /** . */
    private final Node root;

    public SimpleLayout(Node root) {
        this.root = root;
    }

    private abstract static class Node {
        abstract void render(Map<String, String> windows, PageState state, Appendable to) throws IOException;
    }

    private static class Container extends Node {

        /** . */
        final Container parent;

        /** . */
        final ArrayList<Node> children = new ArrayList<Node>();

        private Container(Container parent) {
            this.parent = parent;
        }

        @Override
        void render(Map<String, String> windows, PageState state, Appendable to) throws IOException {
            to.append("<div>");
            for (Node child : children) {
                child.render(windows, state, to);
            }
            to.append("</div>");
        }
    }

    private static class Window extends Node {

        /** . */
        final String name;

        /** . */
        final ElementState.Window state;

        private Window(String name, ElementState.Window state) {
            this.name = name;
            this.state = state;
        }

        @Override
        void render(Map<String, String> fragments, PageState state, Appendable to) throws IOException {
            String fragment = fragments.get(name);
            to.append("<div>");
            to.append("<div>").append(this.state.properties.get(ElementState.Window.TITLE)).append("</div>");
            to.append("<div>").append(fragment).append("</div>");
            to.append("</div>");
        }
    }

    @Override
    public void render(Map<String, String> fragments, PageState state, PropertyMap properties, Appendable to) throws IOException {
        root.render(fragments, state, to);
    }

    public static class Builder implements LayoutBuilder {

        /** . */
        Container current = new Container(null);

        @Override
        public void beginContainer(String name, ElementState.Container state) {
            Container container = new Container(current);
            current.children.add(container);
            current = container;
        }

        @Override
        public void window(String name, ElementState.Window state) {
            current.children.add(new Window(name, state));
        }

        @Override
        public void endContainer(String name, ElementState.Container state) {
            current = current.parent;
        }

        @Override
        public Layout build() {
            return new SimpleLayout(current);
        }
    }
}
