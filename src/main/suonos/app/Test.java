/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package suonos.app;

public class Test {

    interface Base {
        void process1(Base secondObject);

        void process2(A firstObject);

        void process2(B firstObject);

        void process2(C firstObject);
    }

    static class A implements Base {
        public void process1(Base second) {
            second.process2(this);
        }

        public void process2(A first) {
            System.out.println("first is A, second is A");
        }

        public void process2(B first) {
            System.out.println("first is B, second is A");
        }

        public void process2(C first) {
            System.out.println("first is C, second is A");
        }
    }

    static class B implements Base {
        public void process1(Base second) {
            second.process2(this);
        }

        public void process2(A first) {
            System.out.println("first is A, second is B");
        }

        public void process2(B first) {
            System.out.println("first is B, second is B");
        }

        public void process2(C first) {
            System.out.println("first is C, second is B");
        }
    }

    static class C implements Base {
        public void process1(Base second) {
            second.process2(this);
        }

        public void process2(A first) {
            System.out.println("first is A, second is C");
        }

        public void process2(B first) {
            System.out.println("first is B, second is C");
        }

        public void process2(C first) {
            System.out.println("first is C, second is C");
        }
    }

    public void run() {
        Base o1 = new B();
        Base o2 = new C();
        o1.process1(o2);

        System.out.println("\ns1");
        int j1 = solution(6868, 130);

        System.out.println(j1);
    }

    public int solution(int A, int B) {

        String a_s = Integer.toString(A);
        String b_s = Integer.toString(B);
        StringBuilder sb = new StringBuilder();

        int len = Math.max(a_s.length(), b_s.length());
        for (int i = 0; i != len; i++) {
            if (i < a_s.length()) {
                sb.append(a_s.charAt(i));
            }
            if (i < b_s.length()) {
                sb.append(b_s.charAt(i));
            }
        }

        int j = Integer.parseInt(sb.toString());
        if (j > 100000000)
            return -1;

        return j;
    }

    public int solution2(int A, int B) {
        long i;
        long divA = 1;
        long divB = 1;
        long zip = 0;

        for (i = A; i > 10; i /= 10) {
            divA *= 10;
        }

        for (i = B; i > 10; i /= 10) {
            divB *= 10;
        }

        while (divA > 0 || divB > 0) {
            if (divA > 0) {
                zip = (zip * 10) + ((A / divA) % 10);
                divA /= 10;
            }
            if (divB > 0) {
                zip = (zip * 10) + ((B / divB) % 10);
                divB /= 10;
            }
        }

        if (zip > 100000000)
            return -1;
        else
            return (int) zip;
    }
}