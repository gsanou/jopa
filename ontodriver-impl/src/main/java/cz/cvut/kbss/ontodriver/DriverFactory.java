package cz.cvut.kbss.ontodriver;

import java.util.List;

import cz.cvut.kbss.jopa.model.Repository;
import cz.cvut.kbss.jopa.model.EntityDescriptor;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;

public interface DriverFactory extends Closeable {

	/**
	 * Retrieves a list of repositories available to this factory. </p>
	 * 
	 * The repositories in the list are sorted by their priority. The returned
	 * list is unmodifiable
	 * 
	 * @return List of repositories
	 */
	public List<Repository> getRepositories();

	/**
	 * Creates and returns storage module. </p>
	 * 
	 * Implementations may choose to pool storage modules or create lazy loaded
	 * proxies.
	 * 
	 * @param repository
	 *            Repository identifier, the set of contexts can be empty
	 * @param persistenceProvider
	 *            Facade representing the persistence provider
	 * @param autoCommit
	 *            {@code true} if the module is for an auto commit operation
	 * @return StorageModule
	 * @throws OntoDriverException
	 *             If called on a closed factory or if an ontology access error
	 *             occurs
	 */
	public StorageModule createStorageModule(EntityDescriptor repository,
			PersistenceProviderFacade persistenceProvider, boolean autoCommit)
			throws OntoDriverException;

	/**
	 * Releases the specified storage module. </p>
	 * 
	 * Releasing the module can mean closing it as well as just returning it
	 * back to the module pool. This depends on the factory implementation. </p>
	 * 
	 * Releasing a module twice results in an exception.
	 * 
	 * @param module
	 *            The module to release
	 * @throws OntoDriverException
	 *             If called on a closed factory, if the module has been already
	 *             released or if an ontology access error occurs
	 */
	public void releaseStorageModule(StorageModule module) throws OntoDriverException;

	/**
	 * Creates and returns a storage connector. </p>
	 * 
	 * Implementations may choose to pool storage modules or create lazy loaded
	 * proxies.
	 * 
	 * @param repository
	 *            Repository identifier, the set of contexts can be empty
	 * @param autoCommit
	 *            {@code true} if the connector is for an auto commit operation
	 * @return StorageConnector
	 * @throws OntoDriverException
	 *             If called on a closed factory or if an ontology access error
	 *             occurs
	 */
	public StorageConnector createStorageConnector(EntityDescriptor repository, boolean autoCommit)
			throws OntoDriverException;

	/**
	 * Releases the specified storage connector. </p>
	 * 
	 * Releasing the connector can mean closing it as well as just returning it
	 * back to the connector pool. This depends on the factory implementation.
	 * </p>
	 * 
	 * Releasing a connector twice results in an exception.
	 * 
	 * @param connector
	 *            The connector to release
	 * @throws OntoDriverException
	 *             If called on a closed factory, if the module has been already
	 *             released or if an ontology access error occurs
	 */
	public void releaseStorageConnector(StorageConnector connector) throws OntoDriverException;

	/**
	 * Creates internal OntoDriver statement used for SPARQL processing.
	 * 
	 * @param statement
	 *            The statement received from JOPA
	 * @return Internal statement object
	 * @throws OntoDriverException
	 *             If called on a closed factory
	 */
	public DriverStatement createStatement(JopaStatement statement) throws OntoDriverException;
}
