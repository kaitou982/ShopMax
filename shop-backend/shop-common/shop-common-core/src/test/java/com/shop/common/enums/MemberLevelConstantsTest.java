package com.shop.common.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemberLevelConstantsTest {

    @Test
    void getDiscount_normalMember_returns1() {
        assertEquals(1.0, MemberLevelConstants.getDiscount(1));
    }

    @Test
    void getDiscount_silverMember_returns98() {
        assertEquals(0.98, MemberLevelConstants.getDiscount(2));
    }

    @Test
    void getDiscount_goldMember_returns95() {
        assertEquals(0.95, MemberLevelConstants.getDiscount(3));
    }

    @Test
    void getDiscount_diamondMember_returns90() {
        assertEquals(0.90, MemberLevelConstants.getDiscount(4));
    }

    @Test
    void getDiscount_outOfBounds_returns1() {
        assertEquals(1.0, MemberLevelConstants.getDiscount(0));
        assertEquals(1.0, MemberLevelConstants.getDiscount(5));
        assertEquals(1.0, MemberLevelConstants.getDiscount(-1));
    }
}
