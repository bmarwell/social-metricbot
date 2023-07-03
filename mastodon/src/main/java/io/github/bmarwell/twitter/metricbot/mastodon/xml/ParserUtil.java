package io.github.bmarwell.twitter.metricbot.mastodon.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ParserUtil {

    private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
    public static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    public String getRawText(String htmlContent) {
        StreamSource streamSource = new StreamSource(this.getClass().getResourceAsStream("onlytext.xsl"));

        try (var bas = new ByteArrayInputStream(
                        ("<status>" + htmlContent + "</status>").getBytes(StandardCharsets.UTF_8));
                var bos = new ByteArrayOutputStream()) {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer(streamSource);
            DocumentBuilder builder = DBF.newDocumentBuilder();
            Document document = builder.parse(bas);

            transformer.transform(new DOMSource(document), new StreamResult(bos));
            bos.flush();

            return bos.toString(StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        } catch (TransformerException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
