/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Naive implementation, always return FS directory, do not try to cache or optimize anything,
 * recreate if it does not exist
 *
 */
public class SimpleIndexSource implements IndexSource {
    private File m_indexDirectory;

    private boolean m_createDirectory;

    private boolean m_createIndex;

    private Analyzer m_analyzer;

    private Directory getDirectory() {
        try {
            Directory directory = createDirectory(m_indexDirectory);
            m_createDirectory = false;
            return directory;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overwrite to create different directory
     *
     * @throws IOException
     */
    protected Directory createDirectory(File file) throws IOException {
        return FSDirectory.open(file);
    }

    public void setIndexDirectoryName(String indexDirectoryName) {
        m_indexDirectory = new File(indexDirectoryName);
        m_createDirectory = !m_indexDirectory.exists();
        m_createIndex = m_createDirectory;
    }

    public void setAnalyzer(Analyzer analyzer) {
        m_analyzer = analyzer;
    }

    public IndexReader getReader() throws IOException {
        ensureIndexExists();
        return DirectoryReader.open(getDirectory());
    }

    public IndexWriter getWriter(boolean createNew) throws IOException {
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_4, m_analyzer);
        iwc.setOpenMode(createNew || m_createIndex ? OpenMode.CREATE : OpenMode.APPEND);
        IndexWriter writer = new IndexWriter(getDirectory(), iwc);
        m_createIndex = false;
        return writer;
    }

    public IndexSearcher getSearcher() throws IOException {
        ensureIndexExists();
        DirectoryReader dirReader = DirectoryReader.open(getDirectory());
        return new IndexSearcher(dirReader);
    }

    private void ensureIndexExists() throws IOException {
        if (m_createIndex) {
            IndexWriter writer = getWriter(false);
            writer.close();
        }
    }
}
