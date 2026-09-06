package com.hotelpms.billing.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates a generated FatturaPA XML byte array against the official schema
 * published by Agenzia delle Entrate (FPR12, v1.2.3 — valid since 2026-04-01,
 * {@code https://www.fatturapa.gov.it/}). Schema files are bundled under
 * {@code src/main/resources/xsd/}, not fetched at runtime: the FatturaPA
 * schema {@code <xs:import>}s the W3C XML-DSig schema from a live
 * {@code http://www.w3.org} URL, which would make every XML generation
 * depend on an external site being reachable — {@link #resourceResolver()}
 * redirects that import to the bundled copy instead.
 *
 * <p>{@link Schema} is thread-safe and built once; {@link Validator} is not,
 * so a fresh one is created per call (cheap — it wraps the pre-compiled
 * {@link Schema}, it does not recompile it).
 */
@Component
public class FatturaPaXsdValidator {

    private static final String FATTURAPA_XSD = "xsd/Schema_VFPR12_v1.2.3.xsd";
    private static final String XMLDSIG_XSD = "xsd/xmldsig-core-schema.xsd";
    private static final String XMLDSIG_SCHEMA_LOCATION =
            "http://www.w3.org/TR/2002/REC-xmldsig-core-20020212/xmldsig-core-schema.xsd";

    private final Schema schema;

    /**
     * Compiles the bundled FatturaPA schema once at construction.
     *
     * @throws IllegalStateException if the bundled schema files are missing or invalid —
     *                                a deployment defect, not a per-request condition
     */
    public FatturaPaXsdValidator() {
        try {
            this.schema = compileBundledSchema();
        } catch (final IOException | SAXException ex) {
            // Fails application startup rather than at first invoice export — a broken
            // bundled schema is a deployment defect, not a per-request condition.
            throw new IllegalStateException("FATTURAPA_XSD_SCHEMA_LOAD_FAILED", ex);
        }
    }

    /**
     * Validates the given FatturaPA XML against the bundled schema.
     *
     * @param xml the serialized XML to validate
     * @return the list of validation error messages, empty if the XML is schema-valid
     */
    public List<String> validate(final byte[] xml) {
        final List<String> errors = new ArrayList<>();
        final Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(final SAXParseException exception) {
                // schema warnings (e.g. unused import) are not export-blocking
            }

            @Override
            public void error(final SAXParseException exception) {
                errors.add("line " + exception.getLineNumber() + ": " + exception.getMessage());
            }

            @Override
            public void fatalError(final SAXParseException exception) {
                errors.add("line " + exception.getLineNumber() + ": " + exception.getMessage());
            }
        });
        try {
            validator.validate(new StreamSource(new java.io.ByteArrayInputStream(xml)));
        } catch (final SAXException | IOException ex) {
            errors.add(ex.getMessage());
        }
        return errors;
    }

    private static Schema compileBundledSchema() throws IOException, SAXException {
        final Path tempDirectory = Files.createTempDirectory("hotel-pms-fatturapa-xsd-");
        final Path fatturaPath = tempDirectory.resolve("Schema_VFPR12_v1.2.3.xsd");
        final Path xmldsigPath = tempDirectory.resolve("xmldsig-core-schema.xsd");
        try {
            final String fatturaContents;
            try (InputStream fatturaXsd = new ClassPathResource(FATTURAPA_XSD).getInputStream()) {
                fatturaContents = new String(fatturaXsd.readAllBytes(), StandardCharsets.UTF_8)
                        .replace(XMLDSIG_SCHEMA_LOCATION, xmldsigPath.getFileName().toString());
            }
            Files.writeString(fatturaPath, fatturaContents, StandardCharsets.UTF_8);
            try (InputStream xmldsigXsd = new ClassPathResource(XMLDSIG_XSD).getInputStream()) {
                final String xmldsigContents = new String(xmldsigXsd.readAllBytes(), StandardCharsets.UTF_8);
                Files.writeString(xmldsigPath, withoutExternalDtd(xmldsigContents), StandardCharsets.UTF_8);
            }

            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
            return factory.newSchema(fatturaPath.toFile());
        } finally {
            Files.deleteIfExists(fatturaPath);
            Files.deleteIfExists(xmldsigPath);
            Files.deleteIfExists(tempDirectory);
        }
    }

    private static String withoutExternalDtd(final String schemaContents) {
        final int doctypeStart = schemaContents.indexOf("<!DOCTYPE schema");
        final int doctypeEnd = schemaContents.indexOf("]>", doctypeStart);
        if (doctypeStart < 0 || doctypeEnd < 0) {
            return schemaContents;
        }
        return schemaContents.substring(0, doctypeStart) + schemaContents.substring(doctypeEnd + 2);
    }
}
