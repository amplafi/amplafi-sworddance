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
package com.sworddance.beans;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sworddance.beans.BeanComparator;

import static org.testng.Assert.*;
import static com.sworddance.util.CUtilities.*;
/**
 * Test for (@link BeanComparator}.
 */
public class TestBeanComparator {

    @Test(dataProvider="beanComparators")
    public void testCompare(BeanComparator<PostContent> comparator, List<Set<String>> expectedDiffs) {

        PostContent m1 = new PostContent("testHeadLine", "testBody", 3L, new Address("3210 Harriet", "St. Louis"));

        assertTrue(comparator.areEqual(m1, m1));

        PostContent m2 = new PostContent(m1);

        assertTrue(comparator.areEqual(m1, m2));
        Set<String> diffs = comparator.compareToBase(m1, m2);
        assertEquals(diffs.size(), 0, "diffs="+diffs);

        m2.getAddress().setAddress("3489 Georgette Way");
        assertFalse(comparator.areEqual(m1, m2));
        diffs = comparator.compareToBase(m1, m2);

        Set<String> expectedDiff = expectedDiffs.get(0);
		assertDiffsSame(diffs, expectedDiff);

        m2.setMessageText("1");
        assertFalse(comparator.areEqual(m1, m2));

        diffs = comparator.compareToBase(m1, m2);
        expectedDiff = expectedDiffs.get(1);
		assertDiffsSame(diffs, expectedDiff);
    }

	private void assertDiffsSame(Set<String> diffs, Set<String> expectedDiff) {
		assertEquals(diffs.size(), expectedDiff.size(), "diffs="+diffs);
		for(String diff: diffs) {
			assertTrue(expectedDiff.contains(diff), "diffs="+diffs+"; expectedDiff="+expectedDiff);
		}
	}

    @DataProvider(name="beanComparators")
    public Object[][] getBeanComparators() {
        BeanComparator<PostContent> comparatorThatReliesOnSubComparators = new BeanComparator<PostContent>("headline", "messageText", "age");
        BeanComparator<Address> addressBeanComparator = new BeanComparator<Address>("address","city");
		comparatorThatReliesOnSubComparators.addSubComparator("address", addressBeanComparator);
		BeanComparator<PostContent> explicitComparator = new BeanComparator<PostContent>("headline", "messageText", "age", "address.address", "address.city" );
    	return new Object[][] {
    		new Object[] { comparatorThatReliesOnSubComparators, Arrays.asList(asSet("address.address"), asSet("address.address", "messageText")) },
    		new Object[] { explicitComparator, Arrays.asList(asSet("address.address"), asSet("address.address", "messageText")) },
    	};
    }

    public static class PostContent {
        private String headline;
        private String messageText;
        private Long age;
        private Address address;
        public PostContent() {

        }

		public PostContent(String headline, String messageText, Long age, Address address) {
			super();
			this.headline = headline;
			this.messageText = messageText;
			this.address = address;
			this.age = age;
		}

        public PostContent(PostContent postContent) {
            this.headline = postContent.headline;
            this.messageText = postContent.messageText;
            this.address = new Address(postContent.address);
            this.age = postContent.age;
        }
        /**
         * @param headline the headline to set
         */
        public void setHeadline(String headline) {
            this.headline = headline;
        }
        /**
         * @return the headline
         */
        public String getHeadline() {
            return headline;
        }
        /**
         * @param messageText the messageText to set
         */
        public void setMessageText(String messageText) {
            this.messageText = messageText;
        }
        /**
         * @return the messageText
         */
        public String getMessageText() {
            return messageText;
        }
		public void setAddress(Address address) {
			this.address = address;
		}
		public Address getAddress() {
			return address;
		}

		public void setAge(Long age) {
			this.age = age;
		}

		public Long getAge() {
			return age;
		}
    }
    public static class Address {
    	private String address;
    	private String city;
    	public Address() {

    	}
		public Address(String address, String city) {
			super();
			this.address = address;
			this.city = city;
		}
		public Address(Address address) {
			this.address = address.address;
			this.city = address.city;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getAddress() {
			return address;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getCity() {
			return city;
		}

    }
}
