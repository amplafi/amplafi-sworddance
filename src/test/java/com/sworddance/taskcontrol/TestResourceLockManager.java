/**
 *
 */
package com.sworddance.taskcontrol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
/**
 * Tests to make sure the resource lock manager correctly
 * orders the task locks.
 *
 */
@Test
public class TestResourceLockManager {
    /**
     * Resource 4.
     */
    private static final String R4 = "4";

    /**
     * Resource #3.
     */
    private static final String R3 = "3";

    /**
     * Resource #2.
     */
    private static final String R2 = "res2";

    /**
     *
     */
    private static final String R1 = "res1";

    private ResourceLockManager resourceManager;

    @BeforeMethod
    protected void init() throws Exception {
        resourceManager = new ResourceLockManager();
    }

    /**
     * test to make sure that non-exclusive locks don't interfere with each other.
     *
     */
    public void testNonExclusive() {
        DefaultDependentPrioritizedTask t = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertFalse(
                resourceManager.getDependentTasks(t1, false).contains(t)
                        || resourceManager.getDependentTasks(t, false)
                                .contains(t1),
            "non-exclusive tasks should not depend on each other");
    }

    /**
     * test to make sure order is respected with exclusive locks.
     *
     */
    public void testExclusive() {
        DefaultDependentPrioritizedTask t = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R1, ResourceLock.EXCLUSIVE));
        t.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.EXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertTrue(
                resourceManager.getDependentTasks(t1, false).contains(t),
                "exclusive tasks need to depend on previous");
        assertFalse(resourceManager.getDependentTasks(t, false).contains(t1),
                "dependency order reversed");
    }

    /**
     * test to make sure that nonexclusive locks are blocked by earlier exclusive locks.
     *
     */
    public void testNonexclusiveDependOnExclusive() {
        DefaultDependentPrioritizedTask t = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R1, ResourceLock.EXCLUSIVE));
        t.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);
        assertTrue(
                resourceManager.getDependentTasks(t1, false).contains(t),
                "nonexclusive tasks need to depend on previous exclusive");
        assertFalse(
                resourceManager.getDependentTasks(t, false).contains(t1),
                "dependency order reversed");
    }

    /**
     * test to make sure that exclusive locks are blocked by earlier non-exclusive locks.
     *
     */
    public void testExclusiveDependOnNonexclusive() {
        DefaultDependentPrioritizedTask t = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.EXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertTrue(
                resourceManager.getDependentTasks(t1, false).contains(t),
                "exclusive tasks need to depend on previous");
        assertFalse(resourceManager.getDependentTasks(t, false).contains(t1),
                "dependency order reversed");
    }

    /**
     * test that a global exclusive lock will block an nonexclusive,
     * non-globallock.
     *
     */
    public void testGlobalLock1() {
        DefaultDependentPrioritizedTask t = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        t.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.EXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertTrue(
                resourceManager.getDependentTasks(t1, false).contains(t),
                "exclusive tasks need to depend on previous exclusive global");
        assertFalse(resourceManager.getDependentTasks(t, false).contains(t1),
                "dependency order reversed");
    }

    public void testGlobalLock2() {
        DefaultDependentPrioritizedTask t0 = new DefaultDependentPrioritizedTask();
        t0.setName("t0");
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t0.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t0);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        t1.setName("t1");
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertTrue(resourceManager.getDependentTasks(t1, false).contains(t0),
            "exclusive tasks need to depend on previous nonexclusive global");
        assertFalse(resourceManager.getDependentTasks(t0, false).contains(t1),
                "dependency order reversed");
    }

    public void testGlobalLock3() {
        DefaultDependentPrioritizedTask t = new DefaultDependentPrioritizedTask();
        t.addLock(new ResourceLock(ResourceLockManager.GLOBALRESOURCE,
                ResourceLock.POST_INDEX_LOCK));
        t.addLock(ResourceLockManager.createGlobalExclusiveResourceLock());
        resourceManager.addTaskLocks(t);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.EXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);
        for (ResourceLock lock : t.getResourceLocksNeeded()) {
            assertEquals(ResourceLock.GLOBALLOCKTYPE | ResourceLock.POST_INDEX_LOCK, lock.getLockType());
        }
        assertTrue(
                resourceManager.getDependentTasks(t1, false).contains(t),
                "exclusive tasks need to depend on previous exclusive global");
        assertFalse(resourceManager.getDependentTasks(t, false).contains(t1),
                "dependency order reversed");
    }

    public void testSharingLock() {
        DefaultDependentPrioritizedTask parent = new DefaultDependentPrioritizedTask();
        parent.setName("parent");
        List<ResourceLock> parentList = new ArrayList<ResourceLock>();
        parentList.add(new ResourceLock(R1, ResourceLock.READSS));
        parent.setResourceLocksNeeded(parentList);
        resourceManager.addTaskLocks(parent);

        DefaultDependentPrioritizedTask t0 = new DefaultDependentPrioritizedTask();
        t0.setParentTask(parent);
        t0.addDependency(parent);
        t0.setName("t0");
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R1, ResourceLock.READSS));
        t0.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t0);

        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        t1.setParentTask(parent);
        t1.setName("t1");
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.READSS));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertFalse(
                resourceManager.getDependentTasks(t1, false).contains(t0),
                "sharing subtasks need to not depend on previous siblings");
        assertFalse(
                resourceManager.getDependentTasks(t0, false).contains(t1),
                "sharing subtasks need to not depend on previous siblings");
        assertFalse(
                resourceManager.getDependentTasks(t0, false).contains(parent),
                "sharing subtasks with explicit dependency on parent don't depend on parent according to resourcelock manager");
        assertFalse(
                resourceManager.getDependentTasks(t1, false).contains(parent),
                "sharing subtasks need to depend on parents");
    }

    public void testSharingLockDifferentParents() {
        DefaultDependentPrioritizedTask parent0 = new DefaultDependentPrioritizedTask();
        parent0.setName("parent0");
        List<ResourceLock> parentList = new ArrayList<ResourceLock>();
        parentList.add(new ResourceLock(R1, ResourceLock.READSS));
        parent0.setResourceLocksNeeded(parentList);
        resourceManager.addTaskLocks(parent0);

        DefaultDependentPrioritizedTask t0 = new DefaultDependentPrioritizedTask();
        t0.setParentTask(parent0);
        t0.setName("t0");
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R1, ResourceLock.READSS));
        t0.setResourceLocksNeeded(l);
        resourceManager.addTaskLocks(t0);

        DefaultDependentPrioritizedTask parent1 = new DefaultDependentPrioritizedTask();
        parent1.setName("parent1");
        List<ResourceLock> parent1List = new ArrayList<ResourceLock>();
        parent1List.add(new ResourceLock(R1, ResourceLock.READSS));
        parent1.setResourceLocksNeeded(parent1List);
        resourceManager.addTaskLocks(parent1);
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        t1.setParentTask(parent1);
        t1.setName("t1");
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.READSS));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertTrue(
                resourceManager.getDependentTasks(t1, false).contains(t0),
                "sharing subtasks with different parents have dependency");
        assertFalse(
                resourceManager.getDependentTasks(t0, false).contains(t1),
                "sharing subtasks with different parents have dependency");
        assertFalse(
                resourceManager.getDependentTasks(t0, false).contains(parent0),
                "sharing subtasks need to not depend on parent");
        assertFalse(
                resourceManager.getDependentTasks(t1, false).contains(parent1),
                "sharing subtasks need to not depend on parents");
        assertTrue(
                resourceManager.getDependentTasks(t1, false).contains(parent0),
                "sharing subtasks need to depend on previous parent");
    }

    public void testGlobalInsertIntoLocal() {
        DefaultDependentPrioritizedTask t0 = new DefaultDependentPrioritizedTask();
        t0.setName("t0");
        List<ResourceLock> l0 = new ArrayList<ResourceLock>();
        l0.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        t0.setResourceLocksNeeded(l0);
        resourceManager.addTaskLocks(t0);

        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        t1.setName("t1");
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        assertEquals(2, t0.getResourceLocksNeeded().size());
        assertEquals(2, t1.getResourceLocksNeeded().size());

        DefaultDependentPrioritizedTask t2 = new DefaultDependentPrioritizedTask();
        t2.setName("t2");
        List<ResourceLock> l2 = new ArrayList<ResourceLock>();
        l2.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        l2.add(new ResourceLock(R2, ResourceLock.NONEXCLUSIVE));
        t2.setResourceLocksNeeded(l2);
        resourceManager.addTaskLocks(t2);

        assertEquals(3, t0.getResourceLocksNeeded().size());
        assertEquals(2, t1.getResourceLocksNeeded().size());
        // global + R1 + R2 resource.
        assertEquals(3, t2.getResourceLocksNeeded().size());

        DefaultDependentPrioritizedTask t3 = new DefaultDependentPrioritizedTask();
        t3.setName("t3");
        List<ResourceLock> l3 = new ArrayList<ResourceLock>();
        l3.add(new ResourceLock(R3, ResourceLock.NONEXCLUSIVE));
        l3.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        l3.add(new ResourceLock(R4, ResourceLock.NONEXCLUSIVE));
        t3.setResourceLocksNeeded(l3);
        resourceManager.addTaskLocks(t3);

        assertEquals(5, t0.getResourceLocksNeeded().size());
        // just the original 2 locks
        assertEquals(2, t1.getResourceLocksNeeded().size());
        // the original 2 locks + 1*3 for the other resources that weren't given
        // explicit mentions
        assertEquals(5, t2.getResourceLocksNeeded().size());
        // the original 3 locks + 1*2 for each other nonspecified resource
        assertEquals(5, t3.getResourceLocksNeeded().size());
    }

    /**
     * Test to make sure task can create subtasks with locks in the same
     * location as the parent.
     */
    public void testSubtaskAdditions() {
        DefaultDependentPrioritizedTask t0 = new DefaultDependentPrioritizedTask();
        t0.setName("t0");
        List<ResourceLock> l0 = new ArrayList<ResourceLock>();
        l0.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        t0.setResourceLocksNeeded(l0);
        resourceManager.addTaskLocks(t0);

        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        t1.setName("t1");
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);

        DefaultDependentPrioritizedTask t2 = new DefaultDependentPrioritizedTask();
        t2.setName("t2");
        t2.setParentTask(t0);
        List<ResourceLock> l2 = new ArrayList<ResourceLock>();
        l2.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        l2.add(new ResourceLock(R1, ResourceLock.NONEXCLUSIVE));
        t2.setResourceLocksNeeded(l2);
        resourceManager.addTaskLocks(t2);
        Collection<ResourceLocker> l = resourceManager.getDependentTasks(t1, false);
        assertEquals(2, l.size());
        assertTrue(l.contains(t0));
        assertTrue(l.contains(t2));
    }

    public void testSubtasksCannotGetLocksParentsDontHave() {
        DefaultDependentPrioritizedTask parent0 = new DefaultDependentPrioritizedTask();
        parent0.setName("parent0");
        List<ResourceLock> parentList = new ArrayList<ResourceLock>();
        parentList.add(new ResourceLock(R1, ResourceLock.READSS));
        parent0.setResourceLocksNeeded(parentList);
        resourceManager.addTaskLocks(parent0);

        DefaultDependentPrioritizedTask t0 = new DefaultDependentPrioritizedTask();
        t0.setParentTask(parent0);
        t0.setName("t0");
        List<ResourceLock> l = new ArrayList<ResourceLock>();
        l.add(new ResourceLock(R2, ResourceLock.READSS));
        t0.setResourceLocksNeeded(l);
        try {
            resourceManager.addTaskLocks(t0);
            fail("exception should have been thrown - because subtask grabbing lock not allowed to");
        } catch (AssertionError e) {
            throw e;
        } catch (RuntimeException e) {
            // expected.
        }
        DefaultDependentPrioritizedTask t1 = new DefaultDependentPrioritizedTask();
        t1.setName("t1");
        t1.setParentTask(parent0);
        List<ResourceLock> l1 = new ArrayList<ResourceLock>();
        l1.add(new ResourceLock(R1, ResourceLock.EXCLUSIVE));
        t1.setResourceLocksNeeded(l1);
        resourceManager.addTaskLocks(t1);
        try {
            resourceManager.addTaskLocks(t1);
            fail("exception should have been thrown - because subtask grabbing lock level not allowed to");
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            // expected.
        }
    }

    /**
     * make sure a task cannot block itself.
     *
     */
    public void testTaskBlockSelf() {
        DefaultDependentPrioritizedTask t3 = new DefaultDependentPrioritizedTask();
        t3.setName("t3");
        List<ResourceLock> l3 = new ArrayList<ResourceLock>();
        l3.add(new ResourceLock(R3, ResourceLock.NONEXCLUSIVE));
        l3.add(ResourceLockManager.createGlobalExclusiveResourceLock());
        l3.add(new ResourceLock(R4, ResourceLock.NONEXCLUSIVE));
        t3.setResourceLocksNeeded(l3);
        resourceManager.addTaskLocks(t3);

        assertTrue(resourceManager.getUnblockedTasks().contains(t3));
    }

    public void testLockSubsets() {
        ResourceLock r1 = new ResourceLock(R1, ResourceLock.POST_INDEX_LOCK
                | ResourceLock.GLOBALLOCKTYPE);
        ResourceLock r2 = new ResourceLock(R1, ResourceLock.WRITESC);
        assertTrue(r2.isSubset(r1));
    }

    /**
     * useful method for when debugging the resourceManager locks in a test.
     */
    protected void printGenerate() {
        for (String line: resourceManager.generateLockMatrix()) {
            System.out.println(line);
        }
    }
}
