/*
 *  Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.nio.ByteBuffer

object ByteUtils {

    fun fillByteToTexts(bytes: ByteBuffer, offsetText: StringBuilder, hexText: StringBuilder,
                        asciiText: StringBuilder, i: Int, bytesPerLine: Int) {
        if (i % bytesPerLine == 0x0) {
            offsetText.append(String.format("0x%x  \n", i))
        }

        val b = bytes.get()
        hexText.append(byte2hex(b))

        if (b in visibleAsciiRange()) {
            asciiText.append(b.toChar())
        } else {
            asciiText.append('.')
        }

        // new line
        if (i % bytesPerLine == bytesPerLine - 1) {
            hexText.append("\n")
            asciiText.append("\n")
        }
    }

    private fun visibleAsciiRange() = 32..126

    private fun byte2hex(b: Byte): String = String.format("%02X ", b)
}
