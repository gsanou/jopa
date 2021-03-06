/**
 * Copyright (C) 2020 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.sesame;

import cz.cvut.kbss.ontodriver.descriptor.AxiomDescriptor;
import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.Axiom;
import cz.cvut.kbss.ontodriver.sesame.config.RuntimeConfiguration;
import cz.cvut.kbss.ontodriver.sesame.connector.Connector;
import cz.cvut.kbss.ontodriver.sesame.exceptions.SesameDriverException;
import cz.cvut.kbss.ontodriver.sesame.util.AxiomBuilder;
import cz.cvut.kbss.ontodriver.sesame.util.SesameUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

class StatementLoader {

    private final AxiomDescriptor descriptor;
    private final Connector connector;
    private final Resource subject;
    private final ValueFactory vf;
    private final AxiomBuilder axiomBuilder;

    private final int loadAllThreshold;
    private boolean loadAll;
    private boolean includeInferred;

    StatementLoader(RuntimeConfiguration config, AxiomDescriptor descriptor, Connector connector, Resource subject,
                    AxiomBuilder axiomBuilder) {
        this.loadAllThreshold = config.getLoadAllThreshold();
        this.descriptor = descriptor;
        this.connector = connector;
        this.vf = connector.getValueFactory();
        this.subject = subject;
        this.axiomBuilder = axiomBuilder;
    }

    void setIncludeInferred(boolean includeInferred) {
        this.includeInferred = includeInferred;
    }

    Collection<Axiom<?>> loadAxioms(Map<IRI, Assertion> properties)
            throws SesameDriverException {
        this.loadAll = properties.containsValue(Assertion.createUnspecifiedPropertyAssertion(includeInferred));
        if (properties.size() < loadAllThreshold && !loadAll) {
            return loadOneByOne(properties.values());
        } else {
            return loadAll(properties);
        }
    }

    private Collection<Axiom<?>> loadOneByOne(Collection<Assertion> assertions) throws SesameDriverException {
        final Collection<Axiom<?>> result = new HashSet<>();
        for (Assertion a : assertions) {
            final IRI context = SesameUtils.toSesameIri(descriptor.getAssertionContext(a), vf);
            final IRI property = SesameUtils.toSesameIri(a.getIdentifier(), vf);

            final Collection<Statement> statements;
            statements = connector.findStatements(subject, property, null, includeInferred, context);
            for (Statement s : statements) {
                final Axiom<?> axiom = axiomBuilder.statementToAxiom(s, a);
                if (axiom != null) {
                    result.add(axiom);
                }
            }
        }
        return result;
    }

    private Collection<Axiom<?>> loadAll(Map<IRI, Assertion> properties) throws SesameDriverException {
        final Collection<Statement> statements = connector.findStatements(subject, null, null, includeInferred);
        final Collection<Axiom<?>> result = new HashSet<>(statements.size());
        final Assertion unspecified = Assertion.createUnspecifiedPropertyAssertion(includeInferred);
        for (Statement s : statements) {
            if (!properties.containsKey(s.getPredicate()) && !loadAll) {
                continue;
            }
            final Assertion a = getAssertion(properties, s);
            if (!contextMatches(a, s) && !(loadAll && contextMatches(unspecified, s))) {
                continue;
            }
            final Axiom<?> axiom = axiomBuilder.statementToAxiom(s);
            if (axiom != null) {
                result.add(axiom);
            }
        }
        return result;
    }

    private Assertion getAssertion(Map<IRI, Assertion> properties, Statement s) {
        if (properties.containsKey(s.getPredicate())) {
            return properties.get(s.getPredicate());
        }
        return Assertion.createUnspecifiedPropertyAssertion(includeInferred);
    }

    private boolean contextMatches(Assertion a, Statement s) {
        final java.net.URI assertionCtx = descriptor.getAssertionContext(a);
        final Resource statementContext = s.getContext();
        if (assertionCtx == null) {
            // If the assertion should be in default, we don't care about the context of the statement, because
            // the default is a union of all the contexts
            return true;
        }
        return statementContext != null && assertionCtx.toString().equals(statementContext.stringValue());
    }
}
