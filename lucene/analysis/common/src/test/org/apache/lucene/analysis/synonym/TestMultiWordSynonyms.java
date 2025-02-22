/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.synonym;

import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.StringMockResourceLoader;
import org.apache.lucene.tests.analysis.BaseTokenStreamFactoryTestCase;
import org.apache.lucene.util.Version;

/** @since solr 1.4 */
public class TestMultiWordSynonyms extends BaseTokenStreamFactoryTestCase {

  public void testMultiWordSynonyms() throws Exception {
    Reader reader = new StringReader("a e");
    TokenStream stream = whitespaceMockTokenizer(reader);
    stream =
        tokenFilterFactory(
                "Synonym",
                Version.LATEST,
                new StringMockResourceLoader("a b c,d"),
                "synonyms",
                "synonyms.txt")
            .create(stream);
    // This fails because ["e","e"] is the value of the token stream
    assertTokenStreamContents(stream, new String[] {"a", "e"});
  }
}
