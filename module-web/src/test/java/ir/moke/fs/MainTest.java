/*
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
 */

package ir.moke.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MainTest {
    public static void main(String[] args) throws Exception {
        testTail();
    }

    public static void testTail() throws Exception {
        File file = new File("/var/log/syslog");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        bufferedReader.skip(file.length());
        while (true) {
            String line = bufferedReader.readLine();
            if (line != null) {
                System.out.println(line);
            }
        }
    }
}
