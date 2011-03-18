package cz.cvut.kbss.owlpersistence.model.ic;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class IntegrityConstraintFactory {

	public static DataParticipationConstraint MinDataParticipationConstraint(
			final OWLClass s, final OWLDataProperty p, final OWLDatatype o,
			final int card) {
		return new DataParticipationConstraintImpl(s, p, o, card, -1);
	}

	public static DataParticipationConstraint MaxDataParticipationConstraint(
			final OWLClass s, final OWLDataProperty p, final OWLDatatype o,
			final int card) {
		return new DataParticipationConstraintImpl(s, p, o, 0, card);
	}

	public static DataParticipationConstraint DataParticipationConstraint(
			final OWLClass s, final OWLDataProperty p, final OWLDatatype o,
			final int min, int max) {
		return new DataParticipationConstraintImpl(s, p, o, min, max);
	}

	public static ObjectParticipationConstraint MinObjectParticipationConstraint(
			final OWLClass s, final OWLObjectProperty p, final OWLClass o,
			final int card) {
		return new ObjectParticipationConstraintImpl(s, p, o, card, -1);
	}

	public static ObjectParticipationConstraint MaxObjectParticipationConstraint(
			final OWLClass s, final OWLObjectProperty p, final OWLClass o,
			final int card) {
		return new ObjectParticipationConstraintImpl(s, p, o, 0, card);
	}

	public static ObjectParticipationConstraint ObjectParticipationConstraint(
			final OWLClass s, final OWLObjectProperty p, final OWLClass o,
			final int min, int max) {
		return new ObjectParticipationConstraintImpl(s, p, o, min, max);
	}

	public static ObjectDomainConstraint ObjectPropertyDomainConstraint(
			final OWLObjectProperty p, final OWLClass s) {
		return new ObjectDomainConstraintImpl(p, s);
	}

	public static ObjectRangeConstraint ObjectPropertyRangeConstraint(
			final OWLClass subj, final OWLObjectProperty p, final OWLClass s) {
		return new ObjectRangeConstraintImpl(subj, p, s);
	}

	public static DataDomainConstraint DataPropertyDomainConstraint(
			final OWLDataProperty p, final OWLClass s) {
		return new DataDomainConstraintImpl(p, s);
	}

	public static DataRangeConstraint DataPropertyRangeConstraint(
			final OWLClass subj, final OWLDataProperty p, final OWLDatatype s) {
		return new DataRangeConstraintImpl(subj, p, s);
	}
}
