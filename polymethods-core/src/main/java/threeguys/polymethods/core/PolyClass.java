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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class PolyClass implements InvocationHandler {

    private Class<?> targetClass;
    private Map<String, PolyMethod> methods;

    public PolyClass(Class<?> targetClass, Map<String, PolyMethod> methods) {
        this.targetClass = targetClass;
        this.methods = methods;
    }

    public boolean implementsInterface(Class<?> aClass) {
        return targetClass.equals(aClass);
    }

    public Class<?>[] getInterfaces() {
        return new Class<?>[]{ targetClass };
    }

    public boolean implementsMethod(Method method) {
        return targetClass.equals(method.getDeclaringClass()) && methods.containsKey(method.toString());
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        String key = method.toString();
        PolyMethod m = methods.get(key);
        if (m != null) {
            return m.handle(objects);
        }

        throw new NoSuchMethodException("Method implementation: " + key + " was not found!");
    }

}
