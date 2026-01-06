
package com.example.vulndemo.util;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/** Uses ObjectInputStream without validation. */
public class InsecureDeserializer {
    public static Object deserialize(byte[] bytes) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return ois.readObject();
        }
    }
}
