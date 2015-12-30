/*******************************************************************************
 * Copyright (c) 2014 Ministry of Transport and Communications (Finland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Semantum Oy - initial API and implementation
 *******************************************************************************/
package fi.semantum.strategia;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.Version;

public class Lucene {

    static final class LowerCaseWhitespaceTokenizer extends CharTokenizer {
        /**
         * Construct a new WhitespaceTokenizer. * @param matchVersion Lucene version
         * to match See {@link <a href="#version">above</a>}
         * 
         * @param in
         *          the input to split up into tokens
         */
        public LowerCaseWhitespaceTokenizer(Version matchVersion, Reader in) {
            super(matchVersion, in);
        }

        /**
         * Construct a new WhitespaceTokenizer using a given
         * {@link org.apache.lucene.util.AttributeSource.AttributeFactory}.
         *
         * @param
         *          matchVersion Lucene version to match See
         *          {@link <a href="#version">above</a>}
         * @param factory
         *          the attribute factory to use for this {@link Tokenizer}
         * @param in
         *          the input to split up into tokens
         */
        public LowerCaseWhitespaceTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
            super(matchVersion, factory, in);
        }

        @Override
        protected int normalize(int c) {
            return Character.toLowerCase(c);
        }

        protected boolean isTokenChar(int c) {
            return !Character.isWhitespace(c);
        }
    }
	
    static final class LowerCaseWhitespaceAnalyzer extends Analyzer {

        private final Version matchVersion;

        /**
         * Creates a new {@link WhitespaceAnalyzer}
         * @param matchVersion Lucene version to match See {@link <a href="#version">above</a>}
         */
        public LowerCaseWhitespaceAnalyzer(Version matchVersion) {
            this.matchVersion = matchVersion;
        }

        @Override
        protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
            return new TokenStreamComponents(new LowerCaseWhitespaceTokenizer(matchVersion, reader));
        }
    }

    public static final FieldType STRING_TYPE = new FieldType();

    static {
      STRING_TYPE.setIndexed(true);
      STRING_TYPE.setStored(true);
      STRING_TYPE.setTokenized(true);
      STRING_TYPE.freeze();
    }
    
    private static Analyzer getAnalyzer() {
		return new PerFieldAnalyzerWrapper(new LowerCaseWhitespaceAnalyzer(Version.LUCENE_4_9));
    }
    
    private static Directory getDirectory() throws IOException {
		File f = new File(".");
		File dir = new File(f, "lucene");
        return FSDirectory.open(dir);
    }
    
    public static boolean indexExists() {
		File f = new File(".");
		File dir = new File(f, "lucene");
		return dir.exists();
    }
    
    public static synchronized List<String> search(String search) throws IOException {
    	
    	ArrayList<String> result = new ArrayList<String>();

    	IndexReader reader = null;
    	
    	try {

    		reader = DirectoryReader.open(getDirectory());
    		IndexSearcher searcher = new IndexSearcher(reader);

    		QueryParser parser = new QueryParser(Version.LUCENE_4_9, "text", getAnalyzer());
    		parser.setAllowLeadingWildcard(true);
    		Query query = parser.parse(search); 

    		TopDocs docs = searcher.search(query, Integer.MAX_VALUE);


    		for (ScoreDoc scoreDoc : docs.scoreDocs) {

    			try {

    	    		DocumentStoredFieldVisitor visitor = new DocumentStoredFieldVisitor();

    				reader.document(scoreDoc.doc, visitor);

    				Document doc = visitor.getDocument();

    				result.add(doc.get("uuid"));

    			} catch (CorruptIndexException e) {
    				throw new IOException(e);
    			}

    		}
    		
    	} catch (ParseException e) {
    	
    		throw new IOException(e);
    		
    	} finally {
    		
    		if(reader != null)
    			reader.close();
    		
    	}

        return result;


    }

    public static IndexWriter getWriter() throws IOException {

    	Directory directory = getDirectory();

		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_9, getAnalyzer()).setOpenMode(OpenMode.CREATE_OR_APPEND);

		return new IndexWriter(directory, conf);
    	
    }
    
    public static void releaseWriter(IndexWriter writer) throws IOException {
    	
    	writer.close();
    	
    }
    

    public static synchronized void set(IndexWriter writer, String uuid, String text) throws IOException {

        Term term = new Term("uuid", uuid);
        writer.deleteDocuments(term);
        
        if(text != null) {

        	Document document = new Document();
	
	        Field name = new Field("uuid", uuid, STRING_TYPE);
	        Field value = new Field("text", text, STRING_TYPE);
	
	        document.add(name);
	        document.add(value);

	        writer.addDocument(document);
	        
        }
    	
    }

    private static ThreadLocal<IndexWriter> writers = new ThreadLocal<IndexWriter>();
    
    public static void startWrite() throws IOException {
    	writers.set(getWriter());
    }
    
    public static void endWrite() {
    	IndexWriter writer = writers.get();
    	try {
			releaseWriter(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	writers.set(null);
    }
    
    
    public static synchronized void set(String uuid, String text) throws IOException {

    	IndexWriter thread = writers.get();
    	if(thread != null) {
    		set(thread, uuid, text);
    		return;
    	}
    	
    	Directory directory = getDirectory();

		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_9, getAnalyzer()).setOpenMode(OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = null;
        
        try {
        	
        	writer = new IndexWriter(directory, conf);

        	set(writer, uuid, text);
	        
        } finally {

        	if(writer != null)
        		writer.close();

        }

    }

	public static void main(String[] args) throws Exception {
		
		set("1", "aa jee");
		set("2", "aa bee");
		
		search("aa");

	}
	
	
}
