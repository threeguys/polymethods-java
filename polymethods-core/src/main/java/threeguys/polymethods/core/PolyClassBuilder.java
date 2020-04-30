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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PolyClassBuilder<T extends PolyClass> {

    private Class<?> target;
    private Map<String, PolyMethod> methods = new HashMap<>();
    private Constructor<T> constructor;

    private PolyClassBuilder(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    public static PolyClassBuilder<PolyClass> builder() throws NoSuchMethodException {
        return builder(PolyClass.class);
    }

    public static <T extends PolyClass> PolyClassBuilder<T> builder(Class<T> clazz) throws NoSuchMethodException {
        return new PolyClassBuilder<>(clazz.getConstructor(Class.class, Map.class));
    }

    public PolyClassBuilder<T> withTarget(Class<?> target) {
        this.target = target;
        return this;
    }

    public PolyClassBuilder<T> withMethod(Method method, PolyMethod impl) {
        this.methods.put(method.toString(), impl);
        return this;
    }

    public PolyClassBuilder<T> withMethod(String name, PolyMethod impl) throws NoSuchMethodException {
        return withMethod(target.getDeclaredMethod(name), impl);
    }

    public PolyClassBuilder<T> withMethod(String name, Class<?>[] argTypes, PolyMethod impl) throws NoSuchMethodException {
        return withMethod(target.getDeclaredMethod(name, argTypes), impl);
    }

    public PolyClass build() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return constructor.newInstance(target, methods);
    }

}
