package cz.cvut.kbss.ontodriver_new.model;

import java.net.URI;

class PropertyAssertion extends Assertion {

	private static final long serialVersionUID = 8288252700890646040L;

	public PropertyAssertion(URI identifier, boolean isInferred) {
		super(identifier, isInferred);
	}

	@Override
	public AssertionType getType() {
		return AssertionType.PROPERTY;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		return prime * super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
}