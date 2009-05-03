import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

/**
 * some tests for knowledge sakes.
 * @author patmoore
 *
 */
public class TestSerialization {

    @Test(enabled=false)
    public void testTransient() throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
        Foo f = new Foo();
        objectOutputStream.writeObject(f);
        objectOutputStream.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(bis);
        Foo f1 = (Foo) objectInputStream.readObject();
        assertNull(f1.tf);
        assertNull(f1.to);
    }
    public static class Foo implements Serializable {
        public transient Object to = new Object();
        public transient final Object tf = new Object();
    }
}
