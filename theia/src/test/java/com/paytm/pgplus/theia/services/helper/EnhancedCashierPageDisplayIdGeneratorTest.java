package com.paytm.pgplus.theia.services.helper;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnhancedCashierPageDisplayIdGeneratorTest {

    private EnhancedCashierPageDisplayIdGenerator enhancedCashierPageDisplayIdGenerator = new EnhancedCashierPageDisplayIdGenerator();

    @Test
    public void testGenerateDisplayId() {
        enhancedCashierPageDisplayIdGenerator.generateDisplayId();
    }
}