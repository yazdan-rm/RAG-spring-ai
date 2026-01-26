package com.eazybytes.openai.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

//@Component
public class HRPolicyLoader {

    private final VectorStore vectorStore;

    @Value("classpath:military.pdf")
    Resource policyFile;


    public HRPolicyLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadPDF() {

        TikaDocumentReader reader = new TikaDocumentReader(policyFile);
        List<Document> docs = reader.get();

        List<Document> logicalChunks = docs.stream()
                .flatMap(doc -> {
                    String content = normalizePersian(doc.getFormattedContent());

                    String[] parts = content.split(
                            "(?=\\n\\s*(فصل\\s+\\S+|بخش\\s+\\S+|ماده\\s+\\d+|\\d+[\\-.]\\d+|\\d+\\.|[-–—•]))"
                    );

                    return Arrays.stream(parts)
                            .map(text -> new Document(text.trim()));
                })
                .toList();

        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(350)        // Persian tokens are denser
                .withMaxNumChunks(500)
                .build();

        List<Document> finalChunks = logicalChunks.stream()
                .flatMap(doc -> splitter.split(List.of(doc)).stream())
                .toList();

        vectorStore.add(finalChunks);
    }

    private String normalizePersian(String text) {
        return text
                .replace("ي", "ی")
                .replace("ك", "ک")
                .replace("\u200c", " ") // ZWNJ
                .replaceAll("[ \t]+", " ")
                .trim();
    }

}