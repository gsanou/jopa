package cz.cvut.kbss.jopa.model.descriptors;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cz.cvut.kbss.jopa.model.metamodel.FieldSpecification;

/**
 * Defines base descriptor, which is used to specify context information for
 * entities and their fields. </p>
 * 
 * The descriptor hierarchy is a classical <b>Composite</b> pattern.
 * 
 * @author ledvima1
 * 
 */
public abstract class Descriptor implements Serializable {

	private static final long serialVersionUID = 3916845634058702888L;

	protected final URI context;

	protected Descriptor() {
		this.context = null;
	}

	protected Descriptor(URI context) {
		this.context = context;
	}

	/**
	 * Gets context for this descriptor. </p>
	 * 
	 * Note that the context URI may be {@code null}, meaning that the default
	 * context is referenced
	 * 
	 * @return Context URI
	 */
	public URI getContext() {
		return context;
	}

	/**
	 * Gets descriptor for the specified attribute. </p>
	 * 
	 * @param attribute
	 *            Entity attribute, as specified by the application metamodel
	 * @return Descriptor
	 * @throws IllegalArgumentException
	 *             If the descriptor is not available
	 */
	public abstract Descriptor getAttributeDescriptor(FieldSpecification<?, ?> attribute);

	/**
	 * Gets all contexts present in this descriptor. </p>
	 * 
	 * If any of the descriptors specifies the default context, an empty set is
	 * returned. </p>
	 * 
	 * In case of entity descriptor this means recursively asking all of its
	 * attributes for their context.
	 * 
	 * @return Set of context URIs or an empty set, if the default one should be
	 *         used
	 */
	public Set<URI> getAllContexts() {
		Set<URI> contexts = new HashSet<>();
		contexts = getContextsInternal(contexts, new HashSet<Descriptor>());
		return (contexts != null ? contexts : Collections.<URI> emptySet());
	}

	/**
	 * Gets the contexts, discarding any already visited descriptors.
	 * 
	 * @param contexts
	 *            Contexts collected so far
	 * @param visited
	 *            Visited descriptors
	 * @return Already visited contexts + those found in this descriptor
	 */
	protected abstract Set<URI> getContextsInternal(Set<URI> contexts, Set<Descriptor> visited);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Descriptor other = (Descriptor) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (context != null ? context.toString() : "default_context");
	}
}
