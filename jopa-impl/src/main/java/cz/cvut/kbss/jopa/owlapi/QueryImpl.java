/**
 * Copyright (C) 2011 Czech Technical University in Prague
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

package cz.cvut.kbss.jopa.owlapi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.exceptions.NoUniqueResultException;
import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.model.query.Query;
import cz.cvut.kbss.jopa.sessions.ConnectionWrapper;
import cz.cvut.kbss.jopa.utils.ErrorUtils;
import cz.cvut.kbss.ontodriver.ResultSet;
import cz.cvut.kbss.ontodriver.Statement;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;

public class QueryImpl implements Query<List<String>> {

	private final String query;
	private final Set<URI> contexts;
	private final boolean sparql;
	private final ConnectionWrapper connection;

	private int maxResults;
	private boolean useBackupOntology;

	// sparql=false -> abstract syntax
	public QueryImpl(final String query, final boolean sparql, final ConnectionWrapper connection) {
		this.query = Objects.requireNonNull(query, ErrorUtils.constructNPXMessage("query"));
		this.connection = Objects.requireNonNull(connection,
				ErrorUtils.constructNPXMessage("connection"));
		this.contexts = new HashSet<>();
		this.useBackupOntology = false;
		this.sparql = sparql;
		this.maxResults = Integer.MAX_VALUE;
	}

	public List<List<String>> getResultList() {
		if (!sparql) {
			throw new NotYetImplementedException();
		}
		try {
			if (maxResults == 0) {
				return Collections.emptyList();
			}
			return getResultListImpl(maxResults);
		} catch (OntoDriverException e) {
			throw new OWLPersistenceException("Exception caught when evaluating query " + query, e);
		}
	}

	public List<String> getSingleResult() {
		try {
			// Call it with maxResults = 2 just to see whether there are more
			final List<List<String>> list = getResultListImpl(2);
			if (list.isEmpty()) {
				throw new NoResultException("No result found for querytransactional " + query);
			}
			if (list.size() > 1) {
				throw new NoUniqueResultException("Multiple results found for query " + query);
			}
			return list.get(0);
		} catch (OntoDriverException e) {
			throw new OWLPersistenceException("Exception caught when evaluating query " + query, e);
		}
	}

	@Override
	public int getMaxResults() {
		return maxResults;
	}

	public Query<List<String>> setMaxResults(int maxResults) {
		if (maxResults < 0) {
			throw new IllegalArgumentException(
					"Cannot set maximum number of results to less than 0.");
		}
		this.maxResults = maxResults;
		return this;
	}

	/**
	 * Sets ontology used for processing of this query. </p>
	 * 
	 * @param useTransactional
	 *            If true, the backup (central) ontology is used, otherwise the
	 *            transactional ontology is used (default)
	 */
	public void setUseBackupOntology(boolean useBackupOntology) {
		this.useBackupOntology = useBackupOntology;
	}

	private List<List<String>> getResultListImpl(int maxResults) throws OntoDriverException {
		assert maxResults > 0;
		final Statement stmt = connection.createStatement();
		if (useBackupOntology) {
			stmt.setUseBackupOntology();
		} else {
			stmt.setUseTransactionalOntology();
		}
		URI[] uris = new URI[contexts.size()];
		uris = contexts.toArray(uris);
		final ResultSet rs = stmt.executeQuery(query, uris);
		// TODO Fix the bug in Sesame statement, the context information gets
		// lost along the way
		try {
			final int cols = rs.getColumnCount();
			int cnt = 0;
			final List<List<String>> res = new ArrayList<List<String>>();
			// TODO register this as observer on the result set so that
			// additional results can be loaded asynchronously
			while (rs.hasNext() && cnt < maxResults) {
				rs.next();
				final List<String> row = new ArrayList<String>(cols);
				res.add(row);
				for (int i = 0; i < cols; i++) {
					final String ob = rs.getString(i);
					row.add(ob);
				}
				cnt++;
			}
			return res;
		} finally {
			rs.close();
		}
	}

	@Override
	public Query<List<String>> addContext(URI context) {
		Objects.requireNonNull(context, ErrorUtils.constructNPXMessage("context"));
		contexts.add(context);
		return this;
	}

	@Override
	public Query<List<String>> addContexts(Collection<URI> contexts) {
		Objects.requireNonNull(contexts, ErrorUtils.constructNPXMessage("contexts"));
		contexts.addAll(contexts);
		return this;
	}

	@Override
	public Query<List<String>> clearContexts() {
		contexts.clear();
		return this;
	}
}
