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

import java.nio.ByteBuffer;

public class ByteUtils {

    private final static char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static void fillByteToTexts(ByteBuffer bytes, StringBuilder offsetText, StringBuilder hexText,
                                       StringBuilder asciiText, int i, int bytesPerLine) {
        if (i % bytesPerLine == 0x0) {
            offsetText.append(String.format("0x%x  \n", i));
        }

        byte b = bytes.get();
        char[] hex = new char[3];
        hex[0] = HEX_DIGITS[(b >>> 4) & 0x0F];
        hex[1] = HEX_DIGITS[b & 0x0F];
        hex[2] = ' ';
        hexText.append(hex);

        if (b >= ' ' && b <= '~') {
            asciiText.append((char) b);
        } else {
            asciiText.append('.');
        }

        if (i % bytesPerLine == bytesPerLine - 1) {
            hexText.append("\n");
            asciiText.append("\n");
        }
    }

}
