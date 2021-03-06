/*
 * Copyright 2010-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.extensions.dynamodb.mappingclient.operations;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyZeroInteractions;
import static software.amazon.awssdk.extensions.dynamodb.mappingclient.AttributeValues.stringValue;

import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.Expression;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.Key;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.MapperExtension;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.OperationContext;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.TableMetadata;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.functionaltests.models.FakeItem;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.model.ConditionCheck;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

@RunWith(MockitoJUnitRunner.class)
public class ConditionCheckTest {
    @Mock
    private MapperExtension mockMapperExtension;

    @Test
    public void generateTransactWriteItem() {
        FakeItem fakeItem = FakeItem.createUniqueFakeItem();
        Map<String, AttributeValue> keyMap = singletonMap("id", stringValue(fakeItem.getId()));
        Expression conditionExpression = Expression.builder()
                                                   .expression("expression")
                                                   .expressionNames(singletonMap("key1", "value1"))
                                                   .expressionValues(singletonMap("key2", stringValue("value2")))
                                                   .build();
        ConditionCheck<FakeItem> operation =
            ConditionCheck.create(Key.create(stringValue(fakeItem.getId())), conditionExpression);
        OperationContext context = OperationContext.create("table-name", TableMetadata.primaryIndexName());

        TransactWriteItem result = operation.generateTransactWriteItem(FakeItem.getTableSchema(), context,
                                                                                  mockMapperExtension);

        TransactWriteItem expectedResult =
            TransactWriteItem.builder()
                             .conditionCheck(
                                 software.amazon.awssdk.services.dynamodb.model.ConditionCheck
                                     .builder()
                                     .tableName("table-name")
                                     .key(keyMap)
                                     .conditionExpression(conditionExpression.expression())
                                     .expressionAttributeValues(conditionExpression.expressionValues())
                                     .expressionAttributeNames(conditionExpression.expressionNames())
                                     .build())
                             .build();
        assertThat(result, is(expectedResult));
        verifyZeroInteractions(mockMapperExtension);
    }
}
