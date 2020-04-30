/**
 *    Copyright 2020 Ray Cole
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package threeguys.polymethods.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.MessageSystemAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestSqsPolyMethod {

    @Test
    public void happyCase() throws Throwable {
        SendMessageResult result = new SendMessageResult();
        AmazonSQS sqs = mock(AmazonSQS.class);
        when(sqs.sendMessage(any(SendMessageRequest.class))).thenReturn(result);

        Map<String, String> map1 = new HashMap<>();
        map1.put("foo", "bar");

        Map<String, String> map2 = new HashMap<>();
        map2.put("biz", "baz");

        SqsPolyMethod method = new SqsPolyMethod("some-queue", sqs, 42);
        assertNull(method.handle(new Object[]{ map1, map2, "a-string", "yet-another" }));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqs, times(1)).sendMessage(captor.capture());
        SendMessageRequest req = captor.getValue();
        assertEquals("some-queue", req.getQueueUrl());
        assertEquals(42, (int) req.getDelaySeconds());
        assertEquals("[{\"foo\":\"bar\"},{\"biz\":\"baz\"},\"a-string\",\"yet-another\"]", req.getMessageBody());
    }

    @Test
    public void allFields() throws Throwable {
        SendMessageResult result = new SendMessageResult();
        result.setMessageId("dude");

        AmazonSQS sqs = mock(AmazonSQS.class);
        when(sqs.sendMessage(any(SendMessageRequest.class))).thenReturn(result);

        Object[] expectedArgs = new Object[]{ "an arg" };
        Map<String, MessageAttributeValue> attrs = new HashMap<>();
        attrs.put("yo", new MessageAttributeValue().withStringValue("man"));

        Map<String, MessageSystemAttributeValue> sysAttrs = new HashMap<>();
        sysAttrs.put("sys-yo", new MessageSystemAttributeValue().withStringValue("sys-man"));

        SqsPolyMethod method = new SqsPolyMethod("best-queue", sqs,
                SqsPolyMethod.jsonSerializer(),
                (args) -> {
                    assertArrayEquals(expectedArgs, args);
                    return "dedup-id";
                },
                (args) -> {
                    assertArrayEquals(expectedArgs, args);
                    return "group-id";
                },
                (args) -> {
                    assertArrayEquals(expectedArgs, args);
                    return attrs;
                },
                (args) -> {
                    assertArrayEquals(expectedArgs, args);
                    return sysAttrs;
                },
                (args) -> {
                    assertArrayEquals(expectedArgs, args);
                    return 13;
                },
                (args, request, thisResult) -> {
                    assertArrayEquals(expectedArgs, args);
                    return thisResult.getMessageId();
                });

        assertEquals("dude", method.handle(new Object[]{ "an arg" }));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqs, times(1)).sendMessage(captor.capture());
        SendMessageRequest req = captor.getValue();
        assertEquals("best-queue", req.getQueueUrl());
        assertEquals(13, (int) req.getDelaySeconds());
        assertEquals("[\"an arg\"]", req.getMessageBody());
        assertEquals("dedup-id", req.getMessageDeduplicationId());
        assertEquals("group-id", req.getMessageGroupId());
        assertEquals(attrs, req.getMessageAttributes());
        assertEquals(sysAttrs, req.getMessageSystemAttributes());
    }

    @Test(expected = IllegalStateException.class)
    public void emptyResult() throws Throwable {
        AmazonSQS sqs = mock(AmazonSQS.class);
        when(sqs.sendMessage(any(SendMessageRequest.class))).thenReturn(null);

        SqsPolyMethod method = new SqsPolyMethod("another-queue", sqs, 1);
        method.handle(new Object[] {});
    }

    @Test(expected = NullPointerException.class)
    public void constructor_nullQueue() {
        new SqsPolyMethod(null, mock(AmazonSQS.class));
    }

    @Test(expected = NullPointerException.class)
    public void constructor_nullSqs() {
        new SqsPolyMethod("a-queue-url", null);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_nullSerializer() {
        new SqsPolyMethod("a-queue-url", mock(AmazonSQS.class),
                null, null, null,
                null,
                null,
                null, SqsPolyMethod.voidResultCreator());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_nullResultCreator() {
        new SqsPolyMethod("a-queue-url", mock(AmazonSQS.class),
                SqsPolyMethod.jsonSerializer(), null, null,
                null,
                null,
                null, null);
    }

    @Test
    public void constructor_happyCase() {
        new SqsPolyMethod("a-queue-url", mock(AmazonSQS.class),
                SqsPolyMethod.jsonSerializer(), null, null,
                null,
                null,
                null, SqsPolyMethod.voidResultCreator());
    }

}
