package dev.dominion.ecs.test.engine;

import dev.dominion.ecs.engine.EntityRepository;
import dev.dominion.ecs.engine.LongEntity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EntityRepositoryTest {

    @Test
    void createEntity() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity = (LongEntity) entityRepository.createEntity();
        Assertions.assertNotNull(entity.getComposition());
        Assertions.assertEquals(entity.getComposition().getTenant().getPool().getEntry(entity.getId()), entity);
    }

    @Test
    void createEntityWith1Component() {
        EntityRepository entityRepository = new EntityRepository();
        C1 c1 = new C1(0);
        LongEntity entity = (LongEntity) entityRepository.createEntity(c1);
        Assertions.assertNotNull(entity.getComposition());
        Assertions.assertEquals(entity.getComposition().getTenant().getPool().getEntry(entity.getId()), entity);
        Assertions.assertEquals(c1, entity.getSingleComponent());
    }

    @Test
    void createEntityWith2Component() {
        EntityRepository entityRepository = new EntityRepository();
        var c1 = new C1(0);
        var c2 = new C2(0);
        LongEntity entity1 = (LongEntity) entityRepository.createEntity(c1, c2);
        Assertions.assertNotNull(entity1.getComposition());
        Assertions.assertEquals(entity1.getComposition().getTenant().getPool().getEntry(entity1.getId()), entity1);
        Assertions.assertArrayEquals(new Object[]{c1, c2}, entity1.getComponents());
        LongEntity entity2 = (LongEntity) entityRepository.createEntity(c2, c1);
        Assertions.assertArrayEquals(new Object[]{c1, c2}, entity2.getComponents());
    }

    @Test
    void destroyEntity() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity = (LongEntity) entityRepository.createEntity();
        ConcurrentPool<LongEntity> pool = entity.getComposition().getTenant().getPool();
        entityRepository.destroyEntity(entity);
        Assertions.assertNull(entity.getComposition());
        Assertions.assertNull(pool.getEntry(entity.getId()));
    }

    @Test
    void avoidEmptyPositionOnDestroyEntity() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity1 = (LongEntity) entityRepository.createEntity();
        LongEntity entity2 = (LongEntity) entityRepository.createEntity();
        ConcurrentPool<LongEntity> pool = entity1.getComposition().getTenant().getPool();
        long id1 = entity1.getId();
        long id2 = entity2.getId();
        entityRepository.destroyEntity(entity1);
        Assertions.assertNull(pool.getEntry(id2));
        Assertions.assertEquals(entity2, pool.getEntry(id1));
        Assertions.assertEquals(id1, entity2.getId());
    }

    @Test
    void findComponents1() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity1 = (LongEntity) entityRepository.createEntity(new C1(0));
        LongEntity entity2 = (LongEntity) entityRepository.createEntity(new C1(1), new C2(2));

        var results = entityRepository.findComponents(C1.class);
        Assertions.assertNotNull(results);
        var iterator = results.iterator();
        Assertions.assertNotNull(iterator);
        Assertions.assertTrue(iterator.hasNext());
        var next = iterator.next();
        Assertions.assertEquals(0, next.comp().id);
        Assertions.assertEquals(entity1, next.entity());
        Assertions.assertTrue(iterator.hasNext());
        next = iterator.next();
        Assertions.assertEquals(1, next.comp().id);
        Assertions.assertEquals(entity2, next.entity());

        var results2 = entityRepository.findComponents(C2.class);
        var iterator2 = results2.iterator();
        Assertions.assertNotNull(iterator2);
        Assertions.assertTrue(iterator2.hasNext());
        var next2 = iterator2.next();
        Assertions.assertEquals(2, next2.comp().id);
        Assertions.assertEquals(entity2, next2.entity());

        var results3 = entityRepository.findComponents(C3.class);
        var iterator3 = results3.iterator();
        Assertions.assertNotNull(iterator3);
        Assertions.assertFalse(iterator3.hasNext());
    }

    @Test
    void findComponents2() {
        EntityRepository entityRepository = new EntityRepository();
        LongEntity entity1 = (LongEntity) entityRepository.createEntity(new C1(1), new C2(2));
        LongEntity entity2 = (LongEntity) entityRepository.createEntity(new C1(3), new C2(4), new C3(5));

        var k = 0;
        var t = "";
        var results = entityRepository.findComponents(C1.class, C2.class);
        Assertions.assertNotNull(results);
        var iterator = results.iterator();
        Assertions.assertNotNull(iterator);
        Assertions.assertTrue(iterator.hasNext());
        var next = iterator.next();
        Assertions.assertEquals(1, next.comp1().id);
        Assertions.assertEquals(2, next.comp2().id);
        Assertions.assertEquals(entity1, next.entity());
        Assertions.assertTrue(iterator.hasNext());
        next = iterator.next();
        Assertions.assertEquals(3, next.comp1().id);
        Assertions.assertEquals(4, next.comp2().id);
        Assertions.assertEquals(entity2, next.entity());

        var results2 = entityRepository.findComponents(C2.class, C3.class);
        var iterator2 = results2.iterator();
        Assertions.assertNotNull(iterator2);
        Assertions.assertTrue(iterator2.hasNext());
        var next2 = iterator2.next();
        Assertions.assertEquals(4, next2.comp1().id);
        Assertions.assertEquals(5, next2.comp2().id);
        Assertions.assertEquals(entity2, next2.entity());
    }

    record C1(int id) {
    }

    record C2(int id) {
    }

    record C3(int id) {
    }
}