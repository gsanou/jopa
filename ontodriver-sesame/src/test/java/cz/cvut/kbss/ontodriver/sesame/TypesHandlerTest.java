package cz.cvut.kbss.ontodriver.sesame;

import cz.cvut.kbss.ontodriver.sesame.connector.Connector;
import cz.cvut.kbss.ontodriver_new.model.Axiom;
import cz.cvut.kbss.ontodriver_new.model.NamedResource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TypesHandlerTest {

    private static final String STR_PK = "http://krizik.felk.cvut.cz/ontologies/jopa/entityX";
    private static final String TYPE_BASE = "http://krizik.felk.cvut.cz/ontologies/jopa/type";
    private static final NamedResource NAMED_PK = NamedResource.create(STR_PK);

    private static Repository repo;
    private static ValueFactory vf;

    @Mock
    private Connector connectorMock;

    private TypesHandler handler;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final MemoryStore mStore = new MemoryStore();
        repo = new SailRepository(mStore);
        vf = repo.getValueFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.handler = new TypesHandler(connectorMock, vf);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        repo.shutDown();
    }

    @Test
    public void getsTypesWithoutContext() throws Exception {
        final Collection<Statement> statements = initStatements(null);
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, false, (URI[]) null)).thenReturn(statements);
        final Set<Axiom<java.net.URI>> res = handler.getTypes(NamedResource.create(STR_PK), null, false);
        assertEquals(statements.size(), res.size());
        final Set<java.net.URI> uris = new HashSet<>();
        for (Axiom<java.net.URI> u : res) {
            uris.add(u.getValue().getValue());
        }
        for (Statement stmt : statements) {
            assertTrue(uris.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
    }

    private Collection<Statement> initStatements(URI context) {
        final Collection<Statement> statements = new HashSet<>();
        final URI subject = vf.createURI(STR_PK);
        for (int i = 0; i < 6; i++) {
            final Statement stmt = vf.createStatement(subject, RDF.TYPE, vf.createURI(TYPE_BASE + i), context);
            statements.add(stmt);
        }
        return statements;
    }

    @Test
    public void getsTypesIncludingInferredAndInContext() throws Exception {
        final URI context = vf.createURI("http://krizik.felk.cvut.cz/ontologies/contextOne");
        final Collection<Statement> statements = initStatements(context);
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, true, context)).thenReturn(statements);
        final Set<Axiom<java.net.URI>> res = handler.getTypes(NAMED_PK, java.net.URI.create(context.stringValue()), true);
        assertEquals(statements.size(), res.size());
        final Set<java.net.URI> uris = new HashSet<>();
        for (Axiom<java.net.URI> u : res) {
            uris.add(u.getValue().getValue());
        }
        for (Statement stmt : statements) {
            assertTrue(uris.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
        verify(connectorMock).findStatements(vf.createURI(STR_PK), RDF.TYPE, null, true, context);
    }

    @Test
    public void getsEmptyTypes() throws Exception {
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, false, (URI[]) null)).thenReturn(Collections.<Statement>emptySet());
        final Set<Axiom<java.net.URI>> res = handler.getTypes(NamedResource.create(STR_PK), null, true);
        assertTrue(res.isEmpty());
    }

    @Test
    public void persistsTypes() throws Exception {
        final Set<java.net.URI> types = initTypes();
        handler.persistTypes(NAMED_PK, null, types);
        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(connectorMock).addStatements(captor.capture());
        final Collection<?> statements = captor.getValue();
        assertEquals(types.size(), statements.size());
        for (Object o : statements) {
            final Statement stmt = (Statement) o;
            assertEquals(STR_PK, stmt.getSubject().stringValue());
            assertTrue(types.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
    }

    private Set<java.net.URI> initTypes() {
        final Set<java.net.URI> types = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            types.add(java.net.URI.create(TYPE_BASE + i));
        }
        return types;
    }

    @Test
    public void updatesTypesByPersistingSinceOriginalWereEmpty() throws Exception {
        final Set<java.net.URI> types = initTypes();
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, false, (URI[]) null)).thenReturn(Collections.<Statement>emptySet());
        handler.updateTypes(NAMED_PK, null, types);
        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(connectorMock).addStatements(captor.capture());
        final Collection<?> statements = captor.getValue();
        assertEquals(types.size(), statements.size());
        for (Object o : statements) {
            final Statement stmt = (Statement) o;
            assertEquals(STR_PK, stmt.getSubject().stringValue());
            assertTrue(types.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
    }

    @Test
    public void updatesTypesByRemovingAllOriginalOnes() throws Exception {
        final Collection<Statement> origTypes = initStatements(null);
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, false, (URI[]) null)).thenReturn(origTypes);
        handler.updateTypes(NAMED_PK, null, Collections.<java.net.URI>emptySet());
        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(connectorMock).removeStatements(captor.capture());
        assertEquals(origTypes, captor.getValue());
    }

    @Test
    public void updatesTypesByAddingSomeNew() throws Exception {
        final URI context = vf.createURI("http://krizik.felk.cvut.cz/ontologies/contextOne");
        final Collection<Statement> origTypes = initStatements(context);
        final Set<java.net.URI> newTypes = new HashSet<>();
        for (Statement s : origTypes) {
            newTypes.add(java.net.URI.create(s.getObject().stringValue()));
        }
        final Set<java.net.URI> addedTypes = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            final java.net.URI added = java.net.URI.create(TYPE_BASE + "_added" + i);
            newTypes.add(added);
            addedTypes.add(added);
        }
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, false, context)).thenReturn(origTypes);
        handler.updateTypes(NAMED_PK, java.net.URI.create(context.stringValue()), newTypes);
        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(connectorMock).addStatements(captor.capture());
        final Collection<?> statements = captor.getValue();
        assertEquals(addedTypes.size(), statements.size());
        for (Object o : statements) {
            final Statement stmt = (Statement) o;
            assertTrue(addedTypes.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
        verify(connectorMock, never()).removeStatements(anyCollection());
    }

    @Test
    public void updatesTypesByRemovingSome() throws Exception {
        final Collection<Statement> origTypes = initStatements(null);
        final Set<java.net.URI> newTypes = new HashSet<>();
        final Set<java.net.URI> removedTypes = new HashSet<>();
        int i = 0;
        for (Statement s : origTypes) {
            final java.net.URI type = java.net.URI.create(s.getObject().stringValue());
            if (i % 2 == 0) {
                // Remove every even type
                removedTypes.add(type);
            } else {
                newTypes.add(type);
            }
            i++;
        }
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, false, (URI []) null)).thenReturn(origTypes);
        handler.updateTypes(NAMED_PK, null, newTypes);
        final ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(connectorMock).removeStatements(captor.capture());
        final Collection<?> statements = captor.getValue();
        assertEquals(removedTypes.size(), statements.size());
        for (Object o : statements) {
            final Statement stmt = (Statement) o;
            assertTrue(removedTypes.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
        verify(connectorMock, never()).addStatements(anyCollection());
    }

    @Test
    public void updatesTypesByRemovingSomeAndAddingOthers() throws Exception {
        final URI context = vf.createURI("http://krizik.felk.cvut.cz/ontologies/contextOne");
        final Collection<Statement> origTypes = initStatements(context);
        final Set<java.net.URI> newTypes = new HashSet<>();
        final Set<java.net.URI> removedTypes = new HashSet<>();
        int i = 0;
        for (Statement s : origTypes) {
            final java.net.URI type = java.net.URI.create(s.getObject().stringValue());
            if (i % 2 == 0) {
                // Remove every even type
                removedTypes.add(type);
            } else {
                newTypes.add(type);
            }
            i++;
        }
        final Set<java.net.URI> addedTypes = new HashSet<>();
        for (i = 0; i < 5; i++) {
            final java.net.URI added = java.net.URI.create(TYPE_BASE + "_added" + i);
            newTypes.add(added);
            addedTypes.add(added);
        }
        when(connectorMock.findStatements(vf.createURI(STR_PK), RDF.TYPE, null, false, context)).thenReturn(origTypes);
        handler.updateTypes(NAMED_PK, java.net.URI.create(context.stringValue()), newTypes);
        final ArgumentCaptor<Collection> captorAdded = ArgumentCaptor.forClass(Collection.class);
        verify(connectorMock).addStatements(captorAdded.capture());
        final Collection<?> added = captorAdded.getValue();
        assertEquals(addedTypes.size(), added.size());
        for (Object o : added) {
            final Statement stmt = (Statement) o;
            assertTrue(addedTypes.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
        final ArgumentCaptor<Collection> captorRemoved = ArgumentCaptor.forClass(Collection.class);
        verify(connectorMock).removeStatements(captorRemoved.capture());
        final Collection<?> removed = captorRemoved.getValue();
        assertEquals(removedTypes.size(), removed.size());
        for (Object o : removed) {
            final Statement stmt = (Statement) o;
            assertTrue(removedTypes.contains(java.net.URI.create(stmt.getObject().stringValue())));
        }
    }
}