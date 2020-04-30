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

public class MockTargetClass {

    public String stringDoubleStringReturn(String arg1, double arg2) {
        return arg1 + "/" + arg2;
    }

    public String stringReturn() {
        return "a-string";
    }

    public boolean booleanReturn() {
        return true;
    }

    public boolean booleanBooleanReturn(boolean arg) {
        return arg;
    }

    void noop() {
        // intentionally left blank
    }

    void throwsRuntimeException() {
        throw new NumberFormatException("this is a runtime exception");
    }

    void throwsCheckedException() throws MockCheckedException {
        throw new MockCheckedException("this is a checked exception");
    }

}
