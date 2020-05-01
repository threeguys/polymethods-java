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
package threeguys.polymethods.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import threeguys.polymethods.core.PolyMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestPolyInterceptor {

    public static class ExampleRefactor implements ExampleService {

        @Override
        public String localMethod(String arg) {
            return "local";
        }

        @Override
        public String overrideMethod(String arg) {
            throw new NullPointerException("bad things, man!");
        }

    }

    @Aspect
    public static class TestAspect extends PolyAroundAdvice {

        public TestAspect(PolyMethod method) {
            super(method);
        }

        @Around("execution(* threeguys.polymethods.spring.ExampleService.overrideMethod(..))")
        public Object overrideMethod(ProceedingJoinPoint jp) throws Throwable {
            return super.handle(jp.getArgs());
        }

    }

    @Configuration
    @EnableAspectJAutoProxy
    public static class TestConfig {

        @Bean
        public ExampleRefactor refactor() {
            return new ExampleRefactor();
        }

        @Bean
        public TestAspect aspect() {
            return new TestAspect((args) -> "overridden: " + Arrays.toString(args));
        }

    }

    @Autowired
    ExampleService service;

    @Test
    public void simpleCase() {
        assertEquals("local", service.localMethod("dude"));
        assertEquals("overridden: [whoa]", service.overrideMethod("whoa"));
    }

}
