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
package ir.moke.jca.adapter;

public interface TelnetCodes {
    int SE = 240;
    int NOP = 241;
    int Data_Mark = 242;
    int Break = 243;
    int Interrupt_Process = 244;
    int Abort_output = 245;
    int Are_You_There = 246;
    int Erase_character = 247;
    int Erase_Line = 248;
    int Go_ahead = 249;
    int SB = 250;
    int WILL = 251;
    int WONT = 252;
    int DO = 253;
    int DONT = 254;
    int IAC = 255;
}
