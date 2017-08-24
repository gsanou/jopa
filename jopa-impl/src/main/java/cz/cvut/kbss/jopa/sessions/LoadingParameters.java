/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.sessions;

import cz.cvut.kbss.jopa.model.descriptors.Descriptor;

import java.net.URI;

public final class LoadingParameters<T> {

    private final Class<T> cls;
    private final URI identifier;
    private final Descriptor descriptor;
    private final boolean forceEager;
    private boolean bypassCache;

    public LoadingParameters(Class<T> cls, URI identifier, Descriptor descriptor) {
        this.cls = cls;
        this.identifier = identifier;
        this.descriptor = descriptor;
        this.forceEager = false;
        assert paramsLoaded();
    }

    private boolean paramsLoaded() {
        return cls != null && identifier != null && descriptor != null;
    }

    public LoadingParameters(Class<T> cls, URI identifier, Descriptor descriptor, boolean forceEager) {
        this.cls = cls;
        this.identifier = identifier;
        this.descriptor = descriptor;
        this.forceEager = forceEager;
        assert paramsLoaded();
    }

    public Class<T> getEntityType() {
        return cls;
    }

    public URI getIdentifier() {
        return identifier;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public boolean isForceEager() {
        return forceEager;
    }

    public boolean shouldBypassCache() {
        return bypassCache;
    }

    public void bypassCache() {
        this.bypassCache = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoadingParameters)) return false;

        LoadingParameters<?> that = (LoadingParameters<?>) o;

        if (forceEager != that.forceEager) return false;
        if (bypassCache != that.bypassCache) return false;
        if (!cls.equals(that.cls)) return false;
        if (!identifier.equals(that.identifier)) return false;
        return descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        int result = cls.hashCode();
        result = 31 * result + identifier.hashCode();
        result = 31 * result + descriptor.hashCode();
        result = 31 * result + (forceEager ? 1 : 0);
        result = 31 * result + (bypassCache ? 1 : 0);
        return result;
    }
}
