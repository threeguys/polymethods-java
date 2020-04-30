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
package threeguys.polymethods.core;

import org.junit.Test;
import threeguys.polymethods.core.methods.DelegatingPolyMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestPolyClass {

    private static final Method NOOP;

    static {
        try {
            NOOP = MockTargetClass.class.getDeclaredMethod("noop");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void happyCase() throws Throwable {
        Method notImplemented = MockTargetClass.class.getDeclaredMethod("throwsRuntimeException");
        Method differentObject = Integer.class.getDeclaredMethod("byteValue");

        List<Object[]> calls = new ArrayList<>();
        PolyClass clazz = PolyClassBuilder.builder()
                .withTarget(MockTargetClass.class)
                .withMethod("noop", new DelegatingPolyMethod<>(calls, List.class.getMethod("add", Object.class )))
                .build();

        assertArrayEquals(new Class<?>[]{ MockTargetClass.class }, clazz.getInterfaces());

        assertTrue(clazz.implementsInterface(MockTargetClass.class));
        assertFalse(clazz.implementsInterface(Integer.class));

        assertTrue(clazz.implementsMethod(NOOP));
        assertFalse(clazz.implementsMethod(notImplemented));
        assertFalse(clazz.implementsMethod(differentObject));

        assertEquals(Boolean.TRUE, clazz.invoke(null, NOOP, new Object[] { "unit-test" }));
        assertEquals(1, calls.size());
        assertArrayEquals(new Object[] { "unit-test" }, calls.toArray());
    }

    @Test(expected = NoSuchMethodException.class)
    public void missingImplementation() throws Throwable {
        Method notImplemented = MockTargetClass.class.getDeclaredMethod("throwsRuntimeException");
        PolyClass clazz = PolyClassBuilder.builder()
                .withTarget(MockTargetClass.class)
                .withMethod(NOOP, (args) -> null)
                .withMethod("stringDoubleStringReturn", new Class<?>[] { String.class, double.class }, (args) -> null)
                .build();

        clazz.invoke(null, notImplemented, new Object[] {});
    }

}
