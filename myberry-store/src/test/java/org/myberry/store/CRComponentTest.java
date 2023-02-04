/*
* MIT License
*
* Copyright (c) 2021 MyBerry. All rights reserved.
* https://myberry.org/
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:

*   * Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.

*   * Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.

*   * Neither the name of MyBerry. nor the names of its contributors may be used
* to endorse or promote products derived from this software without specific
* prior written permission.

* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package org.myberry.store;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.myberry.common.structure.Structure;

public class CRComponentTest {

  private static String key = "key1";
  private static String expression = "[#time(day) 2 3 #sid(0) #sid(1) m z #incr(0)]";

  private static CRComponent crComponent;

  @BeforeClass
  public static void init() {
    byte[] keyLength = key.getBytes(StandardCharsets.UTF_8);
    byte[] expressionLength = expression.getBytes(StandardCharsets.UTF_8);
    crComponent = new CRComponent();
    crComponent.setComponentLength(
        (short)
            (CRComponent.COMPONENT_FIXED_FIELD_LENGTH
                + keyLength.length
                + expressionLength.length));
    crComponent.setStatus((byte) 1);
    crComponent.setPhyOffset(5);
    crComponent.setCreateTime(System.currentTimeMillis());
    crComponent.setUpdateTime(System.currentTimeMillis());
    crComponent.setIncrNumber(3L);
    crComponent.setKeyLength((short) keyLength.length);
    crComponent.setKey(key);
    crComponent.setExpressionLength((short) expressionLength.length);
    crComponent.setExpression(expression);
  }

  @Test
  public void test() {
    Assert.assertEquals(Structure.CR, crComponent.getStructure());
    Assert.assertEquals(1, crComponent.getStatus());
    Assert.assertEquals(5, crComponent.getPhyOffset());
    Assert.assertEquals(3L, crComponent.getIncrNumber().get());
    Assert.assertEquals(key, crComponent.getKey());
    Assert.assertEquals(expression, crComponent.getExpression());
  }

  @Ignore
  @Test
  public void test_CPUCacheLinePerformance() throws Exception {
    final long millis = System.currentTimeMillis();
    int count = 1_0000_0000;
    CountDownLatch countDownLatch = new CountDownLatch(2);
    Thread thread1 =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                for (int i = 0; i < count; i++) {
                  crComponent.setUpdateTime(millis);
                  crComponent.getIncrNumber().incrementAndGet();
                }
                countDownLatch.countDown();
              }
            });

    Thread thread2 =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                for (int i = 0; i < count; i++) {
                  crComponent.getExpression();
                }
                countDownLatch.countDown();
              }
            });

    long start = System.nanoTime();
    thread1.start();
    thread2.start();
    countDownLatch.await();
    long end = System.nanoTime();

    System.out.println((end - start) / 10000);
  }
}
