package cz.cvut.kbss.jopa.owl2java;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OWL2JavaTransformerTest {

    private static final String MAPPING_FILE_NAME = "mapping";
    private static final String IC_ONTOLOGY_IRI = "http://krizik.felk.cvut.cz/ontologies/owl2java-ics.owl";
    private static final String CONTEXT = "owl2java-ic";

    private static final String VOCABULARY_FILE = "Vocabulary.java";

    // Thing is always generated by OWL2Java
    private static final List<String> KNOWN_CLASSES = Arrays
            .asList("Agent", "Person", "Organization", "Answer", "Question", "Report", "Thing");

    private String mappingFilePath;

    private OWL2JavaTransformer transformer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        this.mappingFilePath = resolveMappingFilePath();
        this.transformer = new OWL2JavaTransformer();
    }

    private String resolveMappingFilePath() {
        final File mf = new File(getClass().getClassLoader().getResource(MAPPING_FILE_NAME).getFile());
        return mf.getAbsolutePath();
    }

    @Test
    public void listContextsShowsContextsInICFile() {
        transformer.setOntology(IC_ONTOLOGY_IRI, mappingFilePath, true);
        final Collection<String> contexts = transformer.listContexts();
        assertEquals(1, contexts.size());
        assertEquals(CONTEXT, contexts.iterator().next());
    }

    @Test
    public void transformGeneratesJavaClassesFromIntegrityConstraints() throws Exception {
        final File targetDir = getTempDirectory();
        assertEquals(0, targetDir.listFiles().length);
        transformer.setOntology(IC_ONTOLOGY_IRI, mappingFilePath, true);
        transformer.transform(CONTEXT, "", targetDir.getAbsolutePath(), true);
        final File generatedModel = new File(targetDir.getAbsolutePath() + File.separator + "model");
        assertTrue(generatedModel.listFiles().length > 0);
        for (String fileName : generatedModel.list()) {
            final String className = fileName.substring(0, fileName.indexOf('.'));  // Strip the suffix
            assertTrue(KNOWN_CLASSES.contains(className));
        }
    }

    private File getTempDirectory() throws IOException {
        final File targetDir = Files.createTempDirectory("owl2java-test").toFile();
        targetDir.deleteOnExit();
        return targetDir;
    }

    @Test
    public void transformGeneratesVocabularyFile() throws Exception {
        final File targetDir = getTempDirectory();
        transformer.setOntology(IC_ONTOLOGY_IRI, mappingFilePath, true);
        transformer.transform(CONTEXT, "", targetDir.getAbsolutePath(), true);
        final List<String> fileNames = Arrays.asList(targetDir.list());
        assertTrue(fileNames.contains(VOCABULARY_FILE));
    }

    @Test
    public void transformThrowsIllegalArgumentForUnknownContext() throws Exception {
        final String unknownContext = "someUnknownContext";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Context " + unknownContext + " not found.");
        final File targetDir = getTempDirectory();
        transformer.setOntology(IC_ONTOLOGY_IRI, mappingFilePath, true);
        transformer.transform(unknownContext, "", targetDir.getAbsolutePath(), true);
    }

    @Test
    public void setUnknownOntologyIriThrowsIllegalArgumentException() throws Exception {
        final String unknownOntoIri = "http://krizik.felk.cvut.cz/ontologies/an-unknown-ontology.owl";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unable to load ontology " + unknownOntoIri);
        transformer.setOntology(unknownOntoIri, mappingFilePath, true);
    }

    @Test
    public void setOntologyWithUnknownMappingFileThrowsIllegalArgument() throws Exception {
        final String unknownMappingFile = "/tmp/unknown-mapping-file";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Mapping file " + unknownMappingFile + " not found.");
        transformer.setOntology(IC_ONTOLOGY_IRI, unknownMappingFile, true);
    }

    @Test
    public void generateVocabularyGeneratesOnlyVocabularyFile() throws Exception {
        final File targetDir = getTempDirectory();
        transformer.setOntology(IC_ONTOLOGY_IRI, mappingFilePath, true);
        transformer.generateVocabulary(CONTEXT, targetDir.getAbsolutePath(), false);
        final List<String> fileNames = Arrays.asList(targetDir.list());
        assertEquals(1, fileNames.size());
        assertEquals(VOCABULARY_FILE, fileNames.get(0));
    }
}