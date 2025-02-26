package be.uliege.speam.team03.MDTools.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
class GroupTest {

    private Group group;

    @BeforeEach
    void setUp() {
        group = new Group("Test Group");
    }

    @Test
    void testGroupInitialization() {
        assertNotNull(group);
        assertEquals("Test Group", group.getName());
        assertEquals(0, group.getInstrCount()); // By default instrCount should be 0
    }

    @Test
    void testIncrInstrCount() {
        group.incrInstrCount();
        assertEquals(1, group.getInstrCount()); // instrCount should increment to 1

        group.incrInstrCount();
        assertEquals(2, group.getInstrCount()); // instrCount should increment to 2
    }

    @Test
    void testDecrInstrCount() {
        group.incrInstrCount();
        group.decrInstrCount();
        assertEquals(0, group.getInstrCount()); // instrCount should decrement to 0

        group.decrInstrCount(); // Trying to decrement below 0
        assertEquals(0, group.getInstrCount()); // instrCount should not go below 0
    }

    @Test
    void testCalculateInstrumentCountAfterLoad() {
        // Simulating @PostLoad behavior
        group.calculateInstrumentCount();
        assertEquals(3, group.getInstrCount()); // The @PostLoad method should set instrCount to 3
    }

    @Test
    void testSetAndGetName() {
        group.setName("Updated Group Name");
        assertEquals("Updated Group Name", group.getName());
    }

    @Test
    void testGroupEquality() {
        Group anotherGroup = new Group("Test Group");
        assertEquals(group, anotherGroup); // Equals should return true for groups with the same name

        anotherGroup.setName("Different Group");
        assertNotEquals(group, anotherGroup); // Groups with different names should not be equal
    }

    @Test
    void testSubGroupsList() {
        // Assuming the Group class has functionality to handle SubGroups (addSubGroup method for example)
        group.setSubGroups(List.of(new SubGroup())); 
        assertNotNull(group.getSubGroups());
        assertEquals(1, group.getSubGroups().size()); // SubGroups list should have one element
    }
}
