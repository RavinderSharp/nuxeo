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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.ecm.core.storage.dbs.DBSDocument.KEY_ID;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.nuxeo.ecm.core.storage.State;

public class TestMarkLogicStateDeserializer extends AbstractTest {

    private MarkLogicStateDeserializer deserializer = new MarkLogicStateDeserializer();

    @Test
    public void testEmptyState() {
        State state = deserializer.apply("{}");
        assertNotNull(state);
        assertEquals(new State(), state);
    }

    @Test
    public void testStateWithSimpleValue() {
        State state = deserializer.apply(String.format("{\"%s\":\"ID\"}", KEY_ID));
        assertNotNull(state);
        State expectedState = new State();
        expectedState.put(KEY_ID, "ID");
        assertEquals(expectedState, state);
    }

    @Test
    public void testStateWithSimpleCalendarValue() {
        State state = deserializer.apply(String.format(
                "{\"%s\":\"ID\",\"dub:creationDate\":\"1970-01-01T00:00:00.001\"}", KEY_ID));
        assertNotNull(state);
        State expectedState = new State();
        expectedState.put(KEY_ID, "ID");
        Calendar creationDate = Calendar.getInstance();
        ZonedDateTime zonedCreationDate = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 1000000, ZoneId.systemDefault());
        creationDate.setTime(Date.from(zonedCreationDate.toInstant()));
        expectedState.put("dub:creationDate", creationDate);
        assertEquals(expectedState, state);
    }

    @Test
    public void testStateWithSubState() {
        String json = String.format("{\"%s\":\"ID\",\"subState\":{\"nbValues\":2,\"valuesPresent\":false}}", KEY_ID);
        State state = deserializer.apply(json);
        assertNotNull(state);
        State expectedState = new State();
        expectedState.put(KEY_ID, "ID");
        State subState = new State();
        subState.put("nbValues", 2L);
        subState.put("valuesPresent", false);
        expectedState.put("subState", subState);
        assertEquals(expectedState, state);
    }

    @Test
    public void testStateWithList() {
        String json = String.format(
                "{\"%s\":\"ID\",\"nbValues\":2,\"values\":[{\"item\":\"itemState1\",\"read\":true,\"write\":true},{\"item\":\"itemState2\",\"read\":true,\"write\":false}]}",
                KEY_ID);
        State state = deserializer.apply(json);
        assertNotNull(state);
        State expectedState = new State();
        expectedState.put(KEY_ID, "ID");
        expectedState.put("nbValues", 2L);
        State state1 = new State();
        state1.put("item", "itemState1");
        state1.put("read", true);
        state1.put("write", true);
        State state2 = new State();
        state2.put("item", "itemState2");
        state2.put("read", true);
        state2.put("write", false);
        expectedState.put("values", new ArrayList<>(Arrays.asList(state1, state2)));
        assertEquals(expectedState, state);
    }

    @Test
    public void testStateWithArray() {
        String json = String.format("{\"%s\":\"ID\",\"nbValues\":2,\"values\":[3,4]}", KEY_ID);
        State state = deserializer.apply(json);
        assertNotNull(state);
        State expectedState = new State();
        expectedState.put(KEY_ID, "ID");
        expectedState.put("nbValues", 2L);
        expectedState.put("values", new Long[] { 3L, 4L });
        assertEquals(expectedState, state);
    }

    @Test
    public void testBijunction() throws Exception {
        String json = readJSONFile("serializer/deserializer-bijunction.json");
        assertEquals(json, deserializer.andThen(new MarkLogicStateSerializer()).apply(json));
    }

    /*
     * Test deserialization of state issued from TestSQLRepositoryAPI#testMarkDirtyForList.
     */
    @Test
    public void testMarkDirtyForList() throws Exception {
        String json = readJSONFile("serializer/mark-dirty-for-list.json");
        State state = deserializer.apply(json);
        assertNotNull(state);
        State expectedState = new State();
        expectedState.put("ecm:id", "672f3fc9-38e3-43ec-8b31-f15f6e89f486");
        expectedState.put("ecm:primaryType", "ComplexDoc");
        expectedState.put("ecm:name", "doc");
        expectedState.put("ecm:parentId", "00000000-0000-0000-0000-000000000000");
        State attachedFile = new State();
        ArrayList<State> vignettes = new ArrayList<>();
        State vignette = new State();
        vignette.put("width", 111L);
        vignettes.add(vignette);
        attachedFile.put("vignettes", vignettes);
        expectedState.put("cmpf:attachedFile", attachedFile);
        // Here we have a String[] in deserialization instead of Object[] in serialization
        expectedState.put("ecm:ancestorIds", new String[] { "00000000-0000-0000-0000-000000000000" });
        expectedState.put("ecm:lifeCyclePolicy", "undefined");
        expectedState.put("ecm:lifeCycleState", "undefined");
        expectedState.put("ecm:majorVersion", 0L);
        expectedState.put("ecm:minorVersion", 0L);
        expectedState.put("ecm:racl", new String[] { "Administrator", "administrators", "members" });
        assertEquals(expectedState, state);
    }

}