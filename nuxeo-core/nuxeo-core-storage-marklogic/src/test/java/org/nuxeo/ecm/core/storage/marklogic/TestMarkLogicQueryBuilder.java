/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Kevin Leturc
 */
package org.nuxeo.ecm.core.storage.marklogic;

import static org.nuxeo.ecm.core.storage.dbs.DBSDocument.KEY_ID;

import java.util.Arrays;

import org.junit.Test;

import com.marklogic.client.io.marker.StructureWriteHandle;

public class TestMarkLogicQueryBuilder extends AbstractTest {

    @Test
    public void testEq() throws Exception {
        StructureWriteHandle query = new MarkLogicQueryBuilder().eq(KEY_ID, "ID").build();
        assertJSONEquals("query-builder/eq.json", query.toString());
    }

    @Test
    public void testNotIn() throws Exception {
        StructureWriteHandle query = new MarkLogicQueryBuilder().notIn(KEY_ID, Arrays.asList("ID1", "ID2")).build();
        assertJSONEquals("query-builder/not-in.json", query.toString());
    }

}