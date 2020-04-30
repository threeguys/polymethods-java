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
import com.fasterxml.jackson.databind.ObjectMapper;
import threeguys.polymethods.core.PolyMethod;

import java.util.Map;

public class SqsPolyMethod implements PolyMethod {

    public static final int MAX_MESSAGE_SIZE = 1024 * 255; // fudge factor

    private String queue;
    private AmazonSQS sqs;
    private ArgumentMapper<String> serializer;
    private ArgumentMapper<String> dedup;
    private ArgumentMapper<String> group;
    private ArgumentMapper<Map<String, MessageAttributeValue>> attributes;
    private ArgumentMapper<Map<String, MessageSystemAttributeValue>> systemAttributes;
    private ArgumentMapper<Integer> delaySeconds;

    private SqsResultCreator resultCreator;

    public static ArgumentMapper<String> jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        return (in) -> mapper.writer().writeValueAsString(in);
    }

    public static SqsResultCreator voidResultCreator() {
        return (args, request, result) -> {
            if (result == null) {
                throw new IllegalStateException("Result cannot be null!");
            }
            return null;
        };
    }

    public SqsPolyMethod(String queue, AmazonSQS sqs) {
        this(queue, sqs, null);
    }

    public SqsPolyMethod(String queue, AmazonSQS sqs, Integer delay) {
        this(queue, sqs, jsonSerializer(), null, null, null, null,
                delay == null ? null : (args) -> delay, voidResultCreator());
    }

    public SqsPolyMethod(String queue, AmazonSQS sqs,
                         ArgumentMapper<String> serializer, ArgumentMapper<String> dedup, ArgumentMapper<String> group,
                         ArgumentMapper<Map<String, MessageAttributeValue>> attributes,
                         ArgumentMapper<Map<String, MessageSystemAttributeValue>> systemAttributes,
                         ArgumentMapper<Integer> delaySeconds, SqsResultCreator resultCreator) {

        if (queue == null || sqs == null || serializer == null || resultCreator == null) {
            throw new NullPointerException();
        }

        this.queue = queue;
        this.sqs = sqs;
        this.serializer = serializer;
        this.dedup = dedup;
        this.group = group;
        this.attributes = attributes;
        this.systemAttributes = systemAttributes;
        this.resultCreator = resultCreator;
        this.delaySeconds = delaySeconds;
    }

    @Override
    public Object handle(Object[] args) throws Throwable {
        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queue)
                .withMessageBody(serializer.map(args));

        if (dedup != null) {
            request.withMessageDeduplicationId(dedup.map(args));
        }

        if (group != null) {
            request.withMessageGroupId(group.map(args));
        }

        if (delaySeconds != null) {
            request.withDelaySeconds(delaySeconds.map(args));
        }

        if (attributes != null) {
            request.withMessageAttributes(attributes.map(args));
        }

        if (systemAttributes != null) {
            request.withMessageSystemAttributes(systemAttributes.map(args));
        }

        SendMessageResult result = sqs.sendMessage(request);
        return resultCreator.results(args, request, result);
    }

}
