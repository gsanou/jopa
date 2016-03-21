package cz.cvut.kbss.ontodriver.owlapi.util;

import cz.cvut.kbss.ontodriver.owlapi.config.OwlapiOntoDriverProperties;
import cz.cvut.kbss.ontodriver.owlapi.exception.MappingFileParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MappingFileParser {

    private static final Logger LOG = LoggerFactory.getLogger(MappingFileParser.class);

    private static final String[] REMOTE_URL_SCHEMES = {"http://", "https://", "ftp://", "sftp://"};

    private final File mappingFile;

    private final String delimiter;

    public MappingFileParser(Map<String, String> properties) {
        final String mappingFilePath = properties.get(OwlapiOntoDriverProperties.MAPPING_FILE_LOCATION);
        assert mappingFilePath != null;
        this.mappingFile = resolveMappingFile(mappingFilePath);
        if (properties.containsKey(OwlapiOntoDriverProperties.IRI_MAPPING_DELIMITER)) {
            this.delimiter = properties.get(OwlapiOntoDriverProperties.IRI_MAPPING_DELIMITER);
        } else {
            this.delimiter = OwlapiOntoDriverProperties.DEFAULT_IRI_MAPPING_DELIMITER;
        }
    }

    private File resolveMappingFile(String path) {
        File mapping = new File(path);
        if (mapping.exists()) {
            return mapping;
        }
        try {
            mapping = new File(URI.create(path));
            if (mapping.exists()) {
                return mapping;
            }
        } catch (IllegalArgumentException e) {
            throw new MappingFileParserException(
                    "Mapping file path " + path + " is neither a valid file path nor a valid URI.");
        }
        throw new MappingFileParserException("Mapping file " + path + " does not exist.");
    }

    public Map<URI, URI> getMappings() {
        final Map<URI, URI> map = new HashMap<>();
        String line;
        final File defaultDir = mappingFile.getParentFile();
        try (final BufferedReader r = new BufferedReader(new FileReader(mappingFile))) {
            while ((line = r.readLine()) != null) {
                final StringTokenizer t = new StringTokenizer(line, delimiter);
                if (t.countTokens() != 2) {
                    LOG.warn("Ignoring line '" + line + "' - invalid number of tokens = " + t.countTokens());
                    continue;
                }
                final String uriName = t.nextToken().trim();
                final String fileName = t.nextToken().trim();
                final URI fileUri = resolveLocation(defaultDir, fileName);

                LOG.trace("Mapping ontology {} to location {}.", uriName, fileUri);
                map.put(URI.create(uriName), fileUri);
            }
        } catch (IOException e) {
            LOG.error("Unable to parse mapping file." + e);
            throw new MappingFileParserException(e);
        }
        return map;
    }

    private URI resolveLocation(File defaultDir, String targetUri) {
        for (String scheme : REMOTE_URL_SCHEMES) {
            if (targetUri.startsWith(scheme)) {
                try {
                    return URI.create(targetUri);
                } catch (IllegalArgumentException e) {
                    LOG.error("Target URI {} looks like a remote URI, but is not valid.", targetUri);
                    throw new MappingFileParserException(e);
                }
            }
        }
        final File actualFile =
                (new File(targetUri).isAbsolute()) ? new File(targetUri) : new File(defaultDir, targetUri);
        return actualFile.toURI();
    }
}