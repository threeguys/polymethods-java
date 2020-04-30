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
package threeguys.polymethods.core.methods;

import org.junit.Test;
import threeguys.polymethods.core.MockTargetClass;
import threeguys.polymethods.core.PolyClass;
import threeguys.polymethods.core.PolyClassBuilder;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class TestDelegatingPolyMethod {

    @Test
    public void happyCase() throws Throwable {
        Method target = MockTargetClass.class.getDeclaredMethod("stringReturn");
        MockTargetClass impl = new MockTargetClass();

        PolyClass clazz = PolyClassBuilder.builder()
                .withTarget(MockTargetClass.class)
                .withMethod(target, new DelegatingPolyMethod<>(impl, target))
                .build();

        assertEquals("a-string", clazz.invoke(impl, target, new Object[]{}));
    }

}
